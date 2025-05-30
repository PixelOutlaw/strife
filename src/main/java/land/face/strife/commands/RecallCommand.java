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
package land.face.strife.commands;

import com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor;
import com.tealcube.minecraft.bukkit.shade.acf.BaseCommand;
import com.tealcube.minecraft.bukkit.shade.acf.annotation.CommandAlias;
import com.tealcube.minecraft.bukkit.shade.acf.annotation.CommandPermission;
import com.tealcube.minecraft.bukkit.shade.acf.annotation.Default;
import com.tealcube.minecraft.bukkit.shade.acf.annotation.Subcommand;
import com.tealcube.minecraft.bukkit.shade.acf.bukkit.contexts.OnlinePlayer;
import land.face.strife.StrifePlugin;
import land.face.strife.data.conditions.GroundedCondition;
import land.face.strife.timers.RecallTimer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("escape|recall")
public class RecallCommand extends BaseCommand {

  private final StrifePlugin plugin;

  public RecallCommand(StrifePlugin plugin) {
    this.plugin = plugin;
  }

  @Default
  public void recallCommand(Player player) {
    if (RecallTimer.TASKS.containsKey(player.getUniqueId())) {
      RecallTimer.cancelRecall(player);
      return;
    }
    if (RecallTimer.COOLDOWNS.getOrDefault(player.getUniqueId(), 0L) > System.currentTimeMillis()) {
      long msLeft = RecallTimer.COOLDOWNS.getOrDefault(player.getUniqueId(), 0L) - System.currentTimeMillis();
      int secondsLeft = (int) (msLeft / 1000);
      if (secondsLeft >= 60) {
        int minutes = secondsLeft / 60;
        secondsLeft = secondsLeft - (minutes * 60);
        player.sendMessage(FaceColor.YELLOW + "Escape is on cooldown! " +
            FaceColor.WHITE + "Time left: " + minutes + "m " + secondsLeft + "s");
      } else {
        player.sendMessage(FaceColor.YELLOW + "Escape is on cooldown! " +
            FaceColor.WHITE + "Time left: " + secondsLeft + "s");
      }
      return;
    }
    if (RecallTimer.ANTI_ABUSE_COOLDOWN.getOrDefault(player.getUniqueId(), 0L) > System.currentTimeMillis()) {
      player.sendMessage(FaceColor.YELLOW + "You tried to escape too recently! Wait a moment and try again!");
      return;
    }
    RecallTimer.ANTI_ABUSE_COOLDOWN.put(player.getUniqueId(), System.currentTimeMillis() + 1000);
    if (!GroundedCondition.isGroundedOrCloseToGround(player, 0)) {
      player.sendMessage(FaceColor.YELLOW + "You cannot escape unless you're on the ground!");
      return;
    }
    new RecallTimer(plugin.getStrifeMobManager().getStatMob(player));
  }

  @Subcommand("set-spawn")
  @CommandPermission("strife.admin")
  public void creationCommand(CommandSender sender, OnlinePlayer target, String worldName, double x,
      double y, double z, float pitch, float yaw) {
    var champion = plugin.getChampionManager().getChampion(target.getPlayer());
    if (champion == null) {
      return;
    }
    var world = Bukkit.getServer().getWorld(worldName);
    if (world == null) {
      return;
    }
    champion.getSaveData().setRespawnPoint(new Location(world, x, y, z, pitch, yaw));
  }
}