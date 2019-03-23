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
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class BossBarManager {

  private final StrifePlugin plugin;
  private final Map<UUID, StrifeBossBar> barMap = new ConcurrentHashMap<>();
  private final List<String> deathMessages;
  private final int duration;
  private final Random random = new Random();

  public BossBarManager(StrifePlugin plugin) {
    this.plugin = plugin;
    this.deathMessages = TextUtils.color(plugin.getSettings()
        .getStringList("config.language.bar-title-entity-killed"));
    this.duration = plugin.getSettings().getInt("config.language.health-bar-duration", 200);
  }

  private void createBars(AttributedEntity target) {
    if (barMap.containsKey(target.getEntity().getUniqueId())) {
      return;
    }
    StrifeBossBar strifeBossBar = new StrifeBossBar(target, makeBarrierBar(target),
        makeHealthBar());
    setFirstBarTitle(strifeBossBar, createBarTitle(target));
    barMap.put(target.getEntity().getUniqueId(), strifeBossBar);
  }

  public void pushBar(Player player, AttributedEntity target) {
    updateBar(target);
    StrifeBossBar strifeBossBar = barMap.get(target.getEntity().getUniqueId());
    for (StrifeBossBar bossBar : barMap.values()) {
      removePlayerFromBar(bossBar, player.getUniqueId());
    }
    addPlayerToBar(strifeBossBar, player);
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

  public void removeBar(UUID uuid) {
    if (!barMap.containsKey(uuid)) {
      return;
    }
    StrifeBossBar strifeBossBar = barMap.get(uuid);
    barMap.remove(uuid);
    strifeBossBar.getHealthBar().removeAll();
    strifeBossBar.setHealthBar(null);
    if (strifeBossBar.getBarrierBar() != null) {
      strifeBossBar.getBarrierBar().removeAll();
      strifeBossBar.setBarrierBar(null);
    }
  }

  public void removeAllBars() {
    for (UUID uuid : barMap.keySet()) {
      removeBar(uuid);
    }
  }

  public void addPlayerToBar(StrifeBossBar strifeBossBar, Player player) {
    if (strifeBossBar.getBarrierBar() != null) {
      strifeBossBar.getBarrierBar().addPlayer(player);
    }
    strifeBossBar.getHealthBar().addPlayer(player);
  }

  public void doBarDeath(LivingEntity livingEntity) {
    StrifeBossBar bossBar = barMap.getOrDefault(livingEntity.getUniqueId(), null);
    if (bossBar != null) {
      doBarDeath(bossBar);
    }
  }

  public void doBarDeath(StrifeBossBar bossBar) {
    bossBar.setDead(true);
    setFirstBarTitle(bossBar, deathMessages.get(random.nextInt(deathMessages.size())));
    if (bossBar.getBarrierBar() != null) {
      bossBar.getBarrierBar().setProgress(0);
    }
    bossBar.getHealthBar().setProgress(0);
    for (UUID playerUuid : bossBar.getPlayerUuidTickMap().keySet()) {
      bossBar.getPlayerUuidTickMap().put(playerUuid, 25);
    }
  }

  private void pruneBarIfNoOwners(UUID uuid) {
    if (barMap.get(uuid).getPlayerUuidTickMap().isEmpty()) {
      removeBar(uuid);
    }
  }

  private void tickDownBar(StrifeBossBar strifeBossBar) {
    for (UUID barPlayer : strifeBossBar.getPlayerUuidTickMap().keySet()) {
      int ticksRemaining = strifeBossBar.getPlayerUuidTickMap().get(barPlayer);
      if (ticksRemaining < 1) {
        removePlayerFromBar(strifeBossBar, barPlayer);
        continue;
      }
      strifeBossBar.getPlayerUuidTickMap().replace(barPlayer, ticksRemaining - 1);
    }
    pruneBarIfNoOwners(strifeBossBar.getOwner().getEntity().getUniqueId());
  }

  private void updateBar(AttributedEntity barOwner) {
    createBars(barOwner);
    UUID uuid = barOwner.getEntity().getUniqueId();
    StrifeBossBar strifeBossBar = barMap.get(uuid);
    if (!strifeBossBar.isDead() && !strifeBossBar.getOwner().getEntity().isValid()) {
      removeBar(barOwner.getEntity().getUniqueId());
      return;
    }
    if (strifeBossBar.isDead()) {
      return;
    }
    if (barOwner.getAttribute(StrifeAttribute.BARRIER) > 0) {
      double barrier = plugin.getBarrierManager().getBarrierMap().getOrDefault(uuid, 0D);
      double maxBarrier = StatUtil.getBarrier(barOwner);
      if (strifeBossBar.getBarrierBar() == null) {
        strifeBossBar.setBarrierBar(makeBarrierBar(barOwner));
        setFirstBarTitle(strifeBossBar, createBarTitle(barOwner));
      }
      strifeBossBar.getBarrierBar().setProgress(Math.min(barrier / maxBarrier, 1D));
    } else if (strifeBossBar.getBarrierBar() != null) {
      strifeBossBar.setBarrierBar(null);
      setFirstBarTitle(strifeBossBar, createBarTitle(barOwner));
    }
    double health = barOwner.getEntity().getHealth();
    double maxHealth = barOwner.getEntity().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
    strifeBossBar.getHealthBar().setProgress(Math.min(health / maxHealth, 1D));
  }

  private void removePlayerFromBar(StrifeBossBar strifeBossBar, Player player) {
    if (strifeBossBar == null || strifeBossBar.getPlayerUuidTickMap().isEmpty()) {
      return;
    }
    strifeBossBar.getPlayerUuidTickMap().remove(player.getUniqueId());
    if (strifeBossBar.getBarrierBar() != null) {
      strifeBossBar.getBarrierBar().removePlayer(player);
    }
    strifeBossBar.getHealthBar().removePlayer(player);
  }

  private void removePlayerFromBar(StrifeBossBar strifeBossBar, UUID uuid) {
    Player player = Bukkit.getPlayer(uuid);
    if (player == null) {
      return;
    }
    removePlayerFromBar(strifeBossBar, player);
  }

  private void refreshCounter(StrifeBossBar strifeBossBar, UUID uuid) {
    if (strifeBossBar.isDead()) {
      return;
    }
    strifeBossBar.getPlayerUuidTickMap().put(uuid, duration);
  }

  private BossBar makeHealthBar() {
    return plugin.getServer().createBossBar("healthbar", BarColor.RED, BarStyle.SOLID,
        new BarFlag[0]);
  }

  private BossBar makeBarrierBar(AttributedEntity attributedEntity) {
    if (attributedEntity.getAttribute(StrifeAttribute.BARRIER) < 1) {
      return null;
    }
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
    if (bossBar.getHealthBar() == null) {
      return;
    }
    if (bossBar.getBarrierBar() != null) {
      bossBar.getBarrierBar().setTitle(title);
      bossBar.getHealthBar().setTitle(null);
      return;
    }
    bossBar.getHealthBar().setTitle(title);
  }
}
