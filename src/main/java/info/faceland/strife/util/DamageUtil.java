package info.faceland.strife.util;

import static info.faceland.strife.stats.StrifeStat.BLEED_CHANCE;
import static info.faceland.strife.stats.StrifeStat.BLEED_DAMAGE;
import static info.faceland.strife.stats.StrifeStat.BLEED_RESIST;
import static info.faceland.strife.stats.StrifeStat.DARK_DAMAGE;
import static info.faceland.strife.stats.StrifeStat.EARTH_DAMAGE;
import static info.faceland.strife.stats.StrifeStat.ELEMENTAL_MULT;
import static info.faceland.strife.stats.StrifeStat.FIRE_DAMAGE;
import static info.faceland.strife.stats.StrifeStat.HP_ON_HIT;
import static info.faceland.strife.stats.StrifeStat.ICE_DAMAGE;
import static info.faceland.strife.stats.StrifeStat.LIGHTNING_DAMAGE;
import static info.faceland.strife.stats.StrifeStat.LIGHT_DAMAGE;
import static info.faceland.strife.stats.StrifeStat.MAGIC_DAMAGE;
import static info.faceland.strife.stats.StrifeStat.MAGIC_MULT;
import static info.faceland.strife.stats.StrifeStat.MELEE_PHYSICAL_MULT;
import static info.faceland.strife.stats.StrifeStat.PHYSICAL_DAMAGE;
import static info.faceland.strife.stats.StrifeStat.PROJECTILE_DAMAGE;
import static info.faceland.strife.stats.StrifeStat.PROJECTILE_REDUCTION;
import static info.faceland.strife.stats.StrifeStat.RANGED_PHYSICAL_MULT;
import static info.faceland.strife.stats.StrifeStat.TRUE_DAMAGE;
import static info.faceland.strife.util.StatUtil.getArmorMult;
import static info.faceland.strife.util.StatUtil.getEarthResist;
import static info.faceland.strife.util.StatUtil.getFireResist;
import static info.faceland.strife.util.StatUtil.getIceResist;
import static info.faceland.strife.util.StatUtil.getLightResist;
import static info.faceland.strife.util.StatUtil.getLightningResist;
import static info.faceland.strife.util.StatUtil.getShadowResist;
import static info.faceland.strife.util.StatUtil.getWardingMult;

import com.tealcube.minecraft.bukkit.TextUtils;
import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;
import info.faceland.strife.StrifePlugin;
import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.data.buff.LoadedBuff;
import info.faceland.strife.events.BlockEvent;
import info.faceland.strife.events.CriticalEvent;
import info.faceland.strife.events.EvadeEvent;
import info.faceland.strife.events.SneakAttackEvent;
import info.faceland.strife.listeners.combat.CombatListener;
import info.faceland.strife.managers.BlockManager;
import info.faceland.strife.managers.DarknessManager;
import info.faceland.strife.stats.StrifeStat;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EvokerFangs;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.ShulkerBullet;
import org.bukkit.entity.SmallFireball;
import org.bukkit.entity.WitherSkull;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier;
import org.bukkit.material.MaterialData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.BlockIterator;

public class DamageUtil {

  private static final String ATTACK_MISSED = TextUtils.color("&f&lMiss!");
  private static final String ATTACK_BLOCKED = TextUtils.color("&f&lBlocked!");
  private static final String ATTACK_DODGED = TextUtils.color("&f&lDodge!");
  private static final Random RANDOM = new Random(System.currentTimeMillis());
  private static final DamageModifier[] MODIFIERS = EntityDamageEvent.DamageModifier.values();
  private static final DamageType[] DMG_TYPES = DamageType.values();

  private static final double BLEED_PERCENT = 0.5;

  public static double dealDirectDamage(StrifeMob attacker, StrifeMob defender, double damage) {
    damage = StrifePlugin.getInstance().getBarrierManager().damageBarrier(defender, damage);
    forceCustomDamage(attacker.getEntity(), defender.getEntity(), damage);
    return damage;
  }

