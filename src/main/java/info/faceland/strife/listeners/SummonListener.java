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

import info.faceland.strife.managers.UniqueEntityManager;
import info.faceland.strife.util.LogUtil;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetEvent;

public class SummonListener implements Listener {

  private final UniqueEntityManager uniqueEntityManager;

  public SummonListener(UniqueEntityManager uniqueEntityManager) {
    this.uniqueEntityManager = uniqueEntityManager;
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onEntityTargetMaster(final EntityTargetEvent event) {
    if (!(event.getEntity() instanceof LivingEntity)) {
      return;
    }
    LivingEntity livingEntity = (LivingEntity) event.getEntity();
    if (!uniqueEntityManager.isUnique(livingEntity)) {
      return;
    }
    if (uniqueEntityManager.getMaster((LivingEntity) event.getTarget()) == livingEntity) {
      LogUtil.printDebug("Ignoring targeting of master for " + livingEntity.getCustomName());
      event.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onEntityDamageMaster(final EntityDamageByEntityEvent event) {
    if (!(event.getEntity() instanceof LivingEntity)) {
      return;
    }
    LivingEntity victim = (LivingEntity) event.getEntity();
    if (!uniqueEntityManager.isUnique(victim)) {
      return;
    }
    if (uniqueEntityManager.getMaster(victim) == event.getDamager()) {
      LogUtil.printDebug("Ignoring damaging of summon for " + event.getDamager());
      event.setCancelled(true);
    }
  }
}
