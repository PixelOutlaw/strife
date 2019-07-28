package info.faceland.strife.managers;

import com.tealcube.minecraft.bukkit.TextUtils;
import info.faceland.strife.conditions.AttributeCondition;
import info.faceland.strife.conditions.BarrierCondition;
import info.faceland.strife.conditions.BleedingCondition;
import info.faceland.strife.conditions.BonusLevelCondition;
import info.faceland.strife.conditions.BurningCondition;
import info.faceland.strife.conditions.ChanceCondition;
import info.faceland.strife.conditions.Condition;
import info.faceland.strife.conditions.Condition.CompareTarget;
import info.faceland.strife.conditions.Condition.Comparison;
import info.faceland.strife.conditions.Condition.ConditionType;
import info.faceland.strife.conditions.CorruptionCondition;
import info.faceland.strife.conditions.EntityTypeCondition;
import info.faceland.strife.conditions.EquipmentCondition;
import info.faceland.strife.conditions.GroundedCondition;
import info.faceland.strife.conditions.HealthCondition;
import info.faceland.strife.conditions.HeightCondition;
import info.faceland.strife.conditions.LevelCondition;
import info.faceland.strife.conditions.PotionCondition;
import info.faceland.strife.conditions.StatCondition;
import info.faceland.strife.conditions.TimeCondition;
import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.data.WorldSpaceEffectEntity;
import info.faceland.strife.data.champion.StrifeAttribute;
import info.faceland.strife.effects.Bleed;
import info.faceland.strife.effects.BuffEffect;
import info.faceland.strife.effects.ConsumeBleed;
import info.faceland.strife.effects.ConsumeCorrupt;
import info.faceland.strife.effects.Corrupt;
import info.faceland.strife.effects.CreateWorldSpaceEntity;
import info.faceland.strife.effects.DealDamage;
import info.faceland.strife.effects.DealDamage.DamageScale;
import info.faceland.strife.effects.Effect;
import info.faceland.strife.effects.ForceTarget;
import info.faceland.strife.effects.Heal;
import info.faceland.strife.effects.Ignite;
import info.faceland.strife.effects.IncreaseRage;
import info.faceland.strife.effects.Knockback;
import info.faceland.strife.effects.Leap;
import info.faceland.strife.effects.PlaySound;
import info.faceland.strife.effects.PotionEffectAction;
import info.faceland.strife.effects.RestoreBarrier;
import info.faceland.strife.effects.ShootProjectile;
import info.faceland.strife.effects.SpawnParticle;
import info.faceland.strife.effects.SpawnParticle.ParticleStyle;
import info.faceland.strife.effects.Speak;
import info.faceland.strife.effects.StandardDamage;
import info.faceland.strife.effects.Summon;
import info.faceland.strife.effects.Wait;
import info.faceland.strife.stats.StrifeStat;
import info.faceland.strife.util.DamageUtil;
import info.faceland.strife.util.DamageUtil.AttackType;
import info.faceland.strife.util.DamageUtil.DamageType;
import info.faceland.strife.util.DamageUtil.OriginLocation;
import info.faceland.strife.util.LogUtil;
import info.faceland.strife.util.PlayerDataUtil;
import info.faceland.strife.util.StatUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

public class EffectManager {

  private final StrifeAttributeManager strifeAttributeManager;
  private final StrifeMobManager aeManager;
  private final Map<String, Effect> loadedEffects;
  private final Map<String, Condition> conditions;
  private final Set<WorldSpaceEffectEntity> worldSpaceEffects;

  public EffectManager(StrifeAttributeManager strifeAttributeManager, StrifeMobManager aeManager) {
    this.strifeAttributeManager = strifeAttributeManager;
    this.aeManager = aeManager;
    this.loadedEffects = new HashMap<>();
    this.conditions = new HashMap<>();
    this.worldSpaceEffects = new HashSet<>();
  }

  public void execute(Effect effect, StrifeMob caster, Set<LivingEntity> targets) {
    applyEffectToTargets(effect, caster, targets);
  }

  public void execute(Effect effect, StrifeMob caster, LivingEntity target) {
    Set<LivingEntity> targets = new HashSet<>();
    targets.add(target);
    applyEffectToTargets(effect, caster, targets);
  }

