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
package info.faceland.strife.listeners.combat;

import static info.faceland.strife.util.DamageUtil.getResistPotionMult;

import info.faceland.strife.StrifePlugin;
import info.faceland.strife.data.DamageOverTimeData;
import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.util.StatUtil;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class DOTListener implements Listener {

  private final StrifePlugin plugin;
  private final Map<LivingEntity, DamageOverTimeData> poisonMap = new ConcurrentHashMap<>();
  private final Map<LivingEntity, DamageOverTimeData> witherMap = new ConcurrentHashMap<>();

  private static final long MAX_DOT_MS = 3000;
  private static final long DOT_PRUNE_TIME_MINIMUM = 10000;
  private static final int WITHER_FLAT_DAMAGE = 3;
  private static final int POISON_FLAT_DAMAGE = 1;
  private static final double POISON_PERCENT_MAX_HEALTH_DAMAGE = 0.01;

  private long lastPruneStamp = System.currentTimeMillis();

  public DOTListener(StrifePlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onEntityDOTEvent(EntityDamageEvent event) {
    if (!(event.getEntity() instanceof LivingEntity) || event.isCancelled()) {
      return;
    }
    LivingEntity entity = (LivingEntity) event.getEntity();
    double damage;
    switch (event.getCause()) {
      case ENTITY_ATTACK:
        return;
      case FIRE_TICK:
        StrifeMob statEntity = plugin.getStrifeMobManager().getStatMob(entity);
        damage = 1 + entity.getHealth() * 0.04;
        damage *= getResistPotionMult(entity);
        damage *= 1 - StatUtil.getFireResist(statEntity) / 100;
        damage = plugin.getBarrierManager().damageBarrier(statEntity, damage);
        entity.getWorld().playSound(entity.getLocation(), Sound.ITEM_FIRECHARGE_USE, 0.8f, 0.5f);
        dealDirectDamage(entity, damage);
        event.setCancelled(true);
        break;
      case FIRE:
      case LAVA:
        StrifeMob statEntity2 = plugin.getStrifeMobManager().getStatMob(entity);
        damage = 2 + entity.getHealth() * 0.1;
        damage *= getResistPotionMult(entity);
        damage *= 1 - StatUtil.getFireResist(statEntity2) / 100;
        damage = plugin.getBarrierManager().damageBarrier(statEntity2, damage);
        event.setDamage(damage);
        break;
      case WITHER:
      case POISON:
        int dotDamage = determineEffectDamage(entity, event.getCause());
        if (dotDamage > 0) {
          plugin.getBarrierManager().interruptBarrier(entity);
          dealEffectDamage(entity, dotDamage, event.getCause());
        }
        event.setCancelled(true);
        break;
      default:
        return;
    }
    pruneInvalidMapEntries();
  }

  private int determineEffectDamage(LivingEntity target, DamageCause cause) {
    PotionEffect effect;
    DamageOverTimeData data;
    if (cause == DamageCause.POISON) {
      poisonMap.putIfAbsent(target, new DamageOverTimeData());
      data = poisonMap.get(target);
      effect = target.getPotionEffect(PotionEffectType.POISON);
    } else {
      witherMap.putIfAbsent(target, new DamageOverTimeData());
      data = witherMap.get(target);
      effect = target.getPotionEffect(PotionEffectType.WITHER);
    }
    data.setStoredDamage(data.getStoredDamage() + 1);
    return getDamageFromDot(data, effect);
  }

  private int getDamageFromDot(DamageOverTimeData data, PotionEffect effect) {
    if (isDamageOverTimeReady(data)) {
      int effectDamage = data.getStoredDamage();
      if (effect.getDuration() < 65) {
        effectDamage += getRemainingDamageTicks(effect);
        data.setLastAddition(-1);
        data.setStoredDamage(0);
        return effectDamage;
      }
      data.setLastAddition(System.currentTimeMillis());
      data.setStoredDamage(0);
      return effectDamage;
    }
    return 0;
  }

  private void dealEffectDamage(LivingEntity target, double damage, DamageCause cause) {
    if (cause == DamageCause.POISON) {
      damage *= POISON_FLAT_DAMAGE + POISON_PERCENT_MAX_HEALTH_DAMAGE * target.getMaxHealth();
      dealDirectDamage(target, damage);
      target.getWorld().playSound(target.getLocation(), Sound.ENTITY_SPIDER_STEP, 1.5f, 1f);
    } else {
      damage *= WITHER_FLAT_DAMAGE;
      dealDirectDamage(target, damage);
      target.getWorld().playSound(target.getLocation(), Sound.ENTITY_WITHER_SHOOT, 0.8f, 1.2f);
    }
  }

  private boolean isDamageOverTimeReady(DamageOverTimeData data) {
    if (data.getLastAddition() == -1) {
      data.setLastAddition(System.currentTimeMillis());
      return false;
    }
    return data.getLastAddition() + MAX_DOT_MS < System.currentTimeMillis();
  }

  private void dealDirectDamage(LivingEntity entity, double damage) {
    entity.setHealth(Math.max(0, entity.getHealth() - damage));
  }

  private int getRemainingDamageTicks(PotionEffect effect) {
    if (effect.getAmplifier() < 0) {
      return 0;
    }
    return (int) ((double) effect.getDuration() / (25D / (effect.getAmplifier() + 1)));
  }

  private void pruneInvalidMapEntries() {
    if (lastPruneStamp + DOT_PRUNE_TIME_MINIMUM > System.currentTimeMillis()) {
      return;
    }
    for (LivingEntity le : witherMap.keySet()) {
      if (!le.isValid()) {
        witherMap.remove(le);
      }
    }
    for (LivingEntity le : poisonMap.keySet()) {
      if (!le.isValid()) {
        poisonMap.remove(le);
      }
    }
    lastPruneStamp = System.currentTimeMillis();
  }
}
