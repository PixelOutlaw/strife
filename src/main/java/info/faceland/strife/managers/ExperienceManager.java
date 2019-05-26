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

import com.tealcube.minecraft.bukkit.TextUtils;
import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;
import com.tealcube.minecraft.bukkit.facecore.utilities.TitleUtils;
import info.faceland.strife.StrifePlugin;
import info.faceland.strife.api.StrifeExperienceManager;
import info.faceland.strife.attributes.StrifeAttribute;
import info.faceland.strife.data.AttributedEntity;
import info.faceland.strife.data.champion.Champion;
import java.text.DecimalFormat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ExperienceManager implements StrifeExperienceManager {

  private final StrifePlugin plugin;
  private static final String EXP_TEXT = "&a&l( &f&l{0} &a&l/ &f&l{1} &a&l)";
  private static final String EXP_MESSAGE = " &a&l+&f&l{0}&a&lXP";
  private static final DecimalFormat FORMAT = new DecimalFormat("###,###,###");

  public ExperienceManager(StrifePlugin plugin) {
    this.plugin = plugin;
  }

  public void addExperience(Player player, double amount, boolean exact) {
    // Get all the values!
    double maxFaceExp = (double) getMaxFaceExp(player.getLevel());
    double currentExpPercent = player.getExp();

    AttributedEntity pStats = plugin.getAttributedEntityManager().getAttributedEntity(player);

    if (!exact) {
      double statsMult = pStats.getAttribute(StrifeAttribute.XP_GAIN) / 100;
      if (pStats.getChampion().getSaveData().isDisplayExp()) {
        String xp = FORMAT.format(amount * (1 + statsMult));
        MessageUtils.sendMessage(player, EXP_MESSAGE.replace("{0}", xp));
      }
      amount = Math.min(amount, (maxFaceExp / Math.pow(player.getLevel(), 1.5)));
      amount *= 1 + statsMult;
    }

    double faceExpToLevel = maxFaceExp * (1 - currentExpPercent);

    while (amount > faceExpToLevel) {
      player.setExp(0);
      amount -= faceExpToLevel;
      currentExpPercent = 0;
      Champion champion = plugin.getChampionManager().getChampion(player);
      if (player.getLevel() < 100) {
        player.setLevel(player.getLevel() + 1);
        pushLevelUpSpam(player, player.getLevel() % 5 == 0);
      } else {
        champion.setBonusLevels(champion.getBonusLevels() + 1);
        pushBonusLevelUpSpam(player, champion.getBonusLevels(),
            champion.getBonusLevels() % 10 == 0);
      }
      maxFaceExp = (double) getMaxFaceExp(player.getLevel());
      faceExpToLevel = maxFaceExp;
    }

    double newExpPercent = currentExpPercent + amount / maxFaceExp;
    int currentExp = (int) (newExpPercent * maxFaceExp);
    String xpMsg = EXP_TEXT.replace("{0}", FORMAT.format(currentExp))
        .replace("{1}", FORMAT.format(maxFaceExp));
    MessageUtils.sendActionBar(player, xpMsg);

    player.setExp((float) newExpPercent);
  }

  public Integer getMaxFaceExp(int level) {
    if (level == 100) {
      return 10000000;
    }
    return plugin.getLevelingRate().get(level);
  }

  private void pushLevelUpSpam(Player player, boolean announce) {
    MessageUtils.sendMessage(player,
        "&a&lCongratulations! You have reached level &f" + player.getLevel() + "&a!");
    MessageUtils.sendMessage(player,
        "&6You gained a Levelpoint! Use &f/levelup &6to spend levelpoints and raise your stats!");
    TitleUtils.sendTitle(player, "&aLEVEL UP!", "&aOh dang, you got stronger!");
    if (announce) {
      for (Player p : Bukkit.getOnlinePlayers()) {
        MessageUtils.sendMessage(p,
            "&a&lLevelup! &f" + player.getDisplayName() + " &ahas reached level &f" + player
                .getLevel() + "&a!");
      }
    }
  }

  private void pushBonusLevelUpSpam(Player player, int bonusLevel, boolean announce) {
    MessageUtils.sendMessage(player,
        "&a&lCongratulations! You have reached bonus level &f" + bonusLevel + "&e!");
    MessageUtils.sendMessage(player,
        "&eYour stats have slightly increased!");
    TitleUtils.sendTitle(player, "&eBONUS LEVEL UP!", "&eOh dang, you got stronger!");
    if (announce) {
      for (Player p : Bukkit.getOnlinePlayers()) {
        MessageUtils.sendMessage(p,
            "&e&lLevelup! &f" + player.getDisplayName() + " &ehas reached bonus level &f"
                + bonusLevel + "&e!");
      }
    }
  }
}
