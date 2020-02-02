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

import java.util.Objects;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.ability.EntityAbilitySet.TriggerAbilityType;
import land.face.strife.data.effects.AreaEffect;
import land.face.strife.data.effects.AreaEffect.AreaType;
import land.face.strife.data.effects.AreaEffect.TargetingPriority;
import land.face.strife.data.effects.Effect;
import land.face.strife.data.effects.LocationEffect;
import land.face.strife.data.effects.StandardDamage;
import land.face.strife.data.effects.StrifeParticle;
import land.face.strife.data.effects.StrifeParticle.ParticleStyle;
import land.face.strife.stats.StrifeStat;
import land.face.strife.util.DamageUtil.AttackType;
import land.face.strife.util.DamageUtil.OriginLocation;
import land.face.strife.util.ItemUtil;
import land.face.strife.util.LogUtil;
import land.face.strife.util.ProjectileUtil;
import land.face.strife.util.StatUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;

public class ShootListener implements Listener {

  private final StrifePlugin plugin;
  private StrifeParticle flintlockSmoke;
  private StrifeParticle flintlockFlare;
  private AreaEffect flintlockHitscan;
  private StandardDamage flintlockDamage;

  public ShootListener(StrifePlugin plugin) {
    this.plugin = plugin;
    flintlockDamage = buildStandardDamage();
    flintlockSmoke = buildFlintlockSmoke();
    flintlockFlare = buildFlintlockFlare();
    flintlockHitscan = buildFlintlockHitscan();
  }

  @EventHandler(priority = EventPriority.HIGH)
  public void onPlayerArrowShoot(ProjectileLaunchEvent event) {
    if (!(event.getEntity().getShooter() instanceof Player) ||
        !(event.getEntity() instanceof Arrow)) {
      return;
    }

    StrifeMob mob = plugin.getStrifeMobManager()
        .getStatMob((LivingEntity) event.getEntity().getShooter());

    event.setCancelled(true);

    ItemStack weapon = Objects.requireNonNull(((LivingEntity) event.getEntity().getShooter())
        .getEquipment()).getItemInMainHand();
    if (weapon.getType() != Material.BOW && weapon.getType() != Material.CROSSBOW) {
      weapon = Objects.requireNonNull(((LivingEntity) event.getEntity().getShooter())
          .getEquipment()).getItemInOffHand();
    }

    float attackMultiplier = plugin.getAttackSpeedManager().getAttackMultiplier(mob);
    attackMultiplier = (float) Math.pow(attackMultiplier, 1.5f);

    if (attackMultiplier < 0.1) {
      event.setCancelled(true);
      return;
    }

    if (ItemUtil.isPistol(weapon)) {
      doPistolShot(mob, attackMultiplier);
      return;
    }

    ProjectileUtil.shootArrow(mob, attackMultiplier);
    mob.getEntity().getWorld()
        .playSound(mob.getEntity().getLocation(), Sound.ENTITY_ARROW_SHOOT, 1f, 1f);
  }

