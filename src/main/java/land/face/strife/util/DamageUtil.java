package land.face.strife.util;

import static land.face.strife.util.StatUtil.getArmorMult;
import static land.face.strife.util.StatUtil.getDefenderArmor;
import static land.face.strife.util.StatUtil.getDefenderWarding;
import static land.face.strife.util.StatUtil.getEarthResist;
import static land.face.strife.util.StatUtil.getFireResist;
import static land.face.strife.util.StatUtil.getIceResist;
import static land.face.strife.util.StatUtil.getLightResist;
import static land.face.strife.util.StatUtil.getLightningResist;
import static land.face.strife.util.StatUtil.getShadowResist;
import static land.face.strife.util.StatUtil.getWardingMult;

import com.tealcube.minecraft.bukkit.TextUtils;
import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.buff.LoadedBuff;
import land.face.strife.events.BlockEvent;
import land.face.strife.events.CriticalEvent;
import land.face.strife.events.EvadeEvent;
import land.face.strife.events.SneakAttackEvent;
import land.face.strife.listeners.combat.CombatListener;
import land.face.strife.managers.BlockManager;
import land.face.strife.managers.CorruptionManager;
import land.face.strife.stats.StrifeStat;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EvokerFangs;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.ShulkerBullet;
import org.bukkit.entity.SmallFireball;
import org.bukkit.entity.WitherSkull;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class DamageUtil {

  private static final String ATTACK_MISSED = TextUtils.color("&f&lMiss!");
  private static final String ATTACK_BLOCKED = TextUtils.color("&f&lBlocked!");
  private static final String ATTACK_DODGED = TextUtils.color("&f&lDodge!");
  public static double EVASION_THRESHOLD = StrifePlugin.getInstance().getSettings()
      .getDouble("config.mechanics.evasion-threshold", 0.5);
  private static final Random RANDOM = new Random(System.currentTimeMillis());
  private static final DamageModifier[] MODIFIERS = EntityDamageEvent.DamageModifier.values();
  private static final DamageType[] DMG_TYPES = DamageType.values();

  private static final float BLEED_PERCENT = 0.5f;

  public static double dealDirectDamage(StrifeMob attacker, StrifeMob defender, float damage) {
    damage = StrifePlugin.getInstance().getBarrierManager().damageBarrier(defender, damage);
    forceCustomDamage(attacker.getEntity(), defender.getEntity(), Math.max(damage, 0.01));
    return damage;
  }

  public static float getRawDamage(StrifeMob attacker, DamageType damageType, AttackType type) {
    switch (damageType) {
      case PHYSICAL:
        float damage = attacker.getStat(StrifeStat.PHYSICAL_DAMAGE);
        if (type == AttackType.MELEE) {
          damage *= 1 + attacker.getStat(StrifeStat.MELEE_PHYSICAL_MULT) / 100;
        } else if (type == AttackType.RANGED) {
          damage *= 1 + attacker.getStat(StrifeStat.RANGED_PHYSICAL_MULT) / 100;
        }
        return damage;
      case MAGICAL:
        return attacker.getStat(StrifeStat.MAGIC_DAMAGE) * (1 + attacker.getStat(
            StrifeStat.MAGIC_MULT) / 100);
      case FIRE:
        return attacker.getStat(StrifeStat.FIRE_DAMAGE) * (1 + attacker.getStat(
            StrifeStat.ELEMENTAL_MULT) / 100);
      case ICE:
        return attacker.getStat(StrifeStat.ICE_DAMAGE) * (1 + attacker.getStat(
            StrifeStat.ELEMENTAL_MULT) / 100);
      case LIGHTNING:
        return attacker.getStat(StrifeStat.LIGHTNING_DAMAGE) * (1 + attacker.getStat(
            StrifeStat.ELEMENTAL_MULT) / 100);
      case DARK:
        return attacker.getStat(StrifeStat.DARK_DAMAGE) * (1 + attacker.getStat(
            StrifeStat.ELEMENTAL_MULT) / 100);
      case EARTH:
        return attacker.getStat(StrifeStat.EARTH_DAMAGE) * (1 + attacker.getStat(
            StrifeStat.ELEMENTAL_MULT) / 100);
      case LIGHT:
        return attacker.getStat(StrifeStat.LIGHT_DAMAGE) * (1 + attacker.getStat(
            StrifeStat.ELEMENTAL_MULT) / 100);
      case TRUE_DAMAGE:
        return attacker.getStat(StrifeStat.TRUE_DAMAGE);
      default:
        return 0;
    }
  }

  public static float applyDamageScale(StrifeMob caster, StrifeMob target, float amount,
      DamageScale damageScale, DamageType damageType, AttackType attackType) {
    switch (damageScale) {
      case FLAT:
        return amount;
      case CASTER_LEVEL:
        return amount * StatUtil.getMobLevel(caster.getEntity());
      case CASTER_DAMAGE:
        return amount * DamageUtil.getRawDamage(caster, damageType, attackType);
      case TARGET_CURRENT_HEALTH:
        return amount * (float) target.getEntity().getHealth();
      case CASTER_CURRENT_HEALTH:
        return amount * (float) caster.getEntity().getHealth();
      case TARGET_MISSING_HEALTH:
        return amount * (float) (target.getEntity().getMaxHealth() - target.getEntity()
            .getHealth());
      case CASTER_MISSING_HEALTH:
        return amount * (float) (caster.getEntity().getMaxHealth() - caster.getEntity()
            .getHealth());
      case TARGET_MAX_HEALTH:
        return amount * (float) target.getEntity().getMaxHealth();
      case CASTER_MAX_HEALTH:
        return amount * (float) caster.getEntity().getMaxHealth();
      case TARGET_CURRENT_BARRIER:
        return amount * StatUtil.getBarrier(target);
      case CASTER_CURRENT_BARRIER:
        return amount * StatUtil.getBarrier(caster);
      case TARGET_MISSING_BARRIER:
        return amount * (StatUtil.getMaximumBarrier(target) - StatUtil.getBarrier(target));
      case CASTER_MISSING_BARRIER:
        return amount * (StatUtil.getMaximumBarrier(caster) - StatUtil.getBarrier(caster));
      case TARGET_MAX_BARRIER:
        return amount * StatUtil.getMaximumBarrier(target);
      case CASTER_MAX_BARRIER:
        return amount * StatUtil.getMaximumBarrier(caster);
    }
    return amount;
  }

  public static Map<DamageType, Float> buildDamageMap(StrifeMob attacker, AttackType attackType) {
    Map<DamageType, Float> damageMap = new HashMap<>();
    for (DamageType damageType : DMG_TYPES) {
      float amount = getRawDamage(attacker, damageType, attackType);
      if (amount > 0) {
        damageMap.put(damageType, amount);
      }
    }
    return damageMap;
  }

  public static void applyDamageReductions(StrifeMob attacker, StrifeMob defender,
      Map<DamageType, Float> damageMap, Map<AbilityMod, Float> abilityMods) {
    damageMap.replaceAll((t, v) ->
        damageMap.get(t) * getDamageReduction(t, attacker, defender, abilityMods));
  }

  public static void applyAttackTypeMods(StrifeMob attacker, AttackType attackType,
      Map<DamageType, Float> damageMap) {
    if (attackType == AttackType.MELEE && damageMap.containsKey(DamageType.PHYSICAL)) {
      damageMap.put(DamageType.PHYSICAL,
          damageMap.get(DamageType.PHYSICAL) * 1 + attacker.getStat(StrifeStat.MELEE_PHYSICAL_MULT) / 100);
    } else if (attackType == AttackType.RANGED && damageMap.containsKey(DamageType.PHYSICAL)) {
      damageMap.put(DamageType.PHYSICAL,
          damageMap.get(DamageType.PHYSICAL) * 1 + attacker.getStat(StrifeStat.RANGED_PHYSICAL_MULT) / 100);
    }
  }

  public static Set<DamageType> applyElementalEffects(StrifeMob attacker, StrifeMob defender,
      Map<DamageType, Float> damageMap, boolean consumeEarthRunes) {
    Set<DamageType> triggeredElements = new HashSet<>();
    for (DamageType type : damageMap.keySet()) {
      float bonus;
      switch (type) {
        case FIRE:
          bonus = attemptIgnite(damageMap.get(type), attacker, defender.getEntity());
          if (bonus != 0) {
            triggeredElements.add(type);
            damageMap.put(type, damageMap.get(type) + bonus);
          }
          break;
        case ICE:
          bonus = attemptFreeze(damageMap.get(type), attacker, defender.getEntity());
          if (bonus != 0) {
            triggeredElements.add(type);
            damageMap.put(type, damageMap.get(type) + bonus);
          }
          break;
        case LIGHTNING:
          bonus = attemptShock(damageMap.get(type), attacker, defender.getEntity());
          if (bonus != 0) {
            triggeredElements.add(type);
            damageMap.put(type, damageMap.get(type) + bonus);
          }
          break;
        case DARK:
          bonus =
              damageMap.get(type) * getDarknessManager().getCorruptionMult(defender.getEntity());
          boolean corrupt = attemptCorrupt(damageMap.get(type), attacker, defender.getEntity());
          if (corrupt) {
            triggeredElements.add(type);
            damageMap.put(type, damageMap.get(type) + bonus);
          }
          break;
        case EARTH:
          if (!consumeEarthRunes) {
            break;
          }
          bonus = consumeEarthRunes(damageMap.get(type), attacker, defender.getEntity());
          if (bonus != 0) {
            triggeredElements.add(type);
            damageMap.put(type, damageMap.get(type) + bonus);
          }
          break;
        case LIGHT:
          bonus = getLightBonus(damageMap.get(type), attacker, defender.getEntity());
          if (bonus > damageMap.get(type) / 2) {
            triggeredElements.add(type);
            damageMap.put(type, damageMap.get(type) + bonus);
          }
          break;
      }
    }
    return triggeredElements;
  }

  public static float getDamageReduction(DamageType type, StrifeMob attack, StrifeMob defend,
      Map<AbilityMod, Float> modDoubleMap) {
    switch (type) {
      case PHYSICAL:
        float armor = getDefenderArmor(attack, defend);
        armor *= 1 - modDoubleMap.getOrDefault(AbilityMod.ARMOR_PEN_MULT, 0f);
        armor -= modDoubleMap.getOrDefault(AbilityMod.ARMOR_PEN, 0f);
        return getArmorMult(armor);
      case MAGICAL:
        float warding = getDefenderWarding(attack, defend);
        warding *= 1 - modDoubleMap.getOrDefault(AbilityMod.WARD_PEN_MULT, 0f);
        warding -= modDoubleMap.getOrDefault(AbilityMod.WARD_PEN, 0f);
        return getWardingMult(warding);
      case FIRE:
        return 1 - getFireResist(defend) / 100;
      case ICE:
        return 1 - getIceResist(defend) / 100;
      case LIGHTNING:
        return 1 - getLightningResist(defend) / 100;
      case DARK:
        return 1 - getShadowResist(defend) / 100;
      case EARTH:
        return 1 - getEarthResist(defend) / 100;
      case LIGHT:
        return 1 - getLightResist(defend) / 100;
      case TRUE_DAMAGE:
      default:
        return 1;
    }
  }

  public static void forceCustomDamage(LivingEntity attacker, LivingEntity target, double amount) {
    if (target == attacker) {
      if (target.getHealth() > amount) {
        target.setHealth(target.getHealth() - amount);
        return;
      }
      target.damage(100000);
      return;
    }
    int noDamageTicks = target.getNoDamageTicks();
    Vector velocity = target.getVelocity();
    target.setNoDamageTicks(0);

    CombatListener.addAttack(attacker, amount);
    target.damage(amount, attacker);

    target.setNoDamageTicks(noDamageTicks);
    target.setVelocity(velocity);
  }

  public static LivingEntity getAttacker(Entity entity) {
    if (entity instanceof LivingEntity) {
      return (LivingEntity) entity;
    } else if (entity instanceof Projectile) {
      if (((Projectile) entity).getShooter() instanceof LivingEntity) {
        return (LivingEntity) ((Projectile) entity).getShooter();
      }
    } else if (entity instanceof EvokerFangs) {
      return ((EvokerFangs) entity).getOwner();
    }
    return null;
  }

  public static float attemptIgnite(float damage, StrifeMob attacker, LivingEntity defender) {
    if (rollDouble() >= attacker.getStat(StrifeStat.IGNITE_CHANCE) / 100) {
      return 0;
    }
    float bonusDamage = defender.getFireTicks() > 0 ? damage : 1f;
    defender.setFireTicks(Math.max(60 + (int) damage, defender.getFireTicks()));
    defender.getWorld().playSound(defender.getEyeLocation(), Sound.ITEM_FLINTANDSTEEL_USE, 1f, 1f);
    defender.getWorld()
        .spawnParticle(Particle.FLAME, defender.getEyeLocation(), 6 + (int) damage / 2,
            0.3, 0.3, 0.3, 0.03);
    return bonusDamage;
  }

  public static float attemptShock(float damage, StrifeMob attacker, LivingEntity defender) {
    if (rollDouble() >= attacker.getStat(StrifeStat.SHOCK_CHANCE) / 100) {
      return 0;
    }
    float multiplier = 0.5f;
    float percentHealth =
        (float) defender.getHealth() / (float) defender.getAttribute(Attribute.GENERIC_MAX_HEALTH)
            .getValue();
    if (percentHealth < 0.5f) {
      multiplier = 1f / (float) Math.max(0.16, percentHealth * 2);
    }
    double particles = damage * multiplier * 0.5;
    double particleRange = 0.8 + multiplier * 0.2;
    defender.getWorld()
        .playSound(defender.getEyeLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.7f, 2f);
    defender.getWorld()
        .spawnParticle(Particle.CRIT_MAGIC, defender.getEyeLocation(), 10 + (int) particles,
            particleRange, particleRange, particleRange, 0.12);
    if (defender instanceof Creeper) {
      ((Creeper) defender).setPowered(true);
    }
    return damage * multiplier;
  }

  public static float attemptFreeze(float damage, StrifeMob attacker, LivingEntity defender) {
    if (rollDouble() >= attacker.getStat(StrifeStat.FREEZE_CHANCE) / 100) {
      return 0;
    }
    float multiplier = 0.25f + 0.25f * (StatUtil.getHealth(attacker) / 100);
    if (!defender.hasPotionEffect(PotionEffectType.SLOW)) {
      defender.getActivePotionEffects().add(new PotionEffect(PotionEffectType.SLOW, 30, 1));
    }
    defender.getWorld().playSound(defender.getEyeLocation(), Sound.BLOCK_GLASS_BREAK, 1f, 1.0f);
    defender.getWorld()
        .spawnParticle(Particle.SNOWBALL, defender.getEyeLocation(), 4 + (int) damage / 2,
            0.3, 0.3, 0.2, 0.0);
    return damage * multiplier;
  }

  public static float consumeEarthRunes(float damage, StrifeMob attacker, LivingEntity defender) {
    int runes = StrifePlugin.getInstance().getBlockManager().consumeEarthRunes(attacker, defender);
    return damage * 0.5f * runes;
  }

  public static float getLightBonus(float damage, StrifeMob attacker, LivingEntity defender) {
    float light = attacker.getEntity().getLocation().getBlock().getLightLevel();
    float multiplier = (light - 4) / 10;
    if (multiplier >= 0.5) {
      defender.getWorld()
          .playSound(defender.getEyeLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1f, 2f);
      defender.getWorld().spawnParticle(
          Particle.FIREWORKS_SPARK,
          defender.getEyeLocation(),
          (int) (20 * multiplier),
          0.1, 0.1, 0.1,
          0.1
      );
    }
    return damage * multiplier;
  }

  public static boolean attemptCorrupt(float baseDamage, StrifeMob attacker,
      LivingEntity defender) {
    if (rollDouble() >= attacker.getStat(StrifeStat.CORRUPT_CHANCE) / 100) {
      return false;
    }
    applyCorrupt(defender, baseDamage);
    return true;
  }

  public static boolean isCrit(StrifeMob attacker, float aMult, float bonusCrit) {
    float critChance = StatUtil.getCriticalChance(attacker, aMult, bonusCrit);
    return critChance >= rollDouble(hasLuck(attacker.getEntity()));
  }

  public static float getFullEvasionMult(StrifeMob attacker, StrifeMob defender,
      Map<AbilityMod, Float> mods) {

    float totalEvasion = StatUtil.getEvasion(defender);
    float totalAccuracy = StatUtil.getAccuracy(attacker);
    totalAccuracy *= 1 + mods.getOrDefault(AbilityMod.ACCURACY_MULT, 0f) / 100;
    totalAccuracy += mods.getOrDefault(AbilityMod.ACCURACY, 0f);

    float evasionMultiplier = StatUtil.getMinimumEvasionMult(totalEvasion, totalAccuracy);
    evasionMultiplier = evasionMultiplier + (rollDouble() * (1 - evasionMultiplier));

    return evasionMultiplier;
  }

  public static void doEvasion(LivingEntity attacker, LivingEntity defender) {
    callEvadeEvent(defender, attacker);
    defender.getWorld().playSound(defender.getEyeLocation(), Sound.ENTITY_GHAST_SHOOT, 0.5f, 2f);
    if (defender instanceof Player) {
      MessageUtils.sendActionBar((Player) defender, ATTACK_DODGED);
    }
    if (attacker instanceof Player) {
      MessageUtils.sendActionBar((Player) attacker, ATTACK_MISSED);
    }
  }

  public static void doBlock(LivingEntity attacker, LivingEntity defender) {
    callBlockEvent(defender, attacker);
    defender.getWorld().playSound(defender.getEyeLocation(), Sound.ITEM_SHIELD_BLOCK, 1f, 1f);
    String defenderBar = ATTACK_BLOCKED;
    int runes = getBlockManager().getEarthRunes(defender.getUniqueId());
    if (runes > 0) {
      StringBuilder sb = new StringBuilder(defenderBar);
      sb.append(TextUtils.color("&2 "));
      sb.append(IntStream.range(0, runes).mapToObj(i -> "▼").collect(Collectors.joining("")));
      defenderBar = sb.toString();
    }
    if (defender instanceof Player) {
      MessageUtils.sendActionBar((Player) defender, defenderBar);
    }
    if (attacker instanceof Player) {
      MessageUtils.sendActionBar((Player) attacker, ATTACK_BLOCKED);
    }
  }

  public static float getPotionMult(LivingEntity attacker, LivingEntity defender) {
    float potionMult = 1.0f;
    Collection<PotionEffect> attackerEffects = attacker.getActivePotionEffects();
    Collection<PotionEffect> defenderEffects = defender.getActivePotionEffects();
    for (PotionEffect effect : attackerEffects) {
      if (effect.getType().equals(PotionEffectType.INCREASE_DAMAGE)) {
        potionMult += 0.1 * (effect.getAmplifier() + 1);
      } else if (effect.getType().equals(PotionEffectType.WEAKNESS)) {
        potionMult -= 0.1 * (effect.getAmplifier() + 1);
      }
    }

    for (PotionEffect effect : defenderEffects) {
      if (effect.getType().equals(PotionEffectType.WITHER)) {
        potionMult += 0.15 * (effect.getAmplifier() + 1);
      } else if (effect.getType().equals(PotionEffectType.DAMAGE_RESISTANCE)) {
        potionMult -= 0.1 * (effect.getAmplifier() + 1);
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
        return mult;
      }
    }
    return mult;
  }

  public static boolean canAttack(Player attacker, Player defender) {
    CombatListener.addPlayer(attacker);
    defender.damage(0, attacker);
    boolean friendly = CombatListener.hasFriendlyPlayer(attacker);
    CombatListener.removePlayer(attacker);
    return !friendly;
  }

  public static double getProjectileMultiplier(StrifeMob atk, StrifeMob def) {
    return Math.max(0.05D,
        1 + (atk.getStat(StrifeStat.PROJECTILE_DAMAGE) - def.getStat(StrifeStat.PROJECTILE_REDUCTION)) / 100);
  }

  public static void applyLifeSteal(StrifeMob attacker, double damage, double healMultiplier,
      double bonus) {
    double lifeSteal = (attacker.getStat(StrifeStat.LIFE_STEAL) + bonus) / 100;
    restoreHealthWithPenalties(attacker.getEntity(), damage * lifeSteal * healMultiplier);
  }

  public static void applyHealthOnHit(StrifeMob attacker, double attackMultiplier,
      double healMultiplier, double bonus) {
    double health = (attacker.getStat(StrifeStat.HP_ON_HIT) + bonus) * attackMultiplier * healMultiplier;
    restoreHealthWithPenalties(attacker.getEntity(), health);
  }

  public static boolean attemptBleed(StrifeMob attacker, StrifeMob defender, float rawPhysical,
      float attackMult, Map<AbilityMod, Float> abilityMods) {
    if (StrifePlugin.getInstance().getBarrierManager().isBarrierUp(defender)) {
      return false;
    }
    if (defender.getStat(StrifeStat.BLEED_RESIST) > 99) {
      return false;
    }
    float chance = (attacker.getStat(StrifeStat.BLEED_CHANCE) +
        abilityMods.getOrDefault(AbilityMod.BLEED_CHANCE, 0f)) / 100;
    if (chance >= rollDouble()) {
      float damage = rawPhysical * attackMult * BLEED_PERCENT;
      float damageMult = 1 + (attacker.getStat(StrifeStat.BLEED_DAMAGE) +
          abilityMods.getOrDefault(AbilityMod.BLEED_DAMAGE, 0f)) / 100;
      damage *= damageMult;
      damage *= 1 - defender.getStat(StrifeStat.BLEED_RESIST) / 100;
      applyBleed(defender, damage);
    }
    return false;
  }

  public static void applyBleed(StrifeMob defender, float amount) {
    if (amount < 0.1) {
      return;
    }
    StrifePlugin.getInstance().getBleedManager().addBleed(defender, amount);
    defender.getEntity().getWorld()
        .playSound(defender.getEntity().getLocation(), Sound.ENTITY_SHEEP_SHEAR, 1f, 1f);
  }

  public static void applyCorrupt(LivingEntity defender, float amount) {
    StrifePlugin.getInstance().getCorruptionManager().applyCorruption(defender, amount);
    defender.getWorld().playSound(defender.getEyeLocation(), Sound.ENTITY_WITHER_SHOOT, 0.7f, 2f);
    defender.getWorld()
        .spawnParticle(Particle.SMOKE_NORMAL, defender.getEyeLocation(), 10, 0.4, 0.4, 0.5, 0.1);
  }

  public static void doReflectedDamage(StrifeMob defender, StrifeMob attacker,
      AttackType damageType) {
    if (defender.getStat(StrifeStat.DAMAGE_REFLECT) < 0.1) {
      return;
    }
    double reflectDamage = defender.getStat(StrifeStat.DAMAGE_REFLECT);
    reflectDamage = damageType == AttackType.MELEE ? reflectDamage : reflectDamage * 0.6D;
    defender.getEntity().getWorld()
        .playSound(defender.getEntity().getLocation(), Sound.ENCHANT_THORNS_HIT, 0.2f, 1f);
    attacker.getEntity().setHealth(Math.max(0D, attacker.getEntity().getHealth() - reflectDamage));
  }

  public static void applyBuff(LoadedBuff buff, StrifeMob target) {
    applyBuff(buff, target, 1);
  }

  public static void applyBuff(LoadedBuff loadedBuff, StrifeMob target, double durationMult) {
    StrifePlugin.getInstance().getStrifeMobManager()
        .addBuff(target.getEntity().getUniqueId(), loadedBuff, durationMult);
  }

  public static LoadedBuff getBuff(String id) {
    return StrifePlugin.getInstance().getBuffManager().getBuffFromId(id);
  }

  public static void callCritEvent(LivingEntity attacker, LivingEntity victim) {
    CriticalEvent c = new CriticalEvent(attacker, victim);
    Bukkit.getPluginManager().callEvent(c);
  }

  public static void callEvadeEvent(LivingEntity evader, LivingEntity attacker) {
    EvadeEvent ev = new EvadeEvent(evader, attacker);
    Bukkit.getPluginManager().callEvent(ev);
  }

  public static SneakAttackEvent callSneakAttackEvent(Player attacker, LivingEntity victim,
      float sneakSkill, float sneakDamage) {
    SneakAttackEvent sneakAttackEvent = new SneakAttackEvent(attacker, victim, sneakSkill,
        sneakDamage);
    Bukkit.getPluginManager().callEvent(sneakAttackEvent);
    return sneakAttackEvent;
  }

  public static void callBlockEvent(LivingEntity evader, LivingEntity attacker) {
    BlockEvent ev = new BlockEvent(evader, attacker);
    Bukkit.getPluginManager().callEvent(ev);
  }

  public static boolean hasLuck(LivingEntity entity) {
    return entity.hasPotionEffect(PotionEffectType.LUCK);
  }

  public static double applyHealPenalties(LivingEntity entity, double amount) {
    if (entity.hasPotionEffect(PotionEffectType.POISON)) {
      return 0;
    }
    if (amount <= 0 || entity.getHealth() <= 0 || entity.isDead()) {
      return 0;
    }
    if (entity instanceof Player) {
      amount *= Math.min(((Player) entity).getFoodLevel() / 7.0D, 1.0D);
    }
    return amount;
  }

  public static void restoreHealthWithPenalties(LivingEntity entity, double amount) {
    restoreHealth(entity, applyHealPenalties(entity, amount));
  }

  public static void restoreHealth(LivingEntity livingEntity, double amount) {
    if (amount == 0) {
      return;
    }
    livingEntity.setHealth(Math.min(livingEntity.getHealth() + amount,
        livingEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()));
  }

  public static void restoreBarrier(StrifeMob strifeMob, float amount) {
    StrifePlugin.getInstance().getBarrierManager().restoreBarrier(strifeMob, amount);
  }

  public static void applyPotionEffect(LivingEntity entity, PotionEffectType type, int power,
      int duration) {
    if (entity == null || !entity.isValid()) {
      return;
    }
    if (!entity.hasPotionEffect(type)) {
      entity.addPotionEffect(new PotionEffect(type, duration, power));
      return;
    }
    PotionEffect effect = entity.getPotionEffect(type);
    if (power < effect.getAmplifier()) {
      return;
    }
    if (power == Math.abs(effect.getAmplifier()) && duration < effect.getDuration()) {
      return;
    }
    entity.removePotionEffect(type);
    entity.addPotionEffect(new PotionEffect(type, duration, power));
  }

  public static AttackType getAttackType(EntityDamageByEntityEvent event) {
    if (event.getCause() == DamageCause.ENTITY_EXPLOSION) {
      return AttackType.EXPLOSION;
    } else if (event.getDamager() instanceof ShulkerBullet || event
        .getDamager() instanceof SmallFireball || event.getDamager() instanceof WitherSkull || event
        .getDamager() instanceof EvokerFangs) {
      return AttackType.MAGIC;
    } else if (event.getDamager() instanceof Projectile) {
      return AttackType.RANGED;
    }
    return AttackType.MELEE;
  }

  public static void removeDamageModifiers(EntityDamageEvent event) {
    for (DamageModifier modifier : MODIFIERS) {
      if (event.isApplicable(modifier)) {
        event.setDamage(modifier, 0D);
      }
    }
  }

  public static double rollDouble(boolean lucky) {
    return lucky ? Math.max(rollDouble(), rollDouble()) : rollDouble();
  }

  public static float rollDouble() {
    return RANDOM.nextFloat();
  }

  public static boolean rollBool(float chance, boolean lucky) {
    return lucky ? rollBool(chance) || rollBool(chance) : rollBool(chance);
  }

  public static boolean rollBool(float chance) {
    return RANDOM.nextFloat() <= chance;
  }

  private static BlockManager getBlockManager() {
    return StrifePlugin.getInstance().getBlockManager();
  }

  private static CorruptionManager getDarknessManager() {
    return StrifePlugin.getInstance().getCorruptionManager();
  }

  public enum DamageScale {
    FLAT,
    CASTER_LEVEL,
    CASTER_DAMAGE,
    TARGET_CURRENT_HEALTH,
    CASTER_CURRENT_HEALTH,
    TARGET_MISSING_HEALTH,
    CASTER_MISSING_HEALTH,
    TARGET_MAX_HEALTH,
    CASTER_MAX_HEALTH,
    TARGET_CURRENT_BARRIER,
    CASTER_CURRENT_BARRIER,
    TARGET_MISSING_BARRIER,
    CASTER_MISSING_BARRIER,
    TARGET_MAX_BARRIER,
    CASTER_MAX_BARRIER,
  }

  public enum OriginLocation {
    BELOW_HEAD,
    HEAD,
    CENTER,
    GROUND
  }

  public enum DamageType {
    TRUE_DAMAGE,
    PHYSICAL,
    MAGICAL,
    FIRE,
    ICE,
    LIGHTNING,
    EARTH,
    LIGHT,
    DARK
  }

  public enum AbilityMod {
    ACCURACY,
    ACCURACY_MULT,
    ARMOR_PEN,
    ARMOR_PEN_MULT,
    WARD_PEN,
    WARD_PEN_MULT,
    CRITICAL_CHANCE,
    CRITICAL_DAMAGE,
    LIFE_STEAL,
    HEALTH_ON_HIT,
    BLEED_CHANCE,
    BLEED_DAMAGE
  }

  public enum AttackType {
    MELEE, RANGED, MAGIC, EXPLOSION, OTHER
  }
}
