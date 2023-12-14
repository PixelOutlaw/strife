package land.face.strife.data;

import java.util.HashMap;
import java.util.Map;
import land.face.strife.StrifePlugin;
import land.face.strife.data.ability.Ability;
import land.face.strife.data.champion.Champion;
import land.face.strife.data.champion.LifeSkillType;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@Getter @Setter
public class CombatDetailsContainer {

  private float totalCombatXpGained = 0;
  private int prayerActiveTicks = 0;
  private int prayerInactiveTicks = 1;
  private final Map<LifeSkillType, Float> skillWeights = new HashMap<>();


  private static final float XP_EXPONENT = (float) StrifePlugin.getInstance().getSettings()
      .getDouble("config.leveling.combat-to-skill-xp-exponent", 0.5);
  private static final float WEIGHT_EXPONENT = (float) StrifePlugin.getInstance().getSettings()
      .getDouble("config.leveling.weight-to-skill-xp-exponent", 0.75);
  private static final float PRAYER_EXPONENT = (float) StrifePlugin.getInstance().getSettings()
      .getDouble("config.leveling.skill-to-prayer-xp-exponent", 0.5);

  public void registerAbilityUse(Ability ability, float multiplier) {
    for (LifeSkillType type : ability.getAbilityIconData().getExpWeights().keySet()) {
      float amount = ability.getAbilityIconData().getExpWeights().get(type) * multiplier;
      skillWeights.put(type, skillWeights.getOrDefault(type, 0f) + amount);
    }
  }

  public void addCombatXp(float amount) {
    totalCombatXpGained += (float) Math.pow(amount, XP_EXPONENT);;
  }

  public void award(Champion champion) {
    if (totalCombatXpGained == 0) {
      clearAll();
      return;
    }
    float totalSkillWeight = 0;
    for (LifeSkillType type : skillWeights.keySet()) {
      totalSkillWeight += skillWeights.get(type);
    }
    float skillXpFromCombat = totalCombatXpGained;

    if (totalSkillWeight > 0) {
      float skillXpFromWeights = (float) Math.pow(totalSkillWeight, WEIGHT_EXPONENT);
      float totalSkillXpAwarded = skillXpFromCombat + skillXpFromWeights;
      totalSkillXpAwarded *= 0.9f + StrifePlugin.RNG.nextFloat() * 0.2f;

      for (LifeSkillType type : skillWeights.keySet()) {
        float amount = skillWeights.get(type);
        float ratio = amount / totalSkillWeight;
        try {
          StrifePlugin.getInstance().getSkillExperienceManager().addExperience(
              champion.getPlayer(), type, totalSkillXpAwarded * ratio, false, false);
        } catch (Exception e) {
          Bukkit.getLogger().warning("[Strife] Failed end of combat xp for " + champion.getPlayer().getName());
          e.printStackTrace();
        }
      }
    }
    sendPrayerXp(champion.getPlayer(), skillXpFromCombat);

    champion.getDetailsContainer().clearAll();
  }

  private void sendPrayerXp(Player player, float totalSkillXp) {
    if (prayerActiveTicks == 0) {
      return;
    }
    float prayerXp = (float) Math.pow(totalSkillXp, PRAYER_EXPONENT);
    prayerXp *= (float) prayerActiveTicks / (prayerActiveTicks + prayerInactiveTicks);
    StrifePlugin.getInstance().getSkillExperienceManager()
        .addExperience(player, LifeSkillType.PRAYER, prayerXp, false, false);
  }

  public void incrementPrayer(int active, int inactive) {
    prayerActiveTicks += active;
    prayerInactiveTicks += inactive;
  }

  public void clearAll() {
    totalCombatXpGained = 0;
    prayerActiveTicks = 0;
    prayerInactiveTicks = 1;
    skillWeights.clear();
  }
}
