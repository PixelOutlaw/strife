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

import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.events.StrifeDamageEvent;
import land.face.strife.util.DamageUtil;
import land.face.strife.util.LogUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetEvent.TargetReason;

public class MinionListener implements Listener {

  private final StrifePlugin plugin;

  public MinionListener(StrifePlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onMinionTargetAnything(final EntityTargetEvent event) {
    if (event.isCancelled() || event.getReason() == TargetReason.CUSTOM) {
      return;
    }
    if (event.getEntity() instanceof LivingEntity) {
      StrifeMob mob = plugin.getStrifeMobManager().getStatMob((LivingEntity) event.getEntity());
      if (mob != null && mob.getMaster() != null) {
        event.setCancelled(true);
      }
    }
  }

  @EventHandler(priority = EventPriority.LOW)
  public void onMasterTargetMinion(final EntityTargetEvent event) {
    if (event.isCancelled()) {
      return;
    }
    if (!(event.getEntity() instanceof Mob) || !(event.getTarget() instanceof LivingEntity)) {
      return;
    }
    if (!plugin.getStrifeMobManager().isTrackedEntity((LivingEntity) event.getEntity())) {
      return;
    }
    StrifeMob attrEnt = plugin.getStrifeMobManager().getStatMob((LivingEntity) event.getEntity());
    if (attrEnt.isMasterOf((LivingEntity) event.getEntity())) {
      LogUtil.printDebug("Ignoring targeting of minion for " + attrEnt.getEntity().getCustomName());
      event.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onEntityDamageMaster(final EntityDamageByEntityEvent event) {
    if (event.isCancelled()) {
      return;
    }
    Entity attacker = event.getDamager();
    if (attacker instanceof Projectile) {
      if (((Projectile) attacker).getShooter() instanceof LivingEntity) {
        attacker = (LivingEntity) ((Projectile) attacker).getShooter();
      }
    }
    if (!(attacker instanceof LivingEntity)) {
      return;
    }
    if (!plugin.getStrifeMobManager().isTrackedEntity((LivingEntity) attacker)) {
      return;
    }
    StrifeMob attackMob = plugin.getStrifeMobManager().getStatMob((LivingEntity) attacker);
    if (event.getEntity() instanceof Player && attackMob.getMaster() != null && attackMob
        .getMaster() instanceof Player) {
      if (!DamageUtil.canAttack((Player) attackMob.getMaster(), (Player) event.getEntity())) {
        event.setCancelled(true);
        return;
      }
    }
    if (attackMob.getMaster() != null && attackMob.getMaster().getEntity() == event.getEntity()) {
      LogUtil.printDebug("Ignoring attacking of master for " + attacker.getCustomName());
      event.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onMasterAttack(final StrifeDamageEvent event) {
    if (event.isCancelled()) {
      return;
    }
    if (!event.getDamageModifiers().isApplyOnHitEffects()) {
      return;
    }
    Bukkit.getScheduler().runTaskLater(plugin, () -> {
      if (event.getDefender().getEntity().isDead()) {
        return;
      }
      StrifeMob attackEntity = event.getAttacker();
      if (attackEntity.getMinions().contains(plugin.getStrifeMobManager()
          .getStatMob(event.getDefender().getEntity()))) {
        return;
      }
      for (StrifeMob minion : attackEntity.getMinions()) {
        if (minion.getEntity() instanceof Mob) {
          ((Mob) minion.getEntity()).setTarget(event.getDefender().getEntity());
        }
      }
    }, 1L);
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onMasterHit(final EntityDamageByEntityEvent event) {
    if (event.isCancelled() || !(event.getEntity() instanceof LivingEntity)) {
      return;
    }
    Entity attacker = getDamagingEntity(event.getDamager());
    if (!(attacker instanceof LivingEntity)) {
      return;
    }
    StrifeMob hitEnt = plugin.getStrifeMobManager().getStatMob((LivingEntity) event.getEntity());
    for (StrifeMob minion : hitEnt.getMinions()) {
      if (!(minion.getEntity() instanceof Mob mob)) {
        continue;
      }
      if (mob.getTarget() == null || !mob.getTarget().isValid()) {
        mob.setTarget((LivingEntity) attacker);
      }
    }
  }

  private Entity getDamagingEntity(Entity attacker) {
    if (attacker instanceof Projectile) {
      if (((Projectile) attacker).getShooter() instanceof Entity) {
        return (Entity) ((Projectile) attacker).getShooter();
      }
    }
    return attacker;
  }
}
