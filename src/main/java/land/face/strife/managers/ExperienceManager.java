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

import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;
import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import github.scarsz.discordsrv.util.DiscordUtil;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.stats.StrifeStat;
import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.EntityEffect;
import org.bukkit.entity.Player;

public class ExperienceManager {

  private final StrifePlugin plugin;
  private static final String EXP_MESSAGE = " &a&l+&f&l{0}&a&lXP";
  private static final DecimalFormat FORMAT = new DecimalFormat("###,###,###");

  private final Date catchupStartDate;
  private final double catchupXpPerDay;
  @Getter
  private double globalCatchupXp;

  @SneakyThrows
  public ExperienceManager(StrifePlugin plugin) {
    this.plugin = plugin;
    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.ENGLISH);
    catchupStartDate = sdf.parse(plugin.getSettings().getString("config.catchup-xp-date"));
    catchupXpPerDay = plugin.getSettings().getDouble("config.catchup-xp-per-day");
    refreshCatchupXp();
  }

  public void refreshCatchupXp() {
    Date currentDate = new Date();
    long diffInMillies = Math.abs(currentDate.getTime() - catchupStartDate.getTime());
    long daysSinceStartTime = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
    globalCatchupXp = daysSinceStartTime * catchupXpPerDay;
    Bukkit.getLogger().info("[Strife] Global catchup recalculated!");
    Bukkit.getLogger().info("[Strife]  - Per Day: " + catchupXpPerDay);
    Bukkit.getLogger().info("[Strife]  - Days Since: " + daysSinceStartTime);
    Bukkit.getLogger().info("[Strife]  - Total: " + globalCatchupXp);
  }

  public void addExperience(Player player, double amount, boolean exact) {
    if (amount < 0.001 || player.getLevel() > 99) {
      return;
    }
    // Get all the values!
    double maxFaceExp = (double) getMaxFaceExp(player.getLevel());
    double currentExpPercent = player.getExp();

    StrifeMob pStats = plugin.getStrifeMobManager().getStatMob(player);

    if (!exact) {
      double original = amount;
      double statsMult = pStats.getStat(StrifeStat.XP_GAIN) / 100;
      pStats.getChampion().getDetailsContainer().addExp((float) amount);
      amount *= 1 + statsMult;
      double catchupDiff = globalCatchupXp - pStats.getChampion().getSaveData().getCatchupExpUsed();
      if (catchupDiff > 0) {
        double bonusXp = Math.min(original, catchupDiff);
        amount += bonusXp;
        pStats.getChampion().getSaveData().setCatchupExpUsed(
            pStats.getChampion().getSaveData().getCatchupExpUsed() + bonusXp);
      }
    }

    if (exact || pStats.getChampion().getSaveData().isDisplayExp()) {
      String xp = FORMAT.format(amount);
      MessageUtils.sendMessage(player, EXP_MESSAGE.replace("{0}", xp));
    }

    double faceExpToLevel = maxFaceExp * (1 - currentExpPercent);

    boolean levelUp = false;
    while (amount > faceExpToLevel) {
      player.setExp(0);
      amount -= faceExpToLevel;
      currentExpPercent = 0;
      if (player.getLevel() < 100) {
        player.setLevel(player.getLevel() + 1);
        pushLevelUpSpam(player, player.getLevel() % 5 == 0, !levelUp);
      }
      maxFaceExp = (double) getMaxFaceExp(player.getLevel());
      faceExpToLevel = maxFaceExp;
      levelUp = true;
    }

    double newExpPercent = currentExpPercent + amount / maxFaceExp;

    player.setExp((float) newExpPercent);
    plugin.getGuiManager().updateLevelDisplay(pStats);
  }

  public double getRemainingCatchupXp(double catchupXpUsed) {
    return globalCatchupXp - catchupXpUsed;
  }

  public Integer getMaxFaceExp(int level) {
    if (level >= 100) {
      return 10000000;
    }
    return plugin.getLevelingRate().get(level);
  }

  private void pushLevelUpSpam(Player player, boolean announce, boolean showTotem) {
    if (showTotem) {
      player.playEffect(EntityEffect.TOTEM_RESURRECT);
    }
    MessageUtils.sendMessage(player,
        "&a&lCongratulations! You have reached level &f" + player.getLevel() + "&a!");
    MessageUtils.sendMessage(player,
        "&6You gained a Levelpoint! Use &f/levelup &6to spend levelpoints and raise your stats!");
    if (announce) {
      String discordMessage = ":levelup: **" + player.getDisplayName() + " has reached level " + player.getLevel() + "!**";
      TextChannel textChannel = DiscordSRV.getPlugin().getMainTextChannel();
      DiscordUtil.sendMessage(textChannel, discordMessage);
      String chatMessage = "&a&lLevelup! &f" + player.getDisplayName() + " &ahas reached level &f" + player.getLevel() + "&a!";
      for (Player p : Bukkit.getOnlinePlayers()) {
        MessageUtils.sendMessage(p, chatMessage);
      }
    }
  }
}