  public static double getRawDamage(StrifeMob attacker, DamageType damageType) {
    switch (damageType) {
      case PHYSICAL:
        return attacker.getStat(PHYSICAL_DAMAGE);
      case MAGICAL:
        return attacker.getStat(MAGIC_DAMAGE) * (1 + attacker.getStat(MAGIC_MULT) / 100);
      case FIRE:
        return attacker.getStat(FIRE_DAMAGE) * (1 + attacker.getStat(ELEMENTAL_MULT) / 100);
      case ICE:
        return attacker.getStat(ICE_DAMAGE) * (1 + attacker.getStat(ELEMENTAL_MULT) / 100);
      case LIGHTNING:
        return attacker.getStat(LIGHTNING_DAMAGE) * (1 + attacker.getStat(ELEMENTAL_MULT) / 100);
      case DARK:
        return attacker.getStat(DARK_DAMAGE) * (1 + attacker.getStat(ELEMENTAL_MULT) / 100);
      case EARTH:
        return attacker.getStat(EARTH_DAMAGE) * (1 + attacker.getStat(ELEMENTAL_MULT) / 100);
      case LIGHT:
        return attacker.getStat(LIGHT_DAMAGE) * (1 + attacker.getStat(ELEMENTAL_MULT) / 100);
      case TRUE_DAMAGE:
        return attacker.getStat(TRUE_DAMAGE);
      default:
        return 0;
    }
  }

  public static Map<DamageType, Double> buildDamageMap(StrifeMob attacker) {
    Map<DamageType, Double> damageMap = new HashMap<>();
    for (DamageType damageType : DMG_TYPES) {
      double amount = getRawDamage(attacker, damageType);
      if (amount > 0) {
        damageMap.put(damageType, getRawDamage(attacker, damageType));
      }
    }
    return damageMap;
  }

  public static void applyApplicableDamageReductions(StrifeMob attacker, StrifeMob defender,
      Map<DamageType, Double> damageMap) {
    for (DamageType type : damageMap.keySet()) {
      damageMap.put(type, damageMap.get(type) * getDamageReduction(type, attacker, defender));
    }
  }

  public static void applyAttackTypeMods(StrifeMob attacker, AttackType attackType,
      Map<DamageType, Double> damageMap) {
    if (attackType == AttackType.MELEE && damageMap.containsKey(DamageType.PHYSICAL)) {
      damageMap.put(DamageType.PHYSICAL,
          damageMap.get(DamageType.PHYSICAL) * 1 + attacker.getStat(MELEE_PHYSICAL_MULT) / 100);
    } else if (attackType == AttackType.RANGED && damageMap.containsKey(DamageType.PHYSICAL)) {
      damageMap.put(DamageType.PHYSICAL,
          damageMap.get(DamageType.PHYSICAL) * 1 + attacker.getStat(RANGED_PHYSICAL_MULT) / 100);
    }
  }

