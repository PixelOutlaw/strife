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

import java.util.Collections;
import java.util.List;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.TargetResponse;
import land.face.strife.data.effects.Effect;
import land.face.strife.events.ProjectileDamageEvent;
import land.face.strife.util.LogUtil;
import land.face.strife.util.ProjectileUtil;
import land.face.strife.util.TargetingUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;

public record ProjectileHitListener(StrifePlugin plugin) implements Listener {

  @EventHandler(priority = EventPriority.LOW)
  public void onProjectileHitEntity(ProjectileHitEvent event) {
    if (event.isCancelled()) {
      return;
    }
    boolean deleteProjectile = true;
    if (event.getHitEntity() != null) {
      if (event.getHitEntity().isInvulnerable() || event.getHitEntity().hasMetadata("NPC")) {
        event.setCancelled(true);
        return;
      }
      if (event.getHitEntity() instanceof LivingEntity le) {
        ProjectileUtil.disableCollision(event.getEntity(), le);
        int pierce = ProjectileUtil.getPierce(event.getEntity());
        if (pierce > 0) {
          deleteProjectile = false;
          ProjectileUtil.reducePierce(event.getEntity());
          event.setCancelled(true);
        }
      }
    }
    // Process hit effects including location effects
    if (ProjectileUtil.getHitEffects(event.getEntity()) != null) {
      doHitEffects(event);
      if (event.getHitBlock() == null && deleteProjectile) {
        event.getEntity().remove();
      }
      return;
    }
    // Entity only (not location) after this point
    if (event.getHitEntity() == null) {
      return;
    }
    // If it's a non-living entity, just bail out
    if (!(event.getHitEntity() instanceof LivingEntity hitEntity)) {
      event.getEntity().remove();
      event.setCancelled(true);
      return;
    }

    // At this point, we'll either pass through or delete the projectile
    event.setCancelled(true);

    var shooter = event.getEntity().getShooter();
    if (shooter instanceof LivingEntity && TargetingUtil.isFriendly((LivingEntity) shooter, hitEntity)) {
      // Friendly fire, don't bother checking for pierce or projectile removal
      return;
    }

    StrifeMob attacker;
    if (shooter instanceof LivingEntity) {
      attacker = plugin.getStrifeMobManager().getStatMob((LivingEntity) shooter);
    } else {
      attacker = null;
    }

    ProjectileDamageEvent pde = new ProjectileDamageEvent(attacker,
        plugin.getStrifeMobManager().getStatMob(hitEntity), event.getEntity());
    Bukkit.getPluginManager().callEvent(pde);

    if (pde.isPassThrough()) {
      return;
    }

    if (deleteProjectile) {
      event.getEntity().remove();
    }
  }

  private void doHitEffects(ProjectileHitEvent event) {
    TargetResponse response = buildFromEvent(event);
    if (response == null) {
      return;
    }
    Projectile projectile = event.getEntity();
    StrifeMob caster = plugin.getStrifeMobManager().getStatMob((LivingEntity) projectile.getShooter());
    List<Effect> hitEffects = ProjectileUtil.getHitEffects(projectile);
    if (hitEffects.isEmpty()) {
      LogUtil.printWarning("A handled effectProjectile was missing effect meta... something's wrong");
      projectile.remove();
      return;
    }
    plugin.getEffectManager().processEffectList(caster, response, hitEffects, ProjectileUtil.getShotId(projectile));
  }

  private TargetResponse buildFromEvent(ProjectileHitEvent event) {
    // any block hit
    if (event.getHitBlock() != null) {
      if (!ProjectileUtil.isContactTrigger(event.getEntity())) {
        return null;
      }
      Location effectLocation = event.getHitBlock().getLocation().clone().add(0.5, 0.5, 0.5);
      if (event.getHitBlockFace() != null) {
        effectLocation.add(event.getHitBlockFace().getDirection());
      } else {
        effectLocation.add(event.getEntity().getLocation().getDirection().clone().multiply(-1.1));
      }
      event.getEntity().remove();
      return new TargetResponse(effectLocation);
    }
    // not block, no entity at all
    if (event.getHitEntity() == null) {
      return null;
    }
    // not block, living entity
    if (event.getHitEntity() instanceof LivingEntity le) {
      if (TargetingUtil.isFriendly((LivingEntity) event.getEntity().getShooter(), le)) {
        ProjectileUtil.disableCollision(event.getEntity(), le);
        event.setCancelled(true);
        return null;
      }
      return new TargetResponse(Collections.singleton(le), event.getEntity().getLocation());
    }
    // Not block, not living
    if (ProjectileUtil.isContactTrigger(event.getEntity())) {
      Location loc = event.getEntity().getLocation().clone();
      event.getEntity().remove();
      return new TargetResponse(loc);
    }
    event.getEntity().remove();
    event.setCancelled(true);
    return null;
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
