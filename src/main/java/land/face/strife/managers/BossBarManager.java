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
package land.face.strife.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import land.face.strife.StrifePlugin;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

public class BossBarManager {

  private final StrifePlugin plugin;
  private final Map<Player, BossBar> statusBar1 = new HashMap<>();
  private final Map<Player, BossBar> statusBar2 = new HashMap<>();
  private final Map<Player, BossBar> statusBar3 = new HashMap<>();
  private final Map<Player, BossBar> statusBar4 = new HashMap<>();


  public BossBarManager(StrifePlugin plugin) {
    this.plugin = plugin;
    // Ensure bars do not expire from inactivity
    Bukkit.getScheduler().runTaskTimer(plugin, () -> {
      for (BossBar b : statusBar1.values()) {
        b.setProgress(Math.random());
        b.setVisible(true);
      }
      for (BossBar b : statusBar2.values()) {
        b.setProgress(Math.random());
        b.setVisible(true);
      }
      for (BossBar b : statusBar3.values()) {
        b.setProgress(Math.random());
        b.setVisible(true);
      }
      for (BossBar b : statusBar4.values()) {
        b.setProgress(Math.random());
        b.setVisible(true);
      }
    },20L * 5, 100L);
  }

  public void createBars(Player player) {
    if (statusBar1.containsKey(player)) {
      statusBar1.get(player).removeAll();
    }
    if (statusBar2.containsKey(player)) {
      statusBar2.get(player).removeAll();
    }
    if (statusBar3.containsKey(player)) {
      statusBar3.get(player).removeAll();
    }
    if (statusBar4.containsKey(player)) {
      statusBar4.get(player).removeAll();
    }
    statusBar1.put(player, buildBar(player));
    Bukkit.getScheduler().runTaskLater(StrifePlugin.getInstance(), () ->
        statusBar2.put(player, buildBar(player)), 1L);
    Bukkit.getScheduler().runTaskLater(StrifePlugin.getInstance(), () ->
        statusBar3.put(player, buildBar(player)), 2L);
    Bukkit.getScheduler().runTaskLater(StrifePlugin.getInstance(), () ->
        statusBar4.put(player, buildBar(player)), 3L);
  }

  public void updateBar(Player player, int barNumber, String text) {
    switch (barNumber) {
      case 1 -> {
        if (statusBar1.containsKey(player)) {
          statusBar1.get(player).setTitle(text);
        }
      }
      case 2 -> {
        if (statusBar2.containsKey(player)) {
          statusBar2.get(player).setTitle(text);
        }
      }
      case 3 -> {
        if (statusBar3.containsKey(player)) {
          statusBar3.get(player).setTitle(text);
        }
      }
      case 4 -> {
        if (statusBar4.containsKey(player)) {
          statusBar4.get(player).setTitle(text);
        }
      }
    }
  }

  public void clearBars(Player p) {
    statusBar1.get(p).removeAll();
    statusBar2.get(p).removeAll();
    statusBar3.get(p).removeAll();
    statusBar4.get(p).removeAll();
    statusBar1.remove(p);
    statusBar2.remove(p);
    statusBar3.remove(p);
    statusBar4.remove(p);
  }

  public void clearBars() {
    for (Player p : statusBar1.keySet()) {
      statusBar1.get(p).removeAll();
      statusBar2.get(p).removeAll();
      statusBar3.get(p).removeAll();
      statusBar4.get(p).removeAll();
    }
    for (Player p : statusBar2.keySet()) {
      statusBar2.get(p).removeAll();
    }
    for (Player p : statusBar3.keySet()) {
      statusBar3.get(p).removeAll();
    }
    for (Player p : statusBar4.keySet()) {
      statusBar4.get(p).removeAll();
    }
    statusBar1.clear();
    statusBar2.clear();
    statusBar3.clear();
    statusBar4.clear();
  }

  private static BossBar buildBar(Player player) {
    BossBar bar = StrifePlugin.getInstance().getServer()
        .createBossBar("", BarColor.PURPLE, BarStyle.SOLID);
    bar.addPlayer(player);
    return bar;
  }
}
