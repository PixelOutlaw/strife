package info.faceland.strife.managers;

import com.tealcube.minecraft.bukkit.TextUtils;
import info.faceland.strife.attributes.StrifeAttribute;
import info.faceland.strife.conditions.*;
import info.faceland.strife.conditions.Condition.CompareTarget;
import info.faceland.strife.conditions.Condition.Comparison;
import info.faceland.strife.conditions.Condition.ConditionType;
import info.faceland.strife.data.AttributedEntity;
import info.faceland.strife.effects.*;
import info.faceland.strife.effects.DealDamage.DamageScale;
import info.faceland.strife.stats.StrifeStat;
import info.faceland.strife.util.DamageUtil.DamageType;
import info.faceland.strife.util.LogUtil;
import info.faceland.strife.util.PlayerDataUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffectType;

public class EffectManager {

  private final StrifeStatManager strifeStatManager;
  private final AttributedEntityManager aeManager;
  private final Map<String, Effect> loadedEffects;
  private final Map<String, Condition> conditions;

  public EffectManager(StrifeStatManager strifeStatManager, AttributedEntityManager aeManager) {
    this.strifeStatManager = strifeStatManager;
    this.aeManager = aeManager;
    this.loadedEffects = new HashMap<>();
    this.conditions = new HashMap<>();
  }

  public void execute(String effectName, AttributedEntity caster, AttributedEntity target) {
    Effect effect = getEffect(effectName);
    if (effect == null) {
      return;
    }
    execute(effect, caster, target);
  }

  public void execute(Effect effect, AttributedEntity caster, AttributedEntity target) {
    if (effect.isForceTargetCaster()) {
      target = caster;
    }
    if (!PlayerDataUtil.areConditionsMet(caster, target, effect.getConditions())) {
      LogUtil.printDebug("Conditions not met for effect. Failed.");
      return;
    }
    LogUtil.printDebug("Looping targets for " + effect.getName());
    for (LivingEntity le : getEffectTargets(caster.getEntity(), target.getEntity(), effect.getRange())) {
      LogUtil.printDebug("Applying effect to " + PlayerDataUtil.getName(le));
      effect.apply(caster, aeManager.getAttributedEntity(le));
    }
  }

  private List<LivingEntity> getEffectTargets(LivingEntity caster, LivingEntity target, double range) {
    List<LivingEntity> targets = new ArrayList<>();
    if (target == null) {
      LogUtil.printError(" Missing targets! Returning empty list");
      return targets;
    }
    if (range < 1) {
      LogUtil.printDebug(" Self casting, low or no range");
      targets.add(target);
      return targets;
    }
    for (Entity e : target.getNearbyEntities(range, range, range)) {
      if (e instanceof LivingEntity && target.hasLineOfSight(e)) {
        targets.add((LivingEntity) e);
      }
    }
    targets.remove(caster);
    LogUtil.printDebug(" Targeting " + targets.size() + " targets!");
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
      case DAMAGE:
        effect = new DealDamage();
        ((DealDamage) effect).setAmount(cs.getDouble("amount", 1));
        try {
          ((DealDamage) effect).setDamageScale(DamageScale.valueOf(cs.getString("scale", "FLAT")));
          ((DealDamage) effect)
              .setDamageType(DamageType.valueOf(cs.getString("damage-type", "TRUE")));
        } catch (Exception e) {
          LogUtil.printError("Skipping effect " + key + " for invalid damage scale/type");
          return;
        }
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
            || projType == EntityType.SPLASH_POTION || projType == EntityType.LINGERING_POTION
            || projType == EntityType.SHULKER_BULLET || projType == EntityType.PRIMED_TNT
            || projType == EntityType.EGG || projType == EntityType.SNOWBALL
            || projType == EntityType.FIREBALL || projType == EntityType.DRAGON_FIREBALL
            || projType == EntityType.SMALL_FIREBALL || projType == EntityType.WITHER_SKULL)) {
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
        ((Knockback) effect).setPower(cs.getDouble("power", 1));
        break;
      case LEAP:
        effect = new Leap();
        ((Leap) effect).setForward(cs.getDouble("forward", 1));
        ((Leap) effect).setHeight(cs.getDouble("height", 1));
        break;
      case SUMMON:
        effect = new Summon();
        ((Summon) effect).setAmount(cs.getInt("amount", 1));
        ((Summon) effect).setUniqueEntity(cs.getString("unique-entity"));
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
        ((PotionEffectAction) effect).setTargetCaster(cs.getBoolean("target-caster", false));
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
        ((SpawnParticle) effect).setSpeed((float) cs.getDouble("speed", 0));
        break;
    }
    if (effectType != EffectType.WAIT) {
      effect.setName(TextUtils.color(cs.getString("name", "&8Unnamed Effect")));
      effect.setRange(cs.getDouble("range", 0));
      effect.setForceTargetCaster(cs.getBoolean("force-target-caster", false));
      effect.setFriendly(cs.getBoolean("friendly", false));
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

    String compType = cs.getString("comparison", "NULL").toUpperCase();
    Comparison comparison;
    try {
      comparison = Comparison.valueOf(compType);
    } catch (Exception e) {
      LogUtil.printError("Failed to load " + key + ". Invalid comparison type (" + compType + ")");
      return;
    }

    String compareTargetString = cs.getString("target", "SELF");
    CompareTarget compareTarget =
        compareTargetString.equalsIgnoreCase("SELF") ? CompareTarget.SELF : CompareTarget.OTHER;

    double value = cs.getDouble("value", 0);

    Condition condition;
    switch (conditionType) {
      case ATTRIBUTE:
        StrifeAttribute attr;
        try {
          attr = StrifeAttribute.valueOf(cs.getString("attribute", null));
        } catch (Exception e) {
          LogUtil.printError("Failed to load condition " + key + ". Invalid attribute.");
          return;
        }
        condition = new AttributeCondition(attr, compareTarget, comparison, value);
        break;
      case STAT:
        StrifeStat stat = strifeStatManager.getStat(cs.getString("stat", null));
        if (stat == null) {
          LogUtil.printError("Failed to load condition " + key + ". Invalid stat.");
          return;
        }
        condition = new StatCondition(stat, compareTarget, comparison, value);
        break;
      case BARRIER:
        boolean percent = cs.getBoolean("percentage", false);
        condition = new BarrierCondition(compareTarget, comparison, value, percent);
        break;
      case CHANCE:
        double chance = cs.getDouble("chance", 0.5);
        condition = new ChanceCondition(comparison, chance);
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
      case LEVEL:
        condition = new LevelCondition(comparison, (int) value);
        break;
      case BONUS_LEVEL:
        condition = new BonusLevelCondition(comparison, (int) value);
        break;
      case ITS_OVER_ANAKIN:
        condition = new HeightCondition(compareTarget);
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
    DAMAGE,
    HEAL,
    RESTORE_BARRIER,
    PROJECTILE,
    IGNITE,
    BLEED,
    WAIT,
    PARTICLE,
    SPEAK,
    KNOCKBACK,
    LEAP,
    POTION,
    TARGET,
    SUMMON
  }
}
