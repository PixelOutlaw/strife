/**
 * The MIT License Copyright (c) 2015 Teal Cube Games
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package land.face.strife.listeners;

import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.champion.Champion;
import land.face.strife.data.champion.ChampionSaveData.HealthDisplayType;
import land.face.strife.data.effects.Riptide;
import land.face.strife.managers.StatUpdateManager;
import land.face.strife.managers.UniqueEntityManager;
import land.face.strife.stats.AbilitySlot;
import land.face.strife.util.ChunkUtil;
import land.face.strife.util.DamageUtil;
import land.face.strife.util.SpecialStatusUtil;
import land.face.strife.util.StatUtil;
import land.face.strife.util.TargetingUtil;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityCombustByBlockEvent;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

public class DataListener implements Listener {

  private final StrifePlugin plugin;
  private final static String UNUSED_MESSAGE_1 =
      "&6&lLevelup! You have &f&l{0} &6&lunused Levelpoints!";
  private final static String UNUSED_MESSAGE_2 =
      "&6&lOpen your inventory or use &e&l/levelup &6&lto spend them!";
  private final static String UNUSED_PATH =
      "&f&lYou have a choice to make! Use &e&l/levelup &f&lto select a path!";

  public DataListener(StrifePlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onPlayerItemDamage(final PlayerItemDamageEvent event) {
    if (event.getDamage() == 0 || event.isCancelled()) {
      Bukkit.getScheduler().runTaskLater(plugin,
          () -> plugin.getAbilityIconManager().updateAllIconProgress(event.getPlayer()), 1L);
    }
  }

  @EventHandler(priority = EventPriority.HIGH)
  public void onEntityCombust(final EntityCombustEvent event) {
    if (event instanceof EntityCombustByEntityEvent) {
      return;
    }
    if (event instanceof EntityCombustByBlockEvent) {
      return;
    }
    if (SpecialStatusUtil.isBurnImmune(event.getEntity())) {
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onSnowmanMelt(final EntityDamageEvent event) {
    if ((event.getCause() == DamageCause.MELTING || event.getCause() == DamageCause.DROWNING) &&
        SpecialStatusUtil.isBurnImmune(event.getEntity())) {
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onTameUnique(final EntityTameEvent event) {
    if (!plugin.getStrifeMobManager().isTrackedEntity(event.getEntity())) {
      return;
    }
    event.setCancelled(true);
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onEntityAttack(final EntityDamageByEntityEvent event) {
    if (event.isCancelled() || !(event.getEntity() instanceof LivingEntity)) {
      return;
    }
    LivingEntity attacker = DamageUtil.getAttacker(event.getDamager());
    if (attacker instanceof Player) {
      plugin.getBossBarManager().pushBar((Player) attacker, plugin.getStrifeMobManager().getStatMob((LivingEntity) event.getEntity()));
    }
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onPlayerJoin(final PlayerJoinEvent event) {
    StrifeMob playerMob = plugin.getStrifeMobManager().getStatMob(event.getPlayer());
    Champion champion = playerMob.getChampion();
    plugin.getAbilityManager().loadPlayerCooldowns(event.getPlayer());
    plugin.getBoostManager().updateGlobalBoostStatus(event.getPlayer());
    plugin.getChampionManager().verifyStatValues(champion);

    if (champion.getUnusedStatPoints() > 0) {
      notifyUnusedPoints(event.getPlayer(), champion.getUnusedStatPoints());
    }
    if (event.getPlayer().getLevel() / 10 > champion.getSaveData().getPathMap().size()) {
      notifyUnusedPaths(event.getPlayer());
    }

    plugin.getCounterManager().clearCounters(event.getPlayer());
    ensureAbilitiesDontInstantCast(event.getPlayer());

    plugin.getChampionManager().update(event.getPlayer());

    event.getPlayer().setHealthScaled(true);
    HealthDisplayType displayType = champion.getSaveData().getHealthDisplayType();
    float maxHealth = Math.max(StatUtil.getHealth(playerMob), 1);
    event.getPlayer().setInvulnerable(true);
    event.getPlayer().setHealthScale(StatUpdateManager.getHealthScale(displayType, maxHealth));
    event.getPlayer().setInvulnerable(false);

    playerMob.updateBarrierScale();

    for (AttributeModifier mod : event.getPlayer().getAttribute(Attribute.GENERIC_ARMOR).getModifiers()) {
      event.getPlayer().getAttribute(Attribute.GENERIC_ARMOR).removeModifier(mod);
    }
    event.getPlayer().getAttribute(Attribute.GENERIC_ARMOR).setBaseValue(-20);

    if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
      Bukkit.getScheduler().runTaskLater(plugin,
          () -> plugin.getAbilityIconManager().setAllAbilityIcons(event.getPlayer()), 2L);
    }
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onPlayerQuit(final PlayerQuitEvent event) {
    doPlayerLeave(event.getPlayer());
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onPlayerKick(final PlayerKickEvent event) {
    doPlayerLeave(event.getPlayer());
  }

  private void doPlayerLeave(Player player) {
    plugin.getAbilityManager().unToggleAll(player);
    plugin.getBoostManager().removeBooster(player.getUniqueId());
    plugin.getAbilityManager().savePlayerCooldowns(player);
    plugin.getAbilityIconManager().removeIconItem(player, AbilitySlot.SLOT_A);
    plugin.getAbilityIconManager().removeIconItem(player, AbilitySlot.SLOT_B);
    plugin.getAbilityIconManager().removeIconItem(player, AbilitySlot.SLOT_C);
    plugin.getCounterManager().clearCounters(player);
    plugin.getBossBarManager().disableBars(player);
    for (AttributeModifier mod : player.getAttribute(Attribute.GENERIC_ARMOR).getModifiers()) {
      player.getAttribute(Attribute.GENERIC_ARMOR).removeModifier(mod);
    }
    player.getAttribute(Attribute.GENERIC_ARMOR).setBaseValue(-20);
  }

  @EventHandler(priority = EventPriority.NORMAL)
  public void onPlayerRespawn(final PlayerRespawnEvent event) {
    ensureAbilitiesDontInstantCast(event.getPlayer());
    StrifeMob mob = plugin.getStrifeMobManager().getStatMob(event.getPlayer());

    plugin.getRageManager().clearRage(event.getPlayer().getUniqueId());
    plugin.getBleedManager().clearBleed(event.getPlayer().getUniqueId());
    plugin.getCorruptionManager().clearCorrupt(event.getPlayer().getUniqueId());
    plugin.getAbilityManager().loadPlayerCooldowns(event.getPlayer());
    plugin.getAbilityIconManager().setAllAbilityIcons(event.getPlayer());
    plugin.getCounterManager().clearCounters(event.getPlayer());
    mob.restoreBarrier(200000);
    StatUtil.changeEnergy(mob, 200000);
    event.getPlayer().setCooldown(Material.DIAMOND_CHESTPLATE, 100);
    Bukkit.getScheduler().runTaskLater(plugin, () ->
        event.getPlayer().setCooldown(Material.DIAMOND_CHESTPLATE, 100), 2L);
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onInteract(final PlayerInteractEntityEvent event) {
    if (TargetingUtil.isInvalidTarget(event.getRightClicked())) {
      return;
    }
    final Player player = event.getPlayer();
    final LivingEntity entity = (LivingEntity) event.getRightClicked();
    plugin.getStrifeMobManager().getStatMob(entity);
    plugin.getBossBarManager().pushBar(player, plugin.getStrifeMobManager().getStatMob(entity));
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void chunkLoadMonitor(ChunkLoadEvent e) {
    ChunkUtil.stampChunk(e.getChunk());
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void chunkUnloadMonitor(ChunkUnloadEvent e) {
    ChunkUtil.unstampChunk(e.getChunk());
  }

  @EventHandler(priority = EventPriority.NORMAL)
  public void onChunkUnload(ChunkUnloadEvent e) {
    for (Entity ent : e.getChunk().getEntities()) {
      if (ent.hasMetadata("NPC")) {
        continue;
      }
      plugin.getStrifeMobManager().doChunkDespawn(ent);
    }
    ChunkUtil.unstampChunk(e.getChunk());
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onEntityChangeBlock(EntityChangeBlockEvent event) {
    if (!(event.getEntity() instanceof FallingBlock)) {
      return;
    }
    if (SpecialStatusUtil.isHandledBlock((FallingBlock) event.getEntity())) {
      event.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onPlayerTeleport(PlayerTeleportEvent event) {
    Riptide.sendCancelPacket(event.getPlayer());
    if (event.getTo().getWorld() != event.getTo().getWorld()) {
      ensureAbilitiesDontInstantCast(event.getPlayer());
      return;
    }
    event.getPlayer().setCooldown(Material.DIAMOND_CHESTPLATE,
        Math.max(5, event.getPlayer().getCooldown(Material.DIAMOND_CHESTPLATE)));
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onPlayerWorldChance(PlayerChangedWorldEvent event) {
    ensureAbilitiesDontInstantCast(event.getPlayer());
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onPlayerPortal(PlayerPortalEvent event) {
    ensureAbilitiesDontInstantCast(event.getPlayer());
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onPostTeleportMinions(PlayerTeleportEvent event) {
    if (event.isCancelled() || event.getPlayer().hasMetadata("NPC")) {
      return;
    }
    StrifeMob mob = plugin.getStrifeMobManager().getStatMob(event.getPlayer());
    if (mob.getMinions().isEmpty()) {
      return;
    }
    Chunk chunk = event.getTo().getChunk();
    if (!chunk.isLoaded()) {
      chunk.load();
    }
    for (StrifeMob minion : mob.getMinions()) {
      minion.getEntity().teleport(event.getTo());
    }
  }

  @EventHandler(priority = EventPriority.NORMAL)
  public void onInvyClose(InventoryCloseEvent event) {
    if (!plugin.getLevelupMenu().getName().equals(event.getView().getTitle())) {
      return;
    }
    if (!plugin.getChampionManager().hasPendingChanges((Player) event.getPlayer())) {
      return;
    }
    Bukkit.getScheduler().runTaskLater(plugin, () ->
        plugin.getConfirmationMenu().open((Player) event.getPlayer()), 1L);
  }

  @EventHandler(priority = EventPriority.NORMAL)
  public void onRespawn(PlayerRespawnEvent event) {
    Riptide.sendCancelPacket(event.getPlayer());
    StrifeMob mob = plugin.getStrifeMobManager().getStatMob(event.getPlayer());
    mob.restartTimers();
    for (AttributeModifier mod : event.getPlayer().getAttribute(Attribute.GENERIC_ARMOR).getModifiers()) {
      event.getPlayer().getAttribute(Attribute.GENERIC_ARMOR).removeModifier(mod);
    }
    event.getPlayer().getAttribute(Attribute.GENERIC_ARMOR).setBaseValue(-20);
  }

  @EventHandler(priority = EventPriority.NORMAL)
  public void onSaddleDrop(ItemSpawnEvent event) {
    if (event.getEntity().getItemStack().getType() == Material.SADDLE) {
      if (event.getEntity().getItemStack().isSimilar(UniqueEntityManager.DEV_SADDLE)) {
        event.getEntity().remove();
        event.setCancelled(true);
      }
    }
  }

  private void ensureAbilitiesDontInstantCast(Player player) {
    plugin.getAbilityManager().setGlobalCooldown(player, 30);
    if (player.getInventory().getHeldItemSlot() < 3) {
      player.getInventory().setHeldItemSlot(3);
    }
  }

  private void notifyUnusedPoints(final Player player, final int unused) {
    Bukkit.getScheduler().runTaskLater(plugin, () -> {
      MessageUtils.sendMessage(player, UNUSED_MESSAGE_1.replace("{0}", String.valueOf(unused)));
      MessageUtils.sendMessage(player, UNUSED_MESSAGE_2);
    }, 20L * 5);
  }

  private void notifyUnusedPaths(final Player player) {
    Bukkit.getScheduler().runTaskLater(plugin, () -> {
      MessageUtils.sendMessage(player, UNUSED_PATH);
    }, 20L * 5 + 1);
  }
}
