package info.faceland.strife.managers;

import static info.faceland.strife.util.PlayerDataUtil.getName;

import com.tealcube.minecraft.bukkit.TextUtils;
import info.faceland.strife.StrifePlugin;
import info.faceland.strife.conditions.AttributeCondition;
import info.faceland.strife.conditions.BarrierCondition;
import info.faceland.strife.conditions.BleedingCondition;
import info.faceland.strife.conditions.BlockingCondition;
import info.faceland.strife.conditions.BonusLevelCondition;
import info.faceland.strife.conditions.BuffCondition;
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
import info.faceland.strife.conditions.MovingCondition;
import info.faceland.strife.conditions.PotionCondition;
import info.faceland.strife.conditions.StatCondition;
import info.faceland.strife.conditions.TimeCondition;
import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.data.champion.StrifeAttribute;
import info.faceland.strife.effects.AreaEffect;
import info.faceland.strife.effects.AreaEffect.AreaType;
import info.faceland.strife.effects.Bleed;
import info.faceland.strife.effects.BuffEffect;
import info.faceland.strife.effects.ConsumeBleed;
import info.faceland.strife.effects.ConsumeCorrupt;
import info.faceland.strife.effects.CooldownReduction;
import info.faceland.strife.effects.Corrupt;
import info.faceland.strife.effects.CreateWorldSpaceEntity;
import info.faceland.strife.effects.DealDamage;
import info.faceland.strife.effects.Effect;
import info.faceland.strife.effects.EndlessEffect;
import info.faceland.strife.effects.Food;
import info.faceland.strife.effects.ForceTarget;
import info.faceland.strife.effects.Heal;
import info.faceland.strife.effects.Ignite;
import info.faceland.strife.effects.IncreaseRage;
import info.faceland.strife.effects.Lightning;
import info.faceland.strife.effects.ModifyProjectile;
import info.faceland.strife.effects.PlaySound;
import info.faceland.strife.effects.PotionEffectAction;
import info.faceland.strife.effects.Push;
import info.faceland.strife.effects.Push.PushType;
import info.faceland.strife.effects.RestoreBarrier;
import info.faceland.strife.effects.ShootBlock;
import info.faceland.strife.effects.ShootProjectile;
import info.faceland.strife.effects.Silence;
import info.faceland.strife.effects.SpawnParticle;
import info.faceland.strife.effects.SpawnParticle.ParticleStyle;
import info.faceland.strife.effects.Speak;
import info.faceland.strife.effects.StandardDamage;
import info.faceland.strife.effects.Summon;
import info.faceland.strife.effects.Wait;
import info.faceland.strife.stats.AbilitySlot;
import info.faceland.strife.stats.StrifeStat;
import info.faceland.strife.util.DamageUtil.AbilityMod;
import info.faceland.strife.util.DamageUtil.AttackType;
import info.faceland.strife.util.DamageUtil.DamageScale;
import info.faceland.strife.util.DamageUtil.DamageType;
import info.faceland.strife.util.DamageUtil.OriginLocation;
import info.faceland.strife.util.LogUtil;
import info.faceland.strife.util.PlayerDataUtil;
import info.faceland.strife.util.ProjectileUtil;
import info.faceland.strife.util.StatUtil;
import info.faceland.strife.util.TargetingUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

public class EffectManager {

  private final StrifeAttributeManager strifeAttributeManager;
  private final StrifeMobManager aeManager;
  private final Map<String, Effect> loadedEffects;
  private final Map<String, Condition> conditions;

  private static Random random = new Random();

  public EffectManager(StrifeAttributeManager strifeAttributeManager, StrifeMobManager aeManager) {
    this.strifeAttributeManager = strifeAttributeManager;
    this.aeManager = aeManager;
    this.loadedEffects = new HashMap<>();
    this.conditions = new HashMap<>();
  }

  public void execute(Effect effect, StrifeMob caster, Set<LivingEntity> targets) {
    applyEffectToTargets(effect, caster, new HashSet<>(targets));
  }

  public void execute(Effect effect, StrifeMob caster, LivingEntity target) {
    Set<LivingEntity> targets = new HashSet<>();
    targets.add(target);
    applyEffectToTargets(effect, caster, targets);
  }

