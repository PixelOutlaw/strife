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

import com.destroystokyo.paper.event.entity.EnderDragonFireballHitEvent;
import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;
import com.tealcube.minecraft.bukkit.facecore.utilities.PaletteUtil;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import land.face.dinvy.DeluxeInvyPlugin;
import land.face.dinvy.windows.equipment.EquipmentMenu.DeluxeSlot;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.TargetResponse;
import land.face.strife.data.ability.EntityAbilitySet.TriggerAbilityType;
import land.face.strife.data.effects.AreaEffect;
import land.face.strife.data.effects.AreaEffect.AreaType;
import land.face.strife.data.effects.AreaEffect.LineOfSight;
import land.face.strife.data.effects.AreaEffect.TargetingPriority;
import land.face.strife.data.effects.Damage;
import land.face.strife.data.effects.Effect;
import land.face.strife.data.effects.LocationEffect;
import land.face.strife.data.effects.StrifeParticle;
import land.face.strife.data.effects.StrifeParticle.ParticleStyle;
import land.face.strife.stats.StrifeStat;
import land.face.strife.util.DamageUtil.AttackType;
import land.face.strife.util.DamageUtil.OriginLocation;
import land.face.strife.util.ItemUtil;
import land.face.strife.util.LogUtil;
import land.face.strife.util.ProjectileUtil;
import land.face.strife.util.StatUtil;
import land.face.strife.util.TargetingUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
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
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class ShootListener implements Listener {

  private final StrifePlugin plugin;
  private final StrifeParticle flintlockSmoke;
  private final StrifeParticle flintlockFlare;
  private final AreaEffect flintlockHitscan;
  private final Damage flintlockDamage;

  private final String quiverTip;
  private final String pistolTip;

  public ShootListener(StrifePlugin plugin) {
    this.plugin = plugin;
    flintlockDamage = buildStandardDamage();
    flintlockSmoke = buildFlintlockSmoke();
    flintlockFlare = buildFlintlockFlare();
    flintlockHitscan = buildFlintlockHitscan();

    quiverTip = PaletteUtil.color(plugin.getSettings().getString("language.shooting.quiver-tip", "need quiver bro"));
    pistolTip = PaletteUtil.color(plugin.getSettings().getString("language.shooting.pistol-tip", "need quiver bro"));
  }

  @EventHandler(priority = EventPriority.HIGH)
  public void onPlayerArrowShoot(ProjectileLaunchEvent event) {
    if (!(event.getEntity().getShooter() instanceof Player player)
        || !(event.getEntity() instanceof Arrow)) {
      return;
    }

    plugin.getPlayerMountManager().despawn(player.getUniqueId());

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

    if (ItemUtil.isPistol(slot == EquipmentSlot.HAND ? mainHand : offHand)) {
      if (!ItemUtil.isBullets(slot == EquipmentSlot.HAND ? offHand : mainHand)) {
        MessageUtils.sendMessage(player, pistolTip);
        event.setCancelled(true);
        return;
      }
      doPistolShot(mob, attackMultiplier);
      return;
    }

    if (!ItemUtil.isQuiver(slot == EquipmentSlot.HAND ? offHand : mainHand)) {
      MessageUtils.sendMessage(player, quiverTip);
      event.setCancelled(true);
      return;
    }
    ProjectileUtil.shootArrow(mob, attackMultiplier);
    player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ARROW_SHOOT, 1f, 1f);
  }

  @EventHandler
  public void onEntitySpell(ProjectileLaunchEvent event) {
    if (event.getEntity().getShooter() instanceof LivingEntity shooter) {
      if (shooter instanceof Player) {
        return;
      }

      StrifeMob mob = plugin.getStrifeMobManager().getStatMob(shooter);

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
      if (weapon.getType() == Material.BOW && ItemUtil.getCustomData(weapon) == 4000) {
        ((LivingEntity) event.getEntity().getShooter()).swingMainHand();
        ProjectileUtil.shootWand(mob, 1);
        event.setCancelled(true);
        return;
      } else if (ItemUtil.isPistol(weapon)) {
        doPistolShot(mob, 1f);
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
  }

  @EventHandler
  public void onEntitySpell(EntitySpellCastEvent event) {
    Mob entity = event.getEntity();
    if (event.getSpell() == Spell.DISAPPEAR ||
        event.getSpell() == Spell.NONE || entity instanceof Player) {
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
    if (weapon.getType() == Material.BOW && ItemUtil.getCustomData(weapon) == 4000) {
      entity.swingMainHand();
      ProjectileUtil.shootWand(mob, 1);
      event.setCancelled(true);
    } else if (ItemUtil.isPistol(weapon)) {
      doPistolShot(mob, 1f);
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

  @EventHandler
  public void onDragonFireballHit(final EnderDragonFireballHitEvent event) {
    event.getAreaEffectCloud().remove();
    event.setCancelled(true);

    if (!(event.getEntity().getShooter() instanceof LivingEntity)) {
      return;
    }

    List<Effect> hitEffects = ProjectileUtil.getHitEffects(event.getEntity());
    if (hitEffects.isEmpty()) {
      return;
    }

    StrifeMob caster = plugin.getStrifeMobManager()
        .getStatMob((LivingEntity) event.getEntity().getShooter());
    Location loc = event.getEntity().getLocation().clone()
        .add(event.getEntity().getLocation().getDirection().multiply(-0.25));

    for (Effect effect : hitEffects) {
      if (effect instanceof LocationEffect) {
        ((LocationEffect) effect).applyAtLocation(caster, loc);
      }
    }
  }

  @EventHandler(priority = EventPriority.HIGH)
  public void onEffectProjectileHit(final ProjectileHitEvent event) {
    if (ProjectileUtil.getHitEffects(event.getEntity()) == null) {
      return;
    }
    TargetResponse response;
    if (!ProjectileUtil.isContactTrigger(event.getEntity())) {
      if ((!(event.getHitEntity() instanceof LivingEntity)
          || event.getHitEntity().isInvulnerable()
          || event.getHitEntity().hasMetadata("NPC"))) {
        return;
      }
      response = new TargetResponse(Collections.singleton((LivingEntity) event.getHitEntity()));
    } else {
      Location effectLocation = null;
      if (event.getHitBlock() != null) {
        effectLocation = event.getHitBlock().getLocation().clone().add(0.5, 0.5, 0.5);
        if (event.getHitBlockFace() != null) {
          effectLocation.add(event.getHitBlockFace().getDirection());
        } else {
          effectLocation.add(
              event.getEntity().getLocation().getDirection().clone().multiply(-1.1));
        }
      } else if (event.getHitEntity() != null) {
        effectLocation = event.getEntity().getLocation().clone();
      }
      response = new TargetResponse(effectLocation);
    }

    StrifeMob caster = plugin.getStrifeMobManager()
        .getStatMob((LivingEntity) Objects.requireNonNull(event.getEntity().getShooter()));

    List<Effect> hitEffects = ProjectileUtil.getHitEffects(event.getEntity());
    if (hitEffects.isEmpty()) {
      LogUtil.printWarning(
          "A handled effectProjectile was missing effect meta... something's wrong");
      return;
    }

    plugin.getEffectManager().processEffectList(caster, response, hitEffects,
        ProjectileUtil.getShotId(event.getEntity()));
    event.getEntity().remove();
  }

  private void doPistolShot(StrifeMob mob, float attackMultiplier) {
    if (mob.getEntity() instanceof Player) {
      if (((Player) mob.getEntity()).getCooldown(Material.BOW) > 0) {
        return;
      }
      ((Player) mob.getEntity()).setCooldown(Material.BOW,
          (int) (StatUtil.getAttackTime(mob) * 20));
    }
    if (mob.getStat(StrifeStat.MULTISHOT) > 0.05) {
      double randomMultishot = 0.15 + Math.random() * 0.85;
      int projectiles = ProjectileUtil.getTotalProjectiles(1,
          mob.getStat(StrifeStat.MULTISHOT) * randomMultishot);
      flintlockHitscan.setMaxTargets(projectiles);
      flintlockHitscan.setRadius(0.4f * projectiles);
    } else {
      flintlockHitscan.setMaxTargets(1);
      flintlockHitscan.setRadius(0.4f);
    }
    flintlockDamage.setAttackMultiplier(attackMultiplier);
    flintlockDamage.setApplyOnHitEffects(attackMultiplier > 0.5);
    flintlockDamage.setDamageReductionRatio(Math.min(attackMultiplier, 1.0f));

    Set<LivingEntity> entities = new HashSet<>();
    entities.add(mob.getEntity());
    TargetResponse response = new TargetResponse(entities);

    plugin.getEffectManager().execute(flintlockHitscan, mob, response);
    flintlockSmoke.apply(mob, mob);
    flintlockFlare.apply(mob, mob);
    mob.getEntity().getWorld().playSound(mob.getEntity().getLocation(),
        Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1f, 0.6f);
  }

  private StrifeParticle buildFlintlockSmoke() {
    StrifeParticle particle = new StrifeParticle();
    particle.setFriendly(true);
    particle.setParticle(Particle.CAMPFIRE_COSY_SMOKE);
    particle.setOrigin(OriginLocation.BELOW_HEAD);
    particle.setStyle(ParticleStyle.LINE);
    particle.setSize(2);
    particle.setRadius(0);
    particle.setLineIncrement(0.25f);
    particle.setQuantity(plugin.getSettings().getInt("config.flintlock.smoke-quantity", 1));
    particle.setLineOffset(
        (float) plugin.getSettings().getDouble("config.flintlock.smoke-offset", 1));
    particle.setSpeed((float) plugin.getSettings().getDouble("config.flintlock.smoke-speed", 2f));
    particle.setSpread((float) plugin.getSettings().getDouble("config.flintlock.smoke-spread", 2f));
    return particle;
  }

  private Damage buildStandardDamage() {
    Damage damage = new Damage();
    damage.setAttackMultiplier(1.0f);
    damage.setHealMultiplier(1.0f);
    damage.setAttackType(AttackType.PROJECTILE);
    damage.setCanBeBlocked(true);
    damage.setCanBeEvaded(true);
    damage.setCanSneakAttack(true);
    damage.setShowPopoffs(true);
    return damage;
  }

  private StrifeParticle buildFlintlockFlare() {
    StrifeParticle particle = new StrifeParticle();
    particle.setFriendly(true);
    particle.setParticle(Particle.FLAME);
    particle.setOrigin(OriginLocation.BELOW_HEAD);
    particle.setStyle(ParticleStyle.LINE);
    particle.setSize(1);
    particle.setRadius(0);
    particle.setLineIncrement(0.25f);
    particle.setQuantity(plugin.getSettings().getInt("config.flintlock.flare-quantity", 1));
    particle.setLineOffset(
        (float) plugin.getSettings().getDouble("config.flintlock.flare-offset", 1));
    particle.setSpeed((float) plugin.getSettings().getDouble("config.flintlock.flare-speed", 1f));
    particle.setSpread((float) plugin.getSettings().getDouble("config.flintlock.flare-spread", 1f));
    return particle;
  }

  private AreaEffect buildFlintlockHitscan() {
    AreaEffect hitscan = new AreaEffect();
    hitscan.setAreaType(AreaType.CONE);
    hitscan.setPriority(TargetingPriority.CLOSEST);
    hitscan.setScaleTargetsWithMultishot(false);
    hitscan.setRange((float) plugin.getSettings().getDouble("config.flintlock.range", 16f));
    hitscan.setLineOfSight(LineOfSight.CASTER);
    hitscan.setMaxTargets(1);
    hitscan.setCanBeBlocked(false);
    hitscan.setCanBeEvaded(false);
    hitscan.setOrigin(OriginLocation.BELOW_HEAD);

    hitscan.getEffects().add(flintlockDamage);

    return hitscan;
  }

}