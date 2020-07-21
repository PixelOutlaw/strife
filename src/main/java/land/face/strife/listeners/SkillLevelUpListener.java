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
package land.face.strife.listeners;

import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;
import com.tealcube.minecraft.bukkit.facecore.utilities.TitleUtils;
import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import github.scarsz.discordsrv.util.DiscordUtil;
import io.pixeloutlaw.minecraft.spigot.config.MasterConfiguration;
import io.pixeloutlaw.minecraft.spigot.garbage.StringExtensionsKt;
import land.face.strife.events.SkillLevelUpEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class SkillLevelUpListener implements Listener {

  private static String SELF_MESSAGE;
  private static String BROADCAST_MESSAGE;

  public SkillLevelUpListener(MasterConfiguration settings) {
    SELF_MESSAGE = settings.getString("language.skills.lvl-up-self");
    BROADCAST_MESSAGE = settings.getString("language.skills.lvl-up-broadcast");
  }

  @EventHandler(priority = EventPriority.NORMAL)
  public void onSkillLevelUp(SkillLevelUpEvent event) {
    ChatColor color = event.getSkillType().getColor();
    String name = event.getSkillType().getName();
    int level = event.getNewSkillLevel();
    String upperTitle = color + "SKILL UP!";
    String lowerTitle =
        color + "You've reached " + ChatColor.WHITE + name + " Lv" + level + color + "!";

    TitleUtils.sendTitle(event.getPlayer(), upperTitle, lowerTitle, 20, 5, 5);
    event.getPlayer()
        .playSound(event.getPlayer().getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1.3f);

    if (event.getNewSkillLevel() % 5 == 0) {
      String discordMessage = ":crafting: **" + event.getPlayer().getDisplayName() + " has reached "
          + event.getSkillType().getName() + " skill level " + event.getNewSkillLevel() + "!**";
      TextChannel textChannel = DiscordSRV.getPlugin().getMainTextChannel();
      DiscordUtil.sendMessage(textChannel, discordMessage);
      String msg = StringExtensionsKt.chatColorize(buildMessage(event.getPlayer().getDisplayName(), name, color, level));
      for (Player p : Bukkit.getOnlinePlayers()) {
        MessageUtils.sendMessage(p, msg);
      }
    } else {
      MessageUtils.sendMessage(event.getPlayer(), buildMessage(name, color, level));
    }
  }

  private String buildMessage(String name, ChatColor color, int level) {
    return SELF_MESSAGE
        .replace("{l}", String.valueOf(level))
        .replace("{c}", "" + color)
        .replace("{n}", name);
  }

  private String buildMessage(String playerName, String name, ChatColor color, int level) {
    return BROADCAST_MESSAGE
        .replace("{p}", playerName)
        .replace("{l}", String.valueOf(level))
        .replace("{c}", "" + color)
        .replace("{n}", name);
  }
}
