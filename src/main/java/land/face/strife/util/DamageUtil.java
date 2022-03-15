package land.face.strife.util;

import static land.face.strife.listeners.LoreAbilityListener.executeBoundEffects;
import static land.face.strife.listeners.LoreAbilityListener.executeFiniteEffects;
import static land.face.strife.managers.LoreAbilityManager.TriggerType.ON_DEATH;
import static land.face.strife.util.StatUtil.getArmorMult;
import static land.face.strife.util.StatUtil.getDefenderArmor;
import static land.face.strife.util.StatUtil.getDefenderWarding;
import static land.face.strife.util.StatUtil.getWardingMult;

import io.pixeloutlaw.minecraft.spigot.garbage.StringExtensionsKt;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import land.face.strife.StrifePlugin;
import land.face.strife.data.BonusDamage;
import land.face.strife.data.DamageModifiers;
import land.face.strife.data.DamageModifiers.ElementalStatus;
import land.face.strife.data.LoreAbility;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.ability.EntityAbilitySet.TriggerAbilityType;
import land.face.strife.data.champion.LifeSkillType;
import land.face.strife.data.effects.Ignite;
import land.face.strife.events.BlockEvent;
import land.face.strife.events.CriticalEvent;
import land.face.strife.events.EvadeEvent;
import land.face.strife.events.SneakAttackEvent;
import land.face.strife.listeners.CombatListener;
import land.face.strife.managers.BlockManager;
import land.face.strife.managers.IndicatorManager.IndicatorStyle;
import land.face.strife.stats.StrifeStat;
import land.face.strife.stats.StrifeTrait;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.api.GuildsAPI;
import me.glaremasters.guilds.guild.Guild;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.EvokerFangs;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class DamageUtil {

  private static StrifePlugin plugin;
  private static GuildsAPI guildsAPI;

  public static String ATTACK_BLOCKED;
  public static String ATTACK_DODGED;

  public static float BASE_ATTACK_SECONDS = 1.6f;

  public static float EVASION_THRESHOLD = 0.5f;
  public static float BASE_EVASION_MULT = 0.8f;
  public static float EVASION_ACCURACY_MULT = 0.6f;

  public static int BASE_RECHARGE_TICKS;
  public static long TICK_RATE;
  public static int DELAY_TICKS;
  public static float FLAT_BARRIER_PER_SECOND;
  public static float PERCENT_BARRIER_PER_SECOND;

  private static final DamageModifier[] MODIFIERS = EntityDamageEvent.DamageModifier.values();
  public static final DamageType[] DMG_TYPES = DamageType.values();

  private static final float BLEED_PERCENT = 0.5f;

  private static float PVP_MULT;

  private static final ItemStack EARTH_CRACK = new ItemStack(Material.COARSE_DIRT);
  private static final Random RANDOM = new Random(System.currentTimeMillis());

  public static void refresh() {
    plugin = StrifePlugin.getInstance();
    guildsAPI = Guilds.getApi();

    BASE_ATTACK_SECONDS = (float) plugin.getSettings()
        .getDouble("config.mechanics.attack-speed.base-attack-time", 1.6);

    EVASION_THRESHOLD = (float) plugin.getSettings()
        .getDouble("config.mechanics.evasion.evade-threshold", 0.5);
    BASE_EVASION_MULT = (float) plugin.getSettings()
        .getDouble("config.mechanics.evasion.evasion-multiplier", 0.8);
    EVASION_ACCURACY_MULT = (float) plugin.getSettings()
        .getDouble("config.mechanics.evasion.accuracy-multiplier", 0.6);

    PVP_MULT = (float) plugin.getSettings()
        .getDouble("config.mechanics.pvp-multiplier", 0.5);
    ATTACK_BLOCKED = StringExtensionsKt.chatColorize(plugin.getSettings()
        .getString("language.status.block-message", "&e&lBlocked!"));
    ATTACK_DODGED = StringExtensionsKt.chatColorize(plugin.getSettings()
        .getString("language.status.evade-message", "&7&l&oDodge!"));

    BASE_RECHARGE_TICKS = plugin.getSettings()
        .getInt("config.mechanics.barrier.base-delay-ticks", 100);
    TICK_RATE = plugin.getSettings()
        .getInt("config.mechanics.barrier.task-tick-rate", 3);
    DELAY_TICKS = (int) ((float) BASE_RECHARGE_TICKS / TICK_RATE);
    FLAT_BARRIER_PER_SECOND = plugin.getSettings()
        .getInt("config.mechanics.barrier.flat-per-second", 4);
    PERCENT_BARRIER_PER_SECOND = (float) plugin.getSettings()
        .getDouble("config.mechanics.barrier.percent-per-second", 0.08);
  }

  public static boolean isGuildAlly(StrifeMob attacker, StrifeMob defender) {
    if (attacker.getEntity().getType() == EntityType.PLAYER) {
      return isGuildAlly(defender, (Player) attacker.getEntity());
    } else if (defender.getEntity().getType() == EntityType.PLAYER) {
      return isGuildAlly(attacker, (Player) defender.getEntity());
    } else {
      // Return true if they're the same - but also not null
      return attacker.getAlliedGuild() != null && attacker.getAlliedGuild()
          .equals(defender.getAlliedGuild());
    }
  }

  public static boolean isGuildAlly(StrifeMob mob, Player player) {
    if (mob.getAlliedGuild() == null) {
      return false;
    }
    Guild guild = guildsAPI.getGuildHandler().getGuild(player);
    if (guild == null) {
      return false;
    }
    if (mob.getAlliedGuild().equals(guild.getId())) {
      return true;
    }
    for (UUID uuid : guild.getAllies()) {
      if (mob.getAlliedGuild().equals(uuid)) {
        return true;
      }
    }
    return false;
  }

  public static boolean preDamage(StrifeMob attacker, StrifeMob defender, DamageModifiers mods) {

    TargetingUtil.expandMobRange(attacker.getEntity(), defender.getEntity());

    if (attacker.isUseEquipment()) {
      plugin.getStrifeMobManager().updateEquipmentStats(attacker);
    }
    if (defender.isUseEquipment()) {
      plugin.getStrifeMobManager().updateEquipmentStats(defender);
      plugin.getStealthManager().unstealthPlayer((Player) defender.getEntity());
    }

    if (attacker != defender) {
      attacker.bumpCombat();
      defender.bumpCombat();
    }

    if (plugin.getCounterManager().executeCounters(attacker.getEntity(), defender.getEntity())) {
      return false;
    }

    float attackMult = mods.getAttackMultiplier();

    if (mods.isCanBeEvaded()) {
      float evadeMult = DamageUtil.determineEvasion(attacker, defender, mods.getAbilityMods());
      if (evadeMult == -1) {
        if (mods.isBasicAttack() && mods.getAttackType() == AttackType.MELEE) {
          plugin.getAttackSpeedManager().resetAttack(attacker, 0.5f, true);
        }
        return false;
      }
      mods.setAttackMultiplier(attackMult * evadeMult);
    }

    if (mods.isCanBeBlocked()) {
      if (plugin.getBlockManager().isAttackBlocked(attacker, defender, attackMult,
          mods.getAttackType(), mods.isBlocking(), mods.isGuardBreak())) {
        DamageUtil.doReflectedDamage(defender, attacker, mods.getAttackType());
        return false;
      }
    }

    return true;
  }

  public static Map<DamageType, Float> buildDamage(StrifeMob attacker, StrifeMob defender,
      DamageModifiers mods) {
    Map<DamageType, Float> damageMap = DamageUtil.buildDamageMap(attacker, defender, mods);
    applyAttackTypeMods(attacker, mods.getAttackType(), damageMap);
    return damageMap;
  }

  public static void reduceDamage(StrifeMob attacker, StrifeMob defender,
      Map<DamageType, Float> damageMap, DamageModifiers mods) {
    DamageUtil.applyDamageReductions(attacker, defender, damageMap, mods.getAbilityMods());
  }

  public static String buildDamageString(int i) {
    String str = Integer.toString(i);
    return str
        .replaceAll("0", "０")
        .replaceAll("1", "１")
        .replaceAll("2", "２")
        .replaceAll("3", "３")
        .replaceAll("4", "４")
        .replaceAll("5", "５")
        .replaceAll("6", "６")
        .replaceAll("7", "７")
        .replaceAll("8", "８")
        .replaceAll("9", "９");
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

    float damageReduction =
        defender.getStat(StrifeStat.DAMAGE_REDUCTION) * mods.getDamageReductionRatio() * pvpMult;
    float rawDamage = (float) Math.max(0D, (standardDamage + elementalDamage) - damageReduction);

    if (mods.getAttackType() == AttackType.PROJECTILE) {
      rawDamage *= DamageUtil.getProjectileMultiplier(attacker, defender);
    }
    rawDamage *= DamageUtil.getTenacityMult(defender);
    rawDamage *= DamageUtil.getMinionMult(attacker);
    if (attacker.getEntity().getFreezeTicks() > 0 && attacker.getEntity() instanceof Player) {
      rawDamage *= 1 - 0.3 * ((float) attacker.getEntity().getFreezeTicks() / attacker.getEntity()
          .getMaxFreezeTicks());
    }
    if (defender.hasTrait(StrifeTrait.STONE_SKIN)) {
      rawDamage *= 1 - (0.03 * plugin.getBlockManager().getEarthRunes(defender));
    }
    rawDamage += damageMap.getOrDefault(DamageType.TRUE_DAMAGE, 0f);

    if (mods.isSneakAttack() && !SpecialStatusUtil.isSneakImmune(defender.getEntity())) {
      rawDamage += doSneakAttack(attacker, defender, mods, pvpMult);
      boolean finishingBlow = rawDamage > defender.getEntity().getHealth() + defender.getBarrier();
      float gainedXp = plugin.getStealthManager().getSneakAttackExp(defender.getEntity(),
          attacker.getChampion().getLifeSkillLevel(LifeSkillType.SNEAK), finishingBlow);
      plugin.getSkillExperienceManager().addExperience((Player) attacker.getEntity(),
          LifeSkillType.SNEAK, gainedXp, false, false);
    }

    if (mods.getAbilityMods().containsKey(AbilityMod.MAX_DAMAGE)) {
      rawDamage = Math.min(mods.getAbilityMods().get(AbilityMod.MAX_DAMAGE), rawDamage);
    }

    if (rawDamage < 1) {
      rawDamage = 0;
    }

    String damageString;
    if (mods.isShowPopoffs() && attacker.getEntity() instanceof Player) {
      if (rawDamage == 0) {
        plugin.getIndicatorManager().addIndicator(attacker.getEntity(), defender.getEntity(),
            IndicatorStyle.RANDOM_POPOFF, 9, ChatColor.AQUA + "０");
      } else {
        damageString = buildDamageString(Math.round(rawDamage));
        if (criticalHit) {
          damageString = ChatColor.WHITE + "✸" + "\uF809" +
              StringUtils.repeat("\uF806", damageString.length()) + damageString;
        }
        plugin.getIndicatorManager().addIndicator(attacker.getEntity(), defender.getEntity(),
            IndicatorStyle.RANDOM_POPOFF, 9, damageString);
      }
    }
    if (mods.isShowPopoffs() && attacker.getMaster() != null &&
        attacker.getMaster().getEntity() instanceof Player) {
      damageString = buildDamageString(Math.round(rawDamage));
      plugin.getIndicatorManager().addIndicator(attacker.getMaster().getEntity(),
          defender.getEntity(), IndicatorStyle.RANDOM_POPOFF, 9, "&7" + damageString);
    }
    return rawDamage;
  }

  public static void postDamage(StrifeMob attacker, StrifeMob defender, DamageModifiers mods) {
    if (mods.getAttackType() == AttackType.BONUS) {
      return;
    }
    float ratio = mods.getDamageReductionRatio();

    DamageUtil.applyHealthOnHit(attacker, ratio, mods.getHealMultiplier(),
        mods.getAbilityMods().getOrDefault(AbilityMod.HEALTH_ON_HIT, 0f));
    DamageUtil.applyEnergyOnHit(attacker, ratio, mods.getHealMultiplier());

    DamageUtil.doReflectedDamage(defender, attacker, mods.getAttackType());

    if (attacker.getStat(StrifeStat.RAGE_ON_HIT) > 0.1) {
      plugin.getRageManager()
          .changeRage(attacker, attacker.getStat(StrifeStat.RAGE_ON_HIT) * ratio);
    }
    if (defender.getStat(StrifeStat.RAGE_WHEN_HIT) > 0.1) {
      plugin.getRageManager().changeRage(defender, defender.getStat(StrifeStat.RAGE_WHEN_HIT));
    }
    if (defender.getStat(StrifeStat.ENERGY_WHEN_HIT) > 0.1) {
      StatUtil.changeEnergy(defender, defender.getStat(StrifeStat.ENERGY_WHEN_HIT));
    }

    plugin.getAbilityManager().abilityCast(defender, attacker, TriggerAbilityType.WHEN_HIT);
  }

  private static float doSneakAttack(StrifeMob attacker, StrifeMob defender, DamageModifiers mods,
      float pvpMult) {
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
          defender.getEntity(), IndicatorStyle.FLOAT_UP_FAST, 4, "&7Sneak Attack!");
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
    float critChance = StatUtil.getCriticalChance(attacker, attackPenalty, mods.getAbilityMods()
        .getOrDefault(AbilityMod.CRITICAL_CHANCE, 0f));
    boolean success = critChance >= rollDouble(hasLuck(attacker.getEntity()));
    if (success) {
      DamageUtil.callCritEvent(attacker, defender);
      defender.getEntity().getWorld().playSound(defender.getEntity().getEyeLocation(),
          Sound.ENTITY_GENERIC_BIG_FALL, 2f, 0.75f);
      //if (attacker.getEntity() instanceof Player) {
      //  StrifePlugin.getInstance().getIndicatorManager().addIndicator(attacker.getEntity(),
      //      defender.getEntity(), IndicatorStyle.FLOAT_UP_FAST, 3, "&c&lCRIT!");
      //}
    }
    return success;
  }

  public static float getRawDamage(StrifeMob attacker, DamageType damageType) {
    return switch (damageType) {
      case PHYSICAL -> attacker.getStat(StrifeStat.PHYSICAL_DAMAGE);
      case MAGICAL -> attacker.getStat(StrifeStat.MAGIC_DAMAGE);
      case FIRE -> attacker.getStat(StrifeStat.FIRE_DAMAGE);
      case ICE -> attacker.getStat(StrifeStat.ICE_DAMAGE);
      case LIGHTNING -> attacker.getStat(StrifeStat.LIGHTNING_DAMAGE);
      case DARK -> attacker.getStat(StrifeStat.DARK_DAMAGE);
      case EARTH -> attacker.getStat(StrifeStat.EARTH_DAMAGE);
      case LIGHT -> attacker.getStat(StrifeStat.LIGHT_DAMAGE);
      case TRUE_DAMAGE -> attacker.getStat(StrifeStat.TRUE_DAMAGE);
    };
  }

  public static float applyDamageScale(StrifeMob caster, StrifeMob target,
      BonusDamage bonusDamage) {
    float amount = bonusDamage.getAmount();
    return switch (bonusDamage.getDamageScale()) {
      case FLAT -> amount;
      case CASTER_STAT_PERCENT -> bonusDamage.getAmount() *
          caster.getStat(bonusDamage.getDamageStat());
      case TARGET_STAT_PERCENT -> bonusDamage.getAmount() *
          target.getStat(bonusDamage.getDamageStat());
      case CASTER_LEVEL -> amount * StatUtil.getMobLevel(caster.getEntity());
      case TARGET_LEVEL -> amount * StatUtil.getMobLevel(target.getEntity());
      case CASTER_DAMAGE -> amount * DamageUtil.getRawDamage(caster, bonusDamage.getDamageType());
      case TARGET_CURRENT_HEALTH -> amount * (float) target.getEntity().getHealth();
      case CASTER_CURRENT_HEALTH -> amount * (float) caster.getEntity().getHealth();
      case TARGET_MISSING_HEALTH -> amount *
          (float) (target.getEntity().getMaxHealth() - target.getEntity().getHealth());
      case CASTER_MISSING_HEALTH -> amount *
          (float) (caster.getEntity().getMaxHealth() - caster.getEntity().getHealth());
      case TARGET_MAX_HEALTH -> amount * (float) target.getEntity().getMaxHealth();
      case CASTER_MAX_HEALTH -> amount * (float) caster.getEntity().getMaxHealth();
      case TARGET_CURRENT_BARRIER -> amount * target.getBarrier();
      case CASTER_CURRENT_BARRIER -> amount * caster.getBarrier();
      case TARGET_MISSING_BARRIER -> amount * (target.getMaxBarrier() - target.getBarrier());
      case CASTER_MISSING_BARRIER -> amount * (caster.getMaxBarrier() - caster.getBarrier());
      case TARGET_MAX_BARRIER -> amount * target.getMaxBarrier();
      case CASTER_MAX_BARRIER -> amount * caster.getMaxBarrier();
      case TARGET_CURRENT_ENERGY -> amount * StatUtil.getEnergy(target);
      case CASTER_CURRENT_ENERGY -> amount * StatUtil.getEnergy(caster);
      case TARGET_MISSING_ENERGY -> amount * (target.getMaxEnergy() - StatUtil.getEnergy(target));
      case CASTER_MISSING_ENERGY -> amount * (caster.getMaxEnergy() - StatUtil.getEnergy(caster));
      case TARGET_MAX_ENERGY -> amount * target.getMaxEnergy();
      case CASTER_MAX_ENERGY -> amount * caster.getMaxEnergy();
      case TARGET_CURRENT_RAGE -> amount *
          StrifePlugin.getInstance().getRageManager().getRage(target.getEntity());
      case CASTER_CURRENT_RAGE -> amount *
          StrifePlugin.getInstance().getRageManager().getRage(caster.getEntity());
      case TARGET_CURRENT_CORRUPTION -> amount * target.getCorruption();
      case CASTER_CURRENT_CORRUPTION -> amount * caster.getCorruption();
      case TARGET_MAX_RAGE -> amount * target.getMaxRage();
      case CASTER_MAX_RAGE -> amount * caster.getMaxRage();
    };
  }

  public static Map<DamageType, Float> buildDamageMap(StrifeMob attacker, StrifeMob target,
      DamageModifiers mods) {
    Map<DamageType, Float> damageMap = new HashMap<>();
    boolean modsEnabled = mods != null;
    for (DamageType damageType : DMG_TYPES) {
      float amount = getRawDamage(attacker, damageType);
      if (amount > 0) {
        if (modsEnabled) {
          amount *= mods.getDamageMultipliers().getOrDefault(damageType, 1f);
          amount *= mods.getAttackMultiplier();
        }
        damageMap.put(damageType, amount);
      }
    }
    if (modsEnabled) {
      for (BonusDamage bd : mods.getBonusDamages()) {
        float bonus = applyDamageScale(attacker, target, bd);
        damageMap.put(bd.getDamageType(), damageMap.getOrDefault(bd.getDamageType(), 0f) + bonus);
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
    if (attackType == AttackType.BONUS) {
      return;
    }
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

  public static void applyElementalEffects(StrifeMob attacker, StrifeMob defender,
      Map<DamageType, Float> damageMap,
      DamageModifiers mods) {
    float baseDarkDamage = damageMap.getOrDefault(DamageType.DARK, 0f);
    if (baseDarkDamage != 0) {
      damageMap.put(DamageType.DARK,
          baseDarkDamage * CorruptionUtil.getCorruptionMultiplier(defender));
    }
    float chance = (mods.getAbilityMods().getOrDefault(AbilityMod.STATUS_CHANCE, 0f) +
        attacker.getStat(StrifeStat.ELEMENTAL_STATUS)) / 100;
    if (mods.isScaleChancesWithAttack()) {
      chance *= Math.min(1.0, mods.getAttackMultiplier());
    }
    if (mods.getAttackType() == AttackType.BONUS || !DamageUtil.rollBool(chance, true)) {
      return;
    }
    float totalElementalDamage = 0;
    Map<DamageType, Float> elementalDamages = new HashMap<>();
    for (DamageType type : damageMap.keySet()) {
      if (type != DamageType.PHYSICAL && type != DamageType.MAGICAL &&
          type != DamageType.TRUE_DAMAGE) {
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
      case FIRE -> {
        mods.getElementalStatuses().add(ElementalStatus.IGNITE);
        doIgnite(defender, damageMap.get(DamageType.FIRE));
      }
      case ICE -> {
        mods.getElementalStatuses().add(ElementalStatus.FREEZE);
        attemptFreeze(attacker, defender, damageMap.get(finalElementType));
        damageMap.put(finalElementType, damageMap.get(finalElementType) * 1.2f);
      }
      case LIGHTNING -> {
        mods.getElementalStatuses().add(ElementalStatus.SHOCK);
        bonus = attemptShock(damageMap.get(finalElementType), defender.getEntity());
        damageMap.put(finalElementType, damageMap.get(finalElementType) + bonus);
      }
      case DARK -> {
        mods.getElementalStatuses().add(ElementalStatus.CORRUPT);
        CorruptionUtil.applyCorrupt(defender, 10 + baseDarkDamage / 4, true);
      }
      case EARTH -> {
        mods.getElementalStatuses().add(ElementalStatus.CRUNCH);
        int runes = plugin.getBlockManager().getEarthRunes(attacker);
        float newDamage = damageMap.get(DamageType.EARTH) * (1.25f + (runes * 0.05f));
        defender.getEntity().getWorld().playSound(
            defender.getEntity().getEyeLocation(), Sound.BLOCK_ROOTED_DIRT_BREAK, 1f, 0.85f);
        defender.getEntity().getWorld().spawnParticle(
            Particle.ITEM_CRACK,
            defender.getEntity().getLocation().clone().add(0, 0.4, 0),
            20, 0.1, 0.1, 0.1, 0.13f,
            EARTH_CRACK
        );
        damageMap.put(DamageType.EARTH, newDamage);
      }
      case LIGHT -> {
        bonus = getLightBonus(damageMap.get(finalElementType), attacker, defender.getEntity());
        if (bonus > damageMap.get(finalElementType) / 2) {
          damageMap.put(finalElementType, damageMap.get(finalElementType) + bonus);
        }
      }
    }
  }

  public static float getDamageReduction(DamageType type, StrifeMob attack, StrifeMob defend,
      Map<AbilityMod, Float> modDoubleMap) {
    switch (type) {
      case PHYSICAL -> {
        float armor = getDefenderArmor(attack, defend);
        armor *= 1 - modDoubleMap.getOrDefault(AbilityMod.ARMOR_PEN_MULT, 0f);
        armor -= modDoubleMap.getOrDefault(AbilityMod.ARMOR_PEN, 0f);
        if (modDoubleMap.containsKey(AbilityMod.BACK_ATTACK)) {
          armor *= 0.8;
        }
        return getArmorMult(armor);
      }
      case MAGICAL -> {
        float warding = getDefenderWarding(attack, defend);
        warding *= 1 - modDoubleMap.getOrDefault(AbilityMod.WARD_PEN_MULT, 0f);
        warding -= modDoubleMap.getOrDefault(AbilityMod.WARD_PEN, 0f);
        if (modDoubleMap.containsKey(AbilityMod.BACK_ATTACK)) {
          warding *= 0.8;
        }
        return getWardingMult(warding);
      }
      case FIRE -> {
        float fireResist = defend.getStat(StrifeStat.FIRE_RESIST) / 100;
        if (attack.hasTrait(StrifeTrait.SOUL_FLAME) && fireResist > 0) {
          fireResist /= 2;
        }
        if (defend.getEntity().getLocation().getBlock().getType() == Material.WATER) {
          fireResist += 0.5;
        }
        fireResist = Math.min(fireResist, 0.85f);
        return fireResist >= 0 ? (1 - fireResist) : 1 + Math.abs(fireResist);
      }
      case ICE -> {
        float iceResist = defend.getStat(StrifeStat.ICE_RESIST) / 100;
        if (defend.getEntity().getLocation().getBlock().getType() == Material.WATER) {
          iceResist -= 0.3;
        }
        iceResist = Math.min(iceResist, 0.85f);
        return iceResist >= 0 ? (1 - iceResist) : 1 + Math.abs(iceResist);
      }
      case LIGHTNING -> {
        float lightningResist = defend.getStat(StrifeStat.LIGHTNING_RESIST) / 100;
        if (defend.getEntity().getLocation().getBlock().getType() == Material.WATER) {
          lightningResist -= 0.3;
        }
        lightningResist = Math.min(lightningResist, 0.85f);
        return lightningResist >= 0 ? (1 - lightningResist) : 1 + Math.abs(lightningResist);
      }
      case DARK -> {
        float darkResist = defend.getStat(StrifeStat.DARK_RESIST) / 100;
        darkResist = Math.min(darkResist, 0.85f);
        return darkResist >= 0 ? (1 - darkResist) : 1 + Math.abs(darkResist);
      }
      case EARTH -> {
        float earthResist = defend.getStat(StrifeStat.EARTH_RESIST) / 100;
        earthResist = Math.min(earthResist, 0.85f);
        return earthResist >= 0 ? (1 - earthResist) : 1 + Math.abs(earthResist);
      }
      case LIGHT -> {
        float lightResist = defend.getStat(StrifeStat.LIGHT_RESIST) / 100;
        lightResist = Math.min(lightResist, 0.85f);
        return lightResist >= 0 ? (1 - lightResist) : 1 + Math.abs(lightResist);
      }
    }
    return 1;
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

  private static void doIgnite(StrifeMob defender, float damage) {
    LivingEntity defendEntity = defender.getEntity();
    defendEntity.getWorld().playSound(defendEntity.getEyeLocation(),
        Sound.ITEM_FLINTANDSTEEL_USE, 1f, 0.8f);
    defendEntity.getWorld().spawnParticle(
        Particle.FLAME,
        defendEntity.getEyeLocation(),
        6 + (int) damage / 2,
        0.3, 0.3, 0.3, 0.03
    );
    boolean igniteSuccess = Ignite.setFlames(defender,
        Math.max(25 + (int) damage, defender.getEntity().getFireTicks()));
    if (igniteSuccess) {
      StrifePlugin.getInstance().getDamageOverTimeTask().trackBurning(defendEntity);
    }
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
        .spawnParticle(Particle.ELECTRIC_SPARK, defender.getEyeLocation(), 10 + (int) particles,
            particleRange, particleRange, particleRange, 0.12);
    if (defender instanceof Creeper) {
      ((Creeper) defender).setPowered(true);
    }
    return damage * multiplier;
  }

  public static void attemptFreeze(StrifeMob attacker, StrifeMob defender, float damage) {
    LivingEntity defendEntity = defender.getEntity();
    defendEntity.getWorld().playSound(defendEntity.getEyeLocation(),
        Sound.BLOCK_GLASS_BREAK, 1f, 1.3f);
    int ticks = 500 + (int) (1000f * (damage / defendEntity.getMaxHealth()));
    DamageUtil.addFrost(attacker, defender, ticks);
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

    if (mods.containsKey(AbilityMod.BACK_ATTACK)) {
      totalEvasion *= 0.8;
    }

    float evasionMultiplier = StatUtil.getMinimumEvasionMult(totalEvasion, totalAccuracy);
    evasionMultiplier = evasionMultiplier + (rollDouble() * (1 - evasionMultiplier));

    return evasionMultiplier;
  }

  // returns -1 for true, and an evasion multiplier above 0 if false.
  public static float determineEvasion(StrifeMob attacker, StrifeMob defender,
      Map<AbilityMod, Float> attackModifiers) {
    if (Math.random() < defender.getStat(StrifeStat.DODGE_CHANCE) / 100) {
      DamageUtil.doEvasion(attacker, defender);
      return -1;
    }
    float evasionMultiplier = getFullEvasionMult(attacker, defender, attackModifiers);
    if (evasionMultiplier < EVASION_THRESHOLD) {
      DamageUtil.doEvasion(attacker, defender);
      return -1;
    }
    return evasionMultiplier;
  }

  public static void doEvasion(StrifeMob attacker, StrifeMob defender) {
    callEvadeEvent(defender, attacker);
    defender.getEntity().getWorld().playSound(defender.getEntity().getEyeLocation(),
        Sound.ENTITY_GHAST_SHOOT, 0.5f, 2f);
    if (attacker.getEntity() instanceof Player) {
      StrifePlugin.getInstance().getIndicatorManager().addIndicator(attacker.getEntity(),
          defender.getEntity(), IndicatorStyle.BOUNCE, 6, "&7&oMiss");
    }
  }

  public static void doBlock(StrifeMob attacker, StrifeMob defender) {
    callBlockEvent(defender, attacker);
    if (attacker.getEntity() instanceof Player) {
      plugin.getIndicatorManager().addIndicator(attacker.getEntity(), defender.getEntity(),
          IndicatorStyle.RANDOM_POPOFF, 7, "&e⛨&lBlock");
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

  public static void applyEnergyOnHit(StrifeMob attacker, float attackMultiplier,
      float healMultiplier) {
    float energy = attacker.getStat(StrifeStat.ENERGY_ON_HIT) * attackMultiplier * healMultiplier;
    StatUtil.changeEnergy(attacker, energy);
  }

  public static boolean attemptBleed(StrifeMob attacker, StrifeMob defender, float rawPhysical,
      DamageModifiers mods, boolean bypassBarrier) {
    if (defender.getBarrier() > 0 || mods.getAttackType() == AttackType.BONUS) {
      return false;
    }
    if (defender.getStat(StrifeStat.BLEED_RESIST) > 99) {
      return false;
    }
    float chance = (attacker.getStat(StrifeStat.BLEED_CHANCE) +
        mods.getAbilityMods().getOrDefault(AbilityMod.BLEED_CHANCE, 0f)) / 100;
    if (chance >= rollDouble()) {
      float damage = rawPhysical * BLEED_PERCENT;
      if (mods.isScaleChancesWithAttack()) {
        damage *= Math.min(1f, mods.getAttackMultiplier());
      }
      float bleedDamage = attacker.getStat(StrifeStat.BLEED_DAMAGE) + mods.getAbilityMods()
          .getOrDefault(AbilityMod.BLEED_DAMAGE, 0f);
      damage *= 1 + (bleedDamage / 100);
      applyBleed(attacker, defender, damage, bypassBarrier);
    }
    return false;
  }

  public static void applyBleed(StrifeMob attacker, StrifeMob defender, float amount, boolean bypassBarrier) {
    if (amount < 0.1) {
      return;
    }
    if (defender.getFrost() > 0 && attacker.hasTrait(StrifeTrait.BLOOD_AND_ICE)) {
      amount *= 1.3;
    }
    amount *= 1 - defender.getStat(StrifeStat.BLEED_RESIST) / 100;
    boolean bleedSuccess = plugin.getBleedManager().addBleed(defender, amount, bypassBarrier);
    if (bleedSuccess) {
      defender.getEntity().getWorld()
          .playSound(defender.getEntity().getLocation(), Sound.ENTITY_SHEEP_SHEAR, 1f, 0.7f);
    }
  }

  public static void addFrost(StrifeMob attacker, StrifeMob defender, float amount) {
    if (attacker != null && attacker.hasTrait(StrifeTrait.BLOOD_AND_ICE) &&
        StrifePlugin.getInstance().getBleedManager().isBleeding(defender.getEntity())) {
      amount *= 1.3;
    }
    if (amount < 0.1) {
      return;
    }
    defender.setFrost(defender.getFrost() + amount);
  }

  public static void doReflectedDamage(StrifeMob defender, StrifeMob attacker,
      AttackType damageType) {
    if (defender.getStat(StrifeStat.DAMAGE_REFLECT) < 0.1) {
      return;
    }
    if (!attacker.getEntity().isValid()) {
      return;
    }
    float reflectDamage = defender.getStat(StrifeStat.DAMAGE_REFLECT);
    reflectDamage = damageType == AttackType.MELEE ? reflectDamage : reflectDamage * 0.6f;
    defender.getEntity().getWorld()
        .playSound(defender.getEntity().getLocation(), Sound.BLOCK_LANTERN_STEP,
            SoundCategory.HOSTILE, 1f, 1.5f);
    if (attacker.getEntity() instanceof Player) {
      ((Player) attacker.getEntity()).spawnParticle(Particle.DAMAGE_INDICATOR,
          TargetingUtil.getOriginLocation(attacker.getEntity(), OriginLocation.CENTER),
          (int) reflectDamage, 0.3, 0.3,
          0.3, 0.1);
      if (defender.getEntity() instanceof Player) {
        attacker.flagPvp();
        defender.flagPvp();
      }
    }
    DamageUtil.dealRawDamage(attacker.getEntity(), reflectDamage);
  }

  public static void callCritEvent(StrifeMob attacker, StrifeMob victim) {
    CriticalEvent c = new CriticalEvent(attacker, victim);
    Bukkit.getPluginManager().callEvent(c);
  }

  public static void doPreDeath(StrifeMob victim) {
    Set<LoreAbility> abilitySet = new HashSet<>(victim.getLoreAbilities(ON_DEATH));
    executeBoundEffects(victim, victim, abilitySet);
    executeFiniteEffects(victim, victim, ON_DEATH);
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

  public static AttackType getAttackType(EntityDamageByEntityEvent event) {
    if (event.getCause() == DamageCause.ENTITY_EXPLOSION
        || event.getDamager() instanceof EvokerFangs) {
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

  public static void dealRawDamage(LivingEntity le, float damage) {
    if (damage >= le.getHealth()) {
      StrifeMob mob = plugin.getStrifeMobManager().getStatMob(le);
      DamageUtil.doPreDeath(mob);
    }
    if (le.getHealth() <= damage) {
      le.setHealth(0);
    } else {
      le.setHealth(le.getHealth() - damage);
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

  public enum DamageScale {
    FLAT,
    CASTER_STAT_PERCENT,
    TARGET_STAT_PERCENT,
    CASTER_LEVEL,
    TARGET_LEVEL,
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
    TARGET_CURRENT_CORRUPTION,
    CASTER_CURRENT_CORRUPTION,
    TARGET_MAX_RAGE,
    CASTER_MAX_RAGE
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
    BLEED_DAMAGE,
    STATUS_CHANCE,
    BACK_ATTACK,
    MAX_DAMAGE
  }

  public enum AttackType {
    MELEE, PROJECTILE, AREA, OTHER, BONUS
  }
}