  private void applyEffectToTargets(Effect effect, StrifeMob caster, Set<LivingEntity> targets) {
    if (targets == null) {
      if (PlayerDataUtil.areConditionsMet(caster, null, effect.getConditions())) {
        effect.apply(caster, null);
        return;
      }
      LogUtil.printDebug(" - Condition not met! Aborting...");
      return;
    }

    Set<LivingEntity> finalTargets = buildValidTargets(effect, caster, targets);

    for (LivingEntity le : finalTargets) {
      StrifeMob targetMob = aeManager.getStatMob(le);
      LogUtil.printDebug(" - Applying effect to " + PlayerDataUtil.getName(le));
      if (!PlayerDataUtil.areConditionsMet(caster, targetMob, effect.getConditions())) {
        LogUtil.printDebug(" - Condition not met! Continuing...");
        continue;
      }
      effect.apply(caster, targetMob);
    }
  }

  private Set<LivingEntity> buildValidTargets(Effect effect, StrifeMob caster,
      Set<LivingEntity> targets) {

    Set<LivingEntity> finalTargets = new HashSet<>();
    for (LivingEntity le : targets) {
      finalTargets.addAll(getEffectTargets(caster.getEntity(), le, effect.getRange()));
    }
    Set<LivingEntity> newTargets = new HashSet<>(finalTargets);
    Set<LivingEntity> friendlyEntities = getFriendlyEntities(caster, finalTargets);
    if (effect.isFriendly()) {
      newTargets.retainAll(friendlyEntities);
    } else {
      newTargets.removeAll(friendlyEntities);
    }
    return newTargets;
  }

  private Set<LivingEntity> getFriendlyEntities(StrifeMob caster, Set<LivingEntity> targets) {
    Set<LivingEntity> friendlyEntities = new HashSet<>();
    friendlyEntities.add(caster.getEntity());
    for (StrifeMob mob : caster.getMinions()) {
      friendlyEntities.add(mob.getEntity());
    }
    // for (StrifeMob mob : getPartyMembers {
    // }
    for (LivingEntity target : targets) {
      if (caster.getEntity() == target) {
        continue;
      }
      if (caster.getEntity() instanceof Player && target instanceof Player) {
        if (DamageUtil.canAttack((Player) caster.getEntity(), (Player) target)) {
          continue;
        }
        friendlyEntities.add(target);
      }
    }
    return friendlyEntities;
  }

  public void addWorldSpaceEffectEntity(WorldSpaceEffectEntity worldSpaceEffectEntity) {
    LogUtil.printDebug(" - Added worldspace entity to effect manager");
    worldSpaceEffects.add(worldSpaceEffectEntity);
  }

  public void tickAllWorldSpaceEffects() {
    List<WorldSpaceEffectEntity> expiredEffects = new ArrayList<>();
    for (WorldSpaceEffectEntity effect : worldSpaceEffects) {
      boolean isAlive = effect.tick();
      if (!isAlive) {
        expiredEffects.add(effect);
      }
    }
    for (WorldSpaceEffectEntity effect : expiredEffects) {
      LogUtil.printDebug(" - Remove expired worldspace entity from effect manager");
      worldSpaceEffects.remove(effect);
    }
  }

  private Set<LivingEntity> getEffectTargets(LivingEntity caster, LivingEntity target,
      double range) {
    Set<LivingEntity> targets = new HashSet<>();
    if (target == null) {
      return targets;
    }
    if (range < 1) {
      targets.add(target);
      return targets;
    }
    for (Entity e : target.getNearbyEntities(range, range, range)) {
      if (e instanceof ArmorStand) {
        continue;
      }
      if (e instanceof LivingEntity && target.hasLineOfSight(e)) {
        targets.add((LivingEntity) e);
      }
    }
    targets.remove(caster);
    return targets;
  }

