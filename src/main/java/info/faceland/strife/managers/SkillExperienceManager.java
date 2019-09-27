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
package info.faceland.strife.managers;

import static info.faceland.strife.stats.StrifeStat.SKILL_XP_GAIN;

import com.comphenix.xp.lookup.LevelingRate;
import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;
import com.tealcube.minecraft.bukkit.shade.objecthunter.exp4j.Expression;
import com.tealcube.minecraft.bukkit.shade.objecthunter.exp4j.ExpressionBuilder;
import info.faceland.strife.StrifePlugin;
import info.faceland.strife.data.champion.Champion;
import info.faceland.strife.data.champion.ChampionSaveData;
import info.faceland.strife.data.champion.LifeSkillType;
import info.faceland.strife.events.SkillExpGainEvent;
import info.faceland.strife.events.SkillLevelUpEvent;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class SkillExperienceManager {

  private final StrifePlugin plugin;
  private static final DecimalFormat FORMAT = new DecimalFormat("###,###,###");
  private static final String XP_AB = "{0}( &f&l{1} {0}/ &f&l{2} XP {0})";
  private final String XP_MSG;
  private final int MAX_SKILL_LEVEL;

  private Map<LifeSkillType, LevelingRate> levelingRates = new HashMap<>();

  public SkillExperienceManager(StrifePlugin plugin) {
    this.plugin = plugin;
    MAX_SKILL_LEVEL = plugin.getSettings().getInt("config.leveling.max-skill-level", 50);
    XP_MSG = plugin.getSettings()
        .getString("language.skills.xp-msg", "{c}Gained &f{n} {c}XP! &f(+{a}XP)");
    setupLevelingRates(plugin);
  }

  public void addExperience(Player player, LifeSkillType type, double amount, boolean exact) {
    addExperience(plugin.getChampionManager().getChampion(player), type, amount, exact);
  }

  public void addExperience(Champion champion, LifeSkillType type, double amount, boolean exact) {
    ChampionSaveData saveData = champion.getSaveData();
    if (amount < 0.001) {
      return;
    }
    if (saveData.getSkillLevel(type) >= plugin.getMaxSkillLevel()) {
      return;
    }
    if (!exact) {
      double statsMult = champion.getCombinedCache().getOrDefault(SKILL_XP_GAIN, 0f) / 100f;
      amount *= 1 + statsMult;
      if (champion.getSaveData().isDisplayExp()) {
        String xp = FORMAT.format(amount * (1 + statsMult));
        MessageUtils.sendMessage(champion.getPlayer(), XP_MSG
            .replace("{c}", "" + type.getColor())
            .replace("{n}", type.getName())
            .replace("{a}", xp)
        );
      }
    }

    SkillExpGainEvent xpEvent = new SkillExpGainEvent(champion, type, (float) amount);
    StrifePlugin.getInstance().getServer().getPluginManager().callEvent(xpEvent);

    double currentExp = saveData.getSkillExp(type) + xpEvent.getAmount();
    double maxExp = (double) getMaxExp(type, saveData.getSkillLevel(type));

    while (currentExp > maxExp) {
      currentExp -= maxExp;
      saveData.setSkillLevel(type, saveData.getSkillLevel(type) + 1);

      SkillLevelUpEvent levelUpEvent = new SkillLevelUpEvent(champion.getPlayer(), type,
          saveData.getSkillLevel(type));
      Bukkit.getPluginManager().callEvent(levelUpEvent);

      if (saveData.getSkillLevel(type) >= plugin.getMaxSkillLevel()) {
        break;
      }
      maxExp = (double) getMaxExp(type, saveData.getSkillLevel(type));
    }

    saveData.setSkillExp(type, (float) currentExp);
    ChatColor c = type.getColor();
    String xpMsg = XP_AB.replace("{0}", "" + c).replace("{1}", FORMAT.format((int) currentExp))
        .replace("{2}", FORMAT.format((int) maxExp));
    MessageUtils.sendActionBar(champion.getPlayer(), xpMsg);
    plugin.getBossBarManager().bumpSkillBar(champion, type);
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
      switch (type) {
        case ARCHERY:
        case AXE_MASTERY:
        case BLACK_MAGICS:
        case DUAL_WIELDING:
        case ARCANE_MAGICS:
        case SWORDSMANSHIP:
        case NATURAL_MAGICS:
        case SHIELD_MASTERY:
        case CELESTIAL_MAGICS:
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
    }
  }

}
