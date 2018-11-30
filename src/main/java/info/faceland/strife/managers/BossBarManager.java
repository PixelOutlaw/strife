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
package info.faceland.strife.managers;

import com.tealcube.minecraft.bukkit.TextUtils;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import info.faceland.strife.StrifePlugin;
import info.faceland.strife.attributes.StrifeAttribute;
import info.faceland.strife.data.AttributedEntity;
import info.faceland.strife.data.StrifeBossBar;
import info.faceland.strife.util.StatUtil;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

public class BossBarManager {

  private final StrifePlugin plugin;
  private final Map<UUID, StrifeBossBar> barMap = new ConcurrentHashMap<>();
  private final Map<Player, StrifeBossBar> playerToCurrentTargetMap = new HashMap<>();
  private final List<String> deathMessages;
  private final Random random = new Random();

  public BossBarManager(StrifePlugin plugin) {
    this.plugin = plugin;
    this.deathMessages = TextUtils.color(plugin.getSettings()
        .getStringList("config.language.bar-title-entity-killed"));
  }

  private void createBars(AttributedEntity target) {
    if (barMap.containsKey(target.getEntity().getUniqueId())) {
      return;
    }
    BossBar barrierBar;
    if (target.getAttribute(StrifeAttribute.BARRIER) > 0) {
      barrierBar = makeBarrierBar();
    } else {
      barrierBar = null;
    }
    BossBar healthBar = makeHealthBar();
    StrifeBossBar strifeBossBar = new StrifeBossBar(target, barrierBar, healthBar);
    setFirstBarTitle(strifeBossBar, createBarTitle(target));
    barMap.put(target.getEntity().getUniqueId(), strifeBossBar);
  }

  public void pushBar(Player player, AttributedEntity target) {
    updateBar(target);
    StrifeBossBar strifeBossBar = barMap.get(target.getEntity().getUniqueId());
    if (playerToCurrentTargetMap.containsKey(player)) {
      removePlayerFromBar(playerToCurrentTargetMap.get(player), player.getUniqueId());
    }
    playerToCurrentTargetMap.put(player, strifeBossBar);
    if (strifeBossBar.getBarrierBar() != null) {
      strifeBossBar.getBarrierBar().addPlayer(player);
    }
    strifeBossBar.getHealthBar().addPlayer(player);
    refreshCounter(strifeBossBar, player.getUniqueId());
  }

  public void tickAllBars() {
    for (UUID uuid : barMap.keySet()) {
      updateBar(barMap.get(uuid).getOwner());
      tickDownBar(barMap.get(uuid));
    }
  }

  public void pruneOldBars() {
    for (UUID uuid : barMap.keySet()) {
      pruneBarIfNoOwners(uuid);
    }
  }

  private void pruneBarIfNoOwners(UUID uuid) {
    if ((barMap.get(uuid).getPlayerUuidTickMap().isEmpty())) {
      barMap.remove(uuid);
    }
  }

  private void tickDownBar(StrifeBossBar strifeBossBar) {
    for (UUID barPlayer : strifeBossBar.getPlayerUuidTickMap().keySet()) {
      int ticksRemaining = strifeBossBar.getPlayerUuidTickMap().get(barPlayer);
      if (ticksRemaining < 1) {
        removePlayerFromBar(strifeBossBar, barPlayer);
        pruneBarIfNoOwners(strifeBossBar.getOwner().getEntity().getUniqueId());
        continue;
      }
      strifeBossBar.getPlayerUuidTickMap().replace(barPlayer, ticksRemaining - 1);
    }
  }

  private void updateBar(AttributedEntity barOwner) {
    createBars(barOwner);
    UUID uuid = barOwner.getEntity().getUniqueId();
    StrifeBossBar strifeBossBar = barMap.get(uuid);
    if (strifeBossBar.isDead()) {
      return;
    }
    setFirstBarTitle(strifeBossBar, createBarTitle(barOwner));
    if (barOwner.getAttribute(StrifeAttribute.BARRIER) > 0) {
      double barrier = plugin.getBarrierManager().getBarrierMap().getOrDefault(uuid, 0D);
      double maxBarrier = StatUtil.getBarrier(barOwner);
      strifeBossBar.getBarrierBar().setProgress(Math.min(barrier / maxBarrier, 1D));
    }
    double health = barOwner.getEntity().getHealth();
    double maxHealth = barOwner.getEntity().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
    strifeBossBar.getHealthBar().setProgress(Math.min(health / maxHealth, 1D));
    if (!barOwner.getEntity().isValid() || health <= 0) {
      setFirstBarTitle(strifeBossBar, deathMessages.get(random.nextInt(deathMessages.size())));
      for (UUID playerUuid : strifeBossBar.getPlayerUuidTickMap().keySet()) {
        strifeBossBar.getPlayerUuidTickMap().put(playerUuid, 25);
      }
      strifeBossBar.setDead(true);
    }
  }

  private void removePlayerFromBar(StrifeBossBar strifeBossBar, UUID uuid) {
    if (strifeBossBar == null || strifeBossBar.getPlayerUuidTickMap().isEmpty()) {
      return;
    }
    strifeBossBar.getPlayerUuidTickMap().remove(uuid);
    Player player = Bukkit.getPlayer(uuid);
    if (player == null) {
      return;
    }
    if (strifeBossBar.getBarrierBar() != null) {
      strifeBossBar.getBarrierBar().removePlayer(player);
    }
    strifeBossBar.getHealthBar().removePlayer(player);
  }

  private void refreshCounter(StrifeBossBar strifeBossBar, UUID uuid) {
    if (strifeBossBar.isDead()) {
      return;
    }
    strifeBossBar.getPlayerUuidTickMap().put(uuid, 100);
  }

  private BossBar makeHealthBar() {
    return plugin.getServer().createBossBar("healthbar", BarColor.RED, BarStyle.SOLID,
        new BarFlag[0]);
  }

  private BossBar makeBarrierBar() {
    return plugin.getServer().createBossBar("barrierBar", BarColor.WHITE, BarStyle.SOLID,
        new BarFlag[0]);
  }

  private String createBarTitle(AttributedEntity barOwner) {
    String customName = barOwner.getEntity().getCustomName();
    if (StringUtils.isBlank(customName)) {
      return StringUtils.capitalize(barOwner.getEntity().getName().replace('_', ' '));
    }
    return customName;
  }

  private void setFirstBarTitle(StrifeBossBar bossBar, String title) {
    if (bossBar.getOwner().getAttribute(StrifeAttribute.BARRIER) > 0) {
      if (bossBar.getBarrierBar() == null) {
        bossBar.setBarrierBar(makeBarrierBar());
      }
      bossBar.getBarrierBar().setTitle(title);
      bossBar.getHealthBar().setTitle(null);
      return;
    }
    bossBar.getHealthBar().setTitle(title);
  }
}
