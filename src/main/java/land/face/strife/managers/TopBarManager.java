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

import com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor;
import eu.decentsoftware.holograms.api.utils.PAPI;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import land.face.strife.StrifePlugin;
import land.face.strife.data.Boost;
import land.face.strife.data.TopBarData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class TopBarManager {

  private final StrifePlugin plugin;
  private final Map<Player, TopBarData> dataMap = new WeakHashMap<>();
  private final Map<Integer, String> compassIcons = new HashMap<>();

  public TopBarManager(StrifePlugin plugin) {
    this.plugin = plugin;
    compassIcons.put(0, FaceColor.NO_SHADOW + "冧 ");
    compassIcons.put(1, FaceColor.NO_SHADOW + "冨 ");
    compassIcons.put(2, FaceColor.NO_SHADOW + "冩 ");
    compassIcons.put(3, FaceColor.NO_SHADOW + "冪 ");
    compassIcons.put(4, FaceColor.NO_SHADOW + "冫 ");
    compassIcons.put(5, FaceColor.NO_SHADOW + "冬 ");
    compassIcons.put(6, FaceColor.NO_SHADOW + "冭 ");
    compassIcons.put(7, FaceColor.NO_SHADOW + "冮 ");
    compassIcons.put(8, FaceColor.NO_SHADOW + "冧 ");

    // LocationTask
    Bukkit.getScheduler().runTaskTimer(plugin, () -> {
      for (Player p : dataMap.keySet()) {
        if (p.isOnline()) {
          updateLocation(p, "%strife_location%   ");
        }
      }
    }, 100L, 100L);

    // event Task
    Bukkit.getScheduler().runTaskTimer(plugin, () -> {
      String eventString = plugin.getBoostManager().getBoostString();
      for (Player p : dataMap.keySet()) {
        if (p.isOnline()) {
          updateEvent(p, eventString);
        }
      }
    }, 100L, 20L * 15 + 1);

    // Compass and clock task
    Bukkit.getScheduler().runTaskTimer(plugin, () -> {
      World world = Bukkit.getServer().getWorld("Quest_world");
      if (world == null) {
        return;
      }
      long time = (int) ((world.getTime() / 1000 + 8) % 24);
      String affix = time < 12 ? "am   " : "pm   ";
      if (time > 12) {
        time -= 12;
      } else if (time == 0) {
        time = 12;
      }
      String clockString = FaceColor.NO_SHADOW + "冦 " + FaceColor.WHITE + time + affix;
      // HALLOWEEN ONLY
      // String clockString = FaceColor.NO_SHADOW + "冦 " + FaceColor.ORANGE + "Witching Hour  ";
      for (Player p : dataMap.keySet()) {
        if (p.isOnline()) {
          TopBarData data = dataMap.get(p);
          int direction = Math.round((p.getLocation().getYaw() + 180) / 45f);
          data.setCompass(compassIcons.get(direction));
          data.setClock(clockString);
          sendUpdate(p);
        }
      }
    }, 101L, 6L);
  }

  public void setupPlayer(Player player) {
    if (!dataMap.containsKey(player)) {
      dataMap.put(player, new TopBarData());
    }
    sendUpdate(player);
  }

  public void updateCompass(Player player, String str) {
    dataMap.get(player).setCompass(str);
    sendUpdate(player);
  }

  public void updateEvent(Player player, String str) {
    dataMap.get(player).setEvent(str);
    sendUpdate(player);
  }

  public void updateLocation(Player player, String str) {
    str = PAPI.setPlaceholders(player, str);
    dataMap.get(player).setLocation(player, str);
    sendUpdate(player);
  }

  public void updateTime(Player player, String str) {
    dataMap.get(player).setClock(str);
    sendUpdate(player);
  }

  public void updateSkills(Player player, String str) {
    dataMap.get(player).setSkills(str);
    sendUpdate(player);
  }

  public void sendUpdate(Player player) {
    TopBarData data = dataMap.get(player);
    plugin.getBossBarManager().updateBar(player, 1, 0, data.getFinalTitle(), 0);
  }

}
