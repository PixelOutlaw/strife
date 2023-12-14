/**
 * The MIT License Copyright (c) 2015 Teal Cube Games
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package land.face.strife.managers;

import com.comphenix.xp.lookup.LevelingRate;
import com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor;
import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;
import com.tealcube.minecraft.bukkit.facecore.utilities.PaletteUtil;
import com.tealcube.minecraft.bukkit.shade.objecthunter.exp4j.Expression;
import com.tealcube.minecraft.bukkit.shade.objecthunter.exp4j.ExpressionBuilder;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.ability.Ability;
import land.face.strife.data.champion.Champion;
import land.face.strife.data.champion.ChampionSaveData;
import land.face.strife.data.champion.LifeSkillType;
import land.face.strife.events.SkillExpGainEvent;
import land.face.strife.events.SkillLevelUpEvent;
import land.face.strife.managers.IndicatorManager.IndicatorStyle;
import land.face.strife.stats.StrifeStat;
import land.face.strife.util.PlayerDataUtil;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class SkillExperienceManager {

  private final StrifePlugin plugin;
  private static final DecimalFormat FORMAT = new DecimalFormat("###,###,###");
  //private static final String XP_AB = "{0}( &f&l{1} {0}/ &f&l{2} XP {0})";
  private final String XP_MSG;
  private final int MAX_SKILL_LEVEL;

  private final List<String> skillBackground = List.of(
      "𫝀\uF809\uF801",
      "𫝁\uF809\uF801",
      "𫝂\uF809\uF801",
      "𫝃\uF809\uF801",
      "𫝄\uF809\uF801",
      "𫝅\uF809\uF801",
      "𫝆\uF809\uF801",
      "𫝇\uF809\uF801",
      "𫝈\uF809\uF801",
      "𫝉\uF809\uF801",
      "𫝊\uF809\uF801",
      "𫝋\uF809\uF801",
      "𫝌\uF809\uF801",
      "𫝍\uF809\uF801",
      "𫝎\uF809\uF801",
      "𫝏\uF809\uF801",
      "𫝐\uF809\uF801",
      "𫝑\uF809\uF801",
      "𫝒\uF809\uF801",
      "𫝓\uF809\uF801"
  );
  private final Map<Integer, String> skillLevel = buildSkillLevelStrings();

  private final Map<LifeSkillType, LevelingRate> levelingRates = new HashMap<>();

  public SkillExperienceManager(StrifePlugin plugin) {
    this.plugin = plugin;
    MAX_SKILL_LEVEL = plugin.getSettings().getInt("config.leveling.max-skill-level", 50);
    XP_MSG = PaletteUtil.color(plugin.getSettings().getString("language.skills.xp-msg", "{c}Gained |white|{n} {c}XP! |white|(+{a}XP)"));
    setupLevelingRates(plugin);
  }

  public void addExperience(Player player, LifeSkillType type, double amount, boolean exact, boolean forceDisplay) {
    addExperience(player, type, null, amount, exact, forceDisplay);
  }

  public void addExperience(Player player, LifeSkillType type, Location location, double amount, boolean exact, boolean forceDisplay) {
    addExperience(plugin.getStrifeMobManager().getStatMob(player), type, location, amount, exact, forceDisplay);
  }

  public void addExperience(StrifeMob mob, LifeSkillType type, double amount, boolean exact,
      boolean forceDisplay) {
    addExperience(mob, type, null, amount, exact, forceDisplay);
  }

  public void addExperience(StrifeMob mob, LifeSkillType type, Location location, double amount,
      boolean exact, boolean forceDisplay) {
    Player player = (Player) mob.getEntity();
    ChampionSaveData saveData = mob.getChampion().getSaveData();
    if (amount < 0.001) {
      return;
    }
    if (saveData.getSkillLevel(type) >= plugin.getMaxSkillLevel()) {
      return;
    }
    if (!exact) {
      float skillXpMult = mob.getStat(StrifeStat.SKILL_XP_GAIN) / 100;
      amount *= 1 + skillXpMult;
    }
    if (location != null) {
      String str = MiniMessage.miniMessage().serialize(LegacyComponentSerializer.legacySection().deserialize(
          type.getColor() + FaceColor.BOLD.s() + "+" +
              FaceColor.WHITE + FaceColor.BOLD.s() + (int) amount +
              type.getColor() + FaceColor.BOLD.s() + "XP!"
      ));
      plugin.getIndicatorManager().addIndicator(mob.getEntity(), location, IndicatorStyle.FLOAT_UP_SLOW, 10, str, 0.9f, 1.0f, 0.7f);
    }
    if (saveData.isDisplayExp() || forceDisplay) {
      String xp = FORMAT.format(amount);
      MessageUtils.sendMessage(player, XP_MSG
          .replace("{c}", "" + type.getColor())
          .replace("{n}", type.getPrettyName())
          .replace("{a}", xp)
      );
    }

    SkillExpGainEvent xpEvent = new SkillExpGainEvent(mob.getChampion(), type, (float) amount);
    StrifePlugin.getInstance().getServer().getPluginManager().callEvent(xpEvent);

    double currentExp = saveData.getSkillExp(type) + xpEvent.getAmount();
    double maxExp = (double) getMaxExp(type, saveData.getSkillLevel(type));

    boolean levelUp = false;
    while (currentExp > maxExp) {
      levelUp = true;
      currentExp -= maxExp;
      saveData.setSkillLevel(type, saveData.getSkillLevel(type) + 1);

      SkillLevelUpEvent levelUpEvent = new SkillLevelUpEvent(player, type,
          saveData.getSkillLevel(type));
      Bukkit.getPluginManager().callEvent(levelUpEvent);

      if (saveData.getSkillLevel(type) >= plugin.getMaxSkillLevel()) {
        break;
      }
      maxExp = (double) getMaxExp(type, saveData.getSkillLevel(type));
      checkSkillUnlock(player, type);
    }
    if (levelUp) {
      mob.setReCache(true);
    }
    saveData.setSkillExp(type, (float) currentExp);
    try {
      plugin.getTopBarManager().updateSkills(player, buildSkillString(mob.getChampion(), type));
    } catch (Exception e) {
      Bukkit.getLogger().warning("[Strife] Exception setting top bar on xp gain!");
      e.printStackTrace();
    }
  }

  private String buildSkillString(Champion champion, LifeSkillType skill) {
    if (skill == null) {
      return "";
    }
    plugin.getChampionManager().updateRecentSkills(champion, skill);
    return updateSkillString(champion);
  }

  public String updateSkillString(Champion champion) {
    StringBuilder newTitle = new StringBuilder();
    for (LifeSkillType skillType : champion.getRecentSkills()) {
      int level = PlayerDataUtil.getLifeSkillLevel(champion, skillType);
      if (level < 100) {
        int progress = Math.min(99, (int) (100D * PlayerDataUtil.getSkillProgress(champion, skillType)));
        newTitle.append(FaceColor.NO_SHADOW).append(skillType.getCharacter()).append(FaceColor.LIME)
            .append(progress).append("%").append("  ");
      }
    }
    return newTitle.toString();
  }

  public Integer getMaxExp(LifeSkillType type, int level) {
    return levelingRates.get(type).get(level);
  }

  public void checkSkillUnlock(Player player, LifeSkillType skill) {
    Champion champion = plugin.getChampionManager().getChampion(player);
    for (Ability ability : plugin.getAbilityManager().getLoadedAbilities().values()) {
      if (ability.isHidden()) {
        continue;
      }
      if (ability.getAbilityIconData() != null) {
        int reqLv = ability.getAbilityIconData().getLifeSkillRequirements().getOrDefault(skill, -1);
        if (reqLv == champion.getLifeSkillLevel(skill)) {
          if (ability.getAbilityIconData().isRequirementMet(champion)) {
            MessageUtils.sendMessage(player,
                "&b❖ Neato! You've unlocked the " + skill.getColor() + skill.getPrettyName()
                    + "&b ability, " + skill.getColor() + ability.getName()
                    + "&b! Visit an ability trainer to try it out!");
          }
        }
      }
    }
  }

  private void setupLevelingRates(StrifePlugin plugin) {
    LevelingRate combatRate = new LevelingRate();
    Expression expression = new ExpressionBuilder(plugin.getSettings()
        .getString("config.leveling.COMBAT-SKILLS", "(5+(2*LEVEL)+(LEVEL^1.2))*LEVEL"))
        .variable("LEVEL").build();
    for (int i = 0; i < MAX_SKILL_LEVEL; i++) {
      combatRate.put(i, i, (int) Math.round(expression.setVariable("LEVEL", i).evaluate()));
    }
    for (LifeSkillType type : LifeSkillType.values()) {
      if (type.isCombat()) {
        levelingRates.put(type, combatRate);
        continue;
      }
      LevelingRate skillRate = new LevelingRate();
      Expression rateExpr = new ExpressionBuilder(plugin.getSettings()
          .getString("config.leveling." + type.toString(), "(5+(2*LEVEL)+(LEVEL^1.2))*LEVEL"))
          .variable("LEVEL").build();
      for (int i = 0; i < MAX_SKILL_LEVEL; i++) {
        skillRate.put(i, i, (int) Math.round(rateExpr.setVariable("LEVEL", i).evaluate()));
      }
      levelingRates.put(type, skillRate);
    }
  }

  public Map<Integer, String> buildSkillLevelStrings() {
    Map<Integer, String> lvlStrings = new HashMap<>();
    for (int i = 0; i < 100; i++) {
      String s = Integer.toString(i);
      if (i < 10) {
        s = s
            .replaceAll("0", "𝟘")
            .replaceAll("1", "𝟙")
            .replaceAll("2", "𝟚")
            .replaceAll("3", "𝟛")
            .replaceAll("4", "𝟜")
            .replaceAll("5", "𝟝")
            .replaceAll("6", "𝟞")
            .replaceAll("7", "𝟟")
            .replaceAll("8", "𝟠")
            .replaceAll("9", "𝟡");
        s = "\uF808\uF802" + s + "\uF826";
        lvlStrings.put(i, s);
      } else {
        s = s
            .replaceAll("0", "𝟘\uF801")
            .replaceAll("1", "𝟙\uF801")
            .replaceAll("2", "𝟚\uF801")
            .replaceAll("3", "𝟛\uF801")
            .replaceAll("4", "𝟜\uF801")
            .replaceAll("5", "𝟝\uF801")
            .replaceAll("6", "𝟞\uF801")
            .replaceAll("7", "𝟟\uF801")
            .replaceAll("8", "𝟠\uF801")
            .replaceAll("9", "𝟡\uF801");
        s = "\uF819\uF822" + s + "\uF824";
        lvlStrings.put(i, s);
      }
    }
    return lvlStrings;
  }

}
