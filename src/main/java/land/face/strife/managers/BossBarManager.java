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
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.bossbar.BossBar.Color;
import net.kyori.adventure.bossbar.BossBar.Overlay;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class BossBarManager {

  private final Map<Player, BarState> statusBar1 = new HashMap<>();
  private final Map<Player, BarState> statusBar2 = new HashMap<>();
  private final Map<Player, BarState> statusBar3 = new HashMap<>();
  private final Map<Player, BarState> statusBar4 = new HashMap<>();

  public static final LegacyComponentSerializer HOLY_FUCK_FUCK_KYORI = LegacyComponentSerializer.builder()
      .character('§').hexCharacter('#').hexColors().useUnusualXRepeatedCharacterHexFormat().build();

  public BossBarManager(StrifePlugin plugin) {
    Bukkit.getScheduler().runTaskTimer(plugin, this::tickBars,20L * 20, 4L);
  }

  public void createBars(Player player) {
    removeBarViewers(player);
    statusBar1.put(player, buildBar(player));
    Bukkit.getScheduler().runTaskLater(StrifePlugin.getInstance(), () -> statusBar2.put(player, buildBar(player)), 2L);
    Bukkit.getScheduler().runTaskLater(StrifePlugin.getInstance(), () -> statusBar3.put(player, buildBar(player)), 4L);
    Bukkit.getScheduler().runTaskLater(StrifePlugin.getInstance(), () -> statusBar4.put(player, buildBar(player)), 6L);
  }

  public void tickBars() {
    iterateTicks(statusBar1.values());
    iterateTicks(statusBar2.values());
    iterateTicks(statusBar3.values());
    iterateTicks(statusBar4.values());
  }

  private void iterateTicks(Collection<BarState> stateSet) {
    for (BarState state : stateSet) {
      if (state.getTicks() == 0) {
        continue;
      }
      if (state.getTicks() == 1) {
        state.getBar().name(Component.empty());
      }
      state.setTicks(state.getTicks() - 1);
    }
  }

  private void updateBarTitleIfPossible(BarState state, int priority, int ticks, TextComponent text) {
    if (state != null) {
      if (priority <= state.getPriority() || state.getTicks() < 1) {
        state.setPriority(priority);
        state.setTicks(ticks / 4);
        state.getBar().name(text);
      }
    }
  }

  public void updateBar(Player player, int barNumber, int priority, TextComponent text, int ticks) {
    switch (barNumber) {
      case 1 -> updateBarTitleIfPossible(statusBar1.get(player), priority, ticks, text);
      case 2 -> updateBarTitleIfPossible(statusBar2.get(player), priority, ticks, text);
      case 3 -> updateBarTitleIfPossible(statusBar3.get(player), priority, ticks, text);
      case 4 -> updateBarTitleIfPossible(statusBar4.get(player), priority, ticks, text);
    }
  }

  public void clearBars(Player p) {
    removeBarViewers(p);
    statusBar1.remove(p);
    statusBar2.remove(p);
    statusBar3.remove(p);
    statusBar4.remove(p);
  }

  private void removeBarViewers(Player p) {
    Audience audience = Audience.audience(p);
    if (statusBar1.containsKey(p)) {
      audience.hideBossBar(statusBar1.get(p).getBar());
    }
    if (statusBar2.containsKey(p)) {
      audience.hideBossBar(statusBar2.get(p).getBar());
    }
    if (statusBar3.containsKey(p)) {
      audience.hideBossBar(statusBar3.get(p).getBar());
    }
    if (statusBar4.containsKey(p)) {
      audience.hideBossBar(statusBar4.get(p).getBar());
    }
  }

  public void clearBars() {
    for (Player p : statusBar1.keySet()) {
      Audience audience = Audience.audience(p);
      audience.hideBossBar(statusBar1.get(p).getBar());
      audience.hideBossBar(statusBar2.get(p).getBar());
      audience.hideBossBar(statusBar3.get(p).getBar());
      audience.hideBossBar(statusBar4.get(p).getBar());
    }
    for (Player p : statusBar2.keySet()) {
      Audience audience = Audience.audience(p);
      audience.hideBossBar(statusBar2.get(p).getBar());
    }
    for (Player p : statusBar3.keySet()) {
      Audience audience = Audience.audience(p);
      audience.hideBossBar(statusBar3.get(p).getBar());
    }
    for (Player p : statusBar4.keySet()) {
      Audience audience = Audience.audience(p);
      audience.hideBossBar(statusBar4.get(p).getBar());
    }
    statusBar1.clear();
    statusBar2.clear();
    statusBar3.clear();
    statusBar4.clear();
  }

  private static BarState buildBar(Player player) {
    BossBar bar = net.kyori.adventure.bossbar.BossBar.bossBar(Component.empty(), 0, Color.PURPLE, Overlay.PROGRESS);
    BarState state = new BarState();
    state.setBar(bar);
    Audience audience = Audience.audience(player);
    audience.showBossBar(bar);
    return state;
  }

  public static TextComponent covertStringToRetardComponent(String str) {
    return (TextComponent) MiniMessage.miniMessage()
        .deserialize(MiniMessage.miniMessage().serialize(HOLY_FUCK_FUCK_KYORI.deserialize(str)));
  }
}
