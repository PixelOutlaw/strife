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

import com.tealcube.minecraft.bukkit.TextUtils;
import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;
import gyurix.api.TitleAPI;
import info.faceland.strife.events.SkillLevelUpEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class SkillLevelUpListener implements Listener {

  private static final String SELF_MESSAGE =
      "{c}&lSkill Up! {c}Your skill level in &f{n} {c}has increased to &f{l}{c}!";
  private static final String BROADCAST_MESSAGE =
      "{c}&lSkill Up! &f{p} {c}has reached &f{n} {c}skill level &f{l}{c}!";

  @EventHandler(priority = EventPriority.NORMAL)
  public void onSkillLevelUp(SkillLevelUpEvent event) {
    String color;
    String name;
    switch (event.getSkillType()) {
      case CRAFTING:
        color = "&e";
        name = "Crafting";
        break;
      case ENCHANTING:
        color = "&d";
        name = "Enchanting";
        break;
      case FISHING:
        color = "&e";
        name = "Fishing";
        break;
      case MINING:
        color = "&a";
        name = "Mining";
        break;
      default:
        color = "&k";
        name = "NULL";
    }

    int level = event.getNewSkillLevel();

    TitleAPI.set(
        TextUtils.color(color + "SKILL UP!"),
        TextUtils.color(color + name + " Level &f" + level),
        10, 40, 20,
        event.getPlayer()
    );

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
