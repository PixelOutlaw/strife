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
package info.faceland.strife.listeners;

import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.managers.MinionManager;
import info.faceland.strife.managers.StrifeMobManager;
import info.faceland.strife.util.LogUtil;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetEvent.TargetReason;

public class MinionListener implements Listener {

  private final StrifeMobManager entityManager;
  private final MinionManager minionManager;

  public MinionListener(StrifeMobManager entityManager, MinionManager minionManager) {
    this.entityManager = entityManager;
    this.minionManager = minionManager;
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onMinionTargetAnything(final EntityTargetEvent event) {
    if (event.isCancelled() || event.getReason() == TargetReason.CUSTOM) {
      return;
    }
    if (event.getEntity() instanceof LivingEntity) {
      if (minionManager.isMinion((LivingEntity) event.getEntity())) {
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
    if (!entityManager.isTrackedEntity(event.getEntity())) {
      return;
    }
    StrifeMob attrEnt = entityManager.getAttributedEntity((LivingEntity) event.getEntity());
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
    if (!(entityManager.isTrackedEntity(event.getEntity()) && entityManager.isTrackedEntity(attacker))) {
      return;
    }
    StrifeMob defend = entityManager.getAttributedEntity((LivingEntity) event.getEntity());
    if (defend.isMasterOf((LivingEntity)attacker)) {
      LogUtil.printDebug("Ignoring attacking of master for " + attacker.getCustomName());
      event.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onMasterAttack(final EntityDamageByEntityEvent event) {
    if (event.isCancelled()) {
      return;
    }
    if (!(event.getEntity() instanceof LivingEntity)) {
      return;
    }
    Entity attacker = getDamagingEntity(event.getDamager());
    if (!(attacker instanceof LivingEntity)) {
      return;
    }
    StrifeMob attackEntity = entityManager.getAttributedEntity((LivingEntity) attacker);
    if (attackEntity.getMinions()
        .contains(entityManager.getAttributedEntity((LivingEntity) event.getEntity()))) {
      return;
    }
    for (StrifeMob minion : attackEntity.getMinions()) {
      if (minion.getEntity() instanceof Mob) {
        ((Mob) minion.getEntity()).setTarget((LivingEntity) event.getEntity());
      }
    }
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onMasterHit(final EntityDamageByEntityEvent event) {
    if (event.isCancelled()) {
      return;
    }
    if (!(event.getEntity() instanceof LivingEntity)) {
      return;
    }
    Entity attacker = getDamagingEntity(event.getDamager());
    if (!(attacker instanceof LivingEntity)) {
      return;
    }
    StrifeMob hitEnt = entityManager.getAttributedEntity((LivingEntity) event.getEntity());
    for (StrifeMob minion : hitEnt.getMinions()) {
      if (!(minion.getEntity() instanceof Mob)) {
        continue;
      }
      if (((Mob) minion.getEntity()).getTarget() == null) {
        ((Mob) minion.getEntity()).setTarget((LivingEntity) attacker);
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