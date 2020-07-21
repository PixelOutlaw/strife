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

import com.tealcube.minecraft.bukkit.TextUtils;
import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;
import com.tealcube.minecraft.bukkit.facecore.utilities.TitleUtils;
import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import github.scarsz.discordsrv.util.DiscordUtil;
import java.text.DecimalFormat;
import land.face.strife.StrifePlugin;
import land.face.strife.api.StrifeExperienceManager;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.champion.Champion;
import land.face.strife.stats.StrifeStat;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ExperienceManager implements StrifeExperienceManager {

  private final StrifePlugin plugin;
  private static final String EXP_MESSAGE = " &a&l+&f&l{0}&a&lXP";
  private static final DecimalFormat FORMAT = new DecimalFormat("###,###,###");

  public ExperienceManager(StrifePlugin plugin) {
    this.plugin = plugin;
  }

  public void addExperience(Player player, double amount, boolean exact) {
    if (amount < 0.001) {
      return;
    }
    // Get all the values!
    double maxFaceExp = (double) getMaxFaceExp(player.getLevel());
    double currentExpPercent = player.getExp();

    StrifeMob pStats = plugin.getStrifeMobManager().getStatMob(player);

    if (!exact) {
      double statsMult = pStats.getStat(StrifeStat.XP_GAIN) / 100;
      if (pStats.getChampion().getSaveData().isDisplayExp()) {
        String xp = FORMAT.format(amount * (1 + statsMult));
        MessageUtils.sendMessage(player, EXP_MESSAGE.replace("{0}", xp));
      }
      pStats.getChampion().getDetailsContainer().addExp((float) amount);
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

    player.setExp((float) newExpPercent);
  }

  public Integer getMaxFaceExp(int level) {
    if (level >= 100) {
      return 10000000;
    }
    return plugin.getLevelingRate().get(level);
  }

  private void pushLevelUpSpam(Player player, boolean announce) {
    MessageUtils.sendMessage(player,
        "&a&lCongratulations! You have reached level &f" + player.getLevel() + "&a!");
    MessageUtils.sendMessage(player,
        "&6You gained a Levelpoint! Use &f/levelup &6to spend levelpoints and raise your stats!");
    String upperTitle = TextUtils.color("&aLEVEL UP!");
    String lowerTitle = TextUtils.color("&aYou've reached &fLevel " + player.getLevel());
    TitleUtils.sendTitle(player, upperTitle, lowerTitle, 20, 2, 2);
    if (announce) {
      String discordMessage =
          ":video_game: **" + player.getDisplayName() + " has reached level " + player.getLevel()
              + "!**";
      TextChannel textChannel = DiscordSRV.getPlugin().getMainTextChannel();
      DiscordUtil.sendMessage(textChannel, discordMessage);
      String chatMessage =
          "&a&lLevelup! &f" + player.getDisplayName() + " &ahas reached level &f" + player
              .getLevel() + "&a!";
      for (Player p : Bukkit.getOnlinePlayers()) {
        MessageUtils.sendMessage(p, chatMessage);
      }
    }
  }

  private void pushBonusLevelUpSpam(Player player, int bonusLevel, boolean announce) {
    MessageUtils.sendMessage(player,
        "&a&lCongratulations! You have reached bonus level &f" + bonusLevel + "&e!");
    MessageUtils.sendMessage(player,
        "&eYour stats have slightly increased!");
    String upperTitle = TextUtils.color("&eBONUS LEVEL UP!");
    String lowerTitle = TextUtils.color("&eOh dang, you got stronger!");
    TitleUtils.sendTitle(player, upperTitle, lowerTitle, 20, 2, 2);
    if (announce) {
      for (Player p : Bukkit.getOnlinePlayers()) {
        MessageUtils.sendMessage(p,
            "&e&lLevelup! &f" + player.getDisplayName() + " &ehas reached bonus level &f"
                + bonusLevel + "&e!");
      }
    }
  }
}