  public void executeEffectAtLocation(Effect effect, StrifeMob caster, Location location) {
    Set<LivingEntity> locationTargets = new HashSet<>();
    locationTargets.add(TargetingUtil.buildAndRemoveDetectionStand(location));
    applyEffectToTargets(effect, caster, locationTargets);
  }

  private void applyEffectToTargets(Effect effect, StrifeMob caster, Set<LivingEntity> targets) {
    Set<LivingEntity> finalTargets = buildValidTargets(effect, caster, targets);
    for (LivingEntity le : finalTargets) {
      if (TargetingUtil.isDetectionStand(le)) {
        if (runPlayAtLocationEffects(caster, effect, le)) {
          continue;
        } else if (effect.isForceTargetCaster()) {
          applyEffectIfConditionsMet(effect, caster);
          continue;
        }
      }
      applyEffectIfConditionsMet(effect, caster, aeManager.getStatMob(le));
    }
  }

  private void applyEffectIfConditionsMet(Effect effect, StrifeMob caster, StrifeMob targetMob) {
    LogUtil.printDebug(" - Applying '" + effect.getId() + "' to " + getName(targetMob.getEntity()));
    if (!PlayerDataUtil.areConditionsMet(caster, targetMob, effect.getConditions())) {
      return;
    }
    effect.apply(caster, effect.isForceTargetCaster() ? caster : targetMob);
  }

  private void applyEffectIfConditionsMet(Effect effect, StrifeMob caster) {
    LogUtil.printDebug(" - Applying '" + effect.getId() + "' to caster");
    if (!PlayerDataUtil.areConditionsMet(caster, null, effect.getConditions())) {
      return;
    }
    effect.apply(caster, caster);
  }

  private Set<LivingEntity> buildValidTargets(Effect effect, StrifeMob caster,
      Set<LivingEntity> targets) {
    if (effect instanceof AreaEffect) {
      return getAreaEffectTargets(targets, caster, (AreaEffect) effect);
    }
    if (targets.size() == 1 && targets.iterator().next() instanceof ArmorStand) {
      return targets;
    }
    TargetingUtil.filterFriendlyEntities(targets, caster, effect.isFriendly());
    return targets;
  }

  private Set<LivingEntity> getAreaEffectTargets(Set<LivingEntity> targets, StrifeMob caster,
      AreaEffect effect) {
    double range = effect.getRange();
    if (range < 1) {
      return targets;
    }
    Set<LivingEntity> areaTargets = new HashSet<>();
    for (LivingEntity le : targets) {
      switch (effect.getAreaType()) {
        case RADIUS:
          areaTargets.addAll(TargetingUtil.getEntitiesInArea(le, range));
          break;
        case LINE:
          areaTargets.addAll(TargetingUtil.getEntitiesInLine(le, range));
          break;
        default:
          return null;
      }
    }
    if (effect.getMaxTargets() > 0) {
      TargetingUtil.filterFriendlyEntities(areaTargets, caster, effect.isFriendly());
      List<LivingEntity> oldTargetsAsList = new ArrayList<>(areaTargets);
      Set<LivingEntity> newTargetsFromMax = new HashSet<>();
      while (newTargetsFromMax.size() < effect.getMaxTargets() && oldTargetsAsList.size() > 0) {
        int targetIndex = random.nextInt(oldTargetsAsList.size());
        newTargetsFromMax.add(oldTargetsAsList.get(targetIndex));
        oldTargetsAsList.remove(targetIndex);
      }
      return newTargetsFromMax;
    }
    return areaTargets;
  }

