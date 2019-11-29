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
import land.face.strife.stats.StrifeStat;
import land.face.strife.util.DamageUtil;
import land.face.strife.util.StatUtil;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class DOTListener implements Listener {

  private final StrifePlugin plugin;

  private float WITHER_FLAT_DAMAGE;
  private float POISON_FLAT_DAMAGE;
  private float POISON_PERCENT_MAX_HEALTH_DAMAGE;
  private float BURN_FLAT_DAMAGE;
  private float BURN_PERCENT_MAX_HEALTH_DAMAGE;

  public DOTListener(StrifePlugin plugin) {
    this.plugin = plugin;
    WITHER_FLAT_DAMAGE = (float) plugin.getSettings()
        .getDouble("config.mechanics.wither-flat-damage");
    POISON_FLAT_DAMAGE = (float) plugin.getSettings()
        .getDouble("config.mechanics.poison-flat-damage");
    POISON_PERCENT_MAX_HEALTH_DAMAGE = (float) plugin.getSettings()
        .getDouble("config.mechanics.poison-percent-damage");
    BURN_FLAT_DAMAGE = (float) plugin.getSettings()
        .getDouble("config.mechanics.burn-flat-damage");
    BURN_PERCENT_MAX_HEALTH_DAMAGE = (float) plugin.getSettings()
        .getDouble("config.mechanics.burn-percent-damage");
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onEntityDOTEvent(EntityDamageEvent event) {
    if (!(event.getEntity() instanceof LivingEntity) || event.isCancelled()) {
      return;
    }
    LivingEntity entity = (LivingEntity) event.getEntity();
    float damage;
    switch (event.getCause()) {
      case ENTITY_ATTACK:
      case MAGIC:
        return;
      case STARVATION:
      case SUFFOCATION:
      case DROWNING:
        DamageUtil.removeDamageModifiers(event);
        event.setDamage(entity.getMaxHealth() / 10);
        return;
      case FIRE_TICK:
        StrifeMob statEntity = plugin.getStrifeMobManager().getStatMob(entity);
        damage = BURN_FLAT_DAMAGE + (float) entity.getHealth() * BURN_PERCENT_MAX_HEALTH_DAMAGE;
        damage *= DamageUtil.getResistPotionMult(entity);
        damage *= 1 - StatUtil.getFireResist(statEntity) / 100;
        damage *= 1 - statEntity.getStat(StrifeStat.BURNING_RESIST) / 100;
        if (damage < 0.05) {
          event.setCancelled(true);
          return;
        }
        damage = plugin.getBarrierManager().damageBarrier(statEntity, damage);
        entity.getWorld().playSound(entity.getLocation(), Sound.ITEM_FIRECHARGE_USE, 0.8f, 0.5f);
        dealDirectDamage(entity, damage);
        event.setCancelled(true);
        break;
      case FIRE:
      case LAVA:
        StrifeMob statEntity2 = plugin.getStrifeMobManager().getStatMob(entity);
        damage = BURN_FLAT_DAMAGE + (float) entity.getHealth() * BURN_PERCENT_MAX_HEALTH_DAMAGE;
        damage *= DamageUtil.getResistPotionMult(entity);
        damage *= 1 - StatUtil.getFireResist(statEntity2) / 100;
        if (damage < 0.05) {
          event.setCancelled(true);
          return;
        }
        damage = plugin.getBarrierManager().damageBarrier(statEntity2, damage);
        event.setDamage(damage);
        return;
      case WITHER:
        StrifeMob statEntity3 = plugin.getStrifeMobManager().getStatMob(entity);
        damage = WITHER_FLAT_DAMAGE;
        damage *= DamageUtil.getResistPotionMult(entity);
        damage *= 1 - statEntity3.getStat(StrifeStat.WITHER_RESIST) / 100;
        if (damage < 0.05) {
          return;
        }
        event.setCancelled(true);
        dealDirectDamage(entity, Math.min(damage, entity.getHealth() - 1));
        entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_WITHER_SHOOT, 0.8f, 1.2f);
        return;
      case POISON:
        StrifeMob statEntity4 = plugin.getStrifeMobManager().getStatMob(entity);
        damage = POISON_FLAT_DAMAGE + (float) entity.getHealth() * POISON_PERCENT_MAX_HEALTH_DAMAGE;
        damage *= DamageUtil.getResistPotionMult(entity);
        damage *= 1 - statEntity4.getStat(StrifeStat.POISON_RESIST) / 100;
        if (damage < 0.05) {
          return;
        }
        event.setCancelled(true);
        dealDirectDamage(entity, Math.min(damage, entity.getHealth() - 1));
        entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_SPIDER_STEP, 1.5f, 1f);
    }
  }

  private void dealDirectDamage(LivingEntity entity, double damage) {
    entity.setHealth(Math.max(0, entity.getHealth() - damage));
  }
}