  public void loadEffect(String key, ConfigurationSection cs) {
    String type = cs.getString("type", "NULL").toUpperCase();
    EffectType effectType;
    try {
      effectType = EffectType.valueOf(type);
    } catch (Exception e) {
      LogUtil.printError("Skipping effect " + key + " for invalid effect type");
      return;
    }
    Effect effect = null;
    switch (effectType) {
      case HEAL:
        effect = new Heal();
        ((Heal) effect).setAmount(cs.getDouble("amount", 1));
        ((Heal) effect).setDamageScale(DamageScale.valueOf(cs.getString("scale", "FLAT")));
        break;
      case RESTORE_BARRIER:
        effect = new RestoreBarrier();
        ((RestoreBarrier) effect).setAmount(cs.getDouble("amount", 1));
        ((RestoreBarrier) effect)
            .setDamageScale(DamageScale.valueOf(cs.getString("scale", "FLAT")));
        break;
      case INCREASE_RAGE:
        effect = new IncreaseRage();
        ((IncreaseRage) effect).setAmount(cs.getDouble("amount", 1));
        break;
      case DAMAGE:
        effect = new DealDamage();
        ((DealDamage) effect).setAmount(cs.getDouble("amount", 1));
        ((DealDamage) effect).setFlatBonus(cs.getDouble("flat-bonus", 0));
        try {
          ((DealDamage) effect).setDamageScale(DamageScale.valueOf(cs.getString("scale", "FLAT")));
          ((DealDamage) effect)
              .setDamageType(DamageType.valueOf(cs.getString("damage-type", "TRUE")));
        } catch (Exception e) {
          LogUtil.printError("Skipping effect " + key + " for invalid damage scale/type");
          return;
        }
        break;
      case STANDARD_DAMAGE:
        effect = new StandardDamage();
        ((StandardDamage) effect).setAttackMultiplier(cs.getDouble("attack-multiplier", 1D));
        ((StandardDamage) effect).setAttackType(AttackType.valueOf(cs.getString("attack-type")));
        ConfigurationSection modCs = cs.getConfigurationSection("mods");
        Map<DamageType, Double> modMap = new HashMap<>();
        for (String k : modCs.getKeys(false)) {
          DamageType mod = DamageType.valueOf(k);
          modMap.put(mod, modCs.getDouble(k));
        }
        ConfigurationSection flatCs = cs.getConfigurationSection("flat-damage-bonuses");
        Map<DamageType, Double> flatMap = new HashMap<>();
        for (String k : flatCs.getKeys(false)) {
          DamageType mod = DamageType.valueOf(k);
          flatMap.put(mod, flatCs.getDouble(k));
        }
        ((StandardDamage) effect).getDamageModifiers().putAll(modMap);
        ((StandardDamage) effect).getDamageBonuses().putAll(flatMap);
        break;
      case WORLD_SPACE_ENTITY:
        effect = new CreateWorldSpaceEntity();
        Map<Integer, List<String>> effectSchedule = new HashMap<>();
        ConfigurationSection scheduleSection = cs.getConfigurationSection("schedule");
        for (String intKey : scheduleSection.getKeys(false)) {
          int val = Integer.valueOf(intKey);
          List<String> effects = scheduleSection.getStringList(intKey);
          effectSchedule.put(val, effects);
        }
        ((CreateWorldSpaceEntity) effect).setEffectSchedule(effectSchedule);
        ((CreateWorldSpaceEntity) effect).setMaxTicks(cs.getInt("refresh-delay", 5));
        ((CreateWorldSpaceEntity) effect).setLifespan(cs.getInt("life-span", 10));
        ((CreateWorldSpaceEntity) effect).setVelocity(cs.getDouble("velocity", 1));
        break;
      case PROJECTILE:
        effect = new ShootProjectile();
        ((ShootProjectile) effect).setQuantity(cs.getInt("quantity", 1));
        EntityType projType;
        try {
          projType = EntityType.valueOf(cs.getString("projectile-type", "null"));
        } catch (Exception e) {
          LogUtil.printError("Skipping effect " + key + " for invalid projectile type");
          return;
        }
        if (!(projType == EntityType.ARROW || projType == EntityType.THROWN_EXP_BOTTLE
            || projType == EntityType.SPLASH_POTION || projType == EntityType.WITHER_SKULL
            || projType == EntityType.SHULKER_BULLET || projType == EntityType.PRIMED_TNT
            || projType == EntityType.EGG || projType == EntityType.SNOWBALL
            || projType == EntityType.FIREBALL || projType == EntityType.DRAGON_FIREBALL
            || projType == EntityType.SMALL_FIREBALL)) {
          LogUtil.printWarning("Skipping effect " + key + " for non projectile entity");
          return;
        }
        ((ShootProjectile) effect).setProjectileEntity(projType);
        ((ShootProjectile) effect).setVerticalBonus(cs.getDouble("vertical-bonus", 0));
        ((ShootProjectile) effect).setSpread(cs.getDouble("spread", 0));
        ((ShootProjectile) effect).setSpeed(cs.getDouble("speed", 1));
        ((ShootProjectile) effect).setYield((float) cs.getDouble("yield", 0.0D));
        ((ShootProjectile) effect).setIgnite(cs.getBoolean("ignite", false));
        ((ShootProjectile) effect).setIgnite(cs.getBoolean("bounce", false));
        ((ShootProjectile) effect).setHitEffects(cs.getStringList("hit-effects"));
        ((ShootProjectile) effect).setTargeted(cs.getBoolean("targeted", false));
        ((ShootProjectile) effect).setSeeking(cs.getBoolean("seeking", false));
        break;
      case IGNITE:
        effect = new Ignite();
        ((Ignite) effect).setDuration(cs.getInt("duration", 20));
        break;
      case BLEED:
        effect = new Bleed();
        ((Bleed) effect).setAmount(cs.getInt("amount", 10));
        ((Bleed) effect).setIgnoreArmor(cs.getBoolean("ignore-armor", true));
        ((Bleed) effect).setApplyBleedMods(cs.getBoolean("apply-bleed-mods", true));
        break;
      case CORRUPT:
        effect = new Corrupt();
        ((Corrupt) effect).setAmount(cs.getInt("amount", 10));
        break;
      case CONSUME_BLEED:
        effect = new ConsumeBleed();
        ((ConsumeBleed) effect).setDamageRatio(cs.getDouble("damage-ratio", 1));
        ((ConsumeBleed) effect).setHealRatio(cs.getDouble("heal-ratio", 1));
        break;
      case CONSUME_CORRUPT:
        effect = new ConsumeCorrupt();
        ((ConsumeCorrupt) effect).setDamageRatio(cs.getDouble("damage-ratio", 1));
        ((ConsumeCorrupt) effect).setHealRatio(cs.getDouble("heal-ratio", 1));
        break;
      case BUFF_EFFECT:
        effect = new BuffEffect();
        ((BuffEffect) effect).setLoadedBuff(cs.getString("buff-id"));
        ((BuffEffect) effect).setStrictDuration(cs.getBoolean("strict-duration", false));
        break;
      case WAIT:
        effect = new Wait();
        ((Wait) effect).setTickDelay(cs.getInt("duration", 20));
        break;
      case SPEAK:
        effect = new Speak();
        ((Speak) effect).setMessages(
            TextUtils.color(cs.getStringList("messages")));
        break;
      case KNOCKBACK:
        effect = new Knockback();
        ((Knockback) effect).setPower(cs.getDouble("power", 10));
        ((Knockback) effect).setHeight(cs.getDouble("height", 10));
        ((Knockback) effect).setZeroVelocity(cs.getBoolean("zero-velocity", false));
        break;
      case LEAP:
        effect = new Leap();
        ((Leap) effect).setForward(cs.getDouble("forward", 10));
        ((Leap) effect).setHeight(cs.getDouble("height", 10));
        ((Leap) effect).setZeroVelocity(cs.getBoolean("zero-velocity", false));
        break;
      case SUMMON:
        effect = new Summon();
        ((Summon) effect).setAmount(cs.getInt("amount", 1));
        ((Summon) effect).setUniqueEntity(cs.getString("unique-entity"));
        ((Summon) effect).setLifespanSeconds(cs.getInt("lifespan-seconds", 30));
        ((Summon) effect).setSoundEffect(cs.getString("sound-effect-id", null));
        break;
      case TARGET:
        effect = new ForceTarget();
        ((ForceTarget) effect).setOverwrite(cs.getBoolean("overwrite"));
        break;
      case POTION:
        effect = new PotionEffectAction();
        PotionEffectType potionType;
        try {
          potionType = PotionEffectType.getByName(cs.getString("effect"));
        } catch (Exception e) {
          LogUtil.printWarning("Invalid potion effect type in effect " + key + ". Skipping.");
          return;
        }
        ((PotionEffectAction) effect).setPotionEffectType(potionType);
        ((PotionEffectAction) effect).setIntensity(cs.getInt("intensity", 0));
        ((PotionEffectAction) effect).setDuration(cs.getInt("duration", 0));
        ((PotionEffectAction) effect).setStrictDuration(cs.getBoolean("strict-duration", false));
        break;
      case SOUND:
        effect = new PlaySound();
        Sound sound;
        try {
          sound = Sound.valueOf((cs.getString("sound-type")));
        } catch (Exception e) {
          LogUtil.printWarning("Invalid sound effect type in effect " + key + ". Skipping.");
          return;
        }
        ((PlaySound) effect).setSound(sound);
        ((PlaySound) effect).setVolume((float) cs.getDouble("volume", 1));
        ((PlaySound) effect).setPitch((float) cs.getDouble("pitch", 1));
        break;
      case PARTICLE:
        effect = new SpawnParticle();
        Particle particle;
        try {
          particle = Particle.valueOf((cs.getString("particle-type")));
        } catch (Exception e) {
          LogUtil.printWarning("Invalid particle effect type in effect " + key + ". Skipping.");
          return;
        }
        ((SpawnParticle) effect).setParticle(particle);
        ((SpawnParticle) effect).setQuantity(cs.getInt("quantity", 10));
        ((SpawnParticle) effect).setTickDuration(cs.getInt("duration-ticks", 0));
        ((SpawnParticle) effect).setSpeed((float) cs.getDouble("speed", 0));
        ((SpawnParticle) effect).setSpread((float) cs.getDouble("spread", 1));
        ((SpawnParticle) effect).setParticleOriginLocation(
            OriginLocation.valueOf(cs.getString("origin", "HEAD")));
        ((SpawnParticle) effect).setStyle(ParticleStyle.valueOf(cs.getString("style", "NORMAL")));
        ((SpawnParticle) effect).setSize(cs.getDouble("size", 1));
        break;
    }
    if (effectType != EffectType.WAIT) {
      effect.setName(TextUtils.color(cs.getString("name", "&8Unnamed Effect")));
      effect.setRange(cs.getDouble("range", 0));
      effect.setForceTargetCaster(cs.getBoolean("force-target-caster", false));
      effect.setFriendly(cs.getBoolean("friendly", false));
      Map<StrifeStat, Double> statMults = StatUtil
          .getStatMapFromSection(cs.getConfigurationSection("stat-mults"));
      effect.setStatMults(statMults);
    } else {
      effect.setName("wait");
    }
    List<String> conditionStrings = cs.getStringList("conditions");
    for (String s : conditionStrings) {
      Condition condition = conditions.get(s);
      if (condition == null) {
        LogUtil.printWarning("Invalid conditions " + s + " for effect " + key + ". Skipping.");
        continue;
      }
      effect.addCondition(conditions.get(s));
    }
    loadedEffects.put(key, effect);
    LogUtil.printInfo("Loaded effect " + key + " successfully.");
  }

