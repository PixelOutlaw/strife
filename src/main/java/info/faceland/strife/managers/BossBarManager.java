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

import static org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH;

import com.tealcube.minecraft.bukkit.TextUtils;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import info.faceland.strife.StrifePlugin;
import info.faceland.strife.data.SkillBossBar;
import info.faceland.strife.data.StrifeBossBar;
import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.data.champion.Champion;
import info.faceland.strife.data.champion.LifeSkillType;
import info.faceland.strife.util.PlayerDataUtil;
import info.faceland.strife.util.StatUtil;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class BossBarManager {

  private final StrifePlugin plugin;
  private final Map<UUID, StrifeBossBar> barMap = new ConcurrentHashMap<>();
  private final Map<UUID, SkillBossBar> skillBarMap = new HashMap<>();
  private final List<String> deathMessages;
  private final int healthDuration;
  private final int skillDuration;
  private final Random random = new Random();

  public BossBarManager(StrifePlugin plugin) {
    this.plugin = plugin;
    this.deathMessages = TextUtils
        .color(plugin.getSettings().getStringList("language.bar-title-entity-killed"));
    this.healthDuration = plugin.getSettings().getInt("config.mechanics.health-bar-duration", 200);
    this.skillDuration = plugin.getSettings().getInt("config.mechanics.skill-bar-duration", 200);
  }

  private void createBars(StrifeMob target) {
    if (barMap.containsKey(target.getEntity().getUniqueId())) {
      return;
    }
    StrifeBossBar bossBar = new StrifeBossBar(target, makeBarrierBar(target), makeHealthBar());
    updateBarTitle(bossBar, createBarTitle(target));
    barMap.put(target.getEntity().getUniqueId(), bossBar);
  }

  public SkillBossBar getSkillBar(Champion champion) {
    if (skillBarMap.containsKey(champion.getUniqueId())
        && skillBarMap.get(champion.getUniqueId()) != null
        && skillBarMap.get(champion.getUniqueId()).getSkillBar() != null) {
      return skillBarMap.get(champion.getUniqueId());
    }
    SkillBossBar skillBar = new SkillBossBar(champion, makeSkillBar());
    skillBar.getSkillBar().setVisible(false);
    skillBar.getSkillBar().addPlayer(champion.getPlayer());
    skillBarMap.put(champion.getUniqueId(), skillBar);
    return skillBar;
  }

  public void bumpSkillBar(Champion champion, LifeSkillType lifeSkillType) {
    String name = lifeSkillType.getName();
    String barName = name + " Lv" + champion.getSaveData().getSkillLevel(lifeSkillType);
    SkillBossBar skillBar = getSkillBar(champion);
    skillBar.getSkillBar().setVisible(true);
    skillBar.getSkillBar().setTitle(barName);
    skillBar.getSkillBar().setProgress(PlayerDataUtil.getSkillProgress(champion, lifeSkillType));
    skillBar.setDisplayTicks(skillDuration);
  }

  public void pushBar(Player player, StrifeMob target) {
    createBars(target);
    StrifeBossBar strifeBossBar = barMap.get(target.getEntity().getUniqueId());
    for (StrifeBossBar bossBar : barMap.values()) {
      if (bossBar.equals(strifeBossBar)) {
        continue;
      }
      removePlayerFromBar(bossBar, player.getUniqueId());
    }
    addPlayerToBar(strifeBossBar, player);
    updateBar(target);
    refreshCounter(strifeBossBar, player.getUniqueId());
  }

  public void tickHealthBars() {
    for (UUID uuid : barMap.keySet()) {
      updateBar(barMap.get(uuid).getOwner());
      tickDownBar(barMap.get(uuid));
    }
  }

  public void tickSkillBars() {
    for (Player player : Bukkit.getOnlinePlayers()) {
      SkillBossBar skillBossBar = skillBarMap.getOrDefault(player.getUniqueId(),
          getSkillBar(plugin.getChampionManager().getChampion(player)));
      if (!skillBossBar.getSkillBar().isVisible()) {
        continue;
      }
      skillBossBar.setDisplayTicks(skillBossBar.getDisplayTicks() - 1);
      if (skillBossBar.getDisplayTicks() < 1) {
        skillBossBar.getSkillBar().setVisible(false);
      }
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
    if (strifeBossBar.getBarrierBar() != null) {
      strifeBossBar.getBarrierBar().removeAll();
    }

    strifeBossBar.setHealthBar(null);
    strifeBossBar.setBarrierBar(null);
  }

  public void removeAllBars() {
    for (UUID uuid : barMap.keySet()) {
      removeBar(uuid);
    }
  }

  private void addPlayerToBar(StrifeBossBar strifeBossBar, Player player) {
    if (strifeBossBar.getBarrierBar() != null) {
      if (!strifeBossBar.getBarrierBar().getPlayers().contains(player)) {
        strifeBossBar.getBarrierBar().addPlayer(player);
      }
    }
    if (!strifeBossBar.getHealthBar().getPlayers().contains(player)) {
      strifeBossBar.getHealthBar().addPlayer(player);
    }
  }

  public void doBarDeath(LivingEntity livingEntity) {
    StrifeBossBar bossBar = barMap.getOrDefault(livingEntity.getUniqueId(), null);
    if (bossBar != null) {
      doBarDeath(bossBar);
    }
  }

  public void doBarDeath(StrifeBossBar bossBar) {
    bossBar.setDead(true);
    updateBarTitle(bossBar, deathMessages.get(random.nextInt(deathMessages.size())));
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
    if (strifeBossBar == null) {
      return;
    }
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

  private void updateBar(StrifeMob barOwner) {
    UUID uuid = barOwner.getEntity().getUniqueId();
    StrifeBossBar strifeBossBar = barMap.get(uuid);
    if (!strifeBossBar.isDead() && !strifeBossBar.getOwner().getEntity().isValid()) {
      removeBar(barOwner.getEntity().getUniqueId());
      return;
    }
    if (strifeBossBar.isDead()) {
      return;
    }
    updateBarrierProgress(strifeBossBar);
    updateHealthProgress(strifeBossBar);
    updateBarTitle(strifeBossBar, createBarTitle(barOwner));
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
    strifeBossBar.getPlayerUuidTickMap().put(uuid, healthDuration);
  }

  private void updateHealthProgress(StrifeBossBar bar) {
    double health = bar.getOwner().getEntity().getHealth();
    double maxHealth = bar.getOwner().getEntity().getAttribute(GENERIC_MAX_HEALTH).getValue();
    bar.getHealthBar().setProgress(Math.min(health / maxHealth, 1D));
  }

  private void updateBarrierProgress(StrifeBossBar bar) {
    if (StatUtil.getMaximumBarrier(bar.getOwner()) < 1) {
      bar.setBarrierBar(null);
      return;
    }
    double barrier = plugin.getBarrierManager().getBarrierMap()
        .getOrDefault(bar.getOwner().getEntity().getUniqueId(), 0f);
    double maxBarrier = StatUtil.getMaximumBarrier(bar.getOwner());
    bar.getBarrierBar().setProgress(Math.min(barrier / maxBarrier, 1D));
  }

  private BossBar makeSkillBar() {
    return plugin.getServer().createBossBar("skillbar", BarColor.GREEN, BarStyle.SOLID,
        new BarFlag[0]);
  }

  private BossBar makeHealthBar() {
    return plugin.getServer().createBossBar("healthbar", BarColor.RED, BarStyle.SOLID,
        new BarFlag[0]);
  }

  private BossBar makeBarrierBar(StrifeMob entity) {
    if (StatUtil.getMaximumBarrier(entity) < 1) {
      return null;
    }
    return plugin.getServer().createBossBar("barrierBar", BarColor.WHITE, BarStyle.SOLID,
        new BarFlag[0]);
  }

  private String createBarTitle(StrifeMob barOwner) {
    String customName = barOwner.getEntity().getCustomName();
    if (StringUtils.isBlank(customName)) {
      return StringUtils.capitalize(barOwner.getEntity().getName().replace('_', ' '));
    }
    return customName;
  }

  private void updateBarTitle(StrifeBossBar bossBar, String title) {
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
