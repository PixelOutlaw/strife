package land.face.strife.managers;

import static land.face.strife.util.PlayerDataUtil.getName;

import com.tealcube.minecraft.bukkit.TextUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import land.face.strife.StrifePlugin;
import land.face.strife.data.DamageContainer;
import land.face.strife.data.EquipmentItemData;
import land.face.strife.data.LoadedChaser;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.champion.StrifeAttribute;
import land.face.strife.data.conditions.AttributeCondition;
import land.face.strife.data.conditions.BarrierCondition;
import land.face.strife.data.conditions.BleedingCondition;
import land.face.strife.data.conditions.BlockingCondition;
import land.face.strife.data.conditions.BonusLevelCondition;
import land.face.strife.data.conditions.BuffCondition;
import land.face.strife.data.conditions.BurningCondition;
import land.face.strife.data.conditions.ChanceCondition;
import land.face.strife.data.conditions.Condition;
import land.face.strife.data.conditions.Condition.CompareTarget;
import land.face.strife.data.conditions.Condition.Comparison;
import land.face.strife.data.conditions.Condition.ConditionType;
import land.face.strife.data.conditions.Condition.ConditionUser;
import land.face.strife.data.conditions.CorruptionCondition;
import land.face.strife.data.conditions.EarthRunesCondition;
import land.face.strife.data.conditions.EndlessEffectCondition;
import land.face.strife.data.conditions.EntityTypeCondition;
import land.face.strife.data.conditions.FactionCondition;
import land.face.strife.data.conditions.GroundedCondition;
import land.face.strife.data.conditions.HealthCondition;
import land.face.strife.data.conditions.HeightCondition;
import land.face.strife.data.conditions.InCombatCondition;
import land.face.strife.data.conditions.LevelCondition;
import land.face.strife.data.conditions.LightCondition;
import land.face.strife.data.conditions.LoreCondition;
import land.face.strife.data.conditions.MovingCondition;
import land.face.strife.data.conditions.NearbyEntitiesCondition;
import land.face.strife.data.conditions.PotionCondition;
import land.face.strife.data.conditions.RangeCondition;
import land.face.strife.data.conditions.StatCondition;
import land.face.strife.data.conditions.TimeCondition;
import land.face.strife.data.conditions.UniqueCondition;
import land.face.strife.data.conditions.VelocityCondition;
import land.face.strife.data.conditions.VelocityCondition.VelocityType;
import land.face.strife.data.conditions.WeaponsCondition;
import land.face.strife.data.effects.AddEarthRunes;
import land.face.strife.data.effects.AreaEffect;
import land.face.strife.data.effects.AreaEffect.AreaType;
import land.face.strife.data.effects.AreaEffect.TargetingPriority;
import land.face.strife.data.effects.Bleed;
import land.face.strife.data.effects.BuffEffect;
import land.face.strife.data.effects.CancelEndlessEffect;
import land.face.strife.data.effects.Charm;
import land.face.strife.data.effects.ChaserEffect;
import land.face.strife.data.effects.ConsumeBleed;
import land.face.strife.data.effects.ConsumeCorrupt;
import land.face.strife.data.effects.CooldownReduction;
import land.face.strife.data.effects.Corrupt;
import land.face.strife.data.effects.CreateWorldSpaceEntity;
import land.face.strife.data.effects.DirectDamage;
import land.face.strife.data.effects.Effect;
import land.face.strife.data.effects.Effect.EffectType;
import land.face.strife.data.effects.EndlessEffect;
import land.face.strife.data.effects.EquipmentSwap;
import land.face.strife.data.effects.EvokerFangEffect;
import land.face.strife.data.effects.Food;
import land.face.strife.data.effects.ForceTarget;
import land.face.strife.data.effects.Heal;
import land.face.strife.data.effects.Ignite;
import land.face.strife.data.effects.IncreaseRage;
import land.face.strife.data.effects.Lightning;
import land.face.strife.data.effects.ModifyProjectile;
import land.face.strife.data.effects.PlaySound;
import land.face.strife.data.effects.PotionEffectAction;
import land.face.strife.data.effects.Push;
import land.face.strife.data.effects.Push.PushType;
import land.face.strife.data.effects.RestoreBarrier;
import land.face.strife.data.effects.Revive;
import land.face.strife.data.effects.SetFall;
import land.face.strife.data.effects.ShootBlock;
import land.face.strife.data.effects.ShootProjectile;
import land.face.strife.data.effects.Silence;
import land.face.strife.data.effects.Speak;
import land.face.strife.data.effects.StandardDamage;
import land.face.strife.data.effects.StrifeParticle;
import land.face.strife.data.effects.StrifeParticle.ParticleStyle;
import land.face.strife.data.effects.Summon;
import land.face.strife.data.effects.Teleport;
import land.face.strife.data.effects.Wait;
import land.face.strife.stats.AbilitySlot;
import land.face.strife.stats.StrifeStat;
import land.face.strife.util.DamageUtil.AbilityMod;
import land.face.strife.util.DamageUtil.AttackType;
import land.face.strife.util.DamageUtil.DamageScale;
import land.face.strife.util.DamageUtil.DamageType;
import land.face.strife.util.DamageUtil.OriginLocation;
import land.face.strife.util.LogUtil;
import land.face.strife.util.PlayerDataUtil;
import land.face.strife.util.ProjectileUtil;
import land.face.strife.util.StatUtil;
import land.face.strife.util.TargetingUtil;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class EffectManager {

  private final StrifePlugin plugin;
  private final Map<String, Effect> loadedEffects;
  private final Map<String, Condition> conditions;

  public EffectManager(StrifePlugin plugin) {
    this.plugin = plugin;
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

  public void execute(Effect effect, StrifeMob caster, Location location) {
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
          applyEffectIfConditionsMet(effect, caster, null);
          continue;
        }
      }
      applyEffectIfConditionsMet(effect, caster, plugin.getStrifeMobManager().getStatMob(le));
    }
  }

  private void applyEffectIfConditionsMet(Effect effect, StrifeMob caster, StrifeMob targetMob) {
    if (targetMob == null) {
      targetMob = caster;
    }
    if (!PlayerDataUtil.areConditionsMet(caster, targetMob, effect.getConditions())) {
      return;
    }
    LogUtil.printDebug("-- Applying '" + effect.getId() + "' to " + getName(targetMob.getEntity()));
    effect.apply(caster, effect.isForceTargetCaster() ? caster : targetMob);
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
    if (range < 0.1) {
      if (caster.getEntity() instanceof Mob) {
        range = caster.getEntity().getAttribute(Attribute.GENERIC_FOLLOW_RANGE).getBaseValue();
      } else {
        range = 16;
      }
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
        case CONE:
          areaTargets.addAll(TargetingUtil.getEntitiesInCone(caster.getEntity(), le.getLocation(),
              caster.getEntity().getLocation().getDirection(), (float) range,
              effect.getMaxConeRadius()));
          break;
        case PARTY:
          if (caster.getEntity() instanceof Player) {
            areaTargets.addAll(plugin.getSnazzyPartiesHook().getNearbyPartyMembers(
                (Player) caster.getEntity(), le.getLocation(), range));
          } else {
            areaTargets.addAll(TargetingUtil.getEntitiesInArea(le, range));
          }
          break;
        default:
          return null;
      }
    }
    if (effect.getMaxTargets() > 0) {
      TargetingUtil.filterFriendlyEntities(areaTargets, caster, effect.isFriendly());
      int maxTargets = effect.getMaxTargets();
      if (effect.isScaleTargetsWithMultishot()) {
        float mult = caster.getStat(StrifeStat.MULTISHOT) * (float) Math.pow(Math.random(), 1.5);
        maxTargets = ProjectileUtil.getTotalProjectiles(maxTargets, mult);
      }
      TargetingUtil.filterByTargetPriority(areaTargets, effect, caster,
          Math.min(maxTargets, areaTargets.size()));
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
      if (PlayerDataUtil.areConditionsMet(caster, caster, effect.getConditions())) {
        ((CreateWorldSpaceEntity) effect).apply(caster, le);
      }
      return true;
    } else if (effect instanceof PlaySound) {
      if (PlayerDataUtil.areConditionsMet(caster, caster, effect.getConditions())) {
        ((PlaySound) effect).playAtLocation(le.getLocation());
      }
      return true;
    } else if (effect instanceof StrifeParticle) {
      if (PlayerDataUtil.areConditionsMet(caster, caster, effect.getConditions())) {
        ((StrifeParticle) effect).playAtLocation(le);
      }
      return true;
    } else if (effect instanceof Summon) {
      if (PlayerDataUtil.areConditionsMet(caster, caster, effect.getConditions())) {
        ((Summon) effect).summonAtLocation(caster, le.getLocation());
      }
      return true;
    } else if (effect instanceof EvokerFangEffect) {
      if (PlayerDataUtil.areConditionsMet(caster, caster, effect.getConditions())) {
        ((EvokerFangEffect) effect).spawnAtLocation(caster, le.getLocation());
      }
      return true;
    }
    return false;
  }

  public void loadEffect(String key, ConfigurationSection cs) {
    String type = cs.getString("type", "NULL").toUpperCase();
    EffectType effectType;
    try {
      effectType = Effect.EffectType.valueOf(type);
    } catch (Exception e) {
      LogUtil.printError("Skipping effect " + key + " for invalid effect type");
      return;
    }
    Effect effect = null;
    switch (effectType) {
      case REVIVE:
        effect = new Revive();
        ((Revive) effect).setPercentLostExpRestored(cs.getDouble("percent-exp-restored", 1));
        break;
      case HEAL:
        effect = new Heal();
        ((Heal) effect).setAmount((float) cs.getDouble("amount", 1));
        ((Heal) effect).setFlatBonus((float) cs.getDouble("flat-bonus", 0));
        ((Heal) effect).setDamageScale(DamageScale.valueOf(cs.getString("scale", "FLAT")));
        ((Heal) effect).setUseHealingPower(cs.getBoolean("use-healing-power", false));
        break;
      case FOOD:
        effect = new Food();
        ((Food) effect).setAmount(cs.getDouble("amount", 1));
        break;
      case SET_FALL:
        effect = new SetFall();
        ((SetFall) effect).setAmount((float) cs.getDouble("amount", 0));
        break;
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
        effect = new DirectDamage();
        try {
          ConfigurationSection damages = cs.getConfigurationSection("damages");
          if (damages != null) {
            for (String k : damages.getKeys(false)) {
              ConfigurationSection damage = damages.getConfigurationSection(k);
              DamageType damageType = DamageType.valueOf(damage.getString("damage-type"));
              String scaleString = damage.getString("damage-scale", "FLAT");
              DamageScale scale = DamageScale.valueOf(scaleString);
              float amount = (float) damage.getDouble("amount", 1);
              String statString = damage.getString("stat", "");
              StrifeStat damageStat =
                  StringUtils.isBlank(statString) ? null : StrifeStat.valueOf(statString);
              DamageContainer container = new DamageContainer(scale, damageType,
                  damageStat, amount);
              ((DirectDamage) effect).getDamages().add(container);
            }
          }
          ((DirectDamage) effect).setAttackType(
              AttackType.valueOf(cs.getString("attack-type", "OTHER")));
          ((DirectDamage) effect).setDamageReductionRatio(
              (float) cs.getDouble("damage-reduction-ratio", 0.35));
          ((DirectDamage) effect).setCanBeBlocked(cs.getBoolean("can-be-blocked", false));
          ((DirectDamage) effect).setCanBeEvaded(cs.getBoolean("can-be-evaded", false));

          ConfigurationSection damageMod = cs.getConfigurationSection("attack-mods");
          Map<AbilityMod, Float> damageModMap = new HashMap<>();
          if (damageMod != null) {
            for (String k : damageMod.getKeys(false)) {
              AbilityMod mod = AbilityMod.valueOf(k);
              damageModMap.put(mod, (float) damageMod.getDouble(k));
            }
          }
          ((DirectDamage) effect).getAbilityMods().putAll(damageModMap);
        } catch (Exception e) {
          LogUtil.printError("Skipping effect " + key + " for invalid damage config!");
          return;
        }
        break;
      case STANDARD_DAMAGE:
        effect = new StandardDamage();
        ((StandardDamage) effect)
            .setAttackMultiplier((float) cs.getDouble("attack-multiplier", 1D));
        ((StandardDamage) effect)
            .setHealMultiplier((float) cs.getDouble("heal-multiplier", 0.3D));
        ((StandardDamage) effect).setCanBeBlocked(cs.getBoolean("can-be-blocked", true));
        ((StandardDamage) effect).setCanBeEvaded(cs.getBoolean("can-be-evaded", true));
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
        break;
      case CHASER:
        effect = new ChaserEffect();
        LoadedChaser data = plugin.getChaserManager().loadChaser(key, cs);
        ((ChaserEffect) effect).setOriginLocation(
            OriginLocation.valueOf(cs.getString("origin", "HEAD")));
        ((ChaserEffect) effect).setLoadedChaser(data);
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
        ((AreaEffect) effect)
            .setScaleTargetsWithMultishot(cs.getBoolean("scale-targets-with-multishot", false));
        ((AreaEffect) effect).setLineOfSight(cs.getBoolean("line-of-sight", true));
        ((AreaEffect) effect).setAreaType(AreaType.valueOf(cs.getString("area-type", "RADIUS")));
        ((AreaEffect) effect).setCanBeBlocked(cs.getBoolean("can-be-blocked", false));
        ((AreaEffect) effect).setCanBeEvaded(cs.getBoolean("can-be-evaded", false));
        ((AreaEffect) effect).setTargetingCooldown(cs.getLong("target-cooldown", 0));
        if (((AreaEffect) effect).getAreaType() == AreaType.CONE) {
          ((AreaEffect) effect).setMaxConeRadius((float) cs.getDouble("max-cone-radius", 3));
        }
        if (((AreaEffect) effect).getMaxTargets() != -1) {
          ((AreaEffect) effect).setPriority(
              TargetingPriority.valueOf(cs.getString("priority", "RANDOM")));
        }
        break;
      case ENDLESS_EFFECT:
        effect = new EndlessEffect();
        List<String> cancelConditions = cs.getStringList("cancel-conditions");
        ((EndlessEffect) effect).setMaxDuration(cs.getInt("max-duration-seconds", 30));
        ((EndlessEffect) effect).setTickRate(cs.getInt("tick-rate", 5));
        ((EndlessEffect) effect).setStrictDuration(cs.getBoolean("strict-duration", true));
        String strifeStat = cs.getString("period-reduction-stat", "");
        if (StringUtils.isNotBlank(strifeStat)) {
          try {
            ((EndlessEffect) effect).setReducerStat(StrifeStat.valueOf(strifeStat));
            ((EndlessEffect) effect).setReducerValue(
                (float) cs.getDouble("stat-reducer-value", 100f));
          } catch (Exception e) {
            LogUtil.printWarning("Skipping invalid stat " + strifeStat + " in endless effect!");
          }
        }
        List<String> runEffects = cs.getStringList("effects");
        List<String> cancelEffects = cs.getStringList("cancel-effects");
        List<String> expiryEffects = cs.getStringList("expiry-effects");
        for (String s : cancelConditions) {
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
      case CANCEL_ENDLESS_EFFECT:
        effect = new CancelEndlessEffect();
        CancelEndlessEffect cancelEndlessEffect = (CancelEndlessEffect) effect;
        Bukkit.getScheduler().runTaskLater(StrifePlugin.getInstance(), () -> {
          Effect newEffect = getEffect(cs.getString("effect-id", ""));
          if (newEffect instanceof EndlessEffect) {
            cancelEndlessEffect.setEndlessEffect((EndlessEffect) newEffect);
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
        ((ShootProjectile) effect).setSpeed((float) cs.getDouble("speed", 1));
        ((ShootProjectile) effect).setYield((float) cs.getDouble("yield", 0.0D));
        ((ShootProjectile) effect).setIgnite(cs.getBoolean("ignite", false));
        ((ShootProjectile) effect).setBounce(cs.getBoolean("bounce", false));
        ((ShootProjectile) effect).setIgnoreMultishot(cs.getBoolean("ignore-multishot", false));
        ((ShootProjectile) effect).setZeroPitch(cs.getBoolean("zero-pitch", false));
        ((ShootProjectile) effect).setBlockHitEffects(cs.getBoolean("effects-on-block-hit", false));
        ((ShootProjectile) effect).setHitEffects(cs.getStringList("hit-effects"));
        ((ShootProjectile) effect).setAttackMultiplier(cs.getDouble("attack-multiplier", 0D));
        ((ShootProjectile) effect).setTargeted(cs.getBoolean("targeted", false));
        ((ShootProjectile) effect).setSeeking(cs.getBoolean("seeking", false));
        int color = cs.getInt("arrow-rgb-color", -1);
        if (color != -1) {
          ((ShootProjectile) effect).setArrowColor(Color.fromRGB(color));
        }
        break;
      case EQUIPMENT_SWAP:
        effect = new EquipmentSwap();
        List<String> items = cs.getStringList("items");
        for (String s : items) {
          String[] parts = s.split(":");
          if (parts.length != 2) {
            LogUtil.printWarning("Skipping effect " + key + " for invalid equipment entry " + s);
            return;
          }
          EquipmentSlot slot;
          try {
            slot = EquipmentSlot.valueOf(parts[0]);
          } catch (Exception e) {
            LogUtil.printWarning("Skipping effect " + key + ". Invalid equipment enum " + parts[0]);
            return;
          }
          ((EquipmentSwap) effect).addItem(slot, parts[1]);
        }
        break;
      case EVOKER_FANGS:
        effect = new EvokerFangEffect();
        ((EvokerFangEffect) effect).setQuantity(cs.getInt("quantity", 1));
        ((EvokerFangEffect) effect).setSpread((float) cs.getDouble("spread", 0));
        ((EvokerFangEffect) effect)
            .setHitEffects(String.join("~", cs.getStringList("hit-effects")));
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
        ((Ignite) effect).setForceDuration(cs.getBoolean("force-duration", false));
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
      case ADD_EARTH_RUNES:
        effect = new AddEarthRunes();
        ((AddEarthRunes) effect).setAmount(cs.getInt("amount", 1));
        break;
      case TELEPORT:
        effect = new Teleport();
        ((Teleport) effect).setTargeted(cs.getBoolean("targeted", true));
        double x = cs.getDouble("x", 0);
        double y = cs.getDouble("y", 0);
        double z = cs.getDouble("z", 0);
        ((Teleport) effect).setVector(new Vector(x, y, z));
        ((Teleport) effect).setRelative(cs.getBoolean("relative", false));
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
      case CHARM:
        effect = new Charm();
        ((Charm) effect).setChance((float) cs.getDouble("success-chance", 1));
        ((Charm) effect).setChancePerLevel((float) cs.getDouble("chance-per-level", 0));
        ((Charm) effect)
            .setLifespanSeconds((float) cs.getDouble("lifespan-seconds", 30));
        ((Charm) effect).setOverrideMaster(cs.getBoolean("override", false));
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
        effect = new StrifeParticle();
        Particle particle;
        try {
          particle = Particle.valueOf((cs.getString("particle-type", "FLAME")));
        } catch (Exception e) {
          LogUtil.printWarning("Invalid particle effect type in effect " + key + ". Skipping.");
          return;
        }
        ((StrifeParticle) effect).setParticle(particle);
        ParticleStyle style = ParticleStyle.valueOf(cs.getString("style", "NORMAL"));
        ((StrifeParticle) effect).setStyle(style);
        if (particle == Particle.SPELL_MOB || particle == Particle.SPELL_WITCH
            || particle == Particle.SPELL_INSTANT) {
          ((StrifeParticle) effect).setRed(cs.getDouble("red", 0) / 255D);
          ((StrifeParticle) effect).setBlue(cs.getDouble("blue", 0) / 255D);
          ((StrifeParticle) effect).setGreen(cs.getDouble("green", 0) / 255D);
        }
        if (style == ParticleStyle.ARC) {
          ((StrifeParticle) effect).setArcAngle(cs.getDouble("arc-angle", 30));
          ((StrifeParticle) effect).setArcOffset(cs.getDouble("arc-offset", 0));
        }
        if (style == ParticleStyle.ORBIT) {
          ((StrifeParticle) effect).setOrbitSpeed(cs.getDouble("orbit-speed", 1));
        }
        if (style == ParticleStyle.LINE) {
          ((StrifeParticle) effect).setOrbitSpeed(cs.getDouble("orbit-speed", 1));
          ((StrifeParticle) effect).setEndOrbitSpeed(cs.getDouble("end-orbit-speed", 2));
          ((StrifeParticle) effect).setRadius(cs.getDouble("radius", -1));
          ((StrifeParticle) effect).setEndRadius(cs.getDouble("end-radius", 1));
          ((StrifeParticle) effect).setLineVertical(cs.getBoolean("vertical", false));
          ((StrifeParticle) effect).setLineIncrement(cs.getDouble("line-increment", 0.25));
          ((StrifeParticle) effect).setLineOffset(cs.getDouble("line-offset", 0));
        }
        ((StrifeParticle) effect).setQuantity(cs.getInt("quantity", 10));
        ((StrifeParticle) effect).setTickDuration(cs.getInt("duration-ticks", 0));
        ((StrifeParticle) effect).setStrictDuration(cs.getBoolean("strict-duration", false));
        ((StrifeParticle) effect).setSpeed((float) cs.getDouble("speed", 0));
        ((StrifeParticle) effect).setSpread((float) cs.getDouble("spread", 1));
        ((StrifeParticle) effect).setParticleOriginLocation(
            OriginLocation.valueOf(cs.getString("origin", "HEAD")));
        ((StrifeParticle) effect).setSize(cs.getDouble("size", 1));
        String materialType = cs.getString("material", "");
        if (StringUtils.isNotBlank(materialType)) {
          ((StrifeParticle) effect).setBlockData(new ItemStack(Material.getMaterial(materialType)));
        }
        break;
    }
    if (effectType != Effect.EffectType.WAIT) {
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
    LogUtil.printDebug("Loaded effect " + key + " successfully.");
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
        condition = new StatCondition(stat);
        break;
      case ATTRIBUTE:
        StrifeAttribute attribute = plugin.getAttributeManager()
            .getAttribute(cs.getString("attribute", null));
        if (attribute == null) {
          LogUtil.printError("Failed to load condition " + key + ". Invalid attribute.");
          return;
        }
        condition = new AttributeCondition(attribute);
        break;
      case BARRIER:
        boolean percent = cs.getBoolean("percentage", false);
        condition = new BarrierCondition(percent);
        break;
      case BUFF:
        int stacks = cs.getInt("stacks", 1);
        String buffId = cs.getString("buff-id", "");
        condition = new BuffCondition(buffId, stacks);
        break;
      case LORE:
        String loreId = cs.getString("lore-id", "");
        boolean inverted = cs.getBoolean("inverted", false);
        condition = new LoreCondition(loreId, inverted);
        break;
      case ENDLESS_EFFECT:
        boolean state = cs.getBoolean("state", true);
        condition = new EndlessEffectCondition(state);
        EndlessEffectCondition finalCondition = (EndlessEffectCondition) condition;
        Bukkit.getScheduler().runTaskLater(StrifePlugin.getInstance(), () -> {
          Effect newEffect = getEffect(cs.getString("effect-id", ""));
          if (newEffect instanceof EndlessEffect) {
            finalCondition.setEndlessEffect((EndlessEffect) newEffect);
          }
        }, 5L);
      case CHANCE:
        float chance = (float) cs.getDouble("chance", 0.5);
        condition = new ChanceCondition(chance);
        break;
      case LIGHT_LEVEL:
        condition = new LightCondition();
        break;
      case HEALTH:
        boolean percent2 = cs.getBoolean("percentage", false);
        condition = new HealthCondition(percent2);
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
        condition = new PotionCondition(potionEffectType, potionIntensity);
        break;
      case WEAPONS:
        Set<EquipmentItemData> equipmentItemData = new HashSet<>();
        ConfigurationSection ms = cs.getConfigurationSection("item-info");
        for (String matKey : ms.getKeys(false)) {
          EquipmentItemData data = new EquipmentItemData();
          try {
            data.setMaterial(Material.valueOf(matKey));
          } catch (Exception e) {
            LogUtil.printError("Failed to load " + key + ". Invalid material type (" + ms + ")");
            return;
          }
          data.setMinData(ms.getInt(matKey + ".min-data", -1));
          data.setMaxData(ms.getInt(matKey + ".max-data", -1));
          equipmentItemData.add(data);
        }
        boolean strict = cs.getBoolean("strict", false);
        condition = new WeaponsCondition(equipmentItemData, strict);
        break;
      case MOVING:
        condition = new MovingCondition(cs.getBoolean("state", true));
        break;
      case NEARBY_ENTITIES:
        int range = cs.getInt("range", 1);
        condition = new NearbyEntitiesCondition(cs.getBoolean("friendly", true), range);
        break;
      case IN_COMBAT:
        condition = new InCombatCondition(cs.getBoolean("state", true));
        break;
      case TIME:
        long minTime = cs.getLong("min-time", 0);
        long maxTime = cs.getLong("max-time", 0);
        condition = new TimeCondition(minTime, maxTime);
        break;
      case LEVEL:
        condition = new LevelCondition();
        break;
      case BONUS_LEVEL:
        condition = new BonusLevelCondition();
        break;
      case ITS_OVER_ANAKIN:
        condition = new HeightCondition();
        break;
      case BLEEDING:
        condition = new BleedingCondition(cs.getBoolean("state", true));
        break;
      case DARKNESS:
        condition = new CorruptionCondition();
        break;
      case RANGE:
        condition = new RangeCondition();
        break;
      case BURNING:
        condition = new BurningCondition(cs.getBoolean("state", true));
        break;
      case EARTH_RUNES:
        condition = new EarthRunesCondition();
        break;
      case BLOCKING:
        condition = new BlockingCondition(cs.getBoolean("state", true));
        break;
      case GROUNDED:
        condition = new GroundedCondition(cs.getBoolean("inverted", false));
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
      case UNIQUE_ID:
        String uniqueId = cs.getString("unique-id");
        condition = new UniqueCondition(uniqueId);
        break;
      case FACTION_MEMBER:
        String factionId = cs.getString("faction-id");
        condition = new FactionCondition(factionId);
        break;
      case VELOCITY:
        VelocityType velocityType = VelocityType.valueOf(cs.getString("type", "TOTAL"));
        boolean absolute = cs.getBoolean("absolute", true);
        condition = new VelocityCondition(velocityType, absolute);
        break;
      default:
        LogUtil.printError("No valid conditions found for " + key + "... somehow?");
        return;
    }

    Comparison comparison;
    try {
      comparison = Comparison.valueOf(cs.getString("comparison", "NONE").toUpperCase());
    } catch (Exception e) {
      LogUtil.printWarning("No/invalid comparison found for " + key + " defaulting to NONE");
      comparison = Comparison.NONE;
    }

    CompareTarget compareTarget;
    try {
      compareTarget = CompareTarget.valueOf(cs.getString("target", "SELF"));
    } catch (Exception e) {
      LogUtil.printWarning("No/invalid compare target found for " + key + " defaulting to SELF");
      compareTarget = CompareTarget.SELF;
    }

    ConditionUser conditionUser;
    try {
      conditionUser = ConditionUser.valueOf(cs.getString("user", "ANY"));
    } catch (Exception e) {
      LogUtil.printWarning("No/invalid condition user found for " + key + " defaulting to ANY");
      conditionUser = ConditionUser.ANY;
    }

    condition.setCompareTarget(compareTarget);
    condition.setComparison(comparison);
    condition.setConditionUser(conditionUser);
    condition.setType(conditionType);
    condition.setValue((float) cs.getDouble("value", 0));

    conditions.put(key, condition);
  }

  public List<Effect> getEffects(List<String> effectIds) {
    List<Effect> effects = new ArrayList<>();
    for (String s : effectIds) {
      Effect effect = getEffect(s);
      if (effect == null) {
        LogUtil.printWarning(" Failed to add unknown effect '" + s + "'");
        continue;
      }
      effects.add(effect);
      LogUtil.printDebug(" Added effect '" + s + "");
    }
    return effects;
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

}
