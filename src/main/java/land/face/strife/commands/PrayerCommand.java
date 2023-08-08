/**
 * The MIT License Copyright (c) 2015 Teal Cube Games
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package land.face.strife.commands;

import com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor;
import com.tealcube.minecraft.bukkit.facecore.utilities.PaletteUtil;
import com.tealcube.minecraft.bukkit.shade.acf.BaseCommand;
import com.tealcube.minecraft.bukkit.shade.acf.annotation.CommandAlias;
import com.tealcube.minecraft.bukkit.shade.acf.annotation.CommandPermission;
import com.tealcube.minecraft.bukkit.shade.acf.annotation.Default;
import com.tealcube.minecraft.bukkit.shade.acf.annotation.Subcommand;
import com.tealcube.minecraft.bukkit.shade.acf.bukkit.contexts.OnlinePlayer;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.managers.PrayerManager;
import land.face.strife.stats.StrifeStat;
import land.face.strife.util.StatUtil;
import net.kyori.adventure.audience.Audience;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("prayer")
public class PrayerCommand extends BaseCommand {

  private final StrifePlugin plugin;
  public PrayerCommand(StrifePlugin plugin) {
    this.plugin = plugin;
  }

  @Default
  @CommandPermission("prayer.enabled")
  public void baseCommand(Player sender) {
    plugin.getPrayerManager().getPrayerMenu().open(sender);
  }

  @Subcommand("restore")
  @CommandPermission("strife.admin")
  public void creationCommand(CommandSender sender, OnlinePlayer target) {
    StrifeMob mob = plugin.getStrifeMobManager().getStatMob(target.getPlayer());
    StatUtil.getStat(mob, StrifeStat.MAX_PRAYER_POINTS);
    if (target.getPlayer().hasPermission("prayer.enabled") && mob.getPrayer() != mob.getMaxPrayer()) {
      PaletteUtil.sendMessage(target.getPlayer(), FaceColor.WHITE.s() + FaceColor.ITALIC + "Faith restored!");
      Audience audience = Audience.audience(target.getPlayer());
      audience.playSound(PrayerManager.FAITH_RESTORED);
    }
    mob.setPrayer(mob.getMaxPrayer());
    plugin.getPrayerManager().sendPrayerUpdate(
        target.getPlayer(), 1, plugin.getPrayerManager().isPrayerActive(target.getPlayer()));
  }
}