  /**
   * @param caster the strifemob casting the effect
   * @param effect the effect being cast
   * @param le the entity target of the effect
   * @return returns true if a effect is a play at location type
   */
  private boolean runPlayAtLocationEffects(StrifeMob caster, Effect effect, LivingEntity le) {
    if (effect.isForceTargetCaster()) {
      le = caster.getEntity();
    }
    if (effect instanceof CreateWorldSpaceEntity) {
      if (PlayerDataUtil.areConditionsMet(caster, null, effect.getConditions())) {
        ((CreateWorldSpaceEntity) effect).apply(caster, le);
      }
      return true;
    } else if (effect instanceof PlaySound) {
      if (PlayerDataUtil.areConditionsMet(caster, null, effect.getConditions())) {
        ((PlaySound) effect).playAtLocation(le.getLocation());
      }
      return true;
    } else if (effect instanceof SpawnParticle) {
      if (PlayerDataUtil.areConditionsMet(caster, null, effect.getConditions())) {
        ((SpawnParticle) effect).playAtLocation(le);
      }
      return true;
    } else if (effect instanceof Summon) {
      if (PlayerDataUtil.areConditionsMet(caster, null, effect.getConditions())) {
        ((Summon) effect).summonAtLocation(caster, le.getLocation());
      }
      return true;
    }
    return false;
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
        ((Heal) effect).setAmount((float) cs.getDouble("amount", 1));
        ((Heal) effect).setDamageScale(DamageScale.valueOf(cs.getString("scale", "FLAT")));
        break;
      case FOOD:
        effect = new Food();
        ((Food) effect).setAmount(cs.getDouble("amount", 1));
      case RESTORE_BARRIER:
        effect = new RestoreBarrier();
        ((RestoreBarrier) effect).setAmount((float) cs.getDouble("amount", 1));
        ((RestoreBarrier) effect)
            .setDamageScale(DamageScale.valueOf(cs.getString("scale", "FLAT")));
        break;
      case INCREASE_RAGE:
        effect = new IncreaseRage();
        ((IncreaseRage) effect).setAmount((float) cs.getDouble("amount", 1));
        break;
      case DAMAGE:
        effect = new DealDamage();
        ((DealDamage) effect).setAmount((float) cs.getDouble("amount", 1));
        ((DealDamage) effect).setFlatBonus((float) cs.getDouble("flat-bonus", 0));
        try {
          ((DealDamage) effect).setDamageScale(DamageScale.valueOf(cs.getString("scale", "FLAT")));
          ((DealDamage) effect)
              .setDamageType(DamageType.valueOf(cs.getString("damage-type", "TRUE")));
        } catch (Exception e) {
          LogUtil.printError("Skipping effect " + key + " for invalid damage scale/type");
          return;
        }
        ConfigurationSection damageMod = cs.getConfigurationSection("attack-mods");
        Map<AbilityMod, Float> damageModMap = new HashMap<>();
        if (damageMod != null) {
          for (String k : damageMod.getKeys(false)) {
            AbilityMod mod = AbilityMod.valueOf(k);
            damageModMap.put(mod, (float) damageMod.getDouble(k));
          }
        }
        ((DealDamage) effect).getAbilityMods().putAll(damageModMap);
        break;
      case STANDARD_DAMAGE:
        effect = new StandardDamage();
        ((StandardDamage) effect)
            .setAttackMultiplier((float) cs.getDouble("attack-multiplier", 1D));
        ((StandardDamage) effect)
            .setHealMultiplier((float) cs.getDouble("heal-multiplier", 0.3D));
        ((StandardDamage) effect).setCanBeBlocked(cs.getBoolean("can-be-blocked", false));
        ((StandardDamage) effect).setCanBeEvaded(cs.getBoolean("can-be-evaded", false));
        ((StandardDamage) effect).setAttackType(AttackType.valueOf(cs.getString("attack-type")));
        ConfigurationSection multCs = cs.getConfigurationSection("damage-multipliers");
        Map<DamageType, Float> multMap = new HashMap<>();
        if (multCs != null) {
          for (String k : multCs.getKeys(false)) {
            DamageType mod = DamageType.valueOf(k);
            multMap.put(mod, (float) multCs.getDouble(k));
          }
        }
        ConfigurationSection flatCs = cs.getConfigurationSection("flat-damage-bonuses");
        Map<DamageType, Float> flatMap = new HashMap<>();
        if (flatCs != null) {
          for (String k : flatCs.getKeys(false)) {
            DamageType mod = DamageType.valueOf(k);
            flatMap.put(mod, (float) flatCs.getDouble(k));
          }
        }
        ConfigurationSection modsCs = cs.getConfigurationSection("attack-mods");
        Map<AbilityMod, Float> attackModMap = new HashMap<>();
        if (modsCs != null) {
          for (String k : modsCs.getKeys(false)) {
            AbilityMod mod = AbilityMod.valueOf(k);
            attackModMap.put(mod, (float) modsCs.getDouble(k));
          }
        }
        ((StandardDamage) effect).getDamageModifiers().putAll(multMap);
        ((StandardDamage) effect).getDamageBonuses().putAll(flatMap);
        ((StandardDamage) effect).getAbilityMods().putAll(attackModMap);
        break;
      case WORLD_SPACE_ENTITY:
        effect = new CreateWorldSpaceEntity();
        Map<Integer, List<String>> effectSchedule = new HashMap<>();
        ConfigurationSection scheduleSection = cs.getConfigurationSection("schedule");
        for (String intKey : scheduleSection.getKeys(false)) {
          List<String> effects = scheduleSection.getStringList(intKey);
          effectSchedule.put(Integer.valueOf(intKey), effects);
        }
        ((CreateWorldSpaceEntity) effect).setEffectSchedule(effectSchedule);
        ((CreateWorldSpaceEntity) effect).setMaxTicks(cs.getInt("refresh-delay", 5));
        ((CreateWorldSpaceEntity) effect).setLifespan(cs.getInt("life-span", 10));
        ((CreateWorldSpaceEntity) effect)
            .setOriginLocation(OriginLocation.valueOf(cs.getString("origin", "HEAD")));
        ((CreateWorldSpaceEntity) effect).setVelocity(cs.getDouble("speed", 0));
        ((CreateWorldSpaceEntity) effect).setStrictDuration(cs.getBoolean("strict-duration", true));
        ((CreateWorldSpaceEntity) effect).setLockedToEntity(cs.getBoolean("entity-lock", false));
        break;
      case AREA_EFFECT:
        effect = new AreaEffect();
        List<String> areaEffects = cs.getStringList("effects");
        AreaEffect areaEffect = (AreaEffect) effect;
        Bukkit.getScheduler().runTaskLater(StrifePlugin.getInstance(), () -> {
          for (String s : areaEffects) {
            areaEffect.getEffects().add(getEffect(s));
          }
        }, 5L);
        ((AreaEffect) effect).setRange(cs.getDouble("range", 1));
        ((AreaEffect) effect).setMaxTargets(cs.getInt("max-targets", -1));
        ((AreaEffect) effect).setLineOfSight(cs.getBoolean("line-of-sight", true));
        ((AreaEffect) effect).setAreaType(AreaType.valueOf(cs.getString("area-type", "RADIUS")));
        ((AreaEffect) effect).setCanBeBlocked(cs.getBoolean("can-be-blocked", false));
        ((AreaEffect) effect).setCanBeEvaded(cs.getBoolean("can-be-evaded", false));
        ((AreaEffect) effect).setTargetingCooldown(cs.getLong("target-cooldown", 0));
        break;
      case ENDLESS_EFFECT:
        effect = new EndlessEffect();
        List<String> failConditions = cs.getStringList("fail-conditions");
        ((EndlessEffect) effect).setMaxDuration(cs.getInt("max-duration-seconds", 30));
        ((EndlessEffect) effect).setTickRate(cs.getInt("tick-rate", 5));
        ((EndlessEffect) effect).setStrictDuration(cs.getBoolean("strict-duration", true));
        List<String> runEffects = cs.getStringList("effects");
        List<String> cancelEffects = cs.getStringList("cancel-effects");
        List<String> expiryEffects = cs.getStringList("expiry-effects");
        for (String s : failConditions) {
          ((EndlessEffect) effect).getCancelConditions().add(getConditions().get(s));
        }
        EndlessEffect endlessEffect = (EndlessEffect) effect;
        Bukkit.getScheduler().runTaskLater(StrifePlugin.getInstance(), () -> {
          for (String s : runEffects) {
            endlessEffect.getRunEffects().add(getEffect(s));
          }
          for (String s : cancelEffects) {
            endlessEffect.getCancelEffects().add(getEffect(s));
          }
          for (String s : expiryEffects) {
            endlessEffect.getExpiryEffects().add(getEffect(s));
          }
        }, 5L);
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
        if (!ProjectileUtil.isProjectile(projType)) {
          LogUtil.printWarning("Skipping effect " + key + " for non projectile entity");
          return;
        }
        ((ShootProjectile) effect).setProjectileEntity(projType);
        ((ShootProjectile) effect)
            .setOriginType(OriginLocation.valueOf(cs.getString("origin", "HEAD")));
        ((ShootProjectile) effect).setVerticalBonus(cs.getDouble("vertical-bonus", 0));
        ((ShootProjectile) effect).setSpread(cs.getDouble("spread", 0));
        ((ShootProjectile) effect).setRadialAngle(cs.getDouble("radial-angle", 0));
        ((ShootProjectile) effect).setSpeed(cs.getDouble("speed", 1));
        ((ShootProjectile) effect).setYield((float) cs.getDouble("yield", 0.0D));
        ((ShootProjectile) effect).setIgnite(cs.getBoolean("ignite", false));
        ((ShootProjectile) effect).setBounce(cs.getBoolean("bounce", false));
        ((ShootProjectile) effect).setIgnoreMultishot(cs.getBoolean("ignore-multishot", false));
        ((ShootProjectile) effect).setGravity(cs.getBoolean("gravity", true));
        ((ShootProjectile) effect).setZeroPitch(cs.getBoolean("zero-pitch", false));
        ((ShootProjectile) effect).setHitEffects(cs.getStringList("hit-effects"));
        ((ShootProjectile) effect).setAttackMultiplier(cs.getDouble("attack-multiplier", 0D));
        ((ShootProjectile) effect).setTargeted(cs.getBoolean("targeted", false));
        ((ShootProjectile) effect).setSeeking(cs.getBoolean("seeking", false));
        break;
      case FALLING_BLOCK:
        effect = new ShootBlock();
        ((ShootBlock) effect).setQuantity(cs.getInt("quantity", 1));
        Material material;
        try {
          material = Material.valueOf(cs.getString("material", "DIRT"));
        } catch (Exception e) {
          LogUtil.printError("Skipping effect " + key + " for invalid projectile type");
          return;
        }
        ((ShootBlock) effect).setBlockData(Bukkit.getServer().createBlockData(material));
        ((ShootBlock) effect).setOriginType(OriginLocation.valueOf(cs.getString("origin", "HEAD")));
        ((ShootBlock) effect).setVerticalBonus(cs.getDouble("vertical-bonus", 0));
        ((ShootBlock) effect).setSpread(cs.getDouble("spread", 0));
        ((ShootBlock) effect).setSpeed(cs.getDouble("speed", 1));
        ((ShootBlock) effect).setZeroPitch(cs.getBoolean("zero-pitch", false));
        ((ShootBlock) effect).setHitEffects(cs.getStringList("hit-effects"));
        break;
      case IGNITE:
        effect = new Ignite();
        ((Ignite) effect).setDuration(cs.getInt("duration", 20));
        break;
      case SILENCE:
        effect = new Silence();
        ((Silence) effect).setDuration(cs.getInt("duration", 20));
        break;
      case BLEED:
        effect = new Bleed();
        ((Bleed) effect).setAmount((float) cs.getDouble("amount", 10));
        ((Bleed) effect).setDamageScale(DamageScale.valueOf(cs.getString("scale", "FLAT")));
        ((Bleed) effect).setIgnoreArmor(cs.getBoolean("ignore-armor", true));
        ((Bleed) effect).setApplyBleedMods(cs.getBoolean("apply-bleed-mods", true));
        break;
      case CORRUPT:
        effect = new Corrupt();
        ((Corrupt) effect).setAmount((float) cs.getDouble("amount", 10));
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
      case COOLDOWN_REDUCTION:
        effect = new CooldownReduction();
        ((CooldownReduction) effect).setAbilityString(cs.getString("ability-id"));
        ((CooldownReduction) effect).setSeconds(cs.getDouble("seconds"));
        String slot = cs.getString("ability-slot");
        if (StringUtils.isNotBlank(slot)) {
          ((CooldownReduction) effect).setSlot(AbilitySlot.valueOf(slot));
        }
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
      case PUSH:
        effect = new Push();
        ((Push) effect).setPower(cs.getDouble("power", 10));
        ((Push) effect).setHeight(cs.getDouble("height", 10));
        ((Push) effect).setCancelFall(cs.getBoolean("cancel-fall", false));
        ((Push) effect).setPushType(
            PushType.valueOf(cs.getString("push-type", "AWAY_FROM_CASTER")));
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
        ((ForceTarget) effect).setOverwrite(cs.getBoolean("overwrite", true));
        ((ForceTarget) effect).setCasterTargetsTarget(cs.getBoolean("caster-targets-target", true));
        break;
      case LIGHTNING:
        effect = new Lightning();
        break;
      case MODIFY_PROJECTILE:
        effect = new ModifyProjectile();
        ((ModifyProjectile) effect)
            .setFriendlyProjectiles(cs.getBoolean("friendly-projectiles", false));
        ((ModifyProjectile) effect).setRange(cs.getDouble("range", 1));
        ((ModifyProjectile) effect).setRemove(cs.getBoolean("remove", true));
        ((ModifyProjectile) effect).setSpeedMult(cs.getDouble("speed-mult", 0.5));
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
        ((PotionEffectAction) effect)
            .setBumpUpToIntensity(cs.getBoolean("bump-up-to-intensity", false));
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
          particle = Particle.valueOf((cs.getString("particle-type", "FLAME")));
        } catch (Exception e) {
          LogUtil.printWarning("Invalid particle effect type in effect " + key + ". Skipping.");
          return;
        }
        ((SpawnParticle) effect).setParticle(particle);
        ParticleStyle style = ParticleStyle.valueOf(cs.getString("style", "NORMAL"));
        ((SpawnParticle) effect).setStyle(style);
        if (particle == Particle.SPELL_MOB || particle == Particle.SPELL_WITCH
            || particle == Particle.SPELL_INSTANT) {
          ((SpawnParticle) effect).setRed(cs.getDouble("red", 0) / 255D);
          ((SpawnParticle) effect).setBlue(cs.getDouble("blue", 0) / 255D);
          ((SpawnParticle) effect).setGreen(cs.getDouble("green", 0) / 255D);
        }
        if (style == ParticleStyle.ARC) {
          ((SpawnParticle) effect).setArcAngle(cs.getDouble("arc-angle", 30));
          ((SpawnParticle) effect).setArcOffset(cs.getDouble("arc-offset", 0));
        }
        ((SpawnParticle) effect).setQuantity(cs.getInt("quantity", 10));
        ((SpawnParticle) effect).setTickDuration(cs.getInt("duration-ticks", 0));
        ((SpawnParticle) effect).setSpeed((float) cs.getDouble("speed", 0));
        ((SpawnParticle) effect).setSpread((float) cs.getDouble("spread", 1));
        ((SpawnParticle) effect).setParticleOriginLocation(
            OriginLocation.valueOf(cs.getString("origin", "HEAD")));
        ((SpawnParticle) effect).setSize(cs.getDouble("size", 1));
        String materialType = cs.getString("material", "");
        if (StringUtils.isNotBlank(materialType)) {
          ((SpawnParticle) effect).setBlockData(new ItemStack(Material.getMaterial(materialType)));
        }
        break;
    }
    if (effectType != EffectType.WAIT) {
      effect.setForceTargetCaster(cs.getBoolean("force-target-caster", false));
      effect.setFriendly(cs.getBoolean("friendly", false));
      Map<StrifeStat, Float> statMults = StatUtil
          .getStatMapFromSection(cs.getConfigurationSection("stat-mults"));
      effect.setStatMults(statMults);
    }
    effect.setId(key);
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
      case BUFF:
        int stacks = cs.getInt("stacks", 1);
        String buffId = cs.getString("buff-id", "");
        condition = new BuffCondition(compareTarget, comparison, buffId, stacks);
        break;
      case CHANCE:
        float chance = (float) cs.getDouble("chance", 0.5);
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
      case MOVING:
        condition = new MovingCondition(compareTarget, cs.getBoolean("state", true));
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
      case BLOCKING:
        condition = new BlockingCondition(compareTarget, cs.getBoolean("state", true));
        break;
      case GROUNDED:
        condition = new GroundedCondition(compareTarget, cs.getBoolean("inverted", false));
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
    AREA_EFFECT,
    ENDLESS_EFFECT,
    HEAL,
    FOOD,
    COOLDOWN_REDUCTION,
    RESTORE_BARRIER,
    INCREASE_RAGE,
    PROJECTILE,
    FALLING_BLOCK,
    IGNITE,
    SILENCE,
    BLEED,
    CORRUPT,
    CONSUME_BLEED,
    CONSUME_CORRUPT,
    BUFF_EFFECT,
    WAIT,
    SOUND,
    PARTICLE,
    SPEAK,
    PUSH,
    LIGHTNING,
    MODIFY_PROJECTILE,
    POTION,
    TARGET,
    SUMMON
  }
}
