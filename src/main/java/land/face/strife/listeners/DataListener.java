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
package land.face.strife.listeners;

import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;
import land.face.dinvy.events.InventoryLoadComplete;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.effects.Riptide;
import land.face.strife.events.AbilityCastEvent;
import land.face.strife.events.AbilityChangeEvent;
import land.face.strife.events.AbilityCooldownEvent;
import land.face.strife.events.AbilityGainChargeEvent;
import land.face.strife.events.CombatChangeEvent;
import land.face.strife.events.CombatChangeEvent.NewCombatState;
import land.face.strife.managers.UniqueEntityManager;
import land.face.strife.menus.levelup.PathMenu;
import land.face.strife.stats.StrifeStat;
import land.face.strife.util.ItemUtil;
import land.face.strife.util.SpecialStatusUtil;
import land.face.strife.util.StatUtil;
import land.face.strife.util.TargetingUtil;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityCombustByBlockEvent;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent.Cause;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerChangedMainHandEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.MainHand;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public record DataListener(StrifePlugin plugin) implements Listener {

  @EventHandler
  public void onAbilityChange(final AbilityChangeEvent event) {
    Bukkit.getScheduler().runTaskLater(plugin, () -> {
      plugin.getStrifeMobManager().updateEquipmentStats(
          plugin.getStrifeMobManager().getStatMob(event.getChampion().getPlayer()));
      plugin.getChampionManager().update(event.getChampion());
      plugin.getStatUpdateManager().updateAllAttributes(event.getChampion().getPlayer());
      StrifeMob mob = plugin.getStrifeMobManager().getStatMob(event.getChampion().getPlayer());
      StatUtil.getStat(mob, StrifeStat.BARRIER);
      StatUtil.getStat(mob, StrifeStat.MAXIMUM_RAGE);
      StatUtil.getStat(mob, StrifeStat.MAX_EARTH_RUNES);
      plugin.getAbilityIconManager().updateChargesGui(event.getChampion());
    }, 1L);
  }
  @EventHandler
  public void onCast(AbilityCastEvent event) {
    if (event.getCaster().getEntity().getType() == EntityType.PLAYER) {
      plugin.getAbilityIconManager().updateChargesGui((Player) event.getCaster().getEntity());
    }
  }

  @EventHandler
  public void onRecharge(AbilityGainChargeEvent event) {
    if (event.getHolder().getEntity().getType() == EntityType.PLAYER) {
      plugin.getAbilityIconManager().updateChargesGui((Player) event.getHolder().getEntity());
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onCombatChange(CombatChangeEvent event) {
    if (event.getNewState() == NewCombatState.EXIT) {
      event.getTarget().clearMultishot();
      event.getTarget().setBlock(event.getTarget().getMaxBlock());
      if (event.getTarget().getEntity() instanceof Mob) {
        ((Mob) event.getTarget().getEntity()).setTarget(null);
      }
    }
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onAttackEffect(final EntityPotionEffectEvent event) {
    if (event.getCause() == Cause.ATTACK) {
      event.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onAbilityCooldown(final AbilityCooldownEvent event) {
    if (event.getHolder().getChampion() != null) {
      event.getHolder().getChampion().recombineCache();
    }
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onAbilityCast(final AbilityCastEvent event) {
    if (event.getCaster().getChampion() != null) {
      plugin.getPlayerMountManager().despawn((Player) event.getCaster().getEntity());
    }
    if (event.getCaster().getChampion() != null) {
      event.getCaster().getChampion().recombineCache();
    }
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onPlayerItemDamage(final PlayerItemDamageEvent event) {
    if (event.getDamage() == 0 || event.isCancelled()) {
      Bukkit.getScheduler().runTaskLater(plugin,
          () -> plugin.getAbilityIconManager().updateAllIconProgress(event.getPlayer()), 1L);
    }
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onItemPickupEvent(EntityPickupItemEvent event) {
    if (event.getItem().getItemStack().getType() == Material.BLAZE_POWDER
        && event.getEntity() instanceof Player) {
      if (ItemUtil.getCustomData(event.getItem().getItemStack()) == 42069) {
        event.setCancelled(true);
        event.getItem().remove();
        MessageUtils.sendMessage(event.getEntity(),
            "&a&oThe power of &f&l&oFaceguy &a&oflows through you..!");
        ((Player) event.getEntity()).playSound(event.getEntity().getLocation(),
            Sound.BLOCK_BEACON_POWER_SELECT, 1, 1);
        event.getEntity().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 240, 1, false));
        StrifeMob mob = plugin.getStrifeMobManager().getStatMob(event.getEntity());
        mob.setEnergy(mob.getEnergy() + 0.1f * (mob.getMaxEnergy() - mob.getEnergy()));
        event.getEntity().setHealth(event.getEntity().getHealth() +
            0.1f * (event.getEntity().getMaxHealth() - event.getEntity().getHealth()));
      }
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

  @EventHandler
  public void onInvyLoad(final InventoryLoadComplete event) {
    if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
      plugin.getAbilityIconManager().setAllAbilityIcons(event.getPlayer());
    }
    plugin.getStrifeMobManager().updateEquipmentStats(event.getPlayer());
  }

  @EventHandler(priority = EventPriority.NORMAL)
  public void onPlayerRespawn(final PlayerRespawnEvent event) {
    ensureAbilitiesDontInstantCast(event.getPlayer());
    StrifeMob mob = plugin.getStrifeMobManager().getStatMob(event.getPlayer());

    mob.clearBleed();
    mob.setCorruption(0);
    mob.removeFrost(100000);
    plugin.getAbilityManager().loadPlayerCooldowns(event.getPlayer());
    plugin.getAbilityIconManager().setAllAbilityIcons(event.getPlayer());
    plugin.getCounterManager().clearCounters(event.getPlayer());

    StatUtil.getStat(mob, StrifeStat.BARRIER);
    StatUtil.getStat(mob, StrifeStat.HEALTH);
    StatUtil.getStat(mob, StrifeStat.ENERGY);

    mob.restoreBarrier(mob.getMaxBarrier());
    mob.setEnergy(mob.getMaxEnergy());
    mob.getEntity().setHealth(mob.getMaxLife());

    event.getPlayer().setCooldown(Material.DIAMOND_CHESTPLATE, 100);
    event.getPlayer().setCooldown(Material.GOLDEN_CHESTPLATE, 100);
    Bukkit.getScheduler().runTaskLater(plugin, () -> {
      event.getPlayer().setCooldown(Material.DIAMOND_CHESTPLATE, 100);
      event.getPlayer().setCooldown(Material.GOLDEN_CHESTPLATE, 100);
    }, 2L);
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onInteract(final PlayerInteractEntityEvent event) {
    if (TargetingUtil.isInvalidTarget(event.getRightClicked())) {
      return;
    }
    final Player player = event.getPlayer();
    final LivingEntity entity = (LivingEntity) event.getRightClicked();
    plugin.getStrifeMobManager().getStatMob(entity);
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
    plugin.getPlayerMountManager().despawn(event.getPlayer());
    Riptide.sendCancelPacket(event.getPlayer());
    if (event.getTo().getWorld() != event.getTo().getWorld()) {
      ensureAbilitiesDontInstantCast(event.getPlayer());
      return;
    }
    event.getPlayer().setCooldown(Material.GOLDEN_CHESTPLATE,
        Math.max(5, event.getPlayer().getCooldown(Material.GOLDEN_CHESTPLATE)));
    event.getPlayer().setCooldown(Material.DIAMOND_CHESTPLATE,
        Math.max(5, event.getPlayer().getCooldown(Material.DIAMOND_CHESTPLATE)));
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onPlayerWorldChance(PlayerChangedWorldEvent event) {
    plugin.getPlayerMountManager().despawn(event.getPlayer());
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

  @EventHandler
  public void onInvyClose(InventoryCloseEvent event) {
    if (!plugin.getLevelupMenu().getName().equals(event.getView().getTitle())) {
      return;
    }
    if (!plugin.getChampionManager().hasPendingChanges((Player) event.getPlayer())) {
      return;
    }
    plugin.getChampionManager().promptSaveAttributes((Player) event.getPlayer());
  }

  @EventHandler
  public void onPathClose(InventoryCloseEvent event) {
    if (!PathMenu.MENU_NAME.equals(event.getView().getTitle())) {
      return;
    }
    Bukkit.getScheduler().runTaskLater(plugin, () ->
        plugin.getLevelupMenu().open((Player) event.getPlayer()), 1L);
  }

  @EventHandler
  public void onRespawn(PlayerRespawnEvent event) {
    Riptide.sendCancelPacket(event.getPlayer());
    StrifeMob mob = plugin.getStrifeMobManager().getStatMob(event.getPlayer());
    mob.endRageTask();
    mob.setBlock(mob.getMaxBlock());
    mob.restartTimers();
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

  @EventHandler(priority = EventPriority.NORMAL)
  public void onHandChange(PlayerChangedMainHandEvent event) {
    plugin.getGuiManager()
        .updateGodDisplay(event.getPlayer(), event.getMainHand() == MainHand.LEFT);
  }

  private void ensureAbilitiesDontInstantCast(Player player) {
    plugin.getAbilityManager().setGlobalCooldown(player, 30);
    if (player.getInventory().getHeldItemSlot() < 3) {
      player.getInventory().setHeldItemSlot(3);
    }
  }
}
