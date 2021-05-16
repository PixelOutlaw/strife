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
import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;
import com.tealcube.minecraft.bukkit.shade.objecthunter.exp4j.Expression;
import com.tealcube.minecraft.bukkit.shade.objecthunter.exp4j.ExpressionBuilder;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.champion.ChampionSaveData;
import land.face.strife.data.champion.LifeSkillType;
import land.face.strife.events.SkillExpGainEvent;
import land.face.strife.events.SkillLevelUpEvent;
import land.face.strife.stats.StrifeStat;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class SkillExperienceManager {

  private final StrifePlugin plugin;
  private static final DecimalFormat FORMAT = new DecimalFormat("###,###,###");
  //private static final String XP_AB = "{0}( &f&l{1} {0}/ &f&l{2} XP {0})";
  private final String XP_MSG;
  private final int MAX_SKILL_LEVEL;

  private final Map<LifeSkillType, LevelingRate> levelingRates = new HashMap<>();

  public SkillExperienceManager(StrifePlugin plugin) {
    this.plugin = plugin;
    MAX_SKILL_LEVEL = plugin.getSettings().getInt("config.leveling.max-skill-level", 50);
    XP_MSG = plugin.getSettings()
        .getString("language.skills.xp-msg", "{c}Gained &f{n} {c}XP! &f(+{a}XP)");
    setupLevelingRates(plugin);
  }

  public void addExperience(Player player, LifeSkillType type, double amount, boolean exact, boolean forceDisplay) {
    addExperience(plugin.getStrifeMobManager().getStatMob(player), type, amount, exact, forceDisplay);
  }

  public void addExperience(StrifeMob mob, LifeSkillType type, double amount, boolean exact, boolean forceDisplay) {
    Player player = (Player) mob.getEntity();
    ChampionSaveData saveData = mob.getChampion().getSaveData();
    if (amount < 0.001) {
      return;
    }
    if (saveData.getSkillLevel(type) >= plugin.getMaxSkillLevel()) {
      return;
    }
    if (!exact) {
      float skillXpMult = plugin.getStrifeMobManager().getStatMob(player).getStat(StrifeStat.SKILL_XP_GAIN) / 100;
      amount *= 1 + skillXpMult;
    }
    if (saveData.isDisplayExp() || forceDisplay) {
      String xp = FORMAT.format(amount);
      MessageUtils.sendMessage(player, XP_MSG
          .replace("{c}", "" + type.getColor())
          .replace("{n}", type.getName())
          .replace("{a}", xp)
      );
    }

    SkillExpGainEvent xpEvent = new SkillExpGainEvent(mob.getChampion(), type, (float) amount);
    StrifePlugin.getInstance().getServer().getPluginManager().callEvent(xpEvent);

    double currentExp = saveData.getSkillExp(type) + xpEvent.getAmount();
    double maxExp = (double) getMaxExp(type, saveData.getSkillLevel(type));

    while (currentExp > maxExp) {
      currentExp -= maxExp;
      saveData.setSkillLevel(type, saveData.getSkillLevel(type) + 1);

      SkillLevelUpEvent levelUpEvent = new SkillLevelUpEvent(player, type,
          saveData.getSkillLevel(type));
      Bukkit.getPluginManager().callEvent(levelUpEvent);

      if (saveData.getSkillLevel(type) >= plugin.getMaxSkillLevel()) {
        break;
      }
      maxExp = (double) getMaxExp(type, saveData.getSkillLevel(type));
    }
    saveData.setSkillExp(type, (float) currentExp);
    plugin.getBossBarManager().pushSkillBar(player, type);
  }

  public Integer getMaxExp(LifeSkillType type, int level) {
    return levelingRates.get(type).get(level);
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
      if (type.isComnbat()) {
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

}
