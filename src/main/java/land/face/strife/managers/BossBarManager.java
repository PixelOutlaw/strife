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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import land.face.strife.StrifePlugin;
import land.face.strife.data.BarState;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

public class BossBarManager {

  private final StrifePlugin plugin;
  private final Map<Player, BarState> statusBar1 = new HashMap<>();
  private final Map<Player, BarState> statusBar2 = new HashMap<>();
  private final Map<Player, BarState> statusBar3 = new HashMap<>();
  private final Map<Player, BarState> statusBar4 = new HashMap<>();
  private final Map<Player, BarState> statusBar5 = new HashMap<>();

  public BossBarManager(StrifePlugin plugin) {
    this.plugin = plugin;
    // Ensure bars do not expire from inactivity
    Bukkit.getScheduler().runTaskTimer(plugin, () -> {
      randomizeBars(statusBar1);
      randomizeBars(statusBar2);
      randomizeBars(statusBar3);
      randomizeBars(statusBar4);
      randomizeBars(statusBar5);
    },20L * 5, 100L);
    Bukkit.getScheduler().runTaskTimer(plugin, this::tickBars,20L * 10, 4L);
  }

  public void createBars(Player player) {
    if (statusBar1.containsKey(player)) {
      statusBar1.get(player).getBar().removeAll();
    }
    if (statusBar2.containsKey(player)) {
      statusBar2.get(player).getBar().removeAll();
    }
    if (statusBar3.containsKey(player)) {
      statusBar3.get(player).getBar().removeAll();
    }
    if (statusBar4.containsKey(player)) {
      statusBar4.get(player).getBar().removeAll();
    }
    if (statusBar5.containsKey(player)) {
      statusBar5.get(player).getBar().removeAll();
    }
    statusBar1.put(player, buildBar(player));
    Bukkit.getScheduler().runTaskLater(StrifePlugin.getInstance(), () ->
        statusBar2.put(player, buildBar(player)), 1L);
    Bukkit.getScheduler().runTaskLater(StrifePlugin.getInstance(), () ->
        statusBar3.put(player, buildBar(player)), 2L);
    Bukkit.getScheduler().runTaskLater(StrifePlugin.getInstance(), () ->
        statusBar4.put(player, buildBar(player)), 3L);
    Bukkit.getScheduler().runTaskLater(StrifePlugin.getInstance(), () ->
        statusBar5.put(player, buildBar(player)), 4L);
  }

  public void tickBars() {
    iterateTicks(statusBar1.values());
    iterateTicks(statusBar2.values());
    iterateTicks(statusBar3.values());
    iterateTicks(statusBar4.values());
    iterateTicks(statusBar5.values());
  }
  private void iterateTicks(Collection<BarState> stateSet) {
    for (BarState state : stateSet) {
      if (state.getTicks() == 0) {
        continue;
      }
      if (state.getTicks() == 1) {
        state.getBar().setTitle("");
      }
      state.setTicks(state.getTicks() - 1);
    }
  }

  private void randomizeBars(Map<Player, BarState> map) {
    for (BarState b : map.values()) {
      b.getBar().setProgress(Math.random());
      b.getBar().setVisible(true);
    }
  }

  private void updateBarTitleIfPossible(BarState state, int priority, int ticks, String text) {
    if (state != null) {
      if (priority <= state.getPriority() || state.getTicks() < 1) {
        state.setPriority(priority);
        state.setTicks(ticks / 4);
        state.getBar().setTitle(text);
      }
    }
  }

  public void updateBar(Player player, int barNumber, int priority, String text, int ticks) {
    switch (barNumber) {
      case 1 -> updateBarTitleIfPossible(statusBar1.get(player), priority, ticks, text);
      case 2 -> updateBarTitleIfPossible(statusBar2.get(player), priority, ticks, text);
      case 3 -> updateBarTitleIfPossible(statusBar3.get(player), priority, ticks, text);
      case 4 -> updateBarTitleIfPossible(statusBar4.get(player), priority, ticks, text);
      case 5 -> updateBarTitleIfPossible(statusBar5.get(player), priority, ticks, text);
    }
  }

  public void clearBars(Player p) {
    statusBar1.get(p).getBar().removeAll();
    statusBar2.get(p).getBar().removeAll();
    statusBar3.get(p).getBar().removeAll();
    statusBar4.get(p).getBar().removeAll();
    statusBar5.get(p).getBar().removeAll();
    statusBar1.remove(p);
    statusBar2.remove(p);
    statusBar3.remove(p);
    statusBar4.remove(p);
    statusBar5.remove(p);
  }

  public void clearBars() {
    for (Player p : statusBar1.keySet()) {
      statusBar1.get(p).getBar().removeAll();
      statusBar2.get(p).getBar().removeAll();
      statusBar3.get(p).getBar().removeAll();
      statusBar4.get(p).getBar().removeAll();
      statusBar5.get(p).getBar().removeAll();
    }
    for (Player p : statusBar2.keySet()) {
      statusBar2.get(p).getBar().removeAll();
    }
    for (Player p : statusBar3.keySet()) {
      statusBar3.get(p).getBar().removeAll();
    }
    for (Player p : statusBar4.keySet()) {
      statusBar4.get(p).getBar().removeAll();
    }
    for (Player p : statusBar5.keySet()) {
      statusBar5.get(p).getBar().removeAll();
    }
    statusBar1.clear();
    statusBar2.clear();
    statusBar3.clear();
    statusBar4.clear();
    statusBar5.clear();
  }

  private static BarState buildBar(Player player) {
    BossBar bar = StrifePlugin.getInstance().getServer()
        .createBossBar("", BarColor.PURPLE, BarStyle.SOLID);
    BarState state = new BarState();
    state.setBar(bar);
    bar.addPlayer(player);
    return state;
  }
}
