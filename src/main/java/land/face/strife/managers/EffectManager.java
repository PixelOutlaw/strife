package land.face.strife.managers;

import static land.face.strife.util.PlayerDataUtil.getName;

import io.pixeloutlaw.minecraft.spigot.garbage.ListExtensionsKt;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import land.face.strife.StrifePlugin;
import land.face.strife.data.BonusDamage;
import land.face.strife.data.EquipmentItemData;
import land.face.strife.data.LoadedChaser;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.TargetResponse;
import land.face.strife.data.ability.Ability;
import land.face.strife.data.champion.LifeSkillType;
import land.face.strife.data.champion.StrifeAttribute;
import land.face.strife.data.conditions.AliveCondition;
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
import land.face.strife.data.conditions.EnergyCondition;
import land.face.strife.data.conditions.EntityTypeCondition;
import land.face.strife.data.conditions.FactionCondition;
import land.face.strife.data.conditions.FlyingCondition;
import land.face.strife.data.conditions.FrostCondition;
import land.face.strife.data.conditions.GroundedCondition;
import land.face.strife.data.conditions.HealthCondition;
import land.face.strife.data.conditions.HeightCondition;
import land.face.strife.data.conditions.InCombatCondition;
import land.face.strife.data.conditions.LevelCondition;
import land.face.strife.data.conditions.LightCondition;
import land.face.strife.data.conditions.LoreCondition;
import land.face.strife.data.conditions.MinionCondition;
import land.face.strife.data.conditions.MovingCondition;
import land.face.strife.data.conditions.NearbyEntitiesCondition;
import land.face.strife.data.conditions.PotionCondition;
import land.face.strife.data.conditions.RageCondition;
import land.face.strife.data.conditions.RangeCondition;
import land.face.strife.data.conditions.StatCondition;
import land.face.strife.data.conditions.StealthCondition;
import land.face.strife.data.conditions.TimeCondition;
import land.face.strife.data.conditions.UniqueCondition;
import land.face.strife.data.conditions.VelocityCondition;
import land.face.strife.data.conditions.VelocityCondition.VelocityType;
import land.face.strife.data.conditions.WeaponsCondition;
import land.face.strife.data.effects.*;
import land.face.strife.data.effects.AreaEffect.AreaType;
import land.face.strife.data.effects.AreaEffect.LineOfSight;
import land.face.strife.data.effects.AreaEffect.TargetingPriority;
import land.face.strife.data.effects.Effect.EffectType;
import land.face.strife.data.effects.Push.PushType;
import land.face.strife.data.effects.StrifeParticle.ParticleStyle;
import land.face.strife.stats.AbilitySlot;
import land.face.strife.stats.StrifeStat;
import land.face.strife.util.DamageUtil.AbilityMod;
import land.face.strife.util.DamageUtil.AttackType;
import land.face.strife.util.DamageUtil.DamageScale;
import land.face.strife.util.DamageUtil.DamageType;
import land.face.strife.util.DamageUtil.OriginLocation;
import land.face.strife.util.DisguiseUtil;
import land.face.strife.util.LogUtil;
import land.face.strife.util.PlayerDataUtil;
import land.face.strife.util.ProjectileUtil;
import land.face.strife.util.StatUtil;
import land.face.strife.util.TargetingUtil;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
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

  public void processEffectList(StrifeMob caster, TargetResponse response, List<Effect> effectList) {
    if (caster == null || caster.getEntity() == null ||
        (!caster.getEntity().isValid() && response.isCancelOnCasterDeath())) {
      return;
    }
    List<Effect> taskEffects = new ArrayList<>(effectList);
    List<Effect> taskChunk = new ArrayList<>();
    Iterator<Effect> runEffects = taskEffects.listIterator();
    while (runEffects.hasNext()) {
      Effect effect = runEffects.next();
      if (effect == null) {
        LogUtil.printWarning("Effect is null! Skipping...");
        continue;
      }
      if (effect instanceof Wait) {
        LogUtil.printDebug("Effects in this chunk: " + taskChunk.toString());
        executeEffectList(caster, response, taskChunk);
        runEffects.remove();
        List<Effect> newEffectList = new ArrayList<>();
        runEffects.forEachRemaining(newEffectList::add);
        Bukkit.getScheduler().runTaskLater(StrifePlugin.getInstance(), () ->
            processEffectList(caster, response, newEffectList), ((Wait) effect).getTickDelay());
        return;
      }
      taskChunk.add(effect);
      runEffects.remove();
      LogUtil.printDebug("Added effect " + effect.getId() + " to task list");
    }
    executeEffectList(caster, response, taskChunk);
  }

  public void executeEffectList(StrifeMob caster, TargetResponse response, List<Effect> effectList) {
    LogUtil.printDebug("Effect task started - " + effectList.toString());
    if (caster == null || caster.getEntity() == null) {
      LogUtil.printDebug("- Task cancelled, caster is missing");
      return;
    }
    for (Effect effect : effectList) {
      LogUtil.printDebug("- Executing effect " + effect.getId());
      execute(effect, caster, response);
    }
    LogUtil.printDebug("- Completed effect task.");
  }

  public void execute(Effect effect, StrifeMob caster, TargetResponse response) {
    if (response.getLocation() != null) {
      if (!(effect instanceof LocationEffect) && !effect.isForceTargetCaster()) {
        return;
      }
      if (!PlayerDataUtil.areConditionsMet(caster, null, effect.getConditions())) {
        return;
      }
      if (effect.isForceTargetCaster()) {
        effect.apply(caster, caster);
        return;
      }
      assert effect instanceof LocationEffect;
      LocationEffect locEffect = (LocationEffect) effect;
      locEffect.applyAtLocation(caster, response.getLocation());
      return;
    }
    for (LivingEntity le : response.getEntities()) {
      StrifeMob targetMob = plugin.getStrifeMobManager().getStatMob(le);
      if (!PlayerDataUtil.areConditionsMet(caster, targetMob, effect.getConditions())) {
        continue;
      }
      if (effect instanceof LocationEffect) {
        effect.apply(caster, effect.isForceTargetCaster() ? caster : targetMob);
        continue;
      }
      if (!response.isForce()) {
        if (effect.isFriendly() != TargetingUtil.isFriendly(caster, targetMob)) {
          continue;
        }
      }
      LogUtil.printDebug("-- Applying '" + effect.getId() + "' to " + getName(targetMob.getEntity()));
      effect.apply(caster, effect.isForceTargetCaster() ? caster : targetMob);
    }
  }

  public void loadEffect(String id, Effect e) {
    loadedEffects.put(id, e);
  }

  public void loadEffect(String key, ConfigurationSection cs) {
    Effect.setPlugin(plugin);
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
      case REVIVE -> {
        effect = new Revive();
        ((Revive) effect).setPercentLostExpRestored(cs.getDouble("percent-exp-restored", 1));
      }
      case HEAL -> {
        effect = new Heal();
        ((Heal) effect).setAmount((float) cs.getDouble("amount", 1));
        ((Heal) effect).setFlatBonus((float) cs.getDouble("flat-bonus", 0));
        ((Heal) effect).setDamageScale(DamageScale.valueOf(cs.getString("scale", "FLAT")));
        ((Heal) effect).setUseHealingPower(cs.getBoolean("use-healing-power", false));
        ((Heal) effect).setHealCaster(cs.getBoolean("heal-caster", false));
      }
      case FOOD -> {
        effect = new Food();
        ((Food) effect).setAmount(cs.getDouble("amount", 1));
      }
      case SET_FALL -> {
        effect = new SetFall();
        ((SetFall) effect).setAmount((float) cs.getDouble("amount", 0));
      }
      case RESTORE_BARRIER -> {
        effect = new RestoreBarrier();
        ((RestoreBarrier) effect).setAmount((float) cs.getDouble("amount", 1));
        ((RestoreBarrier) effect).setDamageScale(
            DamageScale.valueOf(cs.getString("scale", "FLAT")));
        ((RestoreBarrier) effect).setFlatBonus((float) cs.getDouble("flat-bonus", 0));
        ((RestoreBarrier) effect).setNewDelayTicks(cs.getInt("new-barrier-delay-ticks", -1));
      }
      case RESTORE_ENERGY -> {
        effect = new ChangeEnergy();
        ((ChangeEnergy) effect).setAmount((float) cs.getDouble("amount", 1));
        ((ChangeEnergy) effect).setDamageScale(DamageScale.valueOf(cs.getString("scale", "FLAT")));
      }
      case INCREASE_RAGE -> {
        effect = new ChangeRage();
        ((ChangeRage) effect).setAmount((float) cs.getDouble("amount", 1));
        ((ChangeRage) effect).setDamageScale(DamageScale.valueOf(cs.getString("scale", "FLAT")));
      }
      case DAMAGE -> {
        effect = new Damage();
        float attackMult = (float) cs.getDouble("attack-multiplier", 1D);
        ((Damage) effect).setAttackMultiplier(attackMult);
        float damageReductionRatio = (float) cs.getDouble("damage-reduction-ratio", 1D);
        ((Damage) effect).setDamageReductionRatio(damageReductionRatio);
        ((Damage) effect).setHealMultiplier(
            (float) cs.getDouble("heal-multiplier", Math.min(1.0, damageReductionRatio)));
        ((Damage) effect).setCanBeBlocked(cs.getBoolean("can-be-blocked", true));
        ((Damage) effect).setCanBeEvaded(cs.getBoolean("can-be-evaded", true));
        ((Damage) effect).setCanSneakAttack(cs.getBoolean("can-sneak-attack", false));
        ((Damage) effect).setApplyOnHitEffects(cs.getBoolean("apply-on-hit-effects",
            attackMult >= 0.6 || damageReductionRatio >= 0.6));
        ((Damage) effect).setShowPopoffs(cs.getBoolean("show-popoffs", true));
        ((Damage) effect).setBypassBarrier(cs.getBoolean("bypass-barrier", false));
        ((Damage) effect).setGuardBreak(cs.getBoolean("guard-break", false));
        ((Damage) effect).setSelfInflict(cs.getBoolean("self-inflict", false));
        List<String> hitEffects = cs.getStringList("hit-effects");
        delayedSetEffects(((Damage) effect).getHitEffects(), hitEffects, key, false);
        List<String> killEffects = cs.getStringList("kill-effects");
        delayedSetEffects(((Damage) effect).getKillEffects(), killEffects, key, false);
        ((Damage) effect).setAttackType(AttackType.valueOf(cs.getString("attack-type", "OTHER")));
        ConfigurationSection multCs = cs.getConfigurationSection("damage-multipliers");
        Map<DamageType, Float> multMap = new HashMap<>();
        if (multCs != null) {
          for (String k : multCs.getKeys(false)) {
            DamageType mod = DamageType.valueOf(k);
            multMap.put(mod, (float) multCs.getDouble(k));
          }
        }
        List<BonusDamage> bonusDamages = loadBonusDamages(key,
            cs.getConfigurationSection("bonus-damages"));
        ConfigurationSection modsCs = cs.getConfigurationSection("attack-mods");
        Map<AbilityMod, Float> attackModMap = new HashMap<>();
        if (modsCs != null) {
          for (String k : modsCs.getKeys(false)) {
            AbilityMod mod = AbilityMod.valueOf(k);
            attackModMap.put(mod, (float) modsCs.getDouble(k));
          }
        }
        ((Damage) effect).getDamageMultipliers().putAll(multMap);
        ((Damage) effect).getBonusDamages().addAll(bonusDamages);
        ((Damage) effect).getAbilityMods().putAll(attackModMap);
      }
      case WORLD_SPACE_ENTITY -> {
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
        ((CreateWorldSpaceEntity) effect).setMaxDisplacement(
            (float) cs.getDouble("max-displacement", 0));
        ((CreateWorldSpaceEntity) effect).setOriginLocation(
            OriginLocation.valueOf(cs.getString("origin", "HEAD")));
        ((CreateWorldSpaceEntity) effect).setVelocity(cs.getDouble("speed", 0));
        ((CreateWorldSpaceEntity) effect).setFriction((float) cs.getDouble("friction", 1));
        ((CreateWorldSpaceEntity) effect).setGravity((float) cs.getDouble("gravity", 0));
        ((CreateWorldSpaceEntity) effect).setStrictDuration(cs.getBoolean("strict-duration", true));
        ((CreateWorldSpaceEntity) effect).setZeroVerticalAxis(cs.getBoolean("zero-y-axis", false));
      }
      case CHASER -> {
        effect = new ChaserEffect();
        LoadedChaser data = plugin.getChaserManager().loadChaser(key, cs);
        ((ChaserEffect) effect).setOriginLocation(
            OriginLocation.valueOf(cs.getString("origin", "HEAD")));
        ((ChaserEffect) effect).setLoadedChaser(data);
        ((ChaserEffect) effect).setCanLocationOverride(cs.getBoolean("location-override", false));
        ((ChaserEffect) effect).setChaseCaster(cs.getBoolean("chase-caster", false));
      }
      case CONSOLE_COMMAND -> {
        effect = new ConsoleCommand();
        String cmd = cs.getString("command", "broadcast REEE");
        ((ConsoleCommand) effect).setCommand(cmd);
      }
      case REMOVE_ENTITY -> effect = new RemoveEntity();
      case MINION_CAST -> {
        effect = new MinionCast();
        ((MinionCast) effect).setUniqueId(cs.getString("unique-id", null));
        Effect finalEffect1 = effect;
        Bukkit.getScheduler().runTaskLater(StrifePlugin.getInstance(), () -> {
          Ability ability = plugin.getAbilityManager().getAbility(cs.getString("ability-id", null));
          ((MinionCast) finalEffect1).setAbility(ability);
        }, 5L);
      }
      case COUNTER -> {
        effect = new Counter();
        ((Counter) effect).setDuration(cs.getInt("duration", 500));
        ((Counter) effect).setRemoveOnTrigger(cs.getBoolean("remove-on-trigger", false));
        List<String> counterEffects = cs.getStringList("effects");
        delayedSetEffects(((Counter) effect).getEffects(), counterEffects, key, false);
      }
      case AREA_EFFECT -> {
        effect = new AreaEffect();
        List<String> areaEffects = cs.getStringList("effects");
        List<String> filterConditions = cs.getStringList("filter-conditions");
        AreaEffect areaEffect = (AreaEffect) effect;
        Bukkit.getScheduler().runTaskLater(StrifePlugin.getInstance(), () -> {
          for (String s : filterConditions) {
            areaEffect.getFilterConditions().add(getConditions().get(s));
          }
        }, 5L);
        delayedSetEffects(((AreaEffect) effect).getEffects(), areaEffects, key, false);
        ((AreaEffect) effect).setRange(cs.getDouble("range", 1));
        ((AreaEffect) effect).setMaxTargets(cs.getInt("max-targets", -1));
        ((AreaEffect) effect).setScaleTargetsWithMultishot(
            cs.getBoolean("scale-targets-with-multishot", false));
        ((AreaEffect) effect).setLineOfSight(
            LineOfSight.valueOf(cs.getString("line-of-sight", "CASTER")));
        ((AreaEffect) effect).setAreaType(AreaType.valueOf(cs.getString("area-type", "RADIUS")));
        boolean canBeBlocked = cs.getBoolean("can-be-blocked", false);
        ((AreaEffect) effect).setCanBeBlocked(canBeBlocked);
        ((AreaEffect) effect).setCanBeCountered(cs.getBoolean("can-be-countered", canBeBlocked));
        ((AreaEffect) effect).setCanBeEvaded(cs.getBoolean("can-be-evaded", false));
        ((AreaEffect) effect).setTargetingCooldown(cs.getLong("target-cooldown", 0));
        ((AreaEffect) effect).setRadius((float) cs.getDouble("radius", 0.55));
        if (((AreaEffect) effect).getMaxTargets() != -1) {
          ((AreaEffect) effect).setPriority(
              TargetingPriority.valueOf(cs.getString("priority", "RANDOM")));
        }
      }
      case ENDLESS_EFFECT -> {
        effect = new EndlessEffect();
        List<String> cancelConditions = cs.getStringList("cancel-conditions");
        ((EndlessEffect) effect).setMaxDuration((float) cs.getDouble("max-duration-seconds", 30));
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
        delayedSetEffects(((EndlessEffect) effect).getRunEffects(), runEffects, key, false);
        delayedSetEffects(((EndlessEffect) effect).getCancelEffects(), cancelEffects, key, false);
        delayedSetEffects(((EndlessEffect) effect).getExpiryEffects(), expiryEffects, key, false);
      }
      case CANCEL_ENDLESS_EFFECT -> {
        effect = new CancelEndlessEffect();
        CancelEndlessEffect cancelEndlessEffect = (CancelEndlessEffect) effect;
        Bukkit.getScheduler().runTaskLater(StrifePlugin.getInstance(), () -> {
          Effect newEffect = getEffect(cs.getString("effect-id", ""));
          if (newEffect instanceof EndlessEffect) {
            cancelEndlessEffect.setEndlessEffect((EndlessEffect) newEffect);
          }
        }, 5L);
      }
      case RIPTIDE -> {
        effect = new Riptide();
        ((Riptide) effect).setTicks(cs.getInt("ticks", 40) / 2);
      }
      case PROJECTILE -> {
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
        ((ShootProjectile) effect).setOriginType(
            OriginLocation.valueOf(cs.getString("origin", "HEAD")));
        ((ShootProjectile) effect).setVerticalBonus(cs.getDouble("vertical-bonus", 0));
        ((ShootProjectile) effect).setSpread(cs.getDouble("spread", 0));
        ((ShootProjectile) effect).setRadialAngle(cs.getDouble("radial-angle", 0));
        ((ShootProjectile) effect).setSpeed((float) cs.getDouble("speed", 1));
        ((ShootProjectile) effect).setYield((float) cs.getDouble("yield", 0.0D));
        ((ShootProjectile) effect).setMaxDuration(cs.getInt("max-duration", -1));
        ((ShootProjectile) effect).setIgnite(cs.getBoolean("ignite", false));
        ((ShootProjectile) effect).setBounce(cs.getBoolean("bounce", false));
        ((ShootProjectile) effect).setIgnoreMultishot(cs.getBoolean("ignore-multishot", false));
        ((ShootProjectile) effect).setZeroPitch(cs.getBoolean("zero-pitch", false));
        ((ShootProjectile) effect).setSilent(cs.getBoolean("silent", false));
        ((ShootProjectile) effect).setGravity(cs.getBoolean("gravity", true));
        ((ShootProjectile) effect).setThrowItem(cs.getBoolean("thrown-item", false));
        ((ShootProjectile) effect).setThrowSpin(cs.getBoolean("throw-spin", true));
        ((ShootProjectile) effect).setBlockHitEffects(cs.getBoolean("effects-on-block-hit", false));
        ((ShootProjectile) effect).setAttackMultiplier(cs.getDouble("attack-multiplier", 0D));
        ((ShootProjectile) effect).setDisguise(DisguiseUtil.parseDisguise(
            cs.getConfigurationSection("disguise"), key, false));
        String thrownStackMaterial = cs.getString("thrown-stack-material");
        if (StringUtils.isNotBlank(thrownStackMaterial)) {
          ItemStack stack = new ItemStack(Material.valueOf(thrownStackMaterial));
          stack.setAmount(cs.getInt("thrown-stack-amount", 1));
          ItemStackExtensionsKt.setCustomModelData(stack, cs.getInt("thrown-stack-custom-data", 0));
          ((ShootProjectile) effect).setThrownStack(stack);
        }
        ((ShootProjectile) effect).setTargeted(cs.getBoolean("targeted", false));
        ((ShootProjectile) effect).setSeeking(cs.getBoolean("seeking", false));
        int color = cs.getInt("arrow-rgb-color", -1);
        if (color != -1) {
          ((ShootProjectile) effect).setArrowColor(Color.fromRGB(color));
        }
        List<String> effects = cs.getStringList("hit-effects");
        delayedSetEffects(((ShootProjectile) effect).getHitEffects(), effects, key, false);
      }
      case EQUIPMENT_SWAP -> {
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
      }
      case EVOKER_FANGS -> {
        effect = new EvokerFangEffect();
        ((EvokerFangEffect) effect).setQuantity(cs.getInt("quantity", 1));
        ((EvokerFangEffect) effect).setSpread((float) cs.getDouble("spread", 0));
        delayedSetEffects(((EvokerFangEffect) effect).getHitEffects(),
            cs.getStringList("hit-effects"), key, false);
      }
      case FALLING_BLOCK -> {
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
        ((ShootBlock) effect).setVerticalBonus(cs.getDouble("vertical-bonus", 0));
        ((ShootBlock) effect).setSpread(cs.getDouble("spread", 0));
        ((ShootBlock) effect).setSpeed(cs.getDouble("speed", 1));
        ((ShootBlock) effect).setZeroPitch(cs.getBoolean("zero-pitch", false));
        ((ShootBlock) effect).setHitEffects(cs.getStringList("hit-effects"));
      }
      case IGNITE -> {
        effect = new Ignite();
        ((Ignite) effect).setDuration(cs.getInt("duration", 20));
        ((Ignite) effect).setOverride(cs.getBoolean("override", false));
        ((Ignite) effect).setStrictDuration(cs.getBoolean("strict-duration", false));
        ((Ignite) effect).setAddDuration(cs.getBoolean("add-duration", false));
      }
      case FROST -> {
        effect = new Frost();
        ((Frost) effect).setDuration(cs.getInt("duration", 20));
        ((Frost) effect).setOverride(cs.getBoolean("override", false));
        ((Frost) effect).setStrictDuration(cs.getBoolean("strict-duration", false));
        ((Frost) effect).setAddDuration(cs.getBoolean("add-duration", true));
      }
      case SILENCE -> {
        effect = new Silence();
        ((Silence) effect).setDuration(cs.getInt("duration", 20));
      }
      case BLEED -> {
        effect = new Bleed();
        ((Bleed) effect).setAmount((float) cs.getDouble("amount", 10));
        ((Bleed) effect).setDamageScale(DamageScale.valueOf(cs.getString("scale", "FLAT")));
        ((Bleed) effect).setIgnoreArmor(cs.getBoolean("ignore-armor", true));
        ((Bleed) effect).setBypassBarrier(cs.getBoolean("bypass-barrier", false));
        ((Bleed) effect).setApplyBleedMods(cs.getBoolean("apply-bleed-mods", true));
      }
      case CORRUPT -> {
        effect = new Corrupt();
        ((Corrupt) effect).setAmount((float) cs.getDouble("amount", 10));
      }
      case ADD_EARTH_RUNES -> {
        effect = new AddEarthRunes();
        ((AddEarthRunes) effect).setAmount(cs.getInt("amount", 1));
      }
      case TELEPORT -> {
        effect = new Teleport();
        ((Teleport) effect).setTargeted(cs.getBoolean("targeted", true));
        double x = cs.getDouble("x", 0);
        double y = cs.getDouble("y", 0);
        double z = cs.getDouble("z", 0);
        ((Teleport) effect).setVector(new Vector(x, y, z));
        ((Teleport) effect).setRelative(cs.getBoolean("relative", false));
        ((Teleport) effect).getWorldSwapWhitelist()
            .addAll(cs.getStringList("world-swap-whitelist"));
        List<String> destEffects = cs.getStringList("destination-effects");
        List<String> originEffects = cs.getStringList("origin-effects");
        delayedSetEffects(((Teleport) effect).getDestinationEffects(), destEffects, key, true);
        delayedSetEffects(((Teleport) effect).getOriginEffects(), originEffects, key, true);
      }
      case TELEPORT_BEHIND -> effect = new TeleportBehind();
      case THRALL -> {
        effect = new Thrall();
        ((Thrall) effect).setName(cs.getString("name", "&8«&7Thrall&8»"));
        ((Thrall) effect).setLifeSeconds(cs.getInt("lifespan-seconds", 20));
      }
      case TITLE -> {
        effect = new Title();
        ((Title) effect).setTopTitle(cs.getString("upper", ""));
        ((Title) effect).setLowerTitle(cs.getString("lower", ""));
        ((Title) effect).setRange(cs.getDouble("range", 8));
      }
      case CONSUME_BLEED -> {
        effect = new ConsumeBleed();
        ((ConsumeBleed) effect).setDamageRatio(cs.getDouble("damage-ratio", 1));
        ((ConsumeBleed) effect).setHealRatio(cs.getDouble("heal-ratio", 1));
      }
      case CONSUME_CORRUPT -> {
        effect = new ConsumeCorrupt();
        ((ConsumeCorrupt) effect).setDamageRatio(cs.getDouble("damage-ratio", 1));
        ((ConsumeCorrupt) effect).setHealRatio(cs.getDouble("heal-ratio", 1));
      }
      case BUFF_EFFECT -> {
        effect = new BuffEffect();
        Effect finalEffect = effect;
        String buffId = cs.getString("buff-id");
        Bukkit.getScheduler().runTaskLater(StrifePlugin.getInstance(), () ->
            ((BuffEffect) finalEffect).setLoadedBuff(plugin.getBuffManager()
                .getBuffFromId(buffId)), 10L);
        ((BuffEffect) effect).setStrictDuration(cs.getBoolean("strict-duration", false));
      }
      case REMOVE_BUFF -> {
        effect = new RemoveBuff();
        ((RemoveBuff) effect).setBuffId(cs.getString("buff-id"));
        ((RemoveBuff) effect).setStacks(cs.getInt("stacks", Integer.MAX_VALUE));
        ((RemoveBuff) effect).setFromCaster(cs.getBoolean("from-caster", true));
      }
      case COOLDOWN_REDUCTION -> {
        effect = new CooldownReduction();
        ((CooldownReduction) effect).setAbilityString(cs.getString("ability-id"));
        ((CooldownReduction) effect).setSeconds(cs.getDouble("seconds"));
        String slot = cs.getString("ability-slot");
        if (StringUtils.isNotBlank(slot)) {
          ((CooldownReduction) effect).setSlot(AbilitySlot.valueOf(slot));
        }
      }
      case UNTOGGLE -> {
        effect = new UntoggleAbility();
        ((UntoggleAbility) effect).setAbilityString(cs.getString("ability-id"));
      }
      case WAIT -> {
        effect = new Wait();
        ((Wait) effect).setTickDelay(cs.getInt("duration", 20));
      }
      case SPEAK -> {
        effect = new Speak();
        ((Speak) effect).setMessages(
            ListExtensionsKt.chatColorize(cs.getStringList("messages")));
      }
      case PUSH -> {
        effect = new Push();
        ((Push) effect).setPower(cs.getDouble("power", 10));
        ((Push) effect).setHeight(cs.getDouble("height", 10));
        ((Push) effect).setCancelFall(cs.getBoolean("cancel-fall", false));
        ((Push) effect).setClamp(cs.getBoolean("clamp", true));
        ((Push) effect).setUncheckedHeight(cs.getBoolean("unchecked-height", false));
        ((Push) effect).setPushType(
            PushType.valueOf(cs.getString("push-type", "AWAY_FROM_CASTER")));
      }
      case SUMMON -> {
        effect = new Summon();
        ((Summon) effect).setAmount(cs.getInt("amount", 1));
        ((Summon) effect).setUniqueEntity(cs.getString("unique-entity"));
        ((Summon) effect).setLifespanSeconds(cs.getInt("lifespan-seconds", 30));
        ((Summon) effect).setLifeMult((float) cs.getDouble("life-multiplier", 1.0));
        ((Summon) effect).setSoundEffect(cs.getString("sound-effect-id", null));
        ((Summon) effect).setMount(cs.getBoolean("mount", false));
        ((Summon) effect).setClone(cs.getBoolean("clone", false));
      }
      case CHARM -> {
        effect = new Charm();
        ((Charm) effect).setChance((float) cs.getDouble("success-chance", 1));
        ((Charm) effect).setChancePerLevel((float) cs.getDouble("chance-per-level", 0));
        ((Charm) effect).setLifespanSeconds((float) cs.getDouble("lifespan-seconds", 30));
        ((Charm) effect).setOverrideMaster(cs.getBoolean("override", false));
      }
      case SWING -> {
        effect = new SwingArm();
        ((SwingArm) effect).setDelay(cs.getInt("delay", 0));
        ((SwingArm) effect).setRandom(cs.getBoolean("random", false));
        ((SwingArm) effect).setSlot(EquipmentSlot.valueOf(cs.getString("hand", "HAND")));
      }
      case DISGUISE -> {
        effect = new Disguise();
        ((Disguise) effect).setDisguise(DisguiseUtil.parseDisguise(
            cs.getConfigurationSection("disguise"), key, false));
        ((Disguise) effect).setDuration(cs.getInt("duration", -1));
      }
      case UNDISGUISE -> effect = new Undisguise();
      case MODEL_ANIMATION -> {
        effect = new ModelAnimation();
        ((ModelAnimation) effect).setAnimationId(cs.getString("animation-id"));
        ((ModelAnimation) effect).setLerpIn(cs.getInt("lerp-in", 5));
        ((ModelAnimation) effect).setLerpOut(cs.getInt("lerp-out", 5));
        ((ModelAnimation) effect).setSpeed(cs.getDouble("speed", 1));
      }
      case CREATE_MODEL -> {
        effect = new CreateModelAnimation();
        ((CreateModelAnimation) effect).setModelId(cs.getString("model-id"));
        ((CreateModelAnimation) effect).setAnimationId(cs.getString("animation-id"));
        ((CreateModelAnimation) effect).setLerpIn(cs.getInt("lerp-in", 5));
        ((CreateModelAnimation) effect).setLerpOut(cs.getInt("lerp-out", 5));
        ((CreateModelAnimation) effect).setSpeed(cs.getDouble("speed", 1));
        ((CreateModelAnimation) effect).setLifespan(cs.getInt("lifespan", 50));
        ((CreateModelAnimation) effect).setTargetLock(cs.getBoolean("target-lock", false));
        ((CreateModelAnimation) effect).setRotationLock(cs.getBoolean("rotation-lock", true));
      }
      case CHANGE_PART -> {
        effect = new ChangePart();
        ((ChangePart) effect).setNewModelId(cs.getString("model-id"));
        ((ChangePart) effect).setOldModelId(cs.getString("old-model-id"));
        ((ChangePart) effect).setNewPartId(cs.getString("new-part-id"));
        ((ChangePart) effect).setOldPartId(cs.getString("old-part-id"));
      }
      case TARGET -> {
        effect = new ForceTarget();
        ((ForceTarget) effect).setOverwrite(cs.getBoolean("overwrite", true));
        ((ForceTarget) effect).setCasterTargetsTarget(cs.getBoolean("caster-targets-target", true));
      }
      case FORCE_STAT -> {
        effect = new ForceStat();
        ((ForceStat) effect)
            .setStats(StatUtil.getStatMapFromSection(cs.getConfigurationSection("stats")));
      }
      case LIGHTNING -> effect = new Lightning();
      case STEALTH -> {
        effect = new Stealth();
        ((Stealth) effect).setRemoveStealth(cs.getBoolean("remove", false));
      }
      case STINGER -> {
        effect = new Stinger();
        ((Stinger) effect).setAmount(cs.getInt("amount", 1));
      }
      case MODIFY_PROJECTILE -> {
        effect = new ModifyProjectile();
        ((ModifyProjectile) effect).setFriendlyProjectiles(
            cs.getBoolean("friendly-projectiles", false));
        ((ModifyProjectile) effect).setRange(cs.getDouble("range", 1));
        ((ModifyProjectile) effect).setRemove(cs.getBoolean("remove", true));
        ((ModifyProjectile) effect).setSpeedMult(cs.getDouble("speed-mult", 0.5));
      }
      case POTION -> {
        effect = new PotionEffectAction();
        PotionEffectType potionType;
        try {
          potionType = PotionEffectType.getByName(cs.getString("effect"));
        } catch (Exception e) {
          LogUtil.printWarning("Invalid potion effect type in effect " + key + ". Skipping.");
          return;
        }
        ((PotionEffectAction) effect).setRemove(cs.getBoolean("remove", false));
        ((PotionEffectAction) effect).setPotionEffectType(potionType);
        ((PotionEffectAction) effect).setIntensity(cs.getInt("intensity", 0));
        ((PotionEffectAction) effect).setDuration(cs.getInt("duration", 0));
        ((PotionEffectAction) effect).setStrictDuration(cs.getBoolean("strict-duration", false));
        ((PotionEffectAction) effect).setBumpUpToIntensity(
            cs.getBoolean("bump-up-to-intensity", false));
      }
      case SOUND -> {
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
      }
      case FIREWORK -> {
        effect = new FireworkBurst();
        try {
          ((FireworkBurst) effect).setEffectType(Type.valueOf((cs.getString("effect-type"))));
        } catch (Exception e) {
          LogUtil.printWarning("Invalid firework effect type in effect " + key + ". Skipping.");
          return;
        }
        int colorOne = Integer.parseInt(cs.getString("color-one", "0xFFFFFF"));
        ((FireworkBurst) effect).setColorOne(Color.fromRGB(colorOne));
        int colorTwo = Integer.parseInt(cs.getString("color-two", "0xFFFFFF"));
        ((FireworkBurst) effect).setColorTwo(Color.fromRGB(colorTwo));
        ((FireworkBurst) effect).setFlicker(cs.getBoolean("flicker", false));
        ((FireworkBurst) effect).setTrail(cs.getBoolean("trail", false));
      }
      case SPAWN_ITEM -> {
        effect = new SpawnItem();
        ((SpawnItem) effect).setItemId(cs.getString("item-id", "sneed"));
        ((SpawnItem) effect).setCanPickup(cs.getBoolean("can-pickup", true));
        ((SpawnItem) effect).setNaturalDrop(cs.getBoolean("natural-drop", true));
        ((SpawnItem) effect).setProtectTicks(cs.getInt("theft-prevention-ticks", 200));
      }
      case PARTICLE -> {
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
            || particle == Particle.SPELL_INSTANT || particle == Particle.GLOW
            || particle == Particle.ELECTRIC_SPARK) {
          ((StrifeParticle) effect).setRed(cs.getDouble("red", 0) / 255D);
          ((StrifeParticle) effect).setBlue(cs.getDouble("blue", 0) / 255D);
          ((StrifeParticle) effect).setGreen(cs.getDouble("green", 0) / 255D);
        }
        if (style == ParticleStyle.ARC || style == ParticleStyle.CLAW) {
          ((StrifeParticle) effect).setArcAngle(cs.getDouble("arc-angle", 30));
          ((StrifeParticle) effect).setArcOffset(cs.getDouble("arc-offset", 0));
        }
        if (style == ParticleStyle.ORBIT) {
          ((StrifeParticle) effect).setOrbitSpeed((float) cs.getDouble("orbit-speed", 1));
        }
        if (style == ParticleStyle.LINE) {
          ((StrifeParticle) effect).setOrbitSpeed((float) cs.getDouble("orbit-speed", 1));
          ((StrifeParticle) effect).setRadius((float) cs.getDouble("radius", -1));
          ((StrifeParticle) effect).setEndRadius((float) cs.getDouble("end-radius", 1));
          ((StrifeParticle) effect).setLineVertical(cs.getBoolean("vertical", false));
          ((StrifeParticle) effect).setLineIncrement((float) cs.getDouble("line-increment", 0.25));
          ((StrifeParticle) effect).setLineOffset((float) cs.getDouble("line-offset", 0));
          ((StrifeParticle) effect).setAngleRotation((float) cs.getDouble("line-angle-offset", 1));
        }
        ((StrifeParticle) effect).setQuantity(cs.getInt("quantity", 10));
        ((StrifeParticle) effect).setTickDuration(cs.getInt("duration-ticks", 0));
        ((StrifeParticle) effect).setStrictDuration(cs.getBoolean("strict-duration", false));
        ((StrifeParticle) effect).setSpeed((float) cs.getDouble("speed", 0));
        ((StrifeParticle) effect).setSpread((float) cs.getDouble("spread", 1));
        ((StrifeParticle) effect).setOrigin(
            OriginLocation.valueOf(cs.getString("origin", "HEAD")));
        ((StrifeParticle) effect).setSize((float) cs.getDouble("size", 1));
        String materialType = cs.getString("material", "");
        if (StringUtils.isNotBlank(materialType)) {
          ((StrifeParticle) effect).setItemData(new ItemStack(Material.getMaterial(materialType)));
        }
      }
    }
    if (effect instanceof LocationEffect) {
      ((LocationEffect) effect).setOrigin(OriginLocation.valueOf(cs.getString("origin", "HEAD")));
    }
    if (effectType != Effect.EffectType.WAIT) {
      effect.setForceTargetCaster(cs.getBoolean("force-target-caster", false));
      effect.setFriendly(cs.getBoolean("friendly", false));
      Map<StrifeStat, Float> statMults = StatUtil
          .getStatMapFromSection(cs.getConfigurationSection("stat-mults"));
      effect.setStatMults(statMults);
      Map<LifeSkillType, Float> skillMults = StatUtil
          .getSkillMapFromSection(cs.getConfigurationSection("skill-multipliers"));
      effect.setSkillMults(skillMults);
      Map<StrifeAttribute, Float> attributeMults = StatUtil
          .getAttributeMapFromSection(cs.getConfigurationSection("attribute-multipliers"));
      effect.setAttributeMults(attributeMults);
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

  private List<BonusDamage> loadBonusDamages(String effectId, ConfigurationSection section) {
    List<BonusDamage> damages = new ArrayList<>();
    if (section == null) {
      return damages;
    }
    for (String k : section.getKeys(false)) {
      ConfigurationSection bonus = section.getConfigurationSection(k);
      DamageType type;
      DamageScale scale;
      StrifeStat stat = null;
      try {
        type = DamageType.valueOf(bonus.getString("damage-type"));
        scale = DamageScale.valueOf(bonus.getString("damage-scale", "FLAT"));
        String statString = bonus.getString("damage-stat", "");
        if (StringUtils.isNotBlank(statString)) {
          stat = StrifeStat.valueOf(statString);
        }
      } catch (Exception e) {
        LogUtil.printWarning("Check config for " + effectId + " invalid bonus dmg " + k);
        continue;
      }
      double amount = bonus.getDouble("amount", 0);
      damages.add(new BonusDamage(scale, type, stat, (float) amount));
    }
    return damages;
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
      case ALIVE:
        condition = new AliveCondition();
        break;
      case HEALTH:
        boolean percent3 = cs.getBoolean("percentage", false);
        condition = new HealthCondition(percent3);
        break;
      case BARRIER:
        boolean percent = cs.getBoolean("percentage", false);
        condition = new BarrierCondition(percent);
        break;
      case FROST:
        condition = new FrostCondition();
        break;
      case RAGE:
        boolean percent4 = cs.getBoolean("percentage", false);
        condition = new RageCondition(percent4);
        break;
      case ENERGY:
        boolean percent2 = cs.getBoolean("percentage", false);
        condition = new EnergyCondition(percent2);
        break;
      case BUFF:
        int stacks = cs.getInt("stacks", 1);
        String buffId = cs.getString("buff-id", "");
        condition = new BuffCondition(buffId, stacks);
        break;
      case LORE:
        String loreId = cs.getString("lore-id", "");
        condition = new LoreCondition(loreId);
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
      case MINION:
        condition = new MinionCondition(cs.getBoolean("is-owner", false));
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
        ((RangeCondition) condition).setRangeSquared(Math.pow(cs.getDouble("value", 0), 2));
        break;
      case STEALTHED:
        condition = new StealthCondition();
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
        condition = new GroundedCondition(cs.getBoolean("strict", false));
        break;
      case ENTITY_TYPE:
        List<String> entityTypes = cs.getStringList("types");
        boolean whitelist = cs.getBoolean("whitelist", true);
        boolean useDisguise = cs.getBoolean("use-disguise-type", true);
        Set<EntityType> typesSet = new HashSet<>();
        try {
          for (String s : entityTypes) {
            typesSet.add(EntityType.valueOf(s));
          }
        } catch (Exception e) {
          LogUtil.printError("Failed to load condition " + key + ". Invalid entity type!");
          return;
        }
        condition = new EntityTypeCondition(typesSet, whitelist, useDisguise);
        break;
      case UNIQUE_ID:
        String uniqueId = cs.getString("unique-id");
        condition = new UniqueCondition(uniqueId);
        break;
      case FACTION_MEMBER:
        String factionId = cs.getString("faction-id");
        condition = new FactionCondition(factionId);
        break;
      case FLYING:
        condition = new FlyingCondition();
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

    condition.setInverted(cs.getBoolean("inverted", false));
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

  private void delayedSetEffects(List<Effect> effects, List<String> effectIds, String effectId, boolean locationOnly) {
    effects.clear();
    Bukkit.getScheduler().runTaskLater(StrifePlugin.getInstance(), () -> {
      for (String s : effectIds) {
        Effect effect = getEffect(s);
        if (effect == null) {
          LogUtil.printWarning("Attempted to load invalid effect " + s + " for " + effectId);
          continue;
        }
        if (locationOnly && !(effect instanceof LocationEffect)) {
          LogUtil.printWarning("Non-location effect " + effect.getId() + " used for " + effectId
              + "! This effect's sub-effects may only be location type effects");
          continue;
        }
        effects.add(effect);
      }
    }, 5L);
  }
}
