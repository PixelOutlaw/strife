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
package info.faceland.strife.listeners;

import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;
import com.tealcube.minecraft.bukkit.facecore.utilities.TitleUtils;
import info.faceland.strife.events.SkillLevelUpEvent;
import info.faceland.strife.util.PlayerDataUtil;
import io.pixeloutlaw.minecraft.spigot.config.MasterConfiguration;
import org.bukkit.Bukkit;
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
    String color = PlayerDataUtil.getSkillColor(event.getSkillType());
    String name = event.getSkillType().getName();
    int level = event.getNewSkillLevel();
    String upperTitle = color + "SKILL UP!";
    String lowerTitle = color + name + " Level &f" + level;

    TitleUtils.sendTitle(event.getPlayer(),upperTitle, lowerTitle);

    if (event.getNewSkillLevel() % 5 == 0) {
      for (Player p : Bukkit.getOnlinePlayers()) {
        MessageUtils.sendMessage(p, buildMessage(event.getPlayer().getDisplayName(), name, color, level));
      }
    } else {
      MessageUtils.sendMessage(event.getPlayer(), buildMessage(name, color, level));
    }
  }

  private String buildMessage(String name, String color, int level) {
    return SELF_MESSAGE
        .replace("{l}", String.valueOf(level))
        .replace("{c}", color)
        .replace("{n}", name);
  }

  private String buildMessage(String playerName, String name, String color, int level) {
    return BROADCAST_MESSAGE
        .replace("{p}", playerName)
        .replace("{l}", String.valueOf(level))
        .replace("{c}", color)
        .replace("{n}", name);
  }
}
