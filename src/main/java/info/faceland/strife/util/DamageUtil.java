package info.faceland.strife.util;

import static info.faceland.strife.attributes.StrifeAttribute.HP_ON_HIT;

import com.tealcube.minecraft.bukkit.TextUtils;
import gyurix.spigotlib.ChatAPI;
import info.faceland.strife.attributes.StrifeAttribute;
import info.faceland.strife.data.AttributedEntity;
import info.faceland.strife.events.CriticalEvent;
import info.faceland.strife.events.EvadeEvent;
import info.faceland.strife.managers.DarknessManager;
import java.util.Collection;
import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class DamageUtil {

  private static final String ATTACK_MISSED = TextUtils.color("&f&lMiss!");
  private static final String ATTACK_DODGED = TextUtils.color("&f&lDodge!");
  private static final Random RANDOM = new Random(System.currentTimeMillis());

  public static double attemptIgnite(double damage, AttributedEntity attacker, LivingEntity defender) {
    if (damage == 0 || rollDouble() >= attacker.getAttribute(StrifeAttribute.IGNITE_CHANCE) / 100) {
      return 0D;
    }
    double bonusDamage = defender.getFireTicks() > 0 ? damage : 1D;
    defender.setFireTicks(Math.max(60 + (int) damage, defender.getFireTicks()));
    defender.getWorld().playSound(defender.getEyeLocation(),Sound.ITEM_FLINTANDSTEEL_USE, 1f, 1f);
    defender.getWorld().spawnParticle(Particle.FLAME, defender.getEyeLocation(), 6 + (int) damage / 2,
        0.3, 0.3, 0.3, 0.03);
    return bonusDamage;
  }

  public static double attemptShock(double damage, AttributedEntity attacker, LivingEntity defender) {
    if (damage == 0 || rollDouble() >= attacker.getAttribute(StrifeAttribute.SHOCK_CHANCE) / 100) {
      return 0D;
    }
    double multiplier = 0.5;
    double percentHealth = defender.getHealth() / defender.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
    if (percentHealth < 0.5) {
      multiplier = 1 / Math.max(0.16, percentHealth * 2);
    }
    double particles = damage * multiplier * 0.5;
    double particleRange = 0.8 + multiplier * 0.2;
    defender.getWorld().playSound(defender.getEyeLocation(), Sound.ENTITY_LIGHTNING_THUNDER, 0.7f, 2f);
    defender.getWorld().spawnParticle(Particle.CRIT_MAGIC, defender.getEyeLocation(), 10 + (int) particles,
        particleRange, particleRange, particleRange, 0.12);
    if (defender instanceof Creeper) {
      ((Creeper) defender).setPowered(true);
    }
    return damage * multiplier;
  }

  public static double attemptFreeze(double damage, AttributedEntity attacker, LivingEntity defender) {
    if (damage == 0 || rollDouble() >= attacker.getAttribute(StrifeAttribute.FREEZE_CHANCE) / 100) {
      return 0D;
    }
    double multiplier = 0.25 + 0.25 * (StatUtil.getHealth(attacker) / 100);
    if (!defender.hasPotionEffect(PotionEffectType.SLOW)) {
      defender.getActivePotionEffects().add(new PotionEffect(PotionEffectType.SLOW, 30, 1));
    }
    defender.getWorld().playSound(defender.getEyeLocation(), Sound.BLOCK_GLASS_BREAK, 1f, 1.0f);
    defender.getWorld().spawnParticle(Particle.SNOWBALL, defender.getEyeLocation(), 4 + (int) damage / 2,
        0.3, 0.3, 0.2, 0.0);
    return damage * multiplier;
  }

  public static boolean attemptCorrupt(double damage, AttributedEntity attacker, LivingEntity defender) {
    if (damage == 0 || rollDouble() >= attacker.getAttribute(StrifeAttribute.CORRUPT_CHANCE) / 100) {
      return false;
    }
    defender.getWorld().playSound(defender.getEyeLocation(), Sound.ENTITY_WITHER_SHOOT, 0.7f, 2f);
    defender.getWorld().spawnParticle(Particle.SMOKE_NORMAL, defender.getEyeLocation(), 10,0.4, 0.4, 0.5, 0.1);
    DarknessManager.applyCorruptionStacks(defender, damage);
    return true;
  }

  public static void doEvasion(LivingEntity attacker, LivingEntity defender) {
    callEvadeEvent(defender, attacker);
    defender.getWorld().playSound(defender.getEyeLocation(), Sound.ENTITY_GHAST_SHOOT, 0.5f, 2f);
    if (defender instanceof Player) {
      ChatAPI.sendJsonMsg(ChatAPI.ChatMessageType.ACTION_BAR, ATTACK_DODGED, (Player) defender);
    }
    if (attacker instanceof Player) {
      ChatAPI.sendJsonMsg(ChatAPI.ChatMessageType.ACTION_BAR, ATTACK_MISSED, (Player) attacker);
    }
  }

  public static double getBlockAmount(AttributedEntity defender, double blockTimeLeft) {
    double blockedAmount;
    double maxBlockAmount = defender.getAttribute(StrifeAttribute.BLOCK);
    if (blockTimeLeft > 0) {
      blockedAmount = maxBlockAmount * Math.max(1 - (blockTimeLeft / 6), 0.1);
    } else {
      blockedAmount = maxBlockAmount;
    }
    return blockedAmount;
  }

  public static double getPotionMult(LivingEntity attacker, LivingEntity defender) {
    double potionMult = 1.0;
    Collection<PotionEffect> attackerEffects = attacker.getActivePotionEffects();
    Collection<PotionEffect> defenderEffects = defender.getActivePotionEffects();
    for (PotionEffect effect : attackerEffects) {
      if (effect.getType().equals(PotionEffectType.INCREASE_DAMAGE)) {
        potionMult += 0.1 * (effect.getAmplifier() + 1);
        continue;
      }
      if (effect.getType().equals(PotionEffectType.WEAKNESS)) {
        potionMult -= 0.1 * (effect.getAmplifier() + 1);
        continue;
      }
    }

    for (PotionEffect effect : defenderEffects) {
      if (effect.getType().equals(PotionEffectType.WITHER)) {
        potionMult += 0.15 * (effect.getAmplifier() + 1);
        continue;
      }
      if (effect.getType().equals(PotionEffectType.DAMAGE_RESISTANCE)) {
        potionMult -= 0.1 * (effect.getAmplifier() + 1);
        continue;
      }
    }
    return Math.max(0, potionMult);
  }

  public static double getResistPotionMult(LivingEntity defender) {
    double mult = 1.0;
    Collection<PotionEffect> defenderEffects = defender.getActivePotionEffects();
    for (PotionEffect effect : defenderEffects) {
      if (effect.getType().equals(PotionEffectType.DAMAGE_RESISTANCE)) {
        mult -= 0.1 * (effect.getAmplifier() + 1);
        continue;
      }
    }
    return mult;
  }

  public static void applyLifeSteal(AttributedEntity attacker, double damage, double healMultiplier) {
    double lifeSteal = StatUtil.getLifestealPercentage(attacker);
    if (lifeSteal <= 0 || attacker.getEntity().getHealth() <= 0 || attacker.getEntity().isDead()) {
      return;
    }
    double lifeStolen = damage * lifeSteal;
    if (attacker instanceof Player) {
      lifeStolen *= Math.min(((Player)attacker).getFoodLevel() / 7.0D, 1.0D);
    }
    if (attacker.getEntity().hasPotionEffect(PotionEffectType.POISON)) {
      lifeStolen *= 0.3;
    }
    restoreHealth(attacker.getEntity(), lifeStolen * healMultiplier);
  }

  public static void applyHealthOnHit(AttributedEntity attacker, double attackMultiplier, double healMultiplier) {
    double health = attacker.getAttribute(HP_ON_HIT) * attackMultiplier;
    if (health <= 0 || attacker.getEntity().getHealth() <= 0 || attacker.getEntity().isDead()) {
      return;
    }
    if (attacker instanceof Player) {
      health *= Math.min(((Player)attacker).getFoodLevel() / 7.0D, 1.0D);
    }
    if (attacker.getEntity().hasPotionEffect(PotionEffectType.POISON)) {
      health *= 0.3;
    }
    restoreHealth(attacker.getEntity(), health * healMultiplier);
  }

  public static void callCritEvent(LivingEntity attacker, LivingEntity victim) {
    CriticalEvent c = new CriticalEvent(attacker, victim);
    Bukkit.getPluginManager().callEvent(c);
  }

  public static void callEvadeEvent(LivingEntity evader, LivingEntity attacker) {
    EvadeEvent ev = new EvadeEvent(evader, attacker);
    Bukkit.getPluginManager().callEvent(ev);
  }

  public static boolean hasLuck(LivingEntity entity) {
    return entity.hasPotionEffect(PotionEffectType.LUCK);
  }

  public static void restoreHealth(LivingEntity livingEntity, double amount) {
    livingEntity.setHealth(Math.min(livingEntity.getHealth() + amount, livingEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()));
  }

  public static double rollDouble(boolean lucky) {
    return lucky ? Math.max(rollDouble(), rollDouble()) : rollDouble();
  }

  public static double rollDouble() {
    return RANDOM.nextDouble();
  }

  public static boolean rollBool(double chance, boolean lucky) {
    return lucky ? rollBool(chance) || rollBool(chance) : rollBool(chance);
  }

  public static boolean rollBool(double chance) {
    return RANDOM.nextDouble() <= chance;
  }
}
