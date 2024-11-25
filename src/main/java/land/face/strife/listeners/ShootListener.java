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

import com.tealcube.minecraft.bukkit.facecore.utilities.ItemUtils;
import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;
import com.tealcube.minecraft.bukkit.facecore.utilities.PaletteUtil;
import land.face.dinvy.DeluxeInvyPlugin;
import land.face.dinvy.windows.equipment.EquipmentMenu.DeluxeSlot;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.ability.EntityAbilitySet.TriggerAbilityType;
import land.face.strife.data.effects.StrifeParticle;
import land.face.strife.stats.StrifeStat;
import land.face.strife.util.ItemUtil;
import land.face.strife.util.ProjectileUtil;
import land.face.strife.util.StatUtil;
import land.face.strife.util.TargetingUtil;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Spellcaster.Spell;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpellCastEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class ShootListener implements Listener {

  private final StrifePlugin plugin;
  private final StrifeParticle flintlockSmoke;
  private final StrifeParticle flintlockFlare;

  public static String quiverTip;
  public static String pistolTip;

  public ShootListener(StrifePlugin plugin) {
    this.plugin = plugin;
    flintlockSmoke = (StrifeParticle) plugin.getEffectManager().getEffect("FLINTLOCK-SMOKE");
    flintlockFlare = (StrifeParticle) plugin.getEffectManager().getEffect("FLINTLOCK-FLARE");

    quiverTip = PaletteUtil.color(plugin.getSettings().getString("language.shooting.quiver-tip", "need quiver bro"));
    pistolTip = PaletteUtil.color(plugin.getSettings().getString("language.shooting.pistol-tip", "need quiver bro"));
  }

  @EventHandler(priority = EventPriority.HIGH)
  public void onPlayerArrowShoot(ProjectileLaunchEvent event) {
    if (!(event.getEntity().getShooter() instanceof Player player)
        || !(event.getEntity() instanceof Arrow)) {
      return;
    }
    if (ProjectileUtil.HANDLED_PROJECTILE.contains(player.getUniqueId())) {
      return;
    }

    plugin.getPlayerMountManager().despawn(player);

    StrifeMob mob = plugin.getStrifeMobManager().getStatMob(player);

    event.setCancelled(true);

    float attackMultiplier = plugin.getAttackSpeedManager().getAttackMultiplier(mob, 1);
    attackMultiplier = (float) Math.pow(attackMultiplier, 1.5f);

    if (attackMultiplier < 0.1) {
      event.setCancelled(true);
      return;
    }

    ItemStack mainHand = player.getEquipment().getItemInMainHand();
    ItemStack offHand = DeluxeInvyPlugin.getInstance().getPlayerManager()
        .getPlayerData(player).getEquipmentItem(DeluxeSlot.OFF_HAND);

    EquipmentSlot slot = mainHand.getType() == Material.BOW ||
        mainHand.getType() == Material.CROSSBOW ? EquipmentSlot.HAND : EquipmentSlot.OFF_HAND;

    if (ItemUtil.isFlintlock(slot == EquipmentSlot.HAND ? mainHand : offHand)) {
      if (!ItemUtil.isBullets(slot == EquipmentSlot.HAND ? offHand : mainHand)) {
        MessageUtils.sendMessage(player, pistolTip);
        event.setCancelled(true);
        return;
      }
      ProjectileUtil.shootBullet(mob, attackMultiplier);
      flintlockSmoke.apply(mob, mob);
      flintlockFlare.apply(mob, mob);
      return;
    }

    if (!ItemUtil.isQuiver(slot == EquipmentSlot.HAND ? offHand : mainHand)) {
      MessageUtils.sendMessage(player, quiverTip);
      event.setCancelled(true);
      return;
    }
    ProjectileUtil.shootArrow(mob, attackMultiplier);
  }

  @EventHandler(priority = EventPriority.HIGH)
  public void onPlayerNoRangedOffhand(PlayerInteractEvent event) {
    if (event.getHand() != EquipmentSlot.HAND) {
      return;
    }
    ItemStack mainHand = event.getPlayer().getEquipment().getItemInMainHand();
    if (mainHand.getType() != Material.BOW) {
      return;
    }
    ItemStack offHand = DeluxeInvyPlugin.getInstance().getPlayerManager()
        .getPlayerData(event.getPlayer()).getEquipmentItem(DeluxeSlot.OFF_HAND);
    if (offHand.getType() == Material.ARROW) {
      return;
    }
    if (ItemUtil.isFlintlock(mainHand)) {
      MessageUtils.sendMessage(event.getPlayer(), pistolTip);
      return;
    }
    MessageUtils.sendMessage(event.getPlayer(), quiverTip);
  }

  @EventHandler
  public void onEntitySpell(ProjectileLaunchEvent event) {
    if (!(event.getEntity().getShooter() instanceof LivingEntity shooter) || shooter instanceof Player) {
      return;
    }
    if (ProjectileUtil.HANDLED_PROJECTILE.contains(shooter.getUniqueId())) {
      return;
    }
    StrifeMob mob = plugin.getStrifeMobManager().getStatMob(shooter);

    if (!ProjectileUtil.isAbilityProjectile(event.getEntity())) {
      if (!mob.canAttack()) {
        event.setCancelled(true);
        return;
      }
    }

    if (shooter instanceof Mob && ((Mob) shooter).getTarget() != null) {
      StrifeMob target = plugin.getStrifeMobManager().getStatMob(((Mob) shooter).getTarget());
      if (TargetingUtil.isFriendly(mob, target)) {
        event.setCancelled(true);
        ((Mob) shooter).setTarget(null);
        return;
      }
    }

    if (plugin.getAbilityManager().abilityCast(mob, TriggerAbilityType.SHOOT)) {
      event.setCancelled(true);
      return;
    }

    ItemStack weapon = shooter.getEquipment().getItemInMainHand();
    int bowData = ItemUtils.getModelData(weapon);
    if (weapon.getType() == Material.BOW && bowData > 0 && bowData < 9000) {
      ((LivingEntity) event.getEntity().getShooter()).swingMainHand();
      ProjectileUtil.shootWand(mob, 1);
      event.setCancelled(true);
      return;
    } else if (ItemUtil.isFlintlock(weapon)) {
      ProjectileUtil.shootBullet(mob, 1f);
      flintlockSmoke.apply(mob, mob);
      flintlockFlare.apply(mob, mob);
      event.setCancelled(true);
      return;
    } else if (event.getEntity() instanceof Arrow) {
      ProjectileUtil.shootArrow(mob, 1f);
      event.setCancelled(true);
      return;
    }

    ProjectileUtil.setShotId(event.getEntity());
    ProjectileUtil.bumpShotId();
  }

  @EventHandler
  public void onEntitySpell(EntitySpellCastEvent event) {
    Mob entity = event.getEntity();
    if (event.getSpell() == Spell.DISAPPEAR || event.getSpell() == Spell.NONE || entity instanceof Player) {
      return;
    }

    StrifeMob mob = plugin.getStrifeMobManager().getStatMob(entity);

    if (entity.getTarget() != null) {
      StrifeMob target = plugin.getStrifeMobManager().getStatMob(entity.getTarget());
      if (TargetingUtil.isFriendly(mob, target)) {
        event.setCancelled(true);
        entity.setTarget(null);
        return;
      }
    }

    if (plugin.getAbilityManager().abilityCast(mob, TriggerAbilityType.SHOOT)) {
      event.setCancelled(true);
      return;
    }

    ItemStack weapon = entity.getEquipment().getItemInMainHand();
    int bowData = ItemUtils.getModelData(weapon);
    if (weapon.getType() == Material.BOW && bowData > 0 && bowData < 9000) {
      entity.swingMainHand();
      ProjectileUtil.shootWand(mob, 1);
      event.setCancelled(true);
    } else if (ItemUtil.isFlintlock(weapon)) {
      ProjectileUtil.shootBullet(mob, 1f);
      flintlockSmoke.apply(mob, mob);
      flintlockFlare.apply(mob, mob);
      event.setCancelled(true);
    } else if (event.getEntity() instanceof Arrow) {
      ProjectileUtil.shootArrow(mob, 1f);
      event.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.HIGH)
  public void onPlayerTridentThrow(ProjectileLaunchEvent event) {
    if (!(event.getEntity().getShooter() instanceof Player player)) {
      return;
    }
    if (!(event.getEntity() instanceof Trident)) {
      return;
    }

    event.setCancelled(true);

    player.resetCooldown();

    StrifeMob pStats = plugin.getStrifeMobManager().getStatMob(player);

    float attackMultiplier = plugin.getAttackSpeedManager().getAttackMultiplier(pStats, 1.2f);
    attackMultiplier = (float) Math.pow(attackMultiplier, 1.2);

    int throwCooldownTicks = (int) (40 * StatUtil.getAttackTime(pStats));

    ((Player) event.getEntity().getShooter()).setCooldown(Material.TRIDENT, throwCooldownTicks);

    if (attackMultiplier <= 0.05) {
      event.setCancelled(true);
      return;
    }

    double speedMult = 1 + (pStats.getStat(StrifeStat.PROJECTILE_SPEED) / 100);

    ProjectileUtil.createTrident((Player) event.getEntity().getShooter(),
        (Trident) event.getEntity(), attackMultiplier, speedMult);

    player.getWorld().playSound(player.getLocation(), Sound.ITEM_TRIDENT_THROW, 1f, 1f);
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onDespawnProjectileHit(final ProjectileHitEvent event) {
    if (event.isCancelled() || event.getHitBlock() == null || !event.getEntity().isValid()) {
      return;
    }
    if (!ProjectileUtil.isDespawnOnContact(event.getEntity())) {
      return;
    }
    event.getEntity().remove();
    ProjectileUtil.removeDespawnOnContact(event.getEntity());
  }
}