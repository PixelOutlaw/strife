package land.face.strife.util;

import static com.sk89q.worldedit.math.BlockVector3.at;
import static land.face.strife.listeners.LoreAbilityListener.executeBoundEffects;
import static land.face.strife.listeners.LoreAbilityListener.executeFiniteEffects;
import static land.face.strife.managers.LoreAbilityManager.TriggerType.ON_DEATH;
import static land.face.strife.util.StatUtil.getArmorMult;
import static land.face.strife.util.StatUtil.getDefenderArmor;

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
import com.tealcube.minecraft.bukkit.facecore.utilities.MoveUtil;
import com.tealcube.minecraft.bukkit.facecore.utilities.PaletteUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import land.face.dinvy.DeluxeInvyPlugin;
import land.face.dinvy.entity.PlayerData;
import land.face.dinvy.windows.equipment.EquipmentMenu.DeluxeSlot;
import land.face.learnin.LearninBooksPlugin;
import land.face.strife.StrifePlugin;
import land.face.strife.data.BonusDamage;
import land.face.strife.data.DamageModifiers;
import land.face.strife.data.DamageModifiers.ElementalStatus;
import land.face.strife.data.LoreAbility;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.ability.EntityAbilitySet.TriggerAbilityType;
import land.face.strife.data.champion.LifeSkillType;
import land.face.strife.data.champion.SkillRank;
import land.face.strife.data.effects.Ignite;
import land.face.strife.data.pojo.SkillLevelData;
import land.face.strife.events.CriticalEvent;
import land.face.strife.events.EvadeEvent;
import land.face.strife.events.SneakAttackEvent;
import land.face.strife.events.StrifeEarlyDamageEvent;
import land.face.strife.managers.BossBarManager;
import land.face.strife.managers.GuiManager;
import land.face.strife.managers.IndicatorManager.IndicatorStyle;
import land.face.strife.managers.PrayerManager.Prayer;
import land.face.strife.stats.StrifeStat;
import land.face.strife.stats.StrifeTrait;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.data.BlockData;
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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class DamageUtil {

  private static StrifePlugin plugin;

  public static float BASE_ATTACK_SECONDS = 1.4f;

  public static int BASE_RECHARGE_TICKS;
  public static long TICK_RATE;
  public static int DELAY_TICKS;
  public static float FLAT_BARRIER_PER_SECOND;
  public static float PERCENT_BARRIER_PER_SECOND;

  public static TextComponent deathMessage;
  public static List<TextComponent> sillyDeathMsgs;

  private static final DamageModifier[] MODIFIERS = EntityDamageEvent.DamageModifier.values();
  public static final DamageType[] DMG_TYPES = DamageType.values();

  private static final BlockData DORT = Material.COARSE_DIRT.createBlockData();

  private static final String POISON_TEXT = "彣";

  private static final RegionContainer regionContainer = WorldGuard.getInstance()
      .getPlatform().getRegionContainer();
  private static final StringMatcher stringMatcher = WorldGuard.getInstance()
      .getPlatform().getMatcher();

  public static void refresh(StrifePlugin refreshedPlugin) {
    plugin = refreshedPlugin;

    BASE_ATTACK_SECONDS = (float) plugin.getSettings().getDouble("config.mechanics.attack-speed.base-attack-time", 1.4);

    BASE_RECHARGE_TICKS = plugin.getSettings().getInt("config.mechanics.barrier.base-delay-ticks", 100);
    TICK_RATE = plugin.getSettings().getInt("config.mechanics.barrier.task-tick-rate", 3);
    DELAY_TICKS = (int) ((float) BASE_RECHARGE_TICKS / TICK_RATE);
    FLAT_BARRIER_PER_SECOND = plugin.getSettings().getInt("config.mechanics.barrier.flat-per-second", 4);
    PERCENT_BARRIER_PER_SECOND = (float) plugin.getSettings()
        .getDouble("config.mechanics.barrier.percent-per-second", 0.08);

    deathMessage = BossBarManager.covertStringToRetardComponent(
        PaletteUtil.color(plugin.getSettings().getString("language.enemy-killed-title")));
    sillyDeathMsgs = new ArrayList<>();
    for (String s : plugin.getSettings().getStringList("language.silly-enemy-killed-titles")) {
      sillyDeathMsgs.add(BossBarManager.covertStringToRetardComponent(PaletteUtil.color(s)));
    }
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
    }

    if (attacker != defender) {
      attacker.bumpCombat(defender);
      defender.bumpCombat(null);
    }

    float attackMult = mods.getAttackMultiplier();

    if (plugin.getCounterManager().executeCounters(attacker.getEntity(), defender.getEntity())) {
      return false;
    }

    if (defender instanceof Player) {
      plugin.getStealthManager().unstealthPlayer((Player) defender.getEntity(), true);
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
        plugin.getIndicatorManager().addIndicator(attacker, defender.getEntity(), IndicatorStyle.FLOAT_UP_MEDIUM,
            4, "彡", 1.0f, 1.0f, 1.0f, false);
      }
      defender.getEntity().getWorld().playSound(defender.getEntity().getLocation(),
          Sound.BLOCK_ANVIL_FALL, 1.0f, 2.0f);
      return false;
    }

    return true;
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

  public static float calculateFinalDamage(StrifeMob atkMob, StrifeMob defMob, AttackType attackType,
      Map<DamageType, Float> damageMap, DamageModifiers mods) {

    double physicalDamage = damageMap.getOrDefault(DamageType.PHYSICAL, 0f);
    double elementalDamage = damageMap.getOrDefault(DamageType.FIRE, 0f) +
        damageMap.getOrDefault(DamageType.ICE, 0f) +
        damageMap.getOrDefault(DamageType.LIGHTNING, 0f) +
        damageMap.getOrDefault(DamageType.DARK, 0f) +
        damageMap.getOrDefault(DamageType.EARTH, 0f) +
        damageMap.getOrDefault(DamageType.LIGHT, 0f);

    float critMult = 0;
    boolean criticalHit = false;
    boolean isSneakAttack = false;

    if (mods.getAttackType() != AttackType.BONUS) {
      if (mods.isSneakAttack()) {
        if (!SpecialStatusUtil.isSneakImmune(defMob.getEntity())) {
          damageMap.put(DamageType.TRUE_DAMAGE,
              damageMap.getOrDefault(DamageType.TRUE_DAMAGE, 0f) + doSneakAttack(atkMob, defMob, mods));
          isSneakAttack = true;
        }
        plugin.getStealthManager().unstealthPlayer((Player) atkMob.getEntity(), false);
      }
    }

    float potionMult = DamageUtil.getPotionMult(atkMob.getEntity(), defMob.getEntity());

    if (mods.isCanCrit()) {
      criticalHit = isCriticalHit(atkMob, defMob, mods) && !atkMob.hasTrait(StrifeTrait.ELEMENTAL_CRITS_2);
      if (criticalHit && !atkMob.hasTrait(StrifeTrait.NO_CRIT_MULT)) {
        critMult = (atkMob.getStat(StrifeStat.CRITICAL_DAMAGE) +
            mods.getAbilityMods().getOrDefault(AbilityMod.CRITICAL_DAMAGE, 0f)) / 100;
        if (atkMob.hasTrait(StrifeTrait.LETHAL_STRIKE)) {
          float chance = atkMob.getStat(StrifeStat.CRITICAL_RATE) +
              mods.getAbilityMods().getOrDefault(AbilityMod.CRITICAL_CHANCE, 0f);
          if (chance > 100) {
            if (DamageUtil.isLethalHit(atkMob, defMob, chance - 100)) {
              critMult *= 2f;
            }
          }
        }
        if (defMob.hasTrait(StrifeTrait.IRON_SCARS)) {
          critMult += 0.4F;
        }
      }
    }

    if (atkMob.hasTrait(StrifeTrait.ICE_PICK)) {
      physicalDamage *= 1 + (defMob.getFrost() / 100);
    }

    float generalDamageMultiplier = 1f;
    generalDamageMultiplier *= 1f + defMob.getStat(StrifeStat.DAMAGE_TAKEN) / 100F;
    if (attackType != AttackType.BONUS) {
      if (atkMob.getStat(StrifeStat.PVP_ATTACK) > 0 && defMob.getStat(StrifeStat.PVP_ATTACK) > 0) {
        generalDamageMultiplier *= atkMob.getStat(StrifeStat.PVP_ATTACK) / defMob.getStat(StrifeStat.PVP_DEFENCE);
      }
    }
    if (mods.isApplyMinionDamageMult()) {
      generalDamageMultiplier *= 1 + atkMob.getStat(StrifeStat.MINION_DAMAGE) / 100;
    }
    float minionDamageMultiplier = DamageUtil.getMinionMult(atkMob);

    physicalDamage += physicalDamage * critMult;
    physicalDamage *= potionMult;
    physicalDamage *= generalDamageMultiplier;
    physicalDamage *= minionDamageMultiplier;

    elementalDamage += elementalDamage * critMult;
    elementalDamage *= potionMult;
    elementalDamage *= generalDamageMultiplier;
    elementalDamage *= minionDamageMultiplier;

    float damageReduction = defMob.getStat(StrifeStat.DAMAGE_REDUCTION) * mods.getDamageReductionRatio();
    float rawDamage = (float) Math.max(0D, (physicalDamage + elementalDamage) - damageReduction);

    if (mods.getAttackType() == AttackType.PROJECTILE) {
      rawDamage *= DamageUtil.getProjectileMultiplier(atkMob, defMob);
    }

    rawDamage *= DamageUtil.getTenacityMult(defMob, false);

    if (attackType != AttackType.BONUS) {
      if (atkMob.getEntity().getFreezeTicks() > 0 && !(atkMob.getEntity() instanceof Player)) {
        rawDamage *=
            1 - 0.3f * ((float) atkMob.getEntity().getFreezeTicks() / atkMob.getEntity().getMaxFreezeTicks());
      }
      if (defMob.hasTrait(StrifeTrait.STONE_SKIN)) {
        rawDamage *= 1 - (0.03f * defMob.getEarthRunes());
      }
      if (mods.isFromAbility()) {
        rawDamage *= 1 + atkMob.getStat(StrifeStat.ABILITY_DAMAGE) / 100;
      }
      if (atkMob.hasTrait(StrifeTrait.VENGEANCE)) {
        rawDamage *= 2f - (float) (atkMob.getEntity().getHealth() / atkMob.getMaxLife());
      }
      if (defMob.hasTrait(StrifeTrait.SKY_CRASH)) {
        var velocity = defMob.isPlayer() ? MoveUtil.getVelocity(defMob.getEntity()) : defMob.getEntity().getVelocity();
        if (velocity.getY() < 0) {
          rawDamage *= 0.9f;
        }
      }
      if (mods.isBasicAttack() && mods.isApplyOnHitEffects() && atkMob.hasTrait(StrifeTrait.SKY_CRASH)) {
        var velocity = atkMob.isPlayer() ? MoveUtil.getVelocity(atkMob.getEntity()) : atkMob.getEntity().getVelocity();
        if (velocity.getY() < 0) {
          rawDamage *= 1.2f;
          velocity = velocity.clone();
          velocity.setY(3);
          atkMob.getEntity().setVelocity(velocity);
          atkMob.getEntity().getWorld().playSound(atkMob.getEntity().getLocation(), Sound.ENTITY_IRON_GOLEM_ATTACK, 1f, 1.5f);
        }
      }
      DamageUtil.applyLifeSteal(atkMob, defMob,
          Math.min(rawDamage, defMob.getEntity().getHealth()),
          mods.getHealMultiplier(),
          mods.getAbilityMods().getOrDefault(AbilityMod.LIFE_STEAL, 0f)
      );
    }

    rawDamage += damageMap.getOrDefault(DamageType.TRUE_DAMAGE, 0f);
    rawDamage += DamageUtil.getKnowledgeMult(atkMob, defMob);

    if (mods.getAbilityMods().containsKey(AbilityMod.MAX_DAMAGE)) {
      rawDamage = Math.min(mods.getAbilityMods().get(AbilityMod.MAX_DAMAGE), rawDamage);
    }

    if (rawDamage < 1) {
      rawDamage = 0;
    }

    if (defMob.getChampion() == null) {
      int attackerLevel = StatUtil.getMobLevel(atkMob.getEntity());
      int defenderLevel = StatUtil.getMobLevel(defMob.getEntity());
      int diffCap = (int) Math.max(9f, (float) attackerLevel / 5);
      if (attackerLevel + diffCap < defenderLevel) {
        rawDamage *= (float) Math.pow(0.875, defenderLevel - (attackerLevel + diffCap));
      }
    }

    if (plugin.getPrayerManager().isPrayerActive(atkMob.getEntity(), Prayer.TWO)) {
      if (atkMob.hasTrait(StrifeTrait.FAITH_DAMAGE)) {
        rawDamage *= 1.05f + getMissingArmorItems((Player) atkMob.getEntity()) * 0.05f;
      } else {
        rawDamage *= 1.05F;
      }
    }
    if (plugin.getPrayerManager().isPrayerActive(defMob.getEntity(), Prayer.ONE)) {
      if (defMob.hasTrait(StrifeTrait.FAITH_DEFENSE)) {
        rawDamage *= 0.9f - getMissingArmorItems((Player) defMob.getEntity()) * 0.1f;
      } else {
        rawDamage *= 0.9F;
      }
    }

    String damageString;
    if (mods.isShowPopoffs() && atkMob.getEntity() instanceof Player && atkMob != defMob) {
      if (rawDamage == 0) {
        plugin.getIndicatorManager().addIndicator(atkMob, defMob.getEntity(),
            IndicatorStyle.RANDOM_POPOFF, 9, "<aqua>０", 0.6f, 1.1f, 0.3f, false);
      } else {
        damageString = buildDamageString(Math.round(rawDamage));
        if (criticalHit) {
          damageString += "\uF802✸";
        }
        plugin.getIndicatorManager().addIndicator(atkMob, defMob.getEntity(), IndicatorStyle.RANDOM_POPOFF,
            9, damageString, 0.6f, 1.1f, 0.3f, false);
      }
    } else if (mods.isShowPopoffs() && atkMob.getMaster() != null &&
        atkMob.getMaster().getEntity() instanceof Player) {
      damageString = buildDamageString(Math.round(rawDamage));
      plugin.getIndicatorManager().addIndicator(atkMob.getMaster(), defMob.getEntity(),
          IndicatorStyle.RANDOM_POPOFF, 9, "<gray>" + damageString, 0.6f, 1.1f, 0.3f, false);
    }

    if (isSneakAttack) {
      boolean finishingBlow = rawDamage > defMob.getEntity().getHealth() + defMob.getBarrier();
      float gainedXp = plugin.getStealthManager().getSneakAttackExp(defMob.getEntity(),
          atkMob.getChampion().getLifeSkillLevel(LifeSkillType.SNEAK), finishingBlow);
      plugin.getSkillExperienceManager().addExperience((Player) atkMob.getEntity(),
          LifeSkillType.SNEAK, gainedXp, false, false);
    }

    return rawDamage;
  }

  public static void postDamage(StrifeMob attacker, StrifeMob defender, DamageModifiers mods, boolean isBasicAttack) {
    if (!mods.isApplyOnHitEffects()) {
      return;
    }
    float ratio = mods.getDamageReductionRatio() * mods.getHealMultiplier();
    float healRatio = ratio * (1 + attacker.getStat(StrifeStat.HEALING_POWER) / 300);

    DamageUtil.applyHealthOnHit(attacker, defender, healRatio,
        mods.getAbilityMods().getOrDefault(AbilityMod.HEALTH_ON_HIT, 0f));
    DamageUtil.applyEnergyOnHit(attacker, ratio);

    DamageUtil.doReflectedDamage(defender, attacker);

    if (attacker.getStat(StrifeStat.RAGE_ON_HIT) > 0.1) {
      attacker.changeRage(attacker.getStat(StrifeStat.RAGE_ON_HIT) * ratio);
    }
    doWhenHit(attacker, defender, mods.getAttackType());
    if (plugin.getPrayerManager().isPrayerActive(defender.getEntity(), Prayer.TEN)) {
      float prayerMinus = (float) defender.getChampion().getLifeSkillLevel(LifeSkillType.PRAYER) * 0.05f;
      attacker.setPrayer(Math.max(0, attacker.getPrayer() - prayerMinus));
    }
  }

  public static void doWhenHit(StrifeMob attacker, StrifeMob defender, AttackType attackType) {
    if (defender.getStat(StrifeStat.RAGE_WHEN_HIT) > 0.1) {
      defender.changeRage(defender.getStat(StrifeStat.RAGE_WHEN_HIT));
    }
    if (defender.getStat(StrifeStat.ENERGY_WHEN_HIT) > 0.1) {
      StatUtil.changeEnergy(defender, defender.getStat(StrifeStat.ENERGY_WHEN_HIT));
    }
    plugin.getAbilityManager().abilityCast(defender, attacker, TriggerAbilityType.WHEN_HIT);
    switch (attackType) {
      case MELEE -> plugin.getAbilityManager().abilityCast(defender, attacker, TriggerAbilityType.WHEN_MELEE_HIT);
      case PROJECTILE -> plugin.getAbilityManager().abilityCast(defender, attacker, TriggerAbilityType.WHEN_RANGED_HIT);
    }
  }

  private static float doSneakAttack(StrifeMob attacker, StrifeMob defender, DamageModifiers mods) {
    Player player = (Player) attacker.getEntity();
    SkillLevelData data = PlayerDataUtil.getSkillLevels(player, LifeSkillType.SNEAK, true);
    SkillRank rank = SkillRank.getRank(attacker, LifeSkillType.SNEAK);
    float hpPercent = switch (rank) {
      case NOVICE -> 0.1f;
      case APPRENTICE -> 0.125f;
      case JOURNEYMAN -> 0.15f;
      case EXPERT -> 0.175f;
      case MASTER -> 0.2f;
    };
    float damage = (defender.getMaxLife() * hpPercent) + data.getLevelWithBonus();
    damage *= 1 + attacker.getStat(StrifeStat.SNEAK_ATTACK_BONUS) / 100;
    SneakAttackEvent sneakEvent = DamageUtil.callSneakAttackEvent(attacker, defender, data.getLevel(), damage);

    if (sneakEvent.isCancelled()) {
      return 0f;
    }

    if (SkillRank.isRank(rank, SkillRank.EXPERT)) {
      defender.getEntity().addPotionEffect(new PotionEffect(PotionEffectType.UNLUCK, 40, 1, false));
    }

    if (!(defender.getEntity() instanceof Player)) {
      SpecialStatusUtil.setSneakImmune(defender.getEntity());
    }
    if (mods.isShowPopoffs()) {
      StrifePlugin.getInstance().getIndicatorManager().addIndicator(attacker, defender.getEntity(),
          IndicatorStyle.FLOAT_UP_MEDIUM, 5, "彤", 1.0f, 1.1f, 0.75f, false);
    }
    defender.getEntity().getWorld().playSound(defender.getEntity().getEyeLocation(), Sound.ENTITY_PHANTOM_BITE, 1f, 1f);

    plugin.getStealthManager().unstealthPlayer(player, false);
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
      if (mods.isCanCrit()) {
        DamageUtil.callCritEvent(attacker, defender);
      }
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
      case FIRE -> attacker.getStat(StrifeStat.FIRE_DAMAGE);
      case ICE -> attacker.getStat(StrifeStat.ICE_DAMAGE);
      case LIGHTNING -> attacker.getStat(StrifeStat.LIGHTNING_DAMAGE);
      case DARK -> attacker.getStat(StrifeStat.DARK_DAMAGE);
      case EARTH -> attacker.getStat(StrifeStat.EARTH_DAMAGE);
      case LIGHT -> attacker.getStat(StrifeStat.LIGHT_DAMAGE);
      case TRUE_DAMAGE -> attacker.getStat(StrifeStat.TRUE_DAMAGE);
    };
  }

  public static float applyDamageScale(StrifeMob caster, StrifeMob target, BonusDamage bonusDamage) {
    float amount = bonusDamage.getAmount();
    return (float) switch (bonusDamage.getDamageScale()) {
      case FLAT -> amount;
      case CASTER_STAT_PERCENT -> bonusDamage.getAmount() * caster.getStat(bonusDamage.getDamageStat());
      case TARGET_STAT_PERCENT -> bonusDamage.getAmount() * target.getStat(bonusDamage.getDamageStat());
      case CASTER_LEVEL -> amount * StatUtil.getMobLevel(caster.getEntity());
      case TARGET_LEVEL -> amount * StatUtil.getMobLevel(target.getEntity());
      case CASTER_DAMAGE -> amount * DamageUtil.getRawDamage(caster, bonusDamage.getDamageType());
      case TARGET_CURRENT_HEALTH -> amount * target.getEntity().getHealth();
      case CASTER_CURRENT_HEALTH -> amount * caster.getEntity().getHealth();
      case TARGET_MISSING_HEALTH -> amount * (target.getEntity().getMaxHealth() - target.getEntity().getHealth());
      case CASTER_MISSING_HEALTH -> amount * (caster.getEntity().getMaxHealth() - caster.getEntity().getHealth());
      case TARGET_MAX_HEALTH -> amount * target.getEntity().getMaxHealth();
      case CASTER_MAX_HEALTH -> amount * caster.getEntity().getMaxHealth();
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
      case TARGET_MAX_BLOCK -> amount * target.getMaxBlock();
      case CASTER_MAX_BLOCK -> amount * caster.getMaxBlock();
      case CASTER_ATTRIBUTE -> amount * caster.getChampion().getAttributeLevel(bonusDamage.getAttribute());
      case TARGET_ATTRIBUTE -> amount * target.getChampion().getAttributeLevel(bonusDamage.getAttribute());
      case CASTER_SKILL_LEVEL -> amount * caster.getChampion().getLifeSkillLevel(bonusDamage.getLifeSkillType());
      case TARGET_SKILL_LEVEL -> amount * target.getChampion().getLifeSkillLevel(bonusDamage.getLifeSkillType());
    };
  }

  public static Map<DamageType, Float> buildDamageMap(StrifeMob attacker, StrifeMob target, DamageModifiers mods) {
    Map<DamageType, Float> damageMap = new HashMap<>();
    float multiplier = !mods.isUseBasicDamageMult() ? 1 : 1 + attacker.getStat(StrifeStat.DAMAGE_MULT) / 100;
    for (DamageType damageType : DMG_TYPES) {
      float amount = getRawDamage(attacker, damageType);
      if (amount > 0) {
        amount *= mods.getDamageMultipliers().getOrDefault(damageType, 1f);
        amount *= mods.getAttackMultiplier();
        if (damageType != DamageType.TRUE_DAMAGE) {
          amount *= multiplier;
        }
        damageMap.put(damageType, amount);
      }
    }
    for (BonusDamage bd : mods.getBonusDamages()) {
      float bonus = applyDamageScale(attacker, target, bd);
      if (bd.isNegateMinionDamage()) {
        bonus /= DamageUtil.getMinionMult(attacker);
      }
      damageMap.put(bd.getDamageType(), damageMap.getOrDefault(bd.getDamageType(), 0f) + bonus);
    }
    return damageMap;
  }

  public static void applyDamageReductions(StrifeMob attacker, StrifeMob defender,
      Map<DamageType, Float> damageMap, Map<AbilityMod, Float> abilityMods) {
    if (attacker.hasTrait(StrifeTrait.WEAK_LINK)) {
      abilityMods.put(AbilityMod.CAPPED_RESIST, calcWeakLinkResist(defender));
    }
    damageMap.replaceAll((t, v) ->
        damageMap.get(t) * getDamageReduction(t, attacker, defender, abilityMods) * resistTraitMult(defender, t));
  }

  public static float resistTraitMult(StrifeMob mob, DamageType type) {
    return switch (type) {
      case PHYSICAL -> mob.hasTrait(StrifeTrait.RESIST_PHYSICAL) ? 0.5f : 1f;
      case FIRE -> mob.hasTrait(StrifeTrait.RESIST_FIRE) ? 0.5f : 1f;
      case ICE -> mob.hasTrait(StrifeTrait.RESIST_ICE) ? 0.5f : 1f;
      case LIGHTNING -> mob.hasTrait(StrifeTrait.RESIST_LIGHTNING) ? 0.5f : 1f;
      case EARTH -> mob.hasTrait(StrifeTrait.RESIST_EARTH) ? 0.5f : 1f;
      case DARK -> mob.hasTrait(StrifeTrait.RESIST_DARK) ? 0.5f : 1f;
      case LIGHT -> mob.hasTrait(StrifeTrait.RESIST_LIGHT) ? 0.5f : 1f;
      default -> 1f;
    };
  }

  private static float calcWeakLinkResist(StrifeMob target) {
    Float[] values = new Float[6];
    values[0] = target.getStat(StrifeStat.FIRE_RESIST);
    values[1] = target.getStat(StrifeStat.ICE_RESIST);
    values[2] = target.getStat(StrifeStat.LIGHTNING_RESIST);
    values[3] = target.getStat(StrifeStat.LIGHT_RESIST);
    values[4] = target.getStat(StrifeStat.EARTH_RESIST);
    values[5] = target.getStat(StrifeStat.DARK_RESIST);
    Arrays.sort(values);
    return values[1];
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
    float elementalMult = 1 + attacker.getStat(StrifeStat.ELEMENTAL_MULT) / 100;
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
    if (mods.getAttackType() != AttackType.BONUS) {
      float baseDarkDamage = damageMap.getOrDefault(DamageType.DARK, 0f);
      if (baseDarkDamage > 0.5) {
        damageMap.put(DamageType.DARK,
            baseDarkDamage * plugin.getCorruptionManager().getCorruptionMultiplier(defender));
      }
      float baseFireDmg = damageMap.getOrDefault(DamageType.FIRE, 0f);
      if (baseFireDmg > 0.5 && defender.getEntity().getFireTicks() > 0) {
        damageMap.put(DamageType.FIRE, baseFireDmg * 1.2f);
      }
    }
    if (!mods.isApplyOnHitEffects()) {
      return;
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
      if (type != DamageType.PHYSICAL && type != DamageType.TRUE_DAMAGE) {
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
        damageMap.put(finalElementType, damageMap.get(finalElementType) * 1.25f);
      }
      case LIGHTNING -> {
        mods.getElementalStatuses().add(ElementalStatus.SHOCK);
        attemptShock(attacker, defender);
        damageMap.put(finalElementType, damageMap.get(finalElementType) * 1.4f);
      }
      case DARK -> {
        mods.getElementalStatuses().add(ElementalStatus.CORRUPT);
        plugin.getCorruptionManager().addCorruption(defender, 12.5f, true);
      }
      case EARTH -> {
        mods.getElementalStatuses().add(ElementalStatus.CRUNCH);
        int runes = attacker.getEarthRunes();
        float newDamage = damageMap.get(DamageType.EARTH) * (1.25f + (runes * 0.05f));
        defender.getEntity().getWorld().playSound(
            defender.getEntity().getEyeLocation(), Sound.BLOCK_ROOTED_DIRT_BREAK, 1f, 0.85f);
        defender.getEntity().getWorld().spawnParticle(
            Particle.BLOCK,
            defender.getEntity().getLocation().clone().add(0, 0.4, 0),
            20, 0.1, 0.1, 0.1, 0.13f,
            DORT
        );
        damageMap.put(DamageType.EARTH, newDamage);
      }
      case LIGHT -> {
        mods.getElementalStatuses().add(ElementalStatus.BANISH);
        // half of your healing power, as a multiplier
        float healingBonus = Math.max(0, StatUtil.getStat(attacker, StrifeStat.HEALING_POWER) / 200);
        float lightDamage = damageMap.get(DamageType.LIGHT) * (1 + 0.2f + healingBonus);
        defender.getEntity().getWorld()
            .playSound(defender.getEntity().getEyeLocation(), Sound.BLOCK_BELL_RESONATE, 1f, 2f);
        defender.getEntity().getWorld().spawnParticle(
            Particle.FIREWORK,
            defender.getEntity().getLocation().clone().add(0, 0.4, 0),
            16,
            0.1, 0.1, 0.1,
            0.15
        );
        damageMap.put(DamageType.LIGHT, lightDamage);
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
      case FIRE -> {
        float amount = defend.getStat(StrifeStat.FIRE_RESIST);
        amount = Math.min(amount, modDoubleMap.getOrDefault(AbilityMod.CAPPED_RESIST, 300f));
        float fireResist = amount / 100;
        if (attack.hasTrait(StrifeTrait.SOUL_FLAME) && fireResist > 0) {
          fireResist /= 2;
        }
        if (defend.getEntity().getLocation().getBlock().getType() == Material.WATER) {
          fireResist += 0.3f;
        }
        fireResist = Math.min(fireResist, 0.85f);
        return fireResist >= 0 ? (1 - fireResist) : 1 + Math.abs(fireResist);
      }
      case ICE -> {
        float amount = defend.getStat(StrifeStat.ICE_RESIST);
        amount = Math.min(amount, modDoubleMap.getOrDefault(AbilityMod.CAPPED_RESIST, 300f));
        float iceResist = amount / 100;
        if (defend.getEntity().getLocation().getBlock().getType() == Material.WATER) {
          iceResist -= 0.3f;
        }
        iceResist = Math.min(iceResist, 0.85f);
        return iceResist >= 0 ? (1 - iceResist) : 1 + Math.abs(iceResist);
      }
      case LIGHTNING -> {
        float amount = defend.getStat(StrifeStat.LIGHT_RESIST);
        amount = Math.min(amount, modDoubleMap.getOrDefault(AbilityMod.CAPPED_RESIST, 300f));
        float lightningResist = amount / 100;
        if (defend.getEntity().getLocation().getBlock().getType() == Material.WATER) {
          lightningResist -= 0.3f;
        }
        lightningResist = Math.min(lightningResist, 0.85f);
        return lightningResist >= 0 ? (1 - lightningResist) : 1 + Math.abs(lightningResist);
      }
      case DARK -> {
        float amount = defend.getStat(StrifeStat.DARK_RESIST);
        amount = Math.min(amount, modDoubleMap.getOrDefault(AbilityMod.CAPPED_RESIST, 300f));
        float darkResist = amount / 100;
        darkResist = Math.min(darkResist, 0.85f);
        return darkResist >= 0 ? (1 - darkResist) : 1 + Math.abs(darkResist);
      }
      case EARTH -> {
        float amount = defend.getStat(StrifeStat.EARTH_RESIST);
        amount = Math.min(amount, modDoubleMap.getOrDefault(AbilityMod.CAPPED_RESIST, 300f));
        float earthResist = amount / 100;
        earthResist = Math.min(earthResist, 0.85f);
        return earthResist >= 0 ? (1 - earthResist) : 1 + Math.abs(earthResist);
      }
      case LIGHT -> {
        float amount = defend.getStat(StrifeStat.LIGHT_RESIST);
        amount = Math.min(amount, modDoubleMap.getOrDefault(AbilityMod.CAPPED_RESIST, 300f));
        float lightResist = amount / 100;
        lightResist = Math.min(lightResist, 0.85f);
        return lightResist >= 0 ? (1 - lightResist) : 1 + Math.abs(lightResist);
      }
    }
    return 1;
  }

  public static float getMinionMult(StrifeMob mob) {
    return 1 + mob.getStat(StrifeStat.MINION_MULT_INTERNAL) / 100;
  }

  public static float getTenacityMult(StrifeMob defender, boolean maximumEffect) {
    if (defender.getStat(StrifeStat.TENACITY) < 1) {
      return 1;
    }
    double percent = maximumEffect ? 0 : defender.getEntity().getHealth() / defender.getEntity().getMaxHealth();
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

  public static void attemptShock(StrifeMob attacker, StrifeMob defender) {
    if (defender.getEntity() instanceof Player) {
      float amountStolen = 1 + defender.getMaxEnergy() * 0.01f;
      attacker.setEnergy(attacker.getEnergy() + amountStolen);
      defender.setEnergy(defender.getEnergy() - amountStolen);
    } else {
      float amountStolen = 1 + StatUtil.getMobLevel(defender.getEntity()) * 0.03f;
      attacker.setEnergy(attacker.getEnergy() + amountStolen);
      if (defender instanceof Creeper) {
        ((Creeper) defender).setPowered(true);
      }
    }
    defender.getEntity().getWorld().playSound(
        defender.getEntity().getEyeLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.7f, 2f);
    defender.getEntity().getWorld().spawnParticle(
        Particle.ELECTRIC_SPARK, defender.getEntity().getEyeLocation(), 18, 0.5, 0.5, 0.5, 0.14);
  }

  public static void attemptFreeze(StrifeMob attacker, StrifeMob defender) {
    LivingEntity defendEntity = defender.getEntity();
    defendEntity.getWorld().playSound(defendEntity.getEyeLocation(),
        Sound.BLOCK_GLASS_BREAK, 1f, 1.3f);
    float frost = 15;
    frost *= 1 - defender.getStat(StrifeStat.ICE_RESIST) / 100;
    DamageUtil.addFrost(attacker, defender, frost);
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
    if (defender.getEntity().hasPotionEffect(PotionEffectType.WIND_CHARGED)) {
      int windLevel = defender.getEntity().getPotionEffect(PotionEffectType.WIND_CHARGED).getAmplifier() + 1;
      if (windLevel >= 10) {
        // Do not use DamageUtil.doEvasion here, to skip all notices and sounds!
        callEvadeEvent(defender, attacker);
        return true;
      }
      if (StrifePlugin.RNG.nextDouble() < 0.1f * windLevel) {
        // Do not use DamageUtil.doEvasion here, to skip all notices and sounds!
        callEvadeEvent(defender, attacker);
        if (attacker.getEntity() instanceof Player) {
          StrifePlugin.getInstance().getIndicatorManager().addIndicator(attacker,
              defender.getEntity(), IndicatorStyle.BOUNCE, 6, "彥", 3.5f, 3f, 2f, false);
        }
        return true;
      }
    }
    if (StrifePlugin.RNG.nextFloat() < defender.getStat(StrifeStat.DODGE_CHANCE) / 100) {
      DamageUtil.doEvasion(attacker, defender);
      if (defender.getEntity() instanceof Player) {
        plugin.getGuiManager().postNotice((Player) defender.getEntity(), GuiManager.NOTICE_DODGE, 5);
      }
      return true;
    }
    if (rollDodgeFromEvasion(attacker, defender, attackModifiers)) {
      DamageUtil.doEvasion(attacker, defender);
      if (defender.getEntity() instanceof Player) {
        plugin.getGuiManager().postNotice((Player) defender.getEntity(), GuiManager.NOTICE_DODGE, 5);
      }
      return true;
    }
    return false;
  }

  public static void doEvasion(StrifeMob attacker, StrifeMob defender) {
    callEvadeEvent(defender, attacker);
    defender.getEntity().getWorld().playSound(defender.getEntity().getEyeLocation(),
        Sound.ENTITY_GHAST_SHOOT, 0.5f, 2f);
    if (attacker.getEntity() instanceof Player) {
      StrifePlugin.getInstance().getIndicatorManager().addIndicator(attacker,
          defender.getEntity(), IndicatorStyle.BOUNCE, 6, "彥", 3.5f, 3f, 2f, false);
    }
  }

  public static float getPotionMult(LivingEntity attacker, LivingEntity defender) {
    float potionMult = 1.0f;

    PotionEffect powerEffect = attacker.getPotionEffect(PotionEffectType.STRENGTH);
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
    PotionEffect resistEffect = defender.getPotionEffect(PotionEffectType.RESISTANCE);
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

  public static void applyLifeSteal(StrifeMob attackMob, StrifeMob defendMob, double damage, double healMultiplier, double bonus) {
    if (attackMob.getEntity().hasPotionEffect(PotionEffectType.WITHER)) {
      return;
    }
    if (attackMob.hasTrait(StrifeTrait.POISON_EATER)) {
      var poison = defendMob.getEntity().getPotionEffect(PotionEffectType.POISON);
      if (poison != null) {
        bonus += (poison.getAmplifier() + 1) * 4;
      }
    }
    double lifeSteal = (attackMob.getStat(StrifeStat.LIFE_STEAL) + bonus) / 100;
    lifeSteal *= 1 + attackMob.getStat(StrifeStat.HEALING_POWER) / 300;
    if (attackMob.hasTrait(StrifeTrait.DEATH_EATER)) {
      lifeSteal *= 1 + (defendMob.getCorruption() / 100);
    }
    restoreHealth(attackMob.getEntity(), damage * lifeSteal * healMultiplier, true, false);
  }

  public static void applyHealthOnHit(StrifeMob attackMob, StrifeMob defendMob, float totalMult, double bonus) {
    double health = (attackMob.getStat(StrifeStat.HP_ON_HIT) + bonus) * totalMult;
    if (attackMob.hasTrait(StrifeTrait.DEATH_EATER)) {
      health *= 1 + (defendMob.getCorruption() / 100);
    }
    restoreHealth(attackMob.getEntity(), health, true, false);
  }

  public static void applyEnergyOnHit(StrifeMob attacker, float totalMult) {
    float energy = attacker.getStat(StrifeStat.ENERGY_ON_HIT) * totalMult;
    StatUtil.changeEnergy(attacker, energy);
  }

  public static boolean attemptBleed(StrifeMob attacker, StrifeMob defender, float rawPhysical,
      DamageModifiers mods, boolean bypassArmor, boolean bypassBarrier) {
    if (defender.getBarrier() > 0 || defender.getStat(StrifeStat.BLEED_RESIST) > 99) {
      return false;
    }
    float chance = (attacker.getStat(StrifeStat.BLEED_CHANCE) +
        mods.getAbilityMods().getOrDefault(AbilityMod.BLEED_CHANCE, 0f)) / 100;
    if (mods.isScaleChancesWithAttack()) {
      chance *= Math.min(1f, mods.getAttackMultiplier());
    }
    if (chance >= rollDouble()) {
      float damage = rawPhysical * DOTUtil.PHYSICAL_BLEED_PERCENT;
      float bleedDamage = attacker.getStat(StrifeStat.BLEED_DAMAGE) +
          mods.getAbilityMods().getOrDefault(AbilityMod.BLEED_DAMAGE, 0f);
      damage *= 1 + (bleedDamage / 100);
      applyBleed(attacker, defender, damage, bypassBarrier, bypassArmor, false);
      if (attacker.getEntity() instanceof Player && attacker != defender) {
        ((Player) attacker.getEntity()).playSound(defender.getEntity().getLocation(), Sound.ENTITY_SHEEP_SHEAR, 20f,
            0.8f);
      }
      if (defender.getEntity() instanceof Player) {
        ((Player) defender.getEntity()).playSound(defender.getEntity().getLocation(), Sound.ENTITY_SHEEP_SHEAR, 1f,
            0.8f);
      }
      return true;
    }
    return false;
  }

  public static boolean attemptPoison(StrifeMob attacker, StrifeMob defender, DamageModifiers mods) {
    if (defender.getStat(StrifeStat.POISON_RESIST) > 99) {
      return false;
    }
    float chance = attacker.getStat(StrifeStat.POISON_CHANCE);
    chance *= 1 + mods.getAbilityMods().getOrDefault(AbilityMod.POISON_CHANCE_MULTIPLIER, 0f) / 100;
    chance += mods.getAbilityMods().getOrDefault(AbilityMod.POISON_CHANCE, 0f);
    chance /= 100;
    if (mods.isScaleChancesWithAttack()) {
      chance *= mods.getDamageReductionRatio();
    }
    if (chance < rollDouble()) {
      return false;
    }
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
    plugin.getIndicatorManager().addIndicator(attacker, defender.getEntity(),
        IndicatorStyle.FLOAT_UP_MEDIUM, 5, POISON_TEXT, 1.0f, 1.0f, 1.0f, false);
    attacker.getEntity().getWorld().playSound(defender.getEntity().getLocation(),
        Sound.ENTITY_SILVERFISH_DEATH, 1, 2.0f);
    return true;
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
    DOTUtil.spawnInstantBleedParticles(defender.getEntity());
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
    float reflectDamage = defender.getStat(StrifeStat.DAMAGE_REFLECT);
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
    if (victim.hasTrait(StrifeTrait.REALLY_STUBBORN)) {
      victim.getEntity().setHealth(1);
      return 0.05f;
    }
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
    SneakAttackEvent sneakAttackEvent = new SneakAttackEvent(attacker, victim, sneakSkill, sneakDamage);
    Bukkit.getPluginManager().callEvent(sneakAttackEvent);
    return sneakAttackEvent;
  }

  public static boolean hasLuck(LivingEntity entity) {
    return entity.hasPotionEffect(PotionEffectType.LUCK);
  }

  public static void restoreHealth(LivingEntity livingEntity, double amount) {
    restoreHealth(livingEntity, amount, true, false);
  }

  public static void restoreHealth(LivingEntity livingEntity, double amount, boolean penalties, boolean bypassRot) {
    if (livingEntity.hasPotionEffect(PotionEffectType.WITHER) && !bypassRot) {
      return;
    }
    if (penalties) {
      if (livingEntity.getFireTicks() > 0) {
        amount *= 0.5;
      }
      if (livingEntity.hasPotionEffect(PotionEffectType.POISON)) {
        amount *= 0.25;
      }
    }
    if (amount <= 0 || !livingEntity.isValid()) {
      return;
    }
    livingEntity.setHealth(Math.min(livingEntity.getHealth() + amount,
        livingEntity.getAttribute(Attribute.MAX_HEALTH).getValue()));
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
    dealRawDamage(mob, damage, false);
  }

  public static void dealRawDamage(StrifeMob mob, float damage, boolean bypassInvulnerability) {
    if (mob.isPlayer() && ((Player) mob.getEntity()).getGameMode() == GameMode.CREATIVE) {
      return;
    }
    if (!bypassInvulnerability && mob.isInvincible()) {
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

  private static int getMissingArmorItems(Player player) {
    PlayerData data = DeluxeInvyPlugin.getInstance().getPlayerManager().getPlayerData(player);
    int amount = 0;
    if (data.getEquipmentItem(DeluxeSlot.HELMET) == null ||
        data.getEquipmentItem(DeluxeSlot.HELMET).getType() == Material.AIR) {
      amount++;
    }
    if (data.getEquipmentItem(DeluxeSlot.BODY) == null ||
        data.getEquipmentItem(DeluxeSlot.BODY).getType() == Material.AIR) {
      amount++;
    }
    if (data.getEquipmentItem(DeluxeSlot.LEGS) == null ||
        data.getEquipmentItem(DeluxeSlot.LEGS).getType() == Material.AIR) {
      amount++;
    }
    if (data.getEquipmentItem(DeluxeSlot.BOOTS) == null ||
        data.getEquipmentItem(DeluxeSlot.BOOTS).getType() == Material.AIR) {
      amount++;
    }
    return amount;
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
    TARGET_MAX_BLOCK,
    CASTER_MAX_BLOCK,
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
    GROUND,
    MODEL_BONE,
    UNION_BONE,
  }

  public enum DamageType {
    TRUE_DAMAGE,
    PHYSICAL,
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
    CRITICAL_CHANCE,
    CRITICAL_DAMAGE,
    LIFE_STEAL,
    HEALTH_ON_HIT,
    BLEED_CHANCE,
    BLEED_DAMAGE,
    POISON_CHANCE,
    POISON_CHANCE_MULTIPLIER,
    POISON_DURATION,
    STATUS_CHANCE,
    BACK_ATTACK,
    MAX_DAMAGE,
    CAPPED_RESIST
  }

  public enum AttackType {
    MELEE, PROJECTILE, AREA, OTHER, BONUS, LINGERING
  }
}
