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

import info.faceland.strife.StrifePlugin;
import info.faceland.strife.data.AttributedEntity;
import info.faceland.strife.util.StatUtil;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import static info.faceland.strife.util.DamageUtil.getResistPotionMult;

public class DOTListener implements Listener {

  private final StrifePlugin plugin;

  public DOTListener(StrifePlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onEntityDOTEvent(EntityDamageEvent event) {
    if (!(event.getEntity() instanceof LivingEntity)) {
      return;
    }
    if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
      return;
    }
    LivingEntity entity = (LivingEntity) event.getEntity();
    AttributedEntity statEntity = plugin.getEntityStatCache().getAttributedEntity(entity);

    if (event.getCause() == DamageCause.FIRE_TICK) {
      double damage = (1 + entity.getHealth() * 0.04) * getResistPotionMult(entity) * (1
          - StatUtil.getFireResist(statEntity) / 100);
      damage = plugin.getBarrierManager().damageBarrier(statEntity, damage);
      entity.getWorld().playSound(entity.getLocation(), Sound.ITEM_FLINTANDSTEEL_USE, 0.5f, 0.75f);
      if (entity.getHealth() > damage) {
        entity.setHealth(Math.max(entity.getHealth() - damage, 1));
      } else {
        entity.damage(damage);
      }
      event.setCancelled(true);
      return;
    }
    if (event.getCause() == DamageCause.FIRE || event.getCause() == DamageCause.LAVA) {
      double damage = (2 + entity.getHealth() * 0.1) * getResistPotionMult(entity) * (1
          - StatUtil.getFireResist(statEntity) / 100);
      damage = plugin.getBarrierManager().damageBarrier(statEntity, damage);
      event.setDamage(damage);
      return;
    }
    if (event.getCause() == DamageCause.POISON) {
      plugin.getBarrierManager().interruptBarrier(entity);
      double damage = 1 + entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue() * 0.005;
      damage *= getResistPotionMult(entity);
      entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_SPIDER_STEP, 1.0f, 1.5f);
      entity.setHealth(Math.max(entity.getHealth() - damage, 1));
      event.setCancelled(true);
      return;
    }
    if (event.getCause() == DamageCause.WITHER) {
      plugin.getBarrierManager().interruptBarrier(entity);
      double damage = 1 + entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue() * 0.005;
      damage *= getResistPotionMult(entity);
      entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_WITHER_SHOOT, 0.2f, 0.5f);
      if (entity.getHealth() > damage) {
        entity.setHealth(entity.getHealth() - damage);
      } else {
        entity.damage(damage);
      }
      event.setCancelled(true);
    }
  }
}