  @EventHandler
  public void onEntityShoot(ProjectileLaunchEvent event) {
    if (!(event.getEntity().getShooter() instanceof LivingEntity) ||
        (event.getEntity().getShooter() instanceof Player)) {
      return;
    }

    StrifeMob mob = plugin.getStrifeMobManager()
        .getStatMob((LivingEntity) event.getEntity().getShooter());

    if (mob.getAbilitySet() != null
        && mob.getAbilitySet().getAbilities(TriggerAbilityType.SHOOT) != null) {
      plugin.getAbilityManager().abilityCast(mob, TriggerAbilityType.SHOOT);
      event.setCancelled(true);
      return;
    }

    ItemStack weapon = Objects.requireNonNull(((LivingEntity) event.getEntity().getShooter())
        .getEquipment()).getItemInMainHand();
    if (weapon.getType() == Material.BOW && ItemUtil.getCustomData(weapon) == 4000) {
      WandListener.shootWand(mob, 1, event);
      event.setCancelled(true);
      return;
    }

    if (ItemUtil.isPistol(weapon)) {
      doPistolShot(mob, 1);
      event.setCancelled(true);
      return;
    }

    if (event.getEntity() instanceof Arrow) {
      ProjectileUtil.shootArrow(mob, 1.0f);
      event.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.HIGH)
  public void onPlayerTridentThrow(ProjectileLaunchEvent event) {
    if (!(event.getEntity().getShooter() instanceof Player)) {
      return;
    }
    if (!(event.getEntity() instanceof Trident)) {
      return;
    }

    event.setCancelled(true);

    Player player = (Player) event.getEntity().getShooter();
    StrifeMob pStats = plugin.getStrifeMobManager().getStatMob(player);
    float attackMultiplier = plugin.getAttackSpeedManager().getAttackMultiplier(pStats);
    attackMultiplier = (float) Math.pow(attackMultiplier, 1.5D);

    ((Player) event.getEntity().getShooter()).setCooldown(Material.TRIDENT, 50);

    if (attackMultiplier <= 0.05) {
      event.setCancelled(true);
      return;
    }

    plugin.getChampionManager().updateEquipmentStats(
        plugin.getChampionManager().getChampion(player));

    double speedMult = 1 + (pStats.getStat(StrifeStat.PROJECTILE_SPEED) / 100);

    ProjectileUtil.createTrident((Player) event.getEntity().getShooter(),
        (Trident) event.getEntity(), attackMultiplier, speedMult);

    player.getWorld().playSound(player.getLocation(), Sound.ITEM_TRIDENT_THROW, 1f, 1f);
    plugin.getSneakManager().tempDisableSneak(player);
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onGroundEffectProjectileHit(final ProjectileHitEvent event) {
    if (event.getHitBlock() == null) {
      return;
    }
    if (!event.getEntity().hasMetadata("GROUND_TRIGGER")) {
      return;
    }
    StrifeMob caster = plugin.getStrifeMobManager()
        .getStatMob((LivingEntity) Objects.requireNonNull(event.getEntity().getShooter()));

    String[] effects = event.getEntity().getMetadata("EFFECT_PROJECTILE").get(0).asString()
        .split("~");
    if (effects.length == 0) {
      LogUtil.printWarning(
          "A handled GroundProjectile was missing effect meta... something's wrong");
      return;
    }
    Location loc = event.getEntity().getLocation().clone()
        .add(event.getEntity().getLocation().getDirection().multiply(-0.25));
    for (String s : effects) {
      Effect effect = StrifePlugin.getInstance().getEffectManager().getEffect(s);
      if (effect instanceof LocationEffect) {
        ((LocationEffect) effect).applyAtLocation(caster, loc);
      }
    }
  }

  private void doPistolShot(StrifeMob mob, float attackMultiplier) {
    if (mob.getEntity() instanceof Player) {
      if (((Player) mob.getEntity()).getCooldown(Material.BOW) > 0) {
        return;
      }
      ((Player) mob.getEntity()).setCooldown(Material.BOW, (int) (StatUtil.getAttackTime(mob) * 20));
    }
    if (mob.getStat(StrifeStat.MULTISHOT) > 0.05) {
      double randomMultishot = Math.pow(Math.random(), 1.5);
      int projectiles = ProjectileUtil
          .getTotalProjectiles(1, mob.getStat(StrifeStat.MULTISHOT) * randomMultishot);
      flintlockHitscan.setMaxTargets(projectiles);
      flintlockHitscan.setMaxConeRadius(0.35f * (projectiles - 1));
    } else {
      flintlockHitscan.setMaxTargets(1);
      flintlockHitscan.setMaxConeRadius(0f);
    }
    flintlockDamage.setAttackMultiplier(attackMultiplier);
    plugin.getEffectManager().execute(flintlockHitscan, mob, mob.getEntity());
    flintlockSmoke.apply(null, mob);
    flintlockFlare.apply(null, mob);
    mob.getEntity().getWorld().playSound(mob.getEntity().getLocation(),
        Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1f, 0.6f);
  }

  private StrifeParticle buildFlintlockSmoke() {
    StrifeParticle particle = new StrifeParticle();
    particle.setFriendly(true);
    particle.setParticle(Particle.CAMPFIRE_COSY_SMOKE);
    particle.setParticleOriginLocation(OriginLocation.BELOW_HEAD);
    particle.setStyle(ParticleStyle.LINE);
    particle.setSize(2);
    particle.setRadius(0);
    particle.setLineIncrement(0.25);
    particle.setQuantity(plugin.getSettings().getInt("config.flintlock.smoke-quantity", 1));
    particle.setLineOffset(plugin.getSettings().getDouble("config.flintlock.smoke-offset", 1));
    particle.setSpeed((float) plugin.getSettings().getDouble("config.flintlock.smoke-speed", 2f));
    particle.setSpread((float) plugin.getSettings().getDouble("config.flintlock.smoke-spread", 2f));
    return particle;
  }

  private StandardDamage buildStandardDamage() {
    StandardDamage standardDamage = new StandardDamage();
    standardDamage.setAttackMultiplier(1.0f);
    standardDamage.setHealMultiplier(1.0f);
    standardDamage.setAttackType(AttackType.RANGED);
    standardDamage.setCanBeBlocked(true);
    standardDamage.setCanBeEvaded(true);
    return standardDamage;
  }

  private StrifeParticle buildFlintlockFlare() {
    StrifeParticle particle = new StrifeParticle();
    particle.setFriendly(true);
    particle.setParticle(Particle.FLAME);
    particle.setParticleOriginLocation(OriginLocation.BELOW_HEAD);
    particle.setStyle(ParticleStyle.LINE);
    particle.setSize(1);
    particle.setRadius(0);
    particle.setLineIncrement(0.25);
    particle.setQuantity(plugin.getSettings().getInt("config.flintlock.flare-quantity", 1));
    particle.setLineOffset(plugin.getSettings().getDouble("config.flintlock.flare-offset", 1));
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
    hitscan.setLineOfSight(true);
    hitscan.setMaxTargets(1);
    hitscan.setCanBeBlocked(false);
    hitscan.setCanBeEvaded(false);

    hitscan.getEffects().add(flintlockDamage);

    return hitscan;
  }

}