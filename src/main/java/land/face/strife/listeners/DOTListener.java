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
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public record DOTListener(StrifePlugin plugin) implements Listener {

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onEntityDOTEvent(EntityDamageEvent event) {
    if (event.isCancelled()) {
      return;
    }
    if (!(event.getEntity() instanceof LivingEntity le)) {
      return;
    }
    switch (event.getCause()) {
      case LAVA, FIRE -> {
        le.setFireTicks(Math.max(le.getFireTicks(), 17));
        event.setCancelled(true);
      }
      case HOT_FLOOR -> {
        le.setFireTicks(9);
        event.setCancelled(true);
      }
      case FREEZE -> {
        event.setDamage(0);
        event.setCancelled(true);
      }
      case FIRE_TICK, POISON, WITHER, CONTACT, SUFFOCATION -> event.setCancelled(true);
      case DROWNING -> {
        DamageUtil.dealRawDamage(plugin.getStrifeMobManager().getStatMob(le),
            5 + (float) le.getMaxHealth() * 0.2f);
        event.setDamage(0);
      }
    }
  }
}
