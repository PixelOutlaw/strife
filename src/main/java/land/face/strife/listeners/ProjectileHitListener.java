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
import java.util.UUID;
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
    // Just ignore invulnerable things entirely
    if (event.getHitEntity() != null && event.getHitEntity().isInvulnerable()) {
      if (event.getHitEntity() instanceof LivingEntity le) {
        event.setCancelled(true);
        ProjectileUtil.disableCollision(event.getEntity(), le);
      }
      return;
    }
    // Process hit effects including location effects
    if (ProjectileUtil.getHitEffects(event.getEntity()) != null) {
      doHitEffects(event);
      return;
    }
    // Entity only (not location) after this point
    if (event.getHitEntity() == null) {
      return;
    }

    if (event.getHitEntity() instanceof LivingEntity le) {
      UUID projectileUuid = event.getEntity().getUniqueId();
      if (le.getCollidableExemptions().contains(projectileUuid)) {
        event.setCancelled(true);
      } else {
        ProjectileUtil.disableCollision(event.getEntity(), le);
        event.setCancelled(true);
        if (event.getEntity().getShooter() instanceof LivingEntity &&
            TargetingUtil.isFriendly((LivingEntity) event.getEntity().getShooter(), le)) {
          // Friendly fire, don't bother checking for pierce or projectile removal
          return;
        }
        StrifeMob attacker;
        if (event.getEntity().getShooter() instanceof LivingEntity) {
          attacker = plugin.getStrifeMobManager().getStatMob((LivingEntity) event.getEntity().getShooter());
        } else {
          attacker = null;
        }
        ProjectileDamageEvent pde = new ProjectileDamageEvent(
            attacker, plugin.getStrifeMobManager().getStatMob(le), event.getEntity());
        Bukkit.getPluginManager().callEvent(pde);
        if (!pde.isPassThrough()) {
          int peirce = ProjectileUtil.getPierce(event.getEntity());
          if (peirce == 0) {
            event.getEntity().remove();
            event.setCancelled(true);
          } else {
            ProjectileUtil.reducePierce(event.getEntity());
          }
        }
      }
    } else {
      event.getEntity().remove();
      event.setCancelled(true);
    }
  }

  private void doHitEffects(ProjectileHitEvent event) {

    TargetResponse response = buildFromEvent(event);

    if (response == null) {
      return;
    }

    StrifeMob caster = plugin.getStrifeMobManager().getStatMob((LivingEntity) event.getEntity().getShooter());

    List<Effect> hitEffects = ProjectileUtil.getHitEffects(event.getEntity());
    if (hitEffects.isEmpty()) {
      LogUtil.printWarning("A handled effectProjectile was missing effect meta... something's wrong");
      event.getEntity().remove();
      return;
    }

    plugin.getEffectManager()
        .processEffectList(caster, response, hitEffects, ProjectileUtil.getShotId(event.getEntity()));

    if (event.getHitEntity() != null && event.getHitEntity() instanceof LivingEntity) {
      ProjectileUtil.disableCollision(event.getEntity(), (LivingEntity) event.getHitEntity());
      int peirce = ProjectileUtil.getPierce(event.getEntity());
      if (peirce == 0) {
        event.getEntity().remove();
        event.setCancelled(true);
      } else {
        ProjectileUtil.reducePierce(event.getEntity());
      }
    }
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
      if (le.isInvulnerable() || le.hasMetadata("NPC")) {
        ProjectileUtil.disableCollision(event.getEntity(), le);
        event.setCancelled(true);
        return null;
      }
      if (TargetingUtil.isFriendly((LivingEntity) event.getEntity().getShooter(), le)) {
        ProjectileUtil.disableCollision(event.getEntity(), le);
        event.setCancelled(true);
        return null;
      }
      ProjectileUtil.disableCollision(event.getEntity(), (LivingEntity) event.getHitEntity());
      int pierce = ProjectileUtil.getPierce(event.getEntity());
      if (pierce == 0) {
        event.getEntity().remove();
        event.setCancelled(true);
      } else {
        ProjectileUtil.reducePierce(event.getEntity());
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
}
