package land.face.strife.util;

import static com.sk89q.worldedit.math.BlockVector3.at;
import static land.face.strife.listeners.LoreAbilityListener.executeBoundEffects;
import static land.face.strife.listeners.LoreAbilityListener.executeFiniteEffects;
import static land.face.strife.managers.LoreAbilityManager.TriggerType.ON_DEATH;
import static land.face.strife.util.StatUtil.getArmorMult;
import static land.face.strife.util.StatUtil.getDefenderArmor;
import static land.face.strife.util.StatUtil.getDefenderWarding;
import static land.face.strife.util.StatUtil.getWardingMult;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.internal.platform.StringMatcher;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.soujah.poggersguilds.GuildPlugin;
import com.soujah.poggersguilds.data.Guild;
import com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor;
import com.tealcube.minecraft.bukkit.facecore.utilities.PaletteUtil;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import land.face.learnin.LearninBooksPlugin;
import land.face.strife.StrifePlugin;
import land.face.strife.data.BonusDamage;
import land.face.strife.data.DamageModifiers;
import land.face.strife.data.DamageModifiers.ElementalStatus;
import land.face.strife.data.LoreAbility;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.ability.EntityAbilitySet.TriggerAbilityType;
import land.face.strife.data.champion.LifeSkillType;
import land.face.strife.data.effects.Ignite;
import land.face.strife.data.pojo.SkillLevelData;
import land.face.strife.events.CriticalEvent;
import land.face.strife.events.EvadeEvent;
import land.face.strife.events.SneakAttackEvent;
import land.face.strife.events.StrifeEarlyDamageEvent;
import land.face.strife.managers.IndicatorManager.IndicatorStyle;
import land.face.strife.managers.PrayerManager.Prayer;
import land.face.strife.stats.StrifeStat;
import land.face.strife.stats.StrifeTrait;
import org.bukkit.Bukkit;
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

  public static String ATTACK_BLOCKED;
  public static String ATTACK_DODGED;

  public static float BASE_ATTACK_SECONDS = 1.6f;

  public static int BASE_RECHARGE_TICKS;
  public static long TICK_RATE;
  public static int DELAY_TICKS;
  public static float FLAT_BARRIER_PER_SECOND;
  public static float PERCENT_BARRIER_PER_SECOND;

  public static String deathMessage;
  public static List<String> sillyDeathMsgs;

  private static final DamageModifier[] MODIFIERS = EntityDamageEvent.DamageModifier.values();
  public static final DamageType[] DMG_TYPES = DamageType.values();

  private static final float BLEED_PERCENT = 0.5f;

  private static float PVP_MULT;

  private static final String POISON_TEXT = "彣";

  private static final ItemStack EARTH_CRACK = new ItemStack(Material.COARSE_DIRT);

  private static final RegionContainer regionContainer = WorldGuard.getInstance()
      .getPlatform().getRegionContainer();
  private static final StringMatcher stringMatcher = WorldGuard.getInstance()
      .getPlatform().getMatcher();

  public static void refresh(StrifePlugin refreshedPlugin) {
    plugin = refreshedPlugin;

    BASE_ATTACK_SECONDS = (float) plugin.getSettings()
        .getDouble("config.mechanics.attack-speed.base-attack-time", 1.6);

    PVP_MULT = (float) plugin.getSettings()
        .getDouble("config.mechanics.pvp-multiplier", 0.5);
    ATTACK_BLOCKED = PaletteUtil.color(plugin.getSettings()
        .getString("language.status.block-message", "&e&lBlocked!"));
    ATTACK_DODGED = PaletteUtil.color(plugin.getSettings()
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

    deathMessage = PaletteUtil.color(plugin.getSettings()
        .getString("language.enemy-killed-title"));
    sillyDeathMsgs = PaletteUtil.color(plugin.getSettings()
        .getStringList("language.silly-enemy-killed-titles"));
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
    if (mob == null) {
      return false;
    }
    if (mob.getAlliedGuild() == null) {
      return false;
    }
    Guild guild = GuildPlugin.getInstance().getGuildManager().getGuild(player, false);
    if (guild == null) {
      return false;
    }
    if (mob.getAlliedGuild().equals(guild.getId())) {
      return true;
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
      attacker.bumpCombat(defender);
      defender.bumpCombat(null);
    }

    float attackMult = mods.getAttackMultiplier();

    if (plugin.getCounterManager().executeCounters(attacker.getEntity(), defender.getEntity())) {
      return false;
    }

    if (!mods.isSneakAttack()) {
      if (mods.isCanBeEvaded()) {
        if (DamageUtil.isEvaded(attacker, defender, mods.getAbilityMods())) {
          return false;
        }
      }
      if (mods.isCanBeBlocked()) {
        if (plugin.getBlockManager().attemptBlock(attacker, defender, attackMult,
            mods.getAttackType(), mods.isBlocking(), mods.isGuardBreak())) {
          return false;
        }
      }
    }

    StrifeEarlyDamageEvent earlyDamageEvent = new StrifeEarlyDamageEvent(attacker, defender, mods);
    Bukkit.getPluginManager().callEvent(earlyDamageEvent);

    if (defender.isInvincible()) {
      if (attacker.getEntity() instanceof Player) {
        plugin.getIndicatorManager().addIndicator(attacker.getEntity(), defender.getEntity(),
            IndicatorStyle.FLOAT_UP_MEDIUM, 4, "彡", 1.0f, 1.0f, 1.0f);
      }
      defender.getEntity().getWorld().playSound(defender.getEntity().getLocation(),
          Sound.BLOCK_ANVIL_FALL, 1.0f, 2.0f);
      return false;
    }

    return true;
  }

  public static Map<DamageType, Float> buildDamage(StrifeMob attacker, StrifeMob defender,
      DamageModifiers mods) {
    Map<DamageType, Float> damageMap = DamageUtil.buildDamageMap(attacker, defender, mods);
    applyAttackTypeMods(attacker, mods.getAttackType(), damageMap);
    return damageMap;
  }

  public static FaceColor getColorFromDamages(Map<DamageType, Float> damages) {
    DamageType selected = DamageType.TRUE_DAMAGE;
    float max = 0;
    for (Entry<DamageType, Float> e : damages.entrySet()) {
      if (e.getValue() > max) {
        selected = e.getKey();
        max = e.getValue();
      }
    }
    return switch (selected) {
      case PHYSICAL -> FaceColor.RED;
      case MAGICAL -> FaceColor.BLUE;
      case FIRE -> FaceColor.ORANGE;
      case ICE -> FaceColor.CYAN;
      case LIGHTNING -> FaceColor.YELLOW;
      case EARTH -> FaceColor.BROWN;
      case LIGHT -> FaceColor.WHITE;
      case DARK -> FaceColor.PURPLE;
      default -> FaceColor.TRUE_WHITE;
    };
  }

  public static void reduceDamage(StrifeMob attacker, StrifeMob defender,
      Map<DamageType, Float> damageMap, DamageModifiers mods) {
    DamageUtil.applyDamageReductions(attacker, defender, damageMap, mods.getAbilityMods());
  }

  public static String buildDamageString(int i) {
    String str = Integer.toString(i);
    return str
        .replaceAll("0", "０\uF801")
        .replaceAll("1", "１\uF801")
        .replaceAll("2", "２\uF801")
        .replaceAll("3", "３\uF801")
        .replaceAll("4", "４\uF801")
        .replaceAll("5", "５\uF801")
        .replaceAll("6", "６\uF801")
        .replaceAll("7", "７\uF801")
        .replaceAll("8", "８\uF801")
        .replaceAll("9", "９\uF801");
  }

  public static float calculateFinalDamage(StrifeMob attacker, StrifeMob defender, AttackType attackType,
      Map<DamageType, Float> damageMap, DamageModifiers mods) {

    double standardDamage = damageMap.getOrDefault(DamageType.PHYSICAL, 0f) +
        damageMap.getOrDefault(DamageType.MAGICAL, 0f);
    double elementalDamage = damageMap.getOrDefault(DamageType.FIRE, 0f) +
        damageMap.getOrDefault(DamageType.ICE, 0f) +
        damageMap.getOrDefault(DamageType.LIGHTNING, 0f) +
        damageMap.getOrDefault(DamageType.DARK, 0f) +
        damageMap.getOrDefault(DamageType.EARTH, 0f) +
        damageMap.getOrDefault(DamageType.LIGHT, 0f);

    float potionMult = 1;
    float critMult = 0;
    boolean criticalHit = false;

    if (attackType != AttackType.BONUS) {
      potionMult = DamageUtil.getPotionMult(attacker.getEntity(), defender.getEntity());
      criticalHit = isCriticalHit(attacker, defender, mods) && !attacker.hasTrait(StrifeTrait.ELEMENTAL_CRITS_2);
      if (criticalHit && !attacker.hasTrait(StrifeTrait.NO_CRIT_MULT)) {
        critMult = (attacker.getStat(StrifeStat.CRITICAL_DAMAGE) +
            mods.getAbilityMods().getOrDefault(AbilityMod.CRITICAL_DAMAGE, 0f)) / 100;
        if (attacker.hasTrait(StrifeTrait.LETHAL_STRIKE)) {
          float chance = attacker.getStat(StrifeStat.CRITICAL_RATE) +
              mods.getAbilityMods().getOrDefault(AbilityMod.CRITICAL_CHANCE, 0f);
          if (chance > 100) {
            if (DamageUtil.isLethalHit(attacker, defender, chance - 100)) {
              critMult *= 2f;
            }
          }
        }
        if (defender.hasTrait(StrifeTrait.IRON_SCARS)) {
          critMult += 0.4F;
        }
      }
    }

    float pvpMult = 1f;
    if (attacker.getEntity() instanceof Player && defender.getEntity() instanceof Player) {
      pvpMult = PVP_MULT;
    }

    float generalDamageMultiplier = StatUtil.getDamageMult(attacker);
    float minionDamageMultiplier = DamageUtil.getMinionMult(attacker);

    standardDamage += standardDamage * critMult;
    standardDamage *= potionMult;
    standardDamage *= generalDamageMultiplier;
    standardDamage *= pvpMult;
    standardDamage *= minionDamageMultiplier;

    DamageUtil.applyLifeSteal(attacker, Math.min(standardDamage, defender.getEntity().getHealth()),
        mods.getHealMultiplier(), mods.getAbilityMods().getOrDefault(AbilityMod.LIFE_STEAL, 0f));

    if (criticalHit && attacker.hasTrait(StrifeTrait.ELEMENTAL_CRITS)) {
      elementalDamage += elementalDamage * critMult;
    }
    elementalDamage *= potionMult;
    elementalDamage *= generalDamageMultiplier;
    elementalDamage *= pvpMult;
    elementalDamage *= minionDamageMultiplier;

    float damageReduction = defender.getStat(StrifeStat.DAMAGE_REDUCTION) *
        mods.getDamageReductionRatio() * pvpMult;
    float rawDamage = (float) Math.max(0D, (standardDamage + elementalDamage) - damageReduction);

    if (mods.getAttackType() == AttackType.PROJECTILE) {
      rawDamage *= DamageUtil.getProjectileMultiplier(attacker, defender);
    }

    rawDamage *= DamageUtil.getTenacityMult(defender);

    if (attacker.getEntity().getFreezeTicks() > 0 && !(attacker.getEntity() instanceof Player)) {
      rawDamage *= 1 - 0.3f * ((float) attacker.getEntity().getFreezeTicks() / attacker.getEntity()
          .getMaxFreezeTicks());
    }
    if (defender.hasTrait(StrifeTrait.STONE_SKIN)) {
      rawDamage *= 1 - (0.03f * defender.getEarthRunes());
    }

    rawDamage += damageMap.getOrDefault(DamageType.TRUE_DAMAGE, 0f);
    rawDamage += DamageUtil.getKnowledgeMult(attacker, defender);

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

    if (defender.getChampion() == null) {
      int attackerLevel = StatUtil.getMobLevel(attacker.getEntity());
      int defenderLevel = StatUtil.getMobLevel(defender.getEntity());
      int diffCap = (int) Math.max(9f, (float) attackerLevel / 5);
      if (attackerLevel + diffCap < defenderLevel) {
        rawDamage *= (float) Math.pow(0.875, defenderLevel - (attackerLevel + diffCap));
      }
    }

    if (plugin.getPrayerManager().isPrayerActive(attacker.getEntity(), Prayer.TWO)) {
      rawDamage *= 1.05F;
    }
    if (plugin.getPrayerManager().isPrayerActive(defender.getEntity(), Prayer.ONE)) {
      rawDamage *= 0.9F;
    }

    String damageString;
    if (mods.isShowPopoffs() && attacker.getEntity() instanceof Player && attacker != defender) {
      if (rawDamage == 0) {
        plugin.getIndicatorManager().addIndicator(attacker.getEntity(), defender.getEntity(),
            IndicatorStyle.RANDOM_POPOFF, 9,  "<aqua>０", 0.65f, 1.1f, 0.75f);
      } else {
        damageString = buildDamageString(Math.round(rawDamage));
        if (criticalHit) {
          damageString += "\uF802✸";
        }
        plugin.getIndicatorManager().addIndicator(attacker.getEntity(), defender.getEntity(),
            IndicatorStyle.RANDOM_POPOFF, 9, damageString, 0.65f, 1.1f, 0.75f);
      }
    } else if (mods.isShowPopoffs() && attacker.getMaster() != null &&
        attacker.getMaster().getEntity() instanceof Player) {
      damageString = buildDamageString(Math.round(rawDamage));
      plugin.getIndicatorManager().addIndicator(attacker.getMaster().getEntity(),
          defender.getEntity(), IndicatorStyle.RANDOM_POPOFF, 9, "<gray>" + damageString, 0.65f, 1.1f, 0.75f);
    }
    return rawDamage;
  }

  public static void postDamage(StrifeMob attacker, StrifeMob defender, DamageModifiers mods, boolean isBasicAttack) {
    if (mods.getAttackType() == AttackType.BONUS) {
      return;
    }
    float ratio = mods.getDamageReductionRatio();

    DamageUtil.applyHealthOnHit(attacker, ratio, mods.getHealMultiplier(),
        mods.getAbilityMods().getOrDefault(AbilityMod.HEALTH_ON_HIT, 0f));
    DamageUtil.applyEnergyOnHit(attacker, ratio, mods.getHealMultiplier());

    DamageUtil.doReflectedDamage(defender, attacker);

    if (attacker.getStat(StrifeStat.RAGE_ON_HIT) > 0.1) {
      attacker.changeRage(attacker.getStat(StrifeStat.RAGE_ON_HIT) * ratio);
    }
    doWhenHit(attacker, defender);
    if (plugin.getPrayerManager().isPrayerActive(defender.getEntity(), Prayer.TEN)) {
      float prayerMinus = (float) defender.getChampion().getLifeSkillLevel(LifeSkillType.PRAYER) * 0.05f;
      attacker.setPrayer(Math.max(0, attacker.getPrayer() - prayerMinus));
    }
  }

  public static void doWhenHit(StrifeMob attacker, StrifeMob defender) {
    if (defender.getStat(StrifeStat.RAGE_WHEN_HIT) > 0.1) {
      defender.changeRage(defender.getStat(StrifeStat.RAGE_WHEN_HIT));
    }
    if (defender.getStat(StrifeStat.ENERGY_WHEN_HIT) > 0.1) {
      StatUtil.changeEnergy(defender, defender.getStat(StrifeStat.ENERGY_WHEN_HIT));
    }
    plugin.getAbilityManager().abilityCast(defender, attacker, TriggerAbilityType.WHEN_HIT);
  }

  private static float doSneakAttack(StrifeMob attacker, StrifeMob defender, DamageModifiers mods,
      float pvpMult) {
    Player player = (Player) attacker.getEntity();
    SkillLevelData data = PlayerDataUtil.getSkillLevels(player, LifeSkillType.SNEAK, true);
    float sneakSkill = data.getLevelWithBonus();
    float sneakDamage = sneakSkill;
    sneakDamage += (float) (defender.getEntity().getMaxHealth() * (0.1 + 0.002 * sneakSkill));
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
          defender.getEntity(), IndicatorStyle.FLOAT_UP_MEDIUM, 5, "彤", 1.0f, 1.1f, 0.75f);
    }
    defender.getEntity().getWorld().playSound(defender.getEntity().getEyeLocation(),
        Sound.ENTITY_PHANTOM_BITE, 1f, 1f);
    plugin.getStealthManager().unstealthPlayer(player);
    return sneakEvent.getSneakAttackDamage();
  }

  private static boolean isCriticalHit(StrifeMob attacker, StrifeMob defender, DamageModifiers mods) {
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
    }
    return success;
  }

  private static boolean isLethalHit(StrifeMob attacker, StrifeMob defender, float chance) {
    if (chance / 100 >= rollDouble()) {
      DamageUtil.callCritEvent(attacker, defender);
      return true;
    }
    return false;
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
      case TARGET_CURRENT_RAGE -> amount * target.getRage();
      case CASTER_CURRENT_RAGE -> amount * caster.getRage();
      case TARGET_CURRENT_CORRUPTION -> amount * target.getCorruption();
      case CASTER_CURRENT_CORRUPTION -> amount * caster.getCorruption();
      case TARGET_MAX_RAGE -> amount * target.getMaxRage();
      case CASTER_MAX_RAGE -> amount * caster.getMaxRage();
      case CASTER_ATTRIBUTE -> amount * caster.getChampion().getAttributeLevel(bonusDamage.getAttribute());
      case TARGET_ATTRIBUTE -> amount * target.getChampion().getAttributeLevel(bonusDamage.getAttribute());
      case CASTER_SKILL_LEVEL -> amount * caster.getChampion().getLifeSkillLevel(bonusDamage.getLifeSkillType());
      case TARGET_SKILL_LEVEL -> amount * target.getChampion().getLifeSkillLevel(bonusDamage.getLifeSkillType());
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
        if (bd.isNegateMinionDamage()) {
          bonus /= DamageUtil.getMinionMult(attacker);
        }
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

  public static void applyElementalEffects(StrifeMob attacker, StrifeMob defender, Map<DamageType, Float> damageMap,
      DamageModifiers mods) {
    if (mods.getAttackType() == AttackType.BONUS) {
      return;
    }
    float baseDarkDamage = damageMap.getOrDefault(DamageType.DARK, 0f);
    if (baseDarkDamage != 0) {
      damageMap.put(DamageType.DARK, baseDarkDamage *
          plugin.getCorruptionManager().getCorruptionMultiplier(defender));
    }
    float baseFireDmg = damageMap.getOrDefault(DamageType.FIRE, 0f);
    if (baseFireDmg > 0.04 && defender.getEntity().getFireTicks() > 0) {
      damageMap.put(DamageType.FIRE, baseFireDmg * 1.2f);
    }
    float chance = (mods.getAbilityMods().getOrDefault(AbilityMod.STATUS_CHANCE, 0f) +
        attacker.getStat(StrifeStat.ELEMENTAL_STATUS)) / 100;
    if (mods.isScaleChancesWithAttack()) {
      chance *= (float) Math.min(1.0, mods.getAttackMultiplier());
    }
    if (!DamageUtil.rollBool(chance, true)) {
      if (!attacker.hasTrait(StrifeTrait.ELEMENTAL_CRITS_2) ||
          !isCriticalHit(attacker, defender, mods)) {
        return;
      }
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
    totalElementalDamage *= StrifePlugin.RNG.nextFloat();
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
        doIgnite(attacker, defender, damageMap.get(DamageType.FIRE));
      }
      case ICE -> {
        mods.getElementalStatuses().add(ElementalStatus.FREEZE);
        attemptFreeze(attacker, defender);
        damageMap.put(finalElementType, damageMap.get(finalElementType) * 1.2f);
      }
      case LIGHTNING -> {
        mods.getElementalStatuses().add(ElementalStatus.SHOCK);
        bonus = attemptShock(damageMap.get(finalElementType), defender.getEntity());
        damageMap.put(finalElementType, damageMap.get(finalElementType) + bonus);
      }
      case DARK -> {
        mods.getElementalStatuses().add(ElementalStatus.CORRUPT);
        plugin.getCorruptionManager().addCorruption(defender, 12 + baseDarkDamage / 5, true);
      }
      case EARTH -> {
        mods.getElementalStatuses().add(ElementalStatus.CRUNCH);
        int runes = attacker.getEarthRunes();
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
        if (defend.getTraits().contains(StrifeTrait.INCORPOREAL)) {
          return 0;
        }
        float armor = getDefenderArmor(attack, defend);
        armor *= 1 - modDoubleMap.getOrDefault(AbilityMod.ARMOR_PEN_MULT, 0f);
        armor -= modDoubleMap.getOrDefault(AbilityMod.ARMOR_PEN, 0f);
        if (modDoubleMap.containsKey(AbilityMod.BACK_ATTACK)) {
          armor *= 0.8f;
        }
        return getArmorMult(armor);
      }
      case MAGICAL -> {
        if (defend.getTraits().contains(StrifeTrait.ANTI_MAGIC)) {
          return 0;
        }
        float warding = getDefenderWarding(attack, defend);
        warding *= 1 - modDoubleMap.getOrDefault(AbilityMod.WARD_PEN_MULT, 0f);
        warding -= modDoubleMap.getOrDefault(AbilityMod.WARD_PEN, 0f);
        if (modDoubleMap.containsKey(AbilityMod.BACK_ATTACK)) {
          warding *= 0.8f;
        }
        return getWardingMult(warding);
      }
      case FIRE -> {
        float fireResist = defend.getStat(StrifeStat.FIRE_RESIST) / 100;
        if (attack.hasTrait(StrifeTrait.SOUL_FLAME) && fireResist > 0) {
          fireResist /= 2;
        }
        if (defend.getEntity().getLocation().getBlock().getType() == Material.WATER) {
          fireResist += 0.5f;
        }
        fireResist = Math.min(fireResist, 0.85f);
        return fireResist >= 0 ? (1 - fireResist) : 1 + Math.abs(fireResist);
      }
      case ICE -> {
        float iceResist = defend.getStat(StrifeStat.ICE_RESIST) / 100;
        if (defend.getEntity().getLocation().getBlock().getType() == Material.WATER) {
          iceResist -= 0.3f;
        }
        iceResist = Math.min(iceResist, 0.85f);
        return iceResist >= 0 ? (1 - iceResist) : 1 + Math.abs(iceResist);
      }
      case LIGHTNING -> {
        float lightningResist = defend.getStat(StrifeStat.LIGHTNING_RESIST) / 100;
        if (defend.getEntity().getLocation().getBlock().getType() == Material.WATER) {
          lightningResist -= 0.3f;
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

  public static float getMinionMult(StrifeMob mob) {
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

  public static float getKnowledgeMult(StrifeMob attacker, StrifeMob defender) {
    if (attacker.getEntity() instanceof Player) {
      if (defender.getUniqueEntityId() != null) {
        if (LearninBooksPlugin.instance.getKnowledgeManager()
            .getKnowledgeLevel((Player) attacker.getEntity(), defender.getUniqueEntityId()) > 1) {
          return 1.1f;
        }
      }
    } else if (defender.getEntity() instanceof Player) {
      if (attacker.getUniqueEntityId() != null) {
        if (LearninBooksPlugin.instance.getKnowledgeManager()
            .getKnowledgeLevel((Player) defender.getEntity(), attacker.getUniqueEntityId()) > 0) {
          return 0.9f;
        }
      }
    }
    return 1f;
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

  private static void doIgnite(StrifeMob attacker, StrifeMob defender, float damage) {
    LivingEntity defendEntity = defender.getEntity();
    defendEntity.getWorld().playSound(defendEntity.getEyeLocation(),
        Sound.ITEM_FLINTANDSTEEL_USE, 1f, 0.8f);
    defendEntity.getWorld().spawnParticle(
        Particle.FLAME,
        defendEntity.getEyeLocation(),
        6 + (int) damage / 2,
        0.3, 0.3, 0.3, 0.03
    );
    float duration = (50 + damage / 2) * (1 + attacker.getStat(StrifeStat.EFFECT_DURATION) / 100);
    Ignite.setFlames(defender, Math.max((int) duration, defender.getEntity().getFireTicks()));
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

  public static void attemptFreeze(StrifeMob attacker, StrifeMob defender) {
    LivingEntity defendEntity = defender.getEntity();
    defendEntity.getWorld().playSound(defendEntity.getEyeLocation(),
        Sound.BLOCK_GLASS_BREAK, 1f, 1.3f);
    float frost = 15;
    frost *= 1 - defender.getStat(StrifeStat.ICE_RESIST) / 100;
    DamageUtil.addFrost(attacker, defender, frost);
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

  public static boolean rollDodgeFromEvasion(StrifeMob attacker, StrifeMob defender,
      Map<AbilityMod, Float> mods) {
    float totalEvasion = StatUtil.getEvasion(defender);
    float totalAccuracy = attacker.getStat(StrifeStat.ACCURACY);
    if (mods != null) {
      totalAccuracy *= 1 + mods.getOrDefault(AbilityMod.ACCURACY_MULT, 0f) / 100;
      totalAccuracy += mods.getOrDefault(AbilityMod.ACCURACY, 0f);
      if (mods.containsKey(AbilityMod.BACK_ATTACK)) {
        totalEvasion *= 0.8f;
      }
    }
    float dodgeChance = getDodgeChanceFromEvasion(totalEvasion, totalAccuracy);
    return StrifePlugin.RNG.nextFloat() < dodgeChance;
  }

  public static float getDodgeChanceFromEvasion(float evasion, float accuracy) {
    if (accuracy >= evasion) {
      return 0;
    }
    float advantage = evasion - accuracy;
    return 1f - (float) Math.pow(0.9f, advantage / 20);
  }

  public static boolean isEvaded(StrifeMob attacker, StrifeMob defender, Map<AbilityMod, Float> attackModifiers) {
    if (StrifePlugin.RNG.nextFloat() < defender.getStat(StrifeStat.DODGE_CHANCE) / 100) {
      DamageUtil.doEvasion(attacker, defender);
      return true;
    }
    if (rollDodgeFromEvasion(attacker, defender, attackModifiers)) {
      DamageUtil.doEvasion(attacker, defender);
      return true;
    }
    return false;
  }

  public static void doEvasion(StrifeMob attacker, StrifeMob defender) {
    callEvadeEvent(defender, attacker);
    defender.getEntity().getWorld().playSound(defender.getEntity().getEyeLocation(),
        Sound.ENTITY_GHAST_SHOOT, 0.5f, 2f);
    if (attacker.getEntity() instanceof Player) {
      StrifePlugin.getInstance().getIndicatorManager().addIndicator(attacker.getEntity(),
          defender.getEntity(), IndicatorStyle.BOUNCE, 6, "彥", 3.5f, 3f, 2f);
    }
  }

  public static float getPotionMult(LivingEntity attacker, LivingEntity defender) {
    float potionMult = 1.0f;

    PotionEffect powerEffect = attacker.getPotionEffect(PotionEffectType.INCREASE_DAMAGE);
    if (powerEffect != null) {
      potionMult += 0.1f * (powerEffect.getAmplifier() + 1);
    }
    PotionEffect weaknessEffect = attacker.getPotionEffect(PotionEffectType.WEAKNESS);
    if (weaknessEffect != null) {
      potionMult -= 0.1f * (weaknessEffect.getAmplifier() + 1);
    }

    PotionEffect vulnerabilityEffect = defender.getPotionEffect(PotionEffectType.UNLUCK);
    if (vulnerabilityEffect != null) {
      potionMult += 0.1f * (vulnerabilityEffect.getAmplifier() + 1);
    }
    PotionEffect resistEffect = defender.getPotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
    if (resistEffect != null) {
      potionMult -= 0.1f * (resistEffect.getAmplifier() + 1);
    }
    return Math.max(0, potionMult);
  }

  public static boolean canAttack(Player attacker, Player defender) {
    if (plugin.getSnazzyPartiesHook().inSameParty(attacker, defender)) {
      return false;
    }
    World world = stringMatcher.getWorldByName(defender.getWorld().getName());
    RegionManager manager = regionContainer.get(world);

    BlockVector3 vectorLoc1 = at(
        defender.getLocation().getBlockX(),
        defender.getLocation().getBlockY(),
        defender.getLocation().getBlockZ()
    );
    assert manager != null;
    ApplicableRegionSet regions1 = manager.getApplicableRegions(vectorLoc1);
    if (State.DENY == regions1.queryValue(WorldGuardPlugin.inst()
        .wrapPlayer(attacker), Flags.PVP)) {
      return false;
    }

    BlockVector3 vectorLoc2 = at(
        attacker.getLocation().getBlockX(),
        attacker.getLocation().getBlockY(),
        attacker.getLocation().getBlockZ()
    );
    ApplicableRegionSet regions2 = manager.getApplicableRegions(vectorLoc2);
    if (State.DENY == regions2.queryValue(WorldGuardPlugin.inst()
        .wrapPlayer(attacker), Flags.PVP)) {
      return false;
    }
    return true;
  }

  public static float getProjectileMultiplier(StrifeMob atk, StrifeMob def) {
    return Math.max(0.05f, 1f + (atk.getStat(StrifeStat.PROJECTILE_DAMAGE) -
        def.getStat(StrifeStat.PROJECTILE_REDUCTION)) / 100f);
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
      applyBleed(attacker, defender, damage, bypassBarrier, false, false);
    }
    return true;
  }

  public static boolean attemptPoison(StrifeMob attacker, StrifeMob defender, DamageModifiers mods) {
    if (mods.getAttackType() == AttackType.BONUS) {
      return false;
    }
    if (defender.getStat(StrifeStat.POISON_RESIST) > 99) {
      return false;
    }
    float chance = (attacker.getStat(StrifeStat.POISON_CHANCE) +
        mods.getAbilityMods().getOrDefault(AbilityMod.POISON_CHANCE, 0f)) / 100;
    if (mods.isScaleChancesWithAttack()) {
      chance *= mods.getDamageReductionRatio();
    }
    if (chance >= rollDouble()) {
      PotionEffect potionEffect = defender.getEntity().getPotionEffect(PotionEffectType.POISON);
      int amp;
      if (potionEffect == null) {
        amp = 0;
      } else {
        int maxAmount = attacker.hasTrait(StrifeTrait.DEADLY_POISON) ? 6 : 4;
        int currentAmount = potionEffect.getAmplifier();
        if (currentAmount > maxAmount) {
          return false;
        }
        amp = Math.min(maxAmount, potionEffect.getAmplifier() + 1);
      }
      float totalTicks = 100;
      float durationBonus = attacker.getStat(StrifeStat.EFFECT_DURATION) +
          attacker.getStat(StrifeStat.POISON_DURATION) +
          mods.getAbilityMods().getOrDefault(AbilityMod.POISON_DURATION, 0f);
      totalTicks *= (1 + durationBonus / 100);
      PotionEffect newPoisonEffect = new PotionEffect(
          PotionEffectType.POISON,
          (int) totalTicks,
          amp,
          false,
          true
      );
      defender.getEntity().removePotionEffect(PotionEffectType.POISON);
      defender.getEntity().addPotionEffect(newPoisonEffect);
      plugin.getIndicatorManager().addIndicator(attacker.getEntity(), defender.getEntity(),
          IndicatorStyle.FLOAT_UP_MEDIUM, 5, POISON_TEXT, 1.0f, 1.0f, 1.0f);
      attacker.getEntity().getWorld().playSound(defender.getEntity().getLocation(),
          Sound.ENTITY_SILVERFISH_DEATH, 1, 2.0f);
      return true;
    }
    return false;
  }

  public static void applyBleed(StrifeMob attacker, StrifeMob defender, float amount,
      boolean bypassBarrier, boolean bypassArmor, boolean bypassMultipliers) {
    if (amount < 0.2) {
      return;
    }
    if (!bypassArmor) {
      amount *= (float) StatUtil.getArmorMult(attacker, defender);
    }
    if (!bypassMultipliers) {
      if (attacker != null && defender.getFrost() > 0
          && attacker.hasTrait(StrifeTrait.BLOOD_AND_ICE)) {
        amount *= 1.3F;
      }
      amount *= 1 - defender.getStat(StrifeStat.BLEED_RESIST) / 100;
    }
    defender.addBleed(amount, bypassBarrier);
  }

  public static void addFrost(StrifeMob attacker, StrifeMob defender, float amount) {
    if (attacker != null && attacker.hasTrait(StrifeTrait.BLOOD_AND_ICE) && defender.isBleeding()) {
      amount *= 1.3F;
    }
    if (amount < 0.1) {
      return;
    }
    defender.addFrost((int) amount);
  }

  public static void doReflectedDamage(StrifeMob defender, StrifeMob attacker) {
    if (!defender.canReflectAt(attacker.getEntity().getUniqueId())) {
      return;
    }
    if (defender.getStat(StrifeStat.DAMAGE_REFLECT) < 0.1) {
      return;
    }
    if (!attacker.getEntity().isValid()) {
      return;
    }
    if (defender.isInvincible()) {
      return;
    }
    float reflectDamage = defender.getStat(StrifeStat.DAMAGE_REFLECT) * 1.15f;
    float warding = StatUtil.getStat(defender, StrifeStat.WARDING);
    reflectDamage *= StatUtil.getWardingMult(warding);
    if (reflectDamage <= 0.1) {
      return;
    }
    defender.getEntity().getWorld().playSound(defender.getEntity().getLocation(),
        Sound.BLOCK_LANTERN_STEP, SoundCategory.HOSTILE, 2f, 1.5f);
    if (attacker.getEntity() instanceof Player) {
      ((Player) attacker.getEntity()).spawnParticle(Particle.DAMAGE_INDICATOR,
          TargetingUtil.getOriginLocation(attacker.getEntity(), OriginLocation.CENTER),
          (int) reflectDamage, 0.3, 0.3,
          0.3, 0.1);
      if (defender.getEntity() instanceof Player && attacker != defender) {
        attacker.flagPvp();
        defender.flagPvp();
      }
    }
    DamageUtil.dealRawDamage(attacker, reflectDamage);
    defender.cacheReflect(attacker.getEntity().getUniqueId());
  }

  public static void callCritEvent(StrifeMob attacker, StrifeMob victim) {
    CriticalEvent c = new CriticalEvent(attacker, victim);
    Bukkit.getPluginManager().callEvent(c);
  }

  public static float doPreDeath(StrifeMob victim, float damage) {
    plugin.getStrifeMobManager().updateEquipmentStats(victim);
    Set<LoreAbility> abilitySet = new HashSet<>(victim.getLoreAbilities(ON_DEATH));
    executeBoundEffects(victim, victim, abilitySet);
    executeFiniteEffects(victim, victim, new HashSet<>(List.of(ON_DEATH)));
    plugin.getAbilityIconManager().untoggleDeathToggles(victim);
    if (victim.isInvincible()) {
      victim.getEntity().setHealth(1);
      return 0.05f;
    }
    return damage;
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

  public static void dealRawDamage(StrifeMob mob, float damage) {
    if (mob.isInvincible()) {
      return;
    }
    LivingEntity le = mob.getEntity();
    if (damage >= le.getHealth()) {
      damage = DamageUtil.doPreDeath(mob, damage);
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
    return StrifePlugin.RNG.nextFloat();
  }

  public static boolean rollBool(float chance, boolean lucky) {
    return lucky ? rollBool(chance) || rollBool(chance) : rollBool(chance);
  }

  public static boolean rollBool(float chance) {
    return StrifePlugin.RNG.nextFloat() <= chance;
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
    CASTER_MAX_RAGE,
    CASTER_SKILL_LEVEL,
    TARGET_SKILL_LEVEL,
    CASTER_ATTRIBUTE,
    TARGET_ATTRIBUTE,
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
    POISON_CHANCE,
    POISON_DURATION,
    STATUS_CHANCE,
    BACK_ATTACK,
    MAX_DAMAGE
  }

  public enum AttackType {
    MELEE, PROJECTILE, AREA, OTHER, BONUS
  }
}
