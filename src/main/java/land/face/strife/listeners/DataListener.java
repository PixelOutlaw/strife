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
package land.face.strife.listeners;

import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.champion.Champion;
import land.face.strife.stats.AbilitySlot;
import land.face.strife.util.DamageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
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
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

public class DataListener implements Listener {

  private final StrifePlugin plugin;
  private final static String UNUSED_MESSAGE_1 =
      "&6&lLevelup! You have &f&l{0} &6&lunused Levelpoints!";
  private final static String UNUSED_MESSAGE_2 =
      "&6&lOpen your inventory or use &f&l/levelup &6&lto spend them!";

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

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onEntityCombust(final EntityCombustEvent event) {
    if (event instanceof EntityCombustByEntityEvent) {
      return;
    }
    if (event instanceof EntityCombustByBlockEvent) {
      return;
    }
    if (event.getEntity().hasMetadata("NO_BURN")) {
      event.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onEntityAttack(final EntityDamageByEntityEvent event) {
    if (event.isCancelled() || !(event.getEntity() instanceof LivingEntity)) {
      return;
    }
    LivingEntity attacker = DamageUtil.getAttacker(event.getDamager());
    if (attacker instanceof Player) {
      plugin.getCombatStatusManager().addPlayer((Player) attacker);
      plugin.getBossBarManager().pushBar((Player) attacker,
          plugin.getStrifeMobManager().getStatMob((LivingEntity) event.getEntity()));
      return;
    }
    if (event.getEntity() instanceof Player) {
      plugin.getCombatStatusManager().addPlayer((Player) event.getEntity());
    }
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onPlayerJoin(final PlayerJoinEvent event) {
    Champion champion = plugin.getChampionManager().getChampion(event.getPlayer());
    plugin.getAbilityManager().loadPlayerCooldowns(event.getPlayer());
    plugin.getChampionManager().verifyStatValues(champion);
    if (champion.getUnusedStatPoints() > 0) {
      notifyUnusedPoints(event.getPlayer(), champion.getUnusedStatPoints());
    }
    plugin.getBossBarManager().getSkillBar(champion);
    plugin.getCounterManager().clearCounters(event.getPlayer().getUniqueId());
    ensureAbilitiesDontInstantCast(event.getPlayer());
    Bukkit.getScheduler().runTaskLater(plugin,
        () -> plugin.getAbilityIconManager().setAllAbilityIcons(event.getPlayer()), 2L);
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onPlayerJoinUpdateAttributes(final PlayerJoinEvent event) {
    event.getPlayer().setHealthScaled(false);
    plugin.getStatUpdateManager().updateAttributes(event.getPlayer());
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
    plugin.getAbilityManager().savePlayerCooldowns(player);
    plugin.getBossBarManager().removeBar(player.getUniqueId());
    plugin.getAbilityIconManager().removeIconItem(player, AbilitySlot.SLOT_A);
    plugin.getAbilityIconManager().removeIconItem(player, AbilitySlot.SLOT_B);
    plugin.getAbilityIconManager().removeIconItem(player, AbilitySlot.SLOT_C);
    plugin.getCounterManager().clearCounters(player.getUniqueId());
  }

  @EventHandler(priority = EventPriority.NORMAL)
  public void onPlayerRespawn(final PlayerRespawnEvent event) {
    ensureAbilitiesDontInstantCast(event.getPlayer());
    plugin.getRageManager().clearRage(event.getPlayer().getUniqueId());
    plugin.getBleedManager().clearBleed(event.getPlayer().getUniqueId());
    plugin.getCorruptionManager().clearCorrupt(event.getPlayer().getUniqueId());
    plugin.getAbilityManager().loadPlayerCooldowns(event.getPlayer());
    plugin.getBossBarManager().removeBar(event.getPlayer().getUniqueId());
    plugin.getBarrierManager().createBarrierEntry(
        plugin.getStrifeMobManager().getStatMob(event.getPlayer()));
    plugin.getAbilityIconManager().setAllAbilityIcons(event.getPlayer());
    plugin.getCounterManager().clearCounters(event.getPlayer().getUniqueId());
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onInteract(final PlayerInteractEntityEvent event) {
    if (!(event.getRightClicked() instanceof LivingEntity) || event
        .getRightClicked() instanceof ArmorStand) {
      return;
    }
    if (!event.getRightClicked().isValid() || event.getRightClicked().hasMetadata("NPC")) {
      return;
    }
    final Player player = event.getPlayer();
    final LivingEntity entity = (LivingEntity) event.getRightClicked();
    plugin.getStrifeMobManager().getStatMob(entity);
    plugin.getBossBarManager()
        .pushBar(player, plugin.getStrifeMobManager().getStatMob(entity));
  }

  @EventHandler(priority = EventPriority.NORMAL)
  public void onChunkUnload(ChunkUnloadEvent e) {
    for (Entity ent : e.getChunk().getEntities()) {
      if (!(ent instanceof LivingEntity) || ent.hasMetadata("NPC")) {
        continue;
      }
      plugin.getStrifeMobManager().doChunkDespawn((LivingEntity) ent);
    }
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onEntityChangeBlock(EntityChangeBlockEvent event) {
    if (!(event.getEntity() instanceof FallingBlock)) {
      return;
    }
    if (event.getEntity().hasMetadata("EFFECT_PROJECTILE")) {
      event.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onPlayerTeleport(PlayerTeleportEvent event) {
    event.getPlayer().setCooldown(Material.DIAMOND_CHESTPLATE,
        Math.max(3, event.getPlayer().getCooldown(Material.DIAMOND_CHESTPLATE)));
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

  private void ensureAbilitiesDontInstantCast(Player player) {
    plugin.getAbilityManager().setGlobalCooldown(player, 20);
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
}