  public static Set<DamageType> applyElementalEffects(StrifeMob attacker, StrifeMob defender,
      Map<DamageType, Double> damageMap) {
    Set<DamageType> triggeredElements = new HashSet<>();
    for (DamageType type : damageMap.keySet()) {
      double bonus;
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

  public static double getDamageReduction(DamageType type, StrifeMob attack, StrifeMob defend) {
    switch (type) {
      case PHYSICAL:
        return getArmorMult(attack, defend);
      case MAGICAL:
        return getWardingMult(attack, defend);
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
    target.setNoDamageTicks(0);
    CombatListener.addAttack(attacker, amount);
    target.damage(amount, attacker);
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

  public static double attemptIgnite(double damage, StrifeMob attacker,
      LivingEntity defender) {
    if (rollDouble() >= attacker.getStat(StrifeStat.IGNITE_CHANCE) / 100) {
      return 0D;
    }
    double bonusDamage = defender.getFireTicks() > 0 ? damage : 1D;
    defender.setFireTicks(Math.max(60 + (int) damage, defender.getFireTicks()));
    defender.getWorld().playSound(defender.getEyeLocation(), Sound.ITEM_FLINTANDSTEEL_USE, 1f, 1f);
    defender.getWorld()
        .spawnParticle(Particle.FLAME, defender.getEyeLocation(), 6 + (int) damage / 2,
            0.3, 0.3, 0.3, 0.03);
    return bonusDamage;
  }

  public static double attemptShock(double damage, StrifeMob attacker,
      LivingEntity defender) {
    if (rollDouble() >= attacker.getStat(StrifeStat.SHOCK_CHANCE) / 100) {
      return 0D;
    }
    double multiplier = 0.5;
    double percentHealth =
        defender.getHealth() / defender.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
    if (percentHealth < 0.5) {
      multiplier = 1 / Math.max(0.16, percentHealth * 2);
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

  public static double attemptFreeze(double damage, StrifeMob attacker,
      LivingEntity defender) {
    if (rollDouble() >= attacker.getStat(StrifeStat.FREEZE_CHANCE) / 100) {
      return 0D;
    }
    double multiplier = 0.25 + 0.25 * (StatUtil.getHealth(attacker) / 100);
    if (!defender.hasPotionEffect(PotionEffectType.SLOW)) {
      defender.getActivePotionEffects().add(new PotionEffect(PotionEffectType.SLOW, 30, 1));
    }
    defender.getWorld().playSound(defender.getEyeLocation(), Sound.BLOCK_GLASS_BREAK, 1f, 1.0f);
    defender.getWorld()
        .spawnParticle(Particle.SNOWBALL, defender.getEyeLocation(), 4 + (int) damage / 2,
            0.3, 0.3, 0.2, 0.0);
    return damage * multiplier;
  }

  public static double consumeEarthRunes(double damage, StrifeMob attacker, LivingEntity defender) {
    int runes = getBlockManager().getEarthRunes(attacker.getEntity().getUniqueId());
    getBlockManager().setEarthRunes(attacker.getEntity().getUniqueId(), 0);
    if (runes == 0) {
      return 0;
    }
    defender.getWorld().playSound(defender.getEyeLocation(), Sound.BLOCK_GRASS_BREAK, 1f, 0.8f);
    defender.getWorld().spawnParticle(
        Particle.BLOCK_CRACK,
        defender.getEyeLocation().clone().add(0, -0.7, 0),
        20,
        0.0, 0.0, 0.0,
        new MaterialData(Material.DIRT)
    );
    return damage * 0.5 * runes;
  }

  public static double getLightBonus(double damage, StrifeMob attacker,
      LivingEntity defender) {
    double light = attacker.getEntity().getLocation().getBlock().getLightLevel();
    double multiplier = (light - 4) / 10;
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

  public static boolean attemptCorrupt(double baseDamage, StrifeMob attacker,
      LivingEntity defender) {
    if (rollDouble() >= attacker.getStat(StrifeStat.CORRUPT_CHANCE) / 100) {
      return false;
    }
    applyCorrupt(defender, baseDamage);
    return true;
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

  public static double getPotionMult(LivingEntity attacker, LivingEntity defender) {
    double potionMult = 1.0;
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
        continue;
      }
    }
    return mult;
  }

  public static boolean canAttack(Player attacker, Player defender) {
    EntityDamageByEntityEvent event = new EntityDamageByEntityEvent(attacker, defender,
        DamageCause.ENTITY_ATTACK, 0);
    Bukkit.getPluginManager().callEvent(event);
    return !event.isCancelled();
  }

  public static double getProjectileMultiplier(StrifeMob atk, StrifeMob def) {
    return Math.max(0.05D,
        1 + (atk.getStat(PROJECTILE_DAMAGE) - def.getStat(PROJECTILE_REDUCTION)) / 100);
  }

  public static void applyLifeSteal(StrifeMob attacker, double damage, double healMultiplier) {
    double lifeSteal = StatUtil.getLifestealPercentage(attacker);
    restoreHealthWithPenalties(attacker.getEntity(), damage * lifeSteal * healMultiplier);
  }

  public static void applyHealthOnHit(StrifeMob attacker, double attackMultiplier,
      double healMultiplier) {
    double health = attacker.getStat(HP_ON_HIT) * attackMultiplier * healMultiplier;
    restoreHealthWithPenalties(attacker.getEntity(), health);
  }

  public static boolean attemptBleed(StrifeMob attacker, StrifeMob defender,
      double damage, double critMult, double attackMult) {
    if (StrifePlugin.getInstance().getBarrierManager().isBarrierUp(defender)) {
      return false;
    }
    if (defender.getStat(BLEED_RESIST) > 99) {
      return false;
    }
    if (attackMult * (attacker.getStat(BLEED_CHANCE) / 100) >= rollDouble()) {
      double amount = damage + damage * critMult;
      amount *= 1 + attacker.getStat(BLEED_DAMAGE) / 100;
      amount *= 1 - defender.getStat(BLEED_RESIST) / 100;
      amount *= BLEED_PERCENT;
      applyBleed(defender.getEntity(), amount);
      return true;
    }
    return false;
  }

  public static void applyBleed(LivingEntity defender, double amount) {
    if (amount < 0.5) {
      return;
    }
    StrifePlugin.getInstance().getBleedManager().applyBleed(defender, amount);
    defender.getWorld().playSound(defender.getEyeLocation(), Sound.ENTITY_SHEEP_SHEAR, 1f, 1f);
  }

  public static void applyCorrupt(LivingEntity defender, double amount) {
    StrifePlugin.getInstance().getDarknessManager().applyCorruptionStacks(defender, amount);
    defender.getWorld().playSound(defender.getEyeLocation(), Sound.ENTITY_WITHER_SHOOT, 0.7f, 2f);
    defender.getWorld()
        .spawnParticle(Particle.SMOKE_NORMAL, defender.getEyeLocation(), 10, 0.4, 0.4, 0.5, 0.1);
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

  public static void restoreBarrier(StrifeMob strifeMob, double amount) {
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

  public static Set<LivingEntity> getLOSEntitiesAroundLocation(Location loc, double radius) {
    ArmorStand stando = buildAndRemoveDetectionStand(loc);
    Collection<Entity> targetList = loc.getWorld().getNearbyEntities(loc, radius, radius, radius);
    Set<LivingEntity> validTargets = new HashSet<>();
    for (Entity e : targetList) {
      if (e instanceof LivingEntity && stando.hasLineOfSight(e)) {
        validTargets.add((LivingEntity) e);
      }
    }
    return validTargets;
  }

  public static ArmorStand buildAndRemoveDetectionStand(Location location) {
    ArmorStand stando = location.getWorld().spawn(location, ArmorStand.class,
        e -> e.setVisible(false));
    stando.setSmall(true);
    Bukkit.getScheduler().runTaskLater(StrifePlugin.getInstance(), stando::remove, 1L);
    return stando;
  }

  public static LivingEntity getFirstEntityInLOS(LivingEntity le, int range) {
    List<Entity> targetList = le.getNearbyEntities(range + 1, range + 1, range + 1);
    BlockIterator bi = new BlockIterator(le.getEyeLocation(), 0, range);
    while (bi.hasNext()) {
      Block b = bi.next();
      double bx = b.getX() + 0.5;
      double by = b.getY() + 0.5;
      double bz = b.getZ() + 0.5;
      if (b.getType().isSolid()) {
        break;
      }
      for (Entity e : targetList) {
        if (!(e instanceof LivingEntity)) {
          continue;
        }
        if (!e.isValid()) {
          continue;
        }
        Location l = e.getLocation();
        double ex = l.getX();
        double ey = l.getY();
        double ez = l.getZ();
        if (Math.abs(bx - ex) < 0.5 && Math.abs(bz - ez) < 0.5 && Math.abs(by - ey) < 2.5) {
          return (LivingEntity) e;
        }
      }
    }
    return null;
  }

  public static LivingEntity selectFirstEntityInSight(LivingEntity caster, double range) {
    if (caster instanceof Mob && ((Mob) caster).getTarget() != null) {
      return ((Mob) caster).getTarget();
    }
    return DamageUtil.getFirstEntityInLOS(caster, (int) range);
  }

  public static Location getTargetArea(LivingEntity caster, LivingEntity target, double range,
      OriginLocation originLocation) {
    if (target == null) {
      target = selectFirstEntityInSight(caster, range);
    }
    if (target != null) {
      return getOriginLocation(target, originLocation);
    }
    return getTargetLocation(caster, range);
  }

  public static Location getTargetArea(LivingEntity caster, LivingEntity target, double range) {
    return getTargetArea(caster, target, range, OriginLocation.CENTER);
  }

  private static Location getTargetLocation(LivingEntity caster, double range) {
    BlockIterator bi = new BlockIterator(caster.getEyeLocation(), 0, (int) range + 1);
    Block sightBlock = null;
    while (bi.hasNext()) {
      Block b = bi.next();
      if (b.getType().isSolid()) {
        sightBlock = b;
        break;
      }
    }
    if (sightBlock == null) {
      LogUtil.printDebug(" - Using MAX DISTANCE target location calculation");
      return caster.getEyeLocation().clone().add(
          caster.getEyeLocation().getDirection().multiply(range));
    }
    LogUtil.printDebug(" - Using TARGET BLOCK target location calculation");
    double dist = sightBlock.getLocation().add(0.5, 0.5, 0.5).distance(caster.getEyeLocation());
    return caster.getEyeLocation().add(
        caster.getEyeLocation().getDirection().multiply(Math.max(0, dist - 1)));
  }

  public static Location getOriginLocation(LivingEntity le, OriginLocation origin) {
    switch (origin) {
      case HEAD:
        return le.getEyeLocation();
      case CENTER:
        return le.getEyeLocation().clone()
            .subtract(le.getEyeLocation().clone().subtract(le.getLocation()).multiply(0.5));
      case GROUND:
      default:
        return le.getLocation();
    }
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

  private static BlockManager getBlockManager() {
    return StrifePlugin.getInstance().getBlockManager();
  }

  private static DarknessManager getDarknessManager() {
    return StrifePlugin.getInstance().getDarknessManager();
  }

  public enum OriginLocation {
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

  public enum AttackType {
    MELEE, RANGED, MAGIC, EXPLOSION, OTHER
  }
}