  public void loadCondition(String key, ConfigurationSection cs) {

    String type = cs.getString("type", "NULL").toUpperCase();
    ConditionType conditionType;
    try {
      conditionType = ConditionType.valueOf(type);
    } catch (Exception e) {
      LogUtil.printError("Failed to load " + key + ". Invalid conditions type (" + type + ")");
      return;
    }

    String compType = cs.getString("comparison", "NONE").toUpperCase();
    Comparison comparison;
    try {
      comparison = Comparison.valueOf(compType);
    } catch (Exception e) {
      comparison = Comparison.NONE;
    }

    String compareTargetString = cs.getString("target", "SELF");
    CompareTarget compareTarget =
        compareTargetString.equalsIgnoreCase("SELF") ? CompareTarget.SELF : CompareTarget.OTHER;

    double value = cs.getDouble("value", 0);

    Condition condition;
    switch (conditionType) {
      case STAT:
        StrifeStat stat;
        try {
          stat = StrifeStat.valueOf(cs.getString("stat", null));
        } catch (Exception e) {
          LogUtil.printError("Failed to load condition " + key + ". Invalid stat.");
          return;
        }
        condition = new StatCondition(stat, compareTarget, comparison, value);
        break;
      case ATTRIBUTE:
        StrifeAttribute attribute = strifeAttributeManager
            .getAttribute(cs.getString("attribute", null));
        if (attribute == null) {
          LogUtil.printError("Failed to load condition " + key + ". Invalid attribute.");
          return;
        }
        condition = new AttributeCondition(attribute, compareTarget, comparison, value);
        break;
      case BARRIER:
        boolean percent = cs.getBoolean("percentage", false);
        condition = new BarrierCondition(compareTarget, comparison, value, percent);
        break;
      case CHANCE:
        double chance = cs.getDouble("chance", 0.5);
        condition = new ChanceCondition(chance);
        break;
      case HEALTH:
        boolean percent2 = cs.getBoolean("percentage", false);
        condition = new HealthCondition(compareTarget, comparison, value, percent2);
        break;
      case POTION_EFFECT:
        PotionEffectType potionEffectType;
        try {
          potionEffectType = PotionEffectType.getByName(cs.getString("potion-effect", "p"));
        } catch (Exception e) {
          LogUtil.printError("Failed to load " + key + ". Invalid conditions type (" + type + ")");
          return;
        }
        int potionIntensity = cs.getInt("intensity", 0);
        condition = new PotionCondition(potionEffectType, compareTarget, comparison,
            potionIntensity);
        break;
      case EQUIPMENT:
        Set<Material> materials = new HashSet<>();
        for (String s : cs.getStringList("materials")) {
          try {
            materials.add(Material.valueOf(s));
          } catch (Exception e) {
            LogUtil.printError("Failed to load " + key + ". Invalid material type (" + s + ")");
            return;
          }
        }
        boolean strict = cs.getBoolean("strict", false);
        condition = new EquipmentCondition(materials, strict);
        break;
      case TIME:
        long minTime = cs.getLong("min-time", 0);
        long maxTime = cs.getLong("max-time", 0);
        condition = new TimeCondition(minTime, maxTime);
        break;
      case LEVEL:
        condition = new LevelCondition(comparison, (int) value);
        break;
      case BONUS_LEVEL:
        condition = new BonusLevelCondition(comparison, (int) value);
        break;
      case ITS_OVER_ANAKIN:
        condition = new HeightCondition(compareTarget);
        break;
      case BLEEDING:
        condition = new BleedingCondition(compareTarget, cs.getBoolean("state", true));
        break;
      case DARKNESS:
        condition = new CorruptionCondition(compareTarget, comparison, value);
        break;
      case BURNING:
        condition = new BurningCondition(compareTarget, cs.getBoolean("state", true));
        break;
      case GROUNDED:
        condition = new GroundedCondition();
        break;
      case ENTITY_TYPE:
        List<String> entityTypes = cs.getStringList("types");
        boolean whitelist = cs.getBoolean("whitelist", true);
        Set<EntityType> typesSet = new HashSet<>();
        try {
          for (String s : entityTypes) {
            typesSet.add(EntityType.valueOf(s));
          }
        } catch (Exception e) {
          LogUtil.printError("Failed to load condition " + key + ". Invalid entity type!");
          return;
        }
        condition = new EntityTypeCondition(typesSet, whitelist);
        break;
      default:
        LogUtil.printError("No valid conditions found for " + key + "... somehow?");
        return;
    }
    conditions.put(key, condition);
  }

  public Effect getEffect(String key) {
    if (loadedEffects.containsKey(key)) {
      return loadedEffects.get(key);
    }
    LogUtil.printWarning("Attempted to get unknown effect '" + key + "'");
    return null;
  }

  public Map<String, Effect> getLoadedEffects() {
    return loadedEffects;
  }

  public Map<String, Condition> getConditions() {
    return conditions;
  }

  public enum EffectType {
    STANDARD_DAMAGE,
    DAMAGE,
    WORLD_SPACE_ENTITY,
    HEAL,
    RESTORE_BARRIER,
    INCREASE_RAGE,
    PROJECTILE,
    IGNITE,
    BLEED,
    CORRUPT,
    CONSUME_BLEED,
    CONSUME_CORRUPT,
    BUFF_EFFECT,
    WAIT,
    SOUND,
    PARTICLE,
    SPEAK,
    KNOCKBACK,
    LEAP,
    POTION,
    TARGET,
    SUMMON
  }
}
