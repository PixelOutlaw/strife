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

import info.faceland.strife.data.AttributedEntity;
import info.faceland.strife.managers.AttributedEntityManager;
import info.faceland.strife.util.LogUtil;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetEvent;

public class MinionListener implements Listener {

  private final AttributedEntityManager manager;

  public MinionListener(AttributedEntityManager attributedEntityManager) {
    this.manager = attributedEntityManager;
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onTargetMasterOrMinion(final EntityTargetEvent event) {
    if (event.isCancelled()) {
      return;
    }
    if (!(event.getEntity() instanceof Creature && event.getTarget() instanceof LivingEntity)) {
      return;
    }
    if (!(manager.isTrackedEntity(event.getEntity()) && manager
        .isTrackedEntity(event.getTarget()))) {
      return;
    }
    AttributedEntity attrEnt = manager.getAttributedEntity((LivingEntity) event.getEntity());
    AttributedEntity target = manager.getAttributedEntity((LivingEntity) event.getTarget());
    if (attrEnt.isMasterOf(target)) {
      LogUtil.printDebug("Ignoring targeting of minion for " + attrEnt.getEntity().getCustomName());
      event.setCancelled(true);
    } else if (attrEnt.isMinionOf(target)) {
      LogUtil.printDebug("Ignoring targeting of master for " + attrEnt.getEntity().getCustomName());
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
    if (!(manager.isTrackedEntity(event.getEntity()) && manager.isTrackedEntity(attacker))) {
      return;
    }
    AttributedEntity attack = manager.getAttributedEntity((LivingEntity) attacker);
    AttributedEntity defend = manager.getAttributedEntity((LivingEntity) event.getEntity());
    if (attack.isMinionOf(defend)) {
      LogUtil.printDebug("Ignoring attacking of master for " + attacker.getCustomName());
      event.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.LOW)
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
    AttributedEntity attackEntity = manager.getAttributedEntity((LivingEntity) attacker);
    if (attackEntity.getMinions()
        .contains(manager.getAttributedEntity((LivingEntity) event.getEntity()))) {
      return;
    }
    for (AttributedEntity minion : attackEntity.getMinions()) {
      if (minion.getEntity() instanceof Creature) {
        ((Creature) minion.getEntity()).setTarget((LivingEntity) event.getEntity());
      }
    }
  }

  @EventHandler(priority = EventPriority.LOW)
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
    AttributedEntity hitEnt = manager.getAttributedEntity((LivingEntity) event.getEntity());
    for (AttributedEntity minion : hitEnt.getMinions()) {
      if (!(minion.getEntity() instanceof Creature)) {
        continue;
      }
      if (((Creature) minion.getEntity()).getTarget() == null) {
        ((Creature) minion.getEntity()).setTarget((LivingEntity) attacker);
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
