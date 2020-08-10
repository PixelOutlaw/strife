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
import io.pixeloutlaw.minecraft.spigot.garbage.StringExtensionsKt;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import land.face.strife.StrifePlugin;
import land.face.strife.data.BonusDamage;
import land.face.strife.data.DamageModifiers;
import land.face.strife.data.DamageModifiers.ElementalStatus;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.TargetResponse;
import land.face.strife.data.ability.EntityAbilitySet.TriggerAbilityType;
import land.face.strife.data.buff.LoadedBuff;
import land.face.strife.data.champion.LifeSkillType;
import land.face.strife.data.effects.Effect;
import land.face.strife.events.BlockEvent;
import land.face.strife.events.CriticalEvent;
import land.face.strife.events.EvadeEvent;
import land.face.strife.events.SneakAttackEvent;
import land.face.strife.listeners.CombatListener;
import land.face.strife.managers.BlockManager;
import land.face.strife.managers.CorruptionManager;
import land.face.strife.managers.IndicatorManager.IndicatorStyle;
import land.face.strife.stats.StrifeStat;
import land.face.strife.stats.StrifeTrait;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.api.GuildsAPI;
import me.glaremasters.guilds.guild.Guild;
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
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class DamageUtil {

  private static StrifePlugin plugin;
  private static GuildsAPI guildsAPI;

  private static final String ATTACK_BLOCKED = TextUtils.color("&f&lBlocked!");
  private static final String ATTACK_DODGED = TextUtils.color("&f&lDodge!");
  public static double EVASION_THRESHOLD;

  private static final DamageModifier[] MODIFIERS = EntityDamageEvent.DamageModifier.values();
  public static final DamageType[] DMG_TYPES = DamageType.values();

  private static final float BLEED_PERCENT = 0.5f;

  private static float PVP_MULT;

  private static final Random RANDOM = new Random(System.currentTimeMillis());

  public static void refresh() {
    plugin = StrifePlugin.getInstance();
    guildsAPI = Guilds.getApi();
    EVASION_THRESHOLD = plugin.getSettings().getDouble("config.mechanics.evasion-threshold", 0.5);
    PVP_MULT = (float) plugin.getSettings().getDouble("config.mechanics.pvp-multiplier", 0.5);
  }

  public static void applyExtraEffects(StrifeMob attacker, StrifeMob defender, List<String> effects) {
    if (effects == null) {
      return;
    }
    Set<LivingEntity> entities = new HashSet<>();
    entities.add(defender.getEntity());
    TargetResponse response = new TargetResponse(entities);

    List<Effect> effectList = new ArrayList<>();
    for (String s : effects) {
      Effect effect = plugin.getEffectManager().getEffect(s);
      if (effect != null) {
        effectList.add(effect);
      }
    }
    if (effectList.isEmpty()) {
      return;
    }
    plugin.getEffectManager().executeEffectList(attacker, response, effectList);
  }

  public static boolean isGuildAlly(StrifeMob attacker, Player target) {
    Guild guild = guildsAPI.getGuildHandler().getGuild(target);
    if (guild == null) {
      return false;
    }
    if (attacker.getAlliedGuild().equals(guild.getId())) {
      return true;
    }
    for (UUID uuid : guild.getAllies()) {
      if (attacker.getAlliedGuild().equals(uuid)) {
        return true;
      }
    }
    return false;
  }

  public static boolean preDamage(StrifeMob attacker, StrifeMob defender, DamageModifiers mods) {

    if (attacker.getEntity() instanceof Player) {
      plugin.getChampionManager().updateEquipmentStats(
          plugin.getChampionManager().getChampion((Player) attacker.getEntity()));
    }
    if (defender.getEntity() instanceof Player) {
      plugin.getChampionManager().updateEquipmentStats(
          plugin.getChampionManager().getChampion((Player) defender.getEntity()));
    }

    if (plugin.getCounterManager().executeCounters(attacker.getEntity(), defender.getEntity())) {
      return false;
    }

    float attackMult = mods.getAttackMultiplier();

    if (mods.isCanBeEvaded()) {
      float evasionMultiplier = DamageUtil.getFullEvasionMult(attacker, defender, mods.getAbilityMods());
      if (evasionMultiplier < DamageUtil.EVASION_THRESHOLD) {
        if (defender.getEntity() instanceof Player) {
          plugin.getCombatStatusManager().addPlayer((Player) defender.getEntity());
        }
        DamageUtil.doEvasion(attacker, defender);
        TargetingUtil.expandMobRange(attacker.getEntity(), defender.getEntity());
        return false;
      }
      mods.setAttackMultiplier(attackMult * evasionMultiplier);
    }

    if (mods.isCanBeBlocked()) {
      if (plugin.getBlockManager().isAttackBlocked(attacker, defender, attackMult, mods.getAttackType(), mods.isBlocking())) {
        if (defender.getEntity() instanceof Player) {
          plugin.getCombatStatusManager().addPlayer((Player) defender.getEntity());
        }
        TargetingUtil.expandMobRange(attacker.getEntity(), defender.getEntity());
        DamageUtil.doReflectedDamage(defender, attacker, mods.getAttackType());
        return false;
      }
    }

    return true;
  }

  public static Map<DamageType, Float> buildDamage(StrifeMob attacker, StrifeMob defender, DamageModifiers mods) {
    Map<DamageType, Float> damageMap = DamageUtil.buildDamageMap(attacker, defender, mods);
    applyAttackTypeMods(attacker, mods.getAttackType(), damageMap);
    applyElementalEffects(attacker, defender, damageMap, mods);
    return damageMap;
  }


  public static void reduceDamage(StrifeMob attacker, StrifeMob defender,
      Map<DamageType, Float> damageMap, DamageModifiers mods) {
    DamageUtil.applyDamageReductions(attacker, defender, damageMap, mods.getAbilityMods());
  }

  public static float calculateFinalDamage(StrifeMob attacker, StrifeMob defender,
      Map<DamageType, Float> damageMap, DamageModifiers mods) {

    double standardDamage = damageMap.getOrDefault(DamageType.PHYSICAL, 0f) +
        damageMap.getOrDefault(DamageType.MAGICAL, 0f);
    double elementalDamage = damageMap.getOrDefault(DamageType.FIRE, 0f) +
        damageMap.getOrDefault(DamageType.ICE, 0f) +
        damageMap.getOrDefault(DamageType.LIGHTNING, 0f) +
        damageMap.getOrDefault(DamageType.DARK, 0f) +
        damageMap.getOrDefault(DamageType.EARTH, 0f) +
        damageMap.getOrDefault(DamageType.LIGHT, 0f);

    float potionMult = DamageUtil.getPotionMult(attacker.getEntity(), defender.getEntity());
    float critMult = 0;

    boolean criticalHit = standardDamage > 0.9 && isCriticalHit(attacker, defender, mods);
    if (criticalHit) {
      critMult = (attacker.getStat(StrifeStat.CRITICAL_DAMAGE) +
          mods.getAbilityMods().getOrDefault(AbilityMod.CRITICAL_DAMAGE, 0f)) / 100;
    }

    float pvpMult = 1f;
    if (attacker.getEntity() instanceof Player && defender.getEntity() instanceof Player) {
      pvpMult = PVP_MULT;
    }

    float generalDamageMultiplier = StatUtil.getDamageMult(attacker);

    standardDamage += standardDamage * critMult;
    standardDamage *= potionMult;
    standardDamage *= generalDamageMultiplier;
    standardDamage *= pvpMult;

    DamageUtil.applyLifeSteal(attacker, Math.min(standardDamage, defender.getEntity().getHealth()),
        mods.getHealMultiplier(), mods.getAbilityMods().getOrDefault(AbilityMod.LIFE_STEAL, 0f));

    if (criticalHit && attacker.hasTrait(StrifeTrait.ELEMENTAL_CRITS)) {
      elementalDamage += elementalDamage * critMult;
    }
    elementalDamage *= potionMult;
    elementalDamage *= generalDamageMultiplier;
    elementalDamage *= pvpMult;

    float damageReduction = defender.getStat(StrifeStat.DAMAGE_REDUCTION) * mods.getDamageReductionRatio() * pvpMult;
    float rawDamage = (float) Math.max(0D, (standardDamage + elementalDamage) - damageReduction);

    rawDamage *= DamageUtil.getRageMult(defender);

    if (mods.getAttackType() == AttackType.PROJECTILE) {
      rawDamage *= DamageUtil.getProjectileMultiplier(attacker, defender);
    }
    rawDamage *= DamageUtil.getTenacityMult(defender);
    rawDamage *= DamageUtil.getMinionMult(attacker);
    rawDamage += damageMap.getOrDefault(DamageType.TRUE_DAMAGE, 0f);

    if (mods.isSneakAttack() && !SpecialStatusUtil.isSneakImmune(defender.getEntity())) {
      rawDamage += doSneakAttack(attacker, defender, mods, pvpMult);
      boolean finishingBlow = rawDamage > defender.getEntity().getHealth() +
          plugin.getBarrierManager().getCurrentBarrier(defender);
      float gainedXp = plugin.getStealthManager().getSneakAttackExp(defender.getEntity(),
          attacker.getChampion().getLifeSkillLevel(LifeSkillType.SNEAK), finishingBlow);
      plugin.getSkillExperienceManager().addExperience((Player) attacker.getEntity(),
          LifeSkillType.SNEAK, gainedXp, false, false);
    }

    String damageString = String.valueOf((int) Math.ceil(rawDamage));
    if (criticalHit && (standardDamage > 1 || attacker.hasTrait(StrifeTrait.ELEMENTAL_CRITS))) {
      damageString = "&l" + damageString;
    }
    if (mods.isShowPopoffs() && attacker.getEntity() instanceof Player) {
      plugin.getIndicatorManager().addIndicator(attacker.getEntity(),
          defender.getEntity(), IndicatorStyle.RANDOM_POPOFF, 12, damageString);
    }
    if (mods.isShowPopoffs() && attacker.getMaster() != null && attacker.getMaster() instanceof Player) {
      plugin.getIndicatorManager().addIndicator(attacker.getMaster(),
          defender.getEntity(), IndicatorStyle.RANDOM_POPOFF, 12, "&7" + damageString);
    }

    defender.trackDamage(attacker, rawDamage);
    return rawDamage;
  }

  public static void postDamage(StrifeMob attacker, StrifeMob defender, DamageModifiers mods) {

    if (defender.getEntity() instanceof Player) {
      plugin.getStealthManager().unstealthPlayer((Player) defender.getEntity());
    }

    float ratio = mods.getDamageReductionRatio();

    DamageUtil.applyHealthOnHit(attacker, ratio, mods.getHealMultiplier(),
        mods.getAbilityMods().getOrDefault(AbilityMod.HEALTH_ON_HIT, 0f));
    DamageUtil.applyEnergyOnHit(attacker, ratio, mods.getHealMultiplier());

    DamageUtil.doReflectedDamage(defender, attacker, mods.getAttackType());

    if (attacker.getStat(StrifeStat.RAGE_ON_HIT) > 0.1) {
      plugin.getRageManager().changeRage(attacker, attacker.getStat(StrifeStat.RAGE_ON_HIT) * ratio);
    }
    if (defender.getStat(StrifeStat.RAGE_WHEN_HIT) > 0.1) {
      plugin.getRageManager().changeRage(defender, defender.getStat(StrifeStat.RAGE_WHEN_HIT));
    }
    if (defender.getStat(StrifeStat.ENERGY_WHEN_HIT) > 0.1) {
      DamageUtil.restoreEnergy(defender, defender.getStat(StrifeStat.ENERGY_WHEN_HIT));
    }

    plugin.getAbilityManager().abilityCast(defender, attacker, TriggerAbilityType.WHEN_HIT);
  }

  private static float doSneakAttack(StrifeMob attacker, StrifeMob defender, DamageModifiers mods, float pvpMult) {
    Player player = (Player) attacker.getEntity();
    float sneakSkill = plugin.getChampionManager().getChampion(player)
        .getEffectiveLifeSkillLevel(LifeSkillType.SNEAK, false);
    float sneakDamage = sneakSkill;
    sneakDamage += defender.getEntity().getMaxHealth() * (0.1 + 0.002 * sneakSkill);
    sneakDamage *= mods.getAttackMultiplier();
    sneakDamage *= pvpMult;

    SneakAttackEvent sneakEvent = DamageUtil
        .callSneakAttackEvent(attacker, defender, sneakSkill, sneakDamage);

    if (sneakEvent.isCancelled()) {
      return 0f;
    }

    if (!(defender.getEntity() instanceof Player)) {
      SpecialStatusUtil.setSneakImmune(defender.getEntity());
    }
    if (mods.isShowPopoffs()) {
      StrifePlugin.getInstance().getIndicatorManager().addIndicator(attacker.getEntity(),
          defender.getEntity(), IndicatorStyle.FLOAT_UP_FAST, 7, "&7Sneak Attack!");
    }
    defender.getEntity().getWorld().playSound(defender.getEntity().getEyeLocation(),
        Sound.ENTITY_PHANTOM_BITE, 1f, 1f);
    plugin.getStealthManager().unstealthPlayer(player);
    return sneakEvent.getSneakAttackDamage();
  }

  private static boolean isCriticalHit(StrifeMob attacker, StrifeMob defender,
      DamageModifiers mods) {
    float attackPenalty = 1f;
    if (mods.isScaleChancesWithAttack()) {
      attackPenalty = mods.getAttackMultiplier();
    }
    float critChance = StatUtil.getCriticalChance(attacker, attackPenalty,
        mods.getAbilityMods().getOrDefault(AbilityMod.CRITICAL_CHANCE, 0f));
    boolean success = critChance >= rollDouble(hasLuck(attacker.getEntity()));
    if (success) {
      DamageUtil.callCritEvent(attacker, attacker);
      defender.getEntity().getWorld().playSound(defender.getEntity().getEyeLocation(),
          Sound.ENTITY_GENERIC_BIG_FALL, 2f, 0.8f);
      if (attacker.getEntity() instanceof Player) {
        StrifePlugin.getInstance().getIndicatorManager().addIndicator(attacker.getEntity(),
            defender.getEntity(), IndicatorStyle.FLOAT_UP_FAST, 5, "&c&lCRIT!");
      }
    }
    return success;
  }

  public static float getRawDamage(StrifeMob attacker, DamageType damageType) {
    switch (damageType) {
      case PHYSICAL:
        return attacker.getStat(StrifeStat.PHYSICAL_DAMAGE);
      case MAGICAL:
        return attacker.getStat(StrifeStat.MAGIC_DAMAGE);
      case FIRE:
        return attacker.getStat(StrifeStat.FIRE_DAMAGE);
      case ICE:
        return attacker.getStat(StrifeStat.ICE_DAMAGE);
      case LIGHTNING:
        return attacker.getStat(StrifeStat.LIGHTNING_DAMAGE);
      case DARK:
        return attacker.getStat(StrifeStat.DARK_DAMAGE);
      case EARTH:
        return attacker.getStat(StrifeStat.EARTH_DAMAGE);
      case LIGHT:
        return attacker.getStat(StrifeStat.LIGHT_DAMAGE);
      case TRUE_DAMAGE:
        return attacker.getStat(StrifeStat.TRUE_DAMAGE);
      default:
        return 0;
    }
  }

  public static float applyDamageScale(StrifeMob caster, StrifeMob target, BonusDamage bonusDamage) {
    float amount = bonusDamage.getAmount();
    switch (bonusDamage.getDamageScale()) {
      case FLAT:
        return amount;
      case CASTER_STAT_PERCENT:
        return bonusDamage.getAmount() * caster.getStat(bonusDamage.getDamageStat());
      case TARGET_STAT_PERCENT:
        return bonusDamage.getAmount() * target.getStat(bonusDamage.getDamageStat());
      case CASTER_LEVEL:
        return amount * StatUtil.getMobLevel(caster.getEntity());
      case CASTER_DAMAGE:
        return amount * DamageUtil.getRawDamage(caster, bonusDamage.getDamageType());
      case TARGET_CURRENT_HEALTH:
        return amount * (float) target.getEntity().getHealth();
      case CASTER_CURRENT_HEALTH:
        return amount * (float) caster.getEntity().getHealth();
      case TARGET_MISSING_HEALTH:
        return amount * (float) (target.getEntity().getMaxHealth() - target.getEntity().getHealth());
      case CASTER_MISSING_HEALTH:
        return amount * (float) (caster.getEntity().getMaxHealth() - caster.getEntity().getHealth());
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
      case TARGET_CURRENT_ENERGY:
        return amount * StatUtil.getEnergy(target);
      case CASTER_CURRENT_ENERGY:
        return amount * StatUtil.getEnergy(caster);
      case TARGET_MISSING_ENERGY:
        return amount * (StatUtil.getMaximumEnergy(target) - StatUtil.getEnergy(target));
      case CASTER_MISSING_ENERGY:
        return amount * (StatUtil.getMaximumEnergy(caster) - StatUtil.getEnergy(caster));
      case TARGET_MAX_ENERGY:
        return amount * StatUtil.getMaximumEnergy(target);
      case CASTER_MAX_ENERGY:
        return amount * StatUtil.getMaximumEnergy(caster);
      case TARGET_CURRENT_RAGE:
        return amount * StrifePlugin.getInstance().getRageManager().getRage(target.getEntity());
      case CASTER_CURRENT_RAGE:
        return amount * StrifePlugin.getInstance().getRageManager().getRage(caster.getEntity());
    }
    return amount;
  }

  public static Map<DamageType, Float> buildDamageMap(StrifeMob attacker, StrifeMob target, DamageModifiers mods) {
    Map<DamageType, Float> damageMap = new HashMap<>();
    for (DamageType damageType : DMG_TYPES) {
      float amount = getRawDamage(attacker, damageType);
      if (amount > 0) {
        damageMap.put(damageType, amount * mods.getDamageMultipliers().getOrDefault(damageType, 1f)
            * mods.getAttackMultiplier());
      }
    }
    for (BonusDamage bd : mods.getBonusDamages()) {
      float bonus = applyDamageScale(attacker, target, bd);
      damageMap.put(bd.getDamageType(), damageMap.getOrDefault(bd.getDamageType(), 0f) + bonus);
    }
    return damageMap;
  }

  public static void applyDamageReductions(StrifeMob attacker, StrifeMob defender, Map<DamageType, Float> damageMap,
      Map<AbilityMod, Float> abilityMods) {
    damageMap.replaceAll((t, v) -> damageMap.get(t) * getDamageReduction(t, attacker, defender, abilityMods));
  }

  public static void applyAttackTypeMods(StrifeMob attacker, AttackType attackType, Map<DamageType, Float> damageMap) {
    if (damageMap.containsKey(DamageType.PHYSICAL)) {
      float physicalDamage = damageMap.get(DamageType.PHYSICAL);
      float physicalMult = attacker.getStat(StrifeStat.PHYSICAL_MULT);
      if (attackType == AttackType.MELEE) {
        physicalMult += attacker.getStat(StrifeStat.MELEE_PHYSICAL_MULT);
      } else if (attackType == AttackType.PROJECTILE) {
        physicalMult += attacker.getStat(StrifeStat.RANGED_PHYSICAL_MULT);
      }
      physicalDamage *= 1 + physicalMult / 100;
      damageMap.put(DamageType.PHYSICAL, physicalDamage);
    }
    if (damageMap.containsKey(DamageType.MAGICAL)) {
      damageMap.put(DamageType.MAGICAL, damageMap.get(DamageType.MAGICAL)
          * (1 + attacker.getStat(StrifeStat.MAGIC_MULT) / 100));
    }
    float elementalMult = 1 + (attacker.getStat(StrifeStat.ELEMENTAL_MULT) / 100);
    if (damageMap.containsKey(DamageType.FIRE)) {
      damageMap.put(DamageType.FIRE, damageMap.get(DamageType.FIRE) * elementalMult);
    }
    if (damageMap.containsKey(DamageType.ICE)) {
      damageMap.put(DamageType.ICE, damageMap.get(DamageType.ICE) * elementalMult);
    }
    if (damageMap.containsKey(DamageType.LIGHTNING)) {
      damageMap.put(DamageType.LIGHTNING, damageMap.get(DamageType.LIGHTNING) * elementalMult);
    }
    if (damageMap.containsKey(DamageType.EARTH)) {
      damageMap.put(DamageType.EARTH, damageMap.get(DamageType.EARTH) * elementalMult);
    }
    if (damageMap.containsKey(DamageType.DARK)) {
      damageMap.put(DamageType.DARK, damageMap.get(DamageType.DARK) * elementalMult);
    }
    if (damageMap.containsKey(DamageType.LIGHT)) {
      damageMap.put(DamageType.LIGHT, damageMap.get(DamageType.LIGHT) * elementalMult);
    }
  }

  private static void applyElementalEffects(StrifeMob attacker, StrifeMob defender, Map<DamageType, Float> damageMap,
      DamageModifiers mods) {
    int runes = plugin.getBlockManager().getEarthRunes(attacker.getEntity());
    if (damageMap.containsKey(DamageType.EARTH) && runes > 0) {
      damageMap.put(DamageType.EARTH, damageMap.get(DamageType.EARTH) * (1 + runes * 0.08f));
      if (mods.isConsumeEarthRunes()) {
        damageMap.put(DamageType.EARTH, damageMap.get(DamageType.EARTH) + StatUtil.getHealth(attacker) * 0.1f);
        plugin.getBlockManager().setEarthRunes(attacker, runes - 1);
      }
    }
    if (damageMap.containsKey(DamageType.FIRE) && defender.getEntity().getFireTicks() > 0) {
      damageMap.put(DamageType.FIRE, damageMap.get(DamageType.FIRE) * 1.5f);
    }
    float darkDamage = damageMap.getOrDefault(DamageType.DARK, 0f);
    if (darkDamage != 0) {
      damageMap.put(DamageType.DARK, darkDamage * getDarknessManager().getCorruptionMult(defender.getEntity()));
    }
    float chance = attacker.getStat(StrifeStat.ELEMENTAL_STATUS) / 100;
    if (mods.isScaleChancesWithAttack()) {
      chance *= mods.getAttackMultiplier();
    }
    if (!DamageUtil.rollBool(chance, true)) {
      return;
    }
    float totalElementalDamage = 0;
    Map<DamageType, Float> elementalDamages = new HashMap<>();
    for (DamageType type : damageMap.keySet()) {
      if (type != DamageType.PHYSICAL && type != DamageType.MAGICAL && type != DamageType.TRUE_DAMAGE
          && type != DamageType.EARTH) {
        float amount = damageMap.get(type);
        totalElementalDamage += amount;
        elementalDamages.put(type, amount);
      }
    }
    if (totalElementalDamage <= 0.1) {
      return;
    }
    float currentWeight = 0;
    totalElementalDamage *= Math.random();
    DamageType finalElementType = null;
    for (DamageType type : elementalDamages.keySet()) {
      currentWeight += elementalDamages.get(type);
      if (currentWeight >= totalElementalDamage) {
        finalElementType = type;
        break;
      }
    }
    if (finalElementType == null) {
      return;
    }
    float bonus;
    switch (finalElementType) {
      case FIRE:
        mods.getElementalStatuses().add(ElementalStatus.IGNITE);
        doIgnite(defender.getEntity(), damageMap.get(DamageType.FIRE));
        break;
      case ICE:
        mods.getElementalStatuses().add(ElementalStatus.FREEZE);
        bonus = attemptFreeze(damageMap.get(finalElementType), attacker, defender.getEntity());
        damageMap.put(finalElementType, damageMap.get(finalElementType) + bonus);
        break;
      case LIGHTNING:
        mods.getElementalStatuses().add(ElementalStatus.SHOCK);
        bonus = attemptShock(damageMap.get(finalElementType), defender.getEntity());
        damageMap.put(finalElementType, damageMap.get(finalElementType) + bonus);
        break;
      case DARK:
        mods.getElementalStatuses().add(ElementalStatus.CORRUPT);
        applyCorrupt(defender.getEntity(), 5 + darkDamage / 3);
        break;
      case LIGHT:
        bonus = getLightBonus(damageMap.get(finalElementType), attacker, defender.getEntity());
        if (bonus > damageMap.get(finalElementType) / 2) {
          damageMap.put(finalElementType, damageMap.get(finalElementType) + bonus);
        }
        break;
    }
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
        return 1 - getFireResist(defend, attack.hasTrait(StrifeTrait.SOUL_FLAME)) / 100;
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

  public static double getMinionMult(StrifeMob mob) {
    return 1 + mob.getStat(StrifeStat.MINION_MULT_INTERNAL) / 100;
  }

  public static float getTenacityMult(StrifeMob defender) {
    if (defender.getStat(StrifeStat.TENACITY) < 1) {
      return 1;
    }
    double percent = defender.getEntity().getHealth() / defender.getEntity().getMaxHealth();
    float maxReduction = 1 - (float) Math.pow(0.5f, defender.getStat(StrifeStat.TENACITY) / 200);
    return 1 - (maxReduction * (float) Math.pow(1 - percent, 1.5));
  }

  public static float getRageMult(StrifeMob defender) {
    return 200 / (200 + StrifePlugin.getInstance().getRageManager().getRage(defender.getEntity()));
  }

  public static LivingEntity getAttacker(Entity entity) {
    if (!entity.getPassengers().isEmpty()) {
      if (entity.getPassengers().get(0) instanceof LivingEntity) {
        return (LivingEntity) entity.getPassengers().get(0);
      }
    }
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

  private static void doIgnite(LivingEntity defender, float damage) {
    defender.setFireTicks(Math.max(25 + (int) damage, defender.getFireTicks()));
    defender.getWorld().playSound(defender.getEyeLocation(), Sound.ITEM_FLINTANDSTEEL_USE, 1f, 1f);
    defender.getWorld().spawnParticle(
        Particle.FLAME,
        defender.getEyeLocation(),
        6 + (int) damage / 2,
        0.3, 0.3, 0.3, 0.03
    );
    StrifePlugin.getInstance().getDamageOverTimeTask().trackBurning(defender);
  }

  public static float attemptShock(float damage, LivingEntity defender) {
    float multiplier = 0.5f;
    float percentHealth = (float) defender.getHealth() /
        (float) defender.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
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
    float multiplier = 0.25f + 0.25f * (StatUtil.getHealth(attacker) / 100);
    if (!defender.hasPotionEffect(PotionEffectType.SLOW)) {
      defender.getActivePotionEffects().add(new PotionEffect(PotionEffectType.SLOW, 30, 1));
    }
    defender.getWorld().playSound(defender.getEyeLocation(), Sound.BLOCK_GLASS_BREAK, 1f, 1.0f);
    defender.getWorld().spawnParticle(Particle.SNOWBALL,
        defender.getEyeLocation(), 4 + (int) damage / 2, 0.3, 0.3, 0.2, 0.0);
    return damage * multiplier;
  }

  public static float getLightBonus(float damage, StrifeMob attacker, LivingEntity defender) {
    float light = attacker.getEntity().getLocation().getBlock().getLightLevel();
    float multiplier = (light - 4) / 10;
    if (multiplier >= 0.5) {
      defender.getWorld().playSound(defender.getEyeLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1f, 2f);
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

  public static void doEvasion(StrifeMob attacker, StrifeMob defender) {
    callEvadeEvent(defender, attacker);
    defender.getEntity().getWorld()
        .playSound(defender.getEntity().getEyeLocation(), Sound.ENTITY_GHAST_SHOOT, 0.5f, 2f);
    if (defender.getEntity() instanceof Player) {
      MessageUtils.sendActionBar((Player) defender.getEntity(), ATTACK_DODGED);
    }
    if (attacker.getEntity() instanceof Player) {
      StrifePlugin.getInstance().getIndicatorManager().addIndicator(attacker.getEntity(),
          defender.getEntity(), IndicatorStyle.BOUNCE, 8, "&7&oMiss");
    }
  }

  public static void doBlock(StrifeMob attacker, StrifeMob defender) {
    callBlockEvent(defender, attacker);
    defender.getEntity().getWorld()
        .playSound(defender.getEntity().getEyeLocation(), Sound.ITEM_SHIELD_BLOCK, 1f, 1f);
    String defenderBar = ATTACK_BLOCKED;
    int runes = getBlockManager().getEarthRunes(defender.getEntity());
    if (runes > 0) {
      defenderBar = defenderBar + StringExtensionsKt.chatColorize("&2 ")
          + IntStream.range(0, runes).mapToObj(i -> "▼").collect(Collectors.joining(""));
    }
    if (defender.getEntity() instanceof Player) {
      MessageUtils.sendActionBar((Player) defender.getEntity(), defenderBar);
    }
    if (attacker.getEntity() instanceof Player) {
      MessageUtils.sendActionBar((Player) attacker.getEntity(), ATTACK_BLOCKED);
    }
  }

  public static float getPotionMult(LivingEntity attacker, LivingEntity defender) {
    float potionMult = 1.0f;

    PotionEffect powerEffect = attacker.getPotionEffect(PotionEffectType.INCREASE_DAMAGE);
    if (powerEffect != null) {
      potionMult += 0.1 * (powerEffect.getAmplifier() + 1);
    }
    PotionEffect weaknessEffect = attacker.getPotionEffect(PotionEffectType.WEAKNESS);
    if (weaknessEffect != null) {
      potionMult -= 0.1 * (weaknessEffect.getAmplifier() + 1);
    }

    PotionEffect vulnerabilityEffect = defender.getPotionEffect(PotionEffectType.UNLUCK);
    if (vulnerabilityEffect != null) {
      potionMult += 0.1 * (vulnerabilityEffect.getAmplifier() + 1);
    }
    PotionEffect resistEffect = defender.getPotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
    if (resistEffect != null) {
      potionMult -= 0.1 * (resistEffect.getAmplifier() + 1);
    }

    return Math.max(0, potionMult);
  }

  public static boolean canAttack(Player attacker, Player defender) {
    CombatListener.addPlayer(attacker);
    defender.damage(0, attacker);
    boolean friendly = CombatListener.hasFriendlyPlayer(attacker);
    CombatListener.removePlayer(attacker);
    return !friendly;
  }

  public static double getProjectileMultiplier(StrifeMob atk, StrifeMob def) {
    return Math.max(0.05D, 1 + (atk.getStat(StrifeStat.PROJECTILE_DAMAGE) -
        def.getStat(StrifeStat.PROJECTILE_REDUCTION)) / 100);
  }

  public static void applyLifeSteal(StrifeMob attacker, double damage, double healMultiplier,
      double bonus) {
    double lifeSteal = (attacker.getStat(StrifeStat.LIFE_STEAL) + bonus) / 100;
    restoreHealthWithPenalties(attacker.getEntity(), damage * lifeSteal * healMultiplier);
  }

  public static void applyHealthOnHit(StrifeMob attacker, double attackMultiplier,
      double healMultiplier, double bonus) {
    double health =
        (attacker.getStat(StrifeStat.HP_ON_HIT) + bonus) * attackMultiplier * healMultiplier;
    restoreHealthWithPenalties(attacker.getEntity(), health);
  }

  public static void applyEnergyOnHit(StrifeMob attacker, float attackMultiplier, float healMultiplier) {
    float energy = attacker.getStat(StrifeStat.ENERGY_ON_HIT) * attackMultiplier * healMultiplier;
    restoreEnergy(attacker, energy);
  }

  public static boolean attemptBleed(StrifeMob attacker, StrifeMob defender, float rawPhysical,
      DamageModifiers mods, boolean bypassBarrier) {
    if (StrifePlugin.getInstance().getBarrierManager().isBarrierUp(defender)) {
      return false;
    }
    if (defender.getStat(StrifeStat.BLEED_RESIST) > 99) {
      return false;
    }
    float chance = (attacker.getStat(StrifeStat.BLEED_CHANCE) +
        mods.getAbilityMods().getOrDefault(AbilityMod.BLEED_CHANCE, 0f)) / 100;
    if (chance >= rollDouble()) {
      float multiplier = mods.isScaleChancesWithAttack() ? mods.getAttackMultiplier() : 1f;
      float damage = rawPhysical * multiplier * BLEED_PERCENT;
      float bleedDamage = attacker.getStat(StrifeStat.BLEED_DAMAGE) + mods.getAbilityMods()
          .getOrDefault(AbilityMod.BLEED_DAMAGE, 0f);
      damage *= 1 + (bleedDamage / 100);
      damage *= 1 - defender.getStat(StrifeStat.BLEED_RESIST) / 100;
      applyBleed(defender, damage, bypassBarrier);
    }
    return false;
  }

  public static void applyBleed(StrifeMob defender, float amount, boolean bypassBarrier) {
    if (amount < 0.1) {
      return;
    }
    StrifePlugin.getInstance().getBleedManager().addBleed(defender, amount, bypassBarrier);
    defender.getEntity().getWorld()
        .playSound(defender.getEntity().getLocation(), Sound.ENTITY_SHEEP_SHEAR, 1f, 1f);
  }

  public static void applyCorrupt(LivingEntity defender, float amount) {
    StrifePlugin.getInstance().getCorruptionManager().applyCorruption(defender, amount);
    defender.getWorld().playSound(defender.getEyeLocation(), Sound.ENTITY_WITHER_SHOOT, 0.7f, 2f);
    defender.getWorld().spawnParticle(Particle.SMOKE_NORMAL,
        defender.getEyeLocation(), 10, 0.4, 0.4, 0.5, 0.1);
  }

  public static void doReflectedDamage(StrifeMob defender, StrifeMob attacker,
      AttackType damageType) {
    if (defender.getStat(StrifeStat.DAMAGE_REFLECT) < 0.1) {
      return;
    }
    double reflectDamage = defender.getStat(StrifeStat.DAMAGE_REFLECT);
    reflectDamage = damageType == AttackType.MELEE ? reflectDamage : reflectDamage * 0.6D;
    defender.getEntity().getWorld()
        .playSound(defender.getEntity().getLocation(), Sound.ENCHANT_THORNS_HIT, 1f, 1f);
    if (attacker.getEntity() instanceof Player) {
      ((Player) attacker.getEntity()).spawnParticle(Particle.DAMAGE_INDICATOR,
          TargetingUtil.getOriginLocation(attacker.getEntity(), OriginLocation.CENTER), (int) reflectDamage, 0.3, 0.3,
          0.3, 0.1);
    }
    attacker.getEntity().setHealth(Math.max(0D, attacker.getEntity().getHealth() - reflectDamage));
  }

  public static LoadedBuff getBuff(String id) {
    return StrifePlugin.getInstance().getBuffManager().getBuffFromId(id);
  }

  public static void callCritEvent(StrifeMob attacker, StrifeMob victim) {
    CriticalEvent c = new CriticalEvent(attacker, victim);
    Bukkit.getPluginManager().callEvent(c);
  }

  public static void callEvadeEvent(StrifeMob evader, StrifeMob attacker) {
    EvadeEvent ev = new EvadeEvent(evader, attacker);
    Bukkit.getPluginManager().callEvent(ev);
  }

  public static SneakAttackEvent callSneakAttackEvent(StrifeMob attacker, StrifeMob victim,
      float sneakSkill, float sneakDamage) {
    SneakAttackEvent sneakAttackEvent = new SneakAttackEvent(attacker, victim, sneakSkill,
        sneakDamage);
    Bukkit.getPluginManager().callEvent(sneakAttackEvent);
    return sneakAttackEvent;
  }

  public static void callBlockEvent(StrifeMob evader, StrifeMob attacker) {
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
    if (amount <= 0 || !livingEntity.isValid()) {
      return;
    }
    livingEntity.setHealth(Math.min(livingEntity.getHealth() + amount,
        livingEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()));
  }

  public static void restoreBarrier(StrifeMob strifeMob, float amount) {
    StrifePlugin.getInstance().getBarrierManager().restoreBarrier(strifeMob, amount);
  }

  public static void restoreEnergy(StrifeMob strifeMob, float amount) {
    StrifePlugin.getInstance().getEnergyManager().changeEnergy(strifeMob, amount);
  }

  public static AttackType getAttackType(EntityDamageByEntityEvent event) {
    if (event.getCause() == DamageCause.ENTITY_EXPLOSION) {
      return AttackType.AREA;
    } else if (event.getDamager() instanceof Projectile) {
      return AttackType.PROJECTILE;
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
    CASTER_STAT_PERCENT,
    TARGET_STAT_PERCENT,
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
    TARGET_CURRENT_ENERGY,
    CASTER_CURRENT_ENERGY,
    TARGET_MISSING_ENERGY,
    CASTER_MISSING_ENERGY,
    TARGET_MAX_ENERGY,
    CASTER_MAX_ENERGY,
    TARGET_CURRENT_RAGE,
    CASTER_CURRENT_RAGE,
  }

  public enum OriginLocation {
    ABOVE_HEAD,
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
    MELEE, PROJECTILE, AREA, OTHER
  }
}
