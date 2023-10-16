package land.face.strife.data;

import java.util.HashMap;
import java.util.Map;
import land.face.strife.StrifePlugin;
import land.face.strife.data.ability.Ability;
import land.face.strife.data.champion.LifeSkillType;
import lombok.Getter;

public class CombatDetailsContainer {

  private final Map<LifeSkillType, Float> skillWeight = new HashMap<>();
  @Getter
  private float totalExp = 0;

  private static final float EXP_PERCENTAGE = (float) StrifePlugin.getInstance().getSettings()
      .getDouble("config.leveling.exp-converted-to-combat-skill-exp", 0.05);
  private static final float WEIGHT_PERCENTAGE = (float) StrifePlugin.getInstance().getSettings()
      .getDouble("config.leveling.combat-skill-exp-weight", 0.1);

  public void addWeights(Ability ability) {
    if (totalExp == 0 && !skillWeight.isEmpty()) {
      skillWeight.clear();
    }
    for (LifeSkillType type : ability.getAbilityIconData().getExpWeights().keySet()) {
      skillWeight.put(type, skillWeight.getOrDefault(type, 0f) +
          ability.getAbilityIconData().getExpWeights().get(type));
    }
  }

  public void addExp(float amount) {
    totalExp += amount;
  }

  public Map<LifeSkillType, Float> getExpValues() {
    if (totalExp == 0) {
      return null;
    }
    float totalWeight = 0;
    for (float amount : skillWeight.values()) {
      totalWeight += amount;
    }
    float xpTotal = totalExp * EXP_PERCENTAGE;
    xpTotal += WEIGHT_PERCENTAGE * totalWeight;
    xpTotal *= (float) (0.9f + StrifePlugin.RNG.nextFloat() * 0.2f);
    Map<LifeSkillType, Float> rewards = new HashMap<>();
    for (LifeSkillType type : skillWeight.keySet()) {
      rewards.put(type, Math.max(1, xpTotal * (skillWeight.get(type) / totalWeight)));
    }
    return rewards;
  }

  public void clearAll() {
    totalExp = 0;
    skillWeight.clear();
  }
}
