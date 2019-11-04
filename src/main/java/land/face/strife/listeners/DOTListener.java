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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import land.face.strife.StrifePlugin;
import land.face.strife.data.DamageOverTimeData;
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
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class DOTListener implements Listener {

  private final StrifePlugin plugin;
  private final Map<LivingEntity, DamageOverTimeData> poisonMap = new ConcurrentHashMap<>();
  private final Map<LivingEntity, DamageOverTimeData> witherMap = new ConcurrentHashMap<>();

  private static final long MAX_DOT_MS = 3000;
  private static final long DOT_PRUNE_TIME_MINIMUM = 10000;
  private float WITHER_FLAT_DAMAGE;
  private float POISON_FLAT_DAMAGE;
  private float POISON_PERCENT_MAX_HEALTH_DAMAGE;
  private float BURN_FLAT_DAMAGE;
  private float BURN_PERCENT_MAX_HEALTH_DAMAGE;

  private long lastPruneStamp = System.currentTimeMillis();

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
        damage = plugin.getBarrierManager().damageBarrier(statEntity, (float) damage);
        entity.getWorld().playSound(entity.getLocation(), Sound.ITEM_FIRECHARGE_USE, 0.8f, 0.5f);
        dealDirectDamage(entity, damage);
        event.setCancelled(true);
        break;
      case FIRE:
      case LAVA:
        StrifeMob statEntity2 = plugin.getStrifeMobManager().getStatMob(entity);
        damage = BURN_FLAT_DAMAGE + (float) entity.getHealth() * BURN_PERCENT_MAX_HEALTH_DAMAGE;
        damage *= 2.5;
        damage *= DamageUtil.getResistPotionMult(entity);
        damage *= 1 - StatUtil.getFireResist(statEntity2) / 100;
        damage *= 1 - statEntity2.getStat(StrifeStat.BURNING_RESIST) / 100;
        if (damage < 0.05) {
          event.setCancelled(true);
          return;
        }
        damage = plugin.getBarrierManager().damageBarrier(statEntity2, (float) damage);
        event.setDamage(damage);
        return;
      case WITHER:
      case POISON:
        StrifeMob statEntity3 = plugin.getStrifeMobManager().getStatMob(entity);
        int dotDamage = determineEffectDamage(statEntity3.getEntity(), event.getCause());
        if (dotDamage > 0) {
          plugin.getBarrierManager().interruptBarrier(entity);
          dealEffectDamage(statEntity3, dotDamage, event.getCause());
        }
        event.setCancelled(true);
        pruneInvalidMapEntries();
    }
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

  private void dealEffectDamage(StrifeMob mob, double damage, DamageCause cause) {
    LivingEntity target = mob.getEntity();
    if (cause == DamageCause.POISON) {
      damage *= POISON_FLAT_DAMAGE + POISON_PERCENT_MAX_HEALTH_DAMAGE * target.getMaxHealth();
      damage *= 1 + mob.getStat(StrifeStat.POISON_RESIST) / 100;
      if (target.getHealth() > 1) {
        dealDirectDamage(target, Math.min(damage, target.getHealth() - 1));
        target.getWorld().playSound(target.getLocation(), Sound.ENTITY_SPIDER_STEP, 1.5f, 1f);
      }
    } else {
      damage *= WITHER_FLAT_DAMAGE;
      damage *= 1 + mob.getStat(StrifeStat.WITHER_RESIST) / 100;
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
