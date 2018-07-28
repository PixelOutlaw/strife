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
import java.util.Random;
import org.bukkit.entity.Creature;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class TargetingListener implements Listener {

  private final UniqueEntityManager uniqueEntityManager;
  private final Random random;

  public TargetingListener(UniqueEntityManager uniqueEntityManager) {
    this.uniqueEntityManager = uniqueEntityManager;
    this.random = new Random();
  }

  @EventHandler(priority = EventPriority.NORMAL)
  public void onBossRetarget(EntityDamageByEntityEvent e) {
    if (!(e.getEntity() instanceof Creature)) {
      return;
    }
    if (!(e.getDamager() instanceof LivingEntity || e.getDamager() instanceof Projectile)) {
      return;
    }
    if (!uniqueEntityManager.isUnique((LivingEntity) e.getEntity())) {
      return;
    }
    if (random.nextDouble() > 0.75) {
      if (e.getDamager() instanceof Projectile) {
        ((Creature) e.getEntity()).setTarget((LivingEntity)((Projectile) e.getDamager()).getShooter());
      } else {
        ((Creature) e.getEntity()).setTarget((LivingEntity)e.getDamager());
      }
    }
  }
}
