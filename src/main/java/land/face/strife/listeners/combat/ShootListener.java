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
package land.face.strife.listeners.combat;

import java.util.Objects;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.ability.EntityAbilitySet.TriggerAbilityType;
import land.face.strife.stats.StrifeStat;
import land.face.strife.util.LogUtil;
import land.face.strife.util.ProjectileUtil;
import org.bukkit.Location;
import org.bukkit.Material;
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

public class ShootListener implements Listener {

  private final StrifePlugin plugin;

  public ShootListener(StrifePlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler(priority = EventPriority.HIGH)
  public void onEntityShoot(ProjectileLaunchEvent event) {
    if (!(event.getEntity().getShooter() instanceof LivingEntity) || !(event
        .getEntity() instanceof Arrow)) {
      return;
    }

    StrifeMob mob = plugin.getStrifeMobManager()
        .getStatMob((LivingEntity) event.getEntity().getShooter());

    event.setCancelled(true);

    if (mob.getAbilitySet() != null
        && mob.getAbilitySet().getAbilities(TriggerAbilityType.SHOOT) != null) {
      boolean triggered = plugin.getAbilityManager().abilityCast(mob, TriggerAbilityType.SHOOT);
      if (triggered) {
        return;
      }
    }

    double attackMultiplier = plugin.getAttackSpeedManager().getAttackMultiplier(mob);
    attackMultiplier = Math.pow(attackMultiplier, 1.5D);

    if (attackMultiplier <= 0.05) {
      event.setCancelled(true);
      return;
    }

    float projectileSpeed = 2.5f * (1 + (mob.getStat(StrifeStat.PROJECTILE_SPEED) / 100));
    int projectiles = ProjectileUtil.getTotalProjectiles(1, mob.getStat(StrifeStat.MULTISHOT));

    ProjectileUtil.createArrow(mob.getEntity(), attackMultiplier, projectileSpeed, 0, 0.17);
    projectiles--;

    for (int i = projectiles; i > 0; i--) {
      ProjectileUtil.createArrow(mob.getEntity(), attackMultiplier, projectileSpeed, randomOffset(projectiles),
          0.17);
    }
    mob.getEntity().getWorld()
        .playSound(mob.getEntity().getLocation(), Sound.ENTITY_ARROW_SHOOT, 1f, 1f);
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

    ProjectileUtil.createTrident((Player) event.getEntity().getShooter(), (Trident) event.getEntity(),
        attackMultiplier, speedMult);

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
      LogUtil
          .printWarning("A handled GroundProjectile was missing effect meta... something's wrong");
      return;
    }
    Location loc = event.getEntity().getLocation().clone()
        .add(event.getEntity().getLocation().getDirection().multiply(-0.25));
    for (String s : effects) {
      StrifePlugin.getInstance().getEffectManager()
          .execute(StrifePlugin.getInstance().getEffectManager().getEffect(s), caster, loc);
    }
  }

  private double randomOffset(double magnitude) {
    return 0.11 + magnitude * 0.005;
  }
}