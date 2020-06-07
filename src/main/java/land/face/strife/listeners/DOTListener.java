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
import land.face.strife.util.DamageUtil;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class DOTListener implements Listener {

  private final StrifePlugin plugin;

  public DOTListener(StrifePlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler
  public void onEntityDOTEvent(EntityDamageEvent event) {
    if (event.isCancelled()) {
      return;
    }
    if (!(event.getEntity() instanceof LivingEntity)) {
      return;
    }
    if (event.getCause() == DamageCause.STARVATION) {
      event.setCancelled(true);
      return;
    }
    LivingEntity le = (LivingEntity) event.getEntity();
    if (event.getCause() == DamageCause.LAVA || event.getCause() == DamageCause.FIRE) {
      DamageUtil.removeDamageModifiers(event);
      event.setDamage(1);
      le.setFireTicks(Math.max(le.getFireTicks(), 40));
      plugin.getDamageOverTimeTask().trackBurning(le);
      return;
    }
    if (event.getCause() == DamageCause.HOT_FLOOR) {
      le.setFireTicks(40);
      plugin.getDamageOverTimeTask().trackBurning(le);
      event.setCancelled(true);
      return;
    }
    if (event.getCause() == DamageCause.FIRE_TICK) {
      plugin.getDamageOverTimeTask().trackBurning(le);
      event.setCancelled(true);
      return;
    }
    if (event.getCause() == DamageCause.POISON) {
      plugin.getDamageOverTimeTask().trackPoison(le);
      event.setCancelled(true);
      return;
    }
    if (event.getCause() == DamageCause.WITHER) {
      plugin.getDamageOverTimeTask().trackWither(le);
      event.setCancelled(true);
      return;
    }
    if (event.getCause() == DamageCause.SUFFOCATION || event.getCause() == DamageCause.DROWNING) {
      DamageUtil.removeDamageModifiers(event);
      event.setDamage(le.getMaxHealth() / 10);
    }
  }
}
