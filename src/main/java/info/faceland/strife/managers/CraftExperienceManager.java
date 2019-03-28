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

import static info.faceland.strife.attributes.StrifeAttribute.SKILL_XP_GAIN;
import static info.faceland.strife.events.SkillLevelUpEvent.LifeSkillType.CRAFTING;

import com.tealcube.minecraft.bukkit.TextUtils;
import gyurix.spigotlib.ChatAPI;
import info.faceland.strife.StrifePlugin;
import info.faceland.strife.api.StrifeSkillExperienceManager;
import info.faceland.strife.data.champion.Champion;
import info.faceland.strife.data.champion.ChampionSaveData;
import info.faceland.strife.events.SkillLevelUpEvent;
import info.faceland.strife.events.StrifeCraftEvent;
import java.text.DecimalFormat;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class CraftExperienceManager implements StrifeSkillExperienceManager {

  private final StrifePlugin plugin;
  private static final DecimalFormat FORMAT = new DecimalFormat("###,###,###");

  public CraftExperienceManager(StrifePlugin plugin) {
    this.plugin = plugin;
  }

  public void addExperience(Player player, double amount, boolean exact) {
    addExperience(plugin.getChampionManager().getChampion(player), amount, exact);
  }

  public void addExperience(Champion champion, double amount, boolean exact) {
    ChampionSaveData saveData = champion.getSaveData();
    if (saveData.getCraftingLevel() >= plugin.getMaxSkillLevel()) {
      return;
    }
    if (!exact) {
      double statsMult = champion.getCombinedCache().getOrDefault(SKILL_XP_GAIN, 0D) / 100;
      amount *= 1 + statsMult;
    }
    StrifeCraftEvent craftEvent = new StrifeCraftEvent(champion.getPlayer(), (float) amount);
    StrifePlugin.getInstance().getServer().getPluginManager().callEvent(craftEvent);
    double currentExp = saveData.getCraftingExp() + craftEvent.getAmount();
    double maxExp = (double) getMaxExp(saveData.getCraftingLevel());

    while (currentExp > maxExp) {
      currentExp -= maxExp;
      saveData.setCraftingLevel(saveData.getCraftingLevel() + 1);

      SkillLevelUpEvent craftingLevelUp =
          new SkillLevelUpEvent(champion.getPlayer(), CRAFTING, saveData.getCraftingLevel());
      Bukkit.getPluginManager().callEvent(craftingLevelUp);

      if (saveData.getCraftingLevel() >= plugin.getMaxSkillLevel()) {
        break;
      }
      maxExp = (double) getMaxExp(saveData.getCraftingLevel());
    }

    saveData.setCraftingExp((float) currentExp);
    String xpMsg = "&e&l( &f&l" + FORMAT.format((int) currentExp) + " &e&l/ &f&l" + FORMAT.format((int) maxExp) + " XP &e&l)";
    ChatAPI.sendJsonMsg(ChatAPI.ChatMessageType.ACTION_BAR, TextUtils.color(xpMsg),
        champion.getPlayer());
  }

  public Integer getMaxExp(int level) {
    return plugin.getCraftingRate().get(level);
  }
}
