package info.faceland.strife.util;

import static info.faceland.strife.stats.StrifeStat.ACCURACY;
import static info.faceland.strife.stats.StrifeStat.ACCURACY_MULT;
import static info.faceland.strife.stats.StrifeStat.ALL_RESIST;
import static info.faceland.strife.stats.StrifeStat.APEN_MULT;
import static info.faceland.strife.stats.StrifeStat.ARMOR;
import static info.faceland.strife.stats.StrifeStat.ARMOR_MULT;
import static info.faceland.strife.stats.StrifeStat.ARMOR_PENETRATION;
import static info.faceland.strife.stats.StrifeStat.BARRIER;
import static info.faceland.strife.stats.StrifeStat.BARRIER_SPEED;
import static info.faceland.strife.stats.StrifeStat.CRITICAL_DAMAGE;
import static info.faceland.strife.stats.StrifeStat.DAMAGE_MULT;
import static info.faceland.strife.stats.StrifeStat.DARK_DAMAGE;
import static info.faceland.strife.stats.StrifeStat.DARK_RESIST;
import static info.faceland.strife.stats.StrifeStat.EARTH_DAMAGE;
import static info.faceland.strife.stats.StrifeStat.EARTH_RESIST;
import static info.faceland.strife.stats.StrifeStat.ELEMENTAL_MULT;
import static info.faceland.strife.stats.StrifeStat.EVASION;
import static info.faceland.strife.stats.StrifeStat.EVASION_MULT;
import static info.faceland.strife.stats.StrifeStat.FIRE_DAMAGE;
import static info.faceland.strife.stats.StrifeStat.FIRE_RESIST;
import static info.faceland.strife.stats.StrifeStat.HEALTH;
import static info.faceland.strife.stats.StrifeStat.HEALTH_MULT;
import static info.faceland.strife.stats.StrifeStat.ICE_DAMAGE;
import static info.faceland.strife.stats.StrifeStat.ICE_RESIST;
import static info.faceland.strife.stats.StrifeStat.LIFE_STEAL;
import static info.faceland.strife.stats.StrifeStat.LIGHTNING_DAMAGE;
import static info.faceland.strife.stats.StrifeStat.LIGHTNING_RESIST;
import static info.faceland.strife.stats.StrifeStat.LIGHT_DAMAGE;
import static info.faceland.strife.stats.StrifeStat.LIGHT_RESIST;
import static info.faceland.strife.stats.StrifeStat.MAGIC_DAMAGE;
import static info.faceland.strife.stats.StrifeStat.MAGIC_MULT;
import static info.faceland.strife.stats.StrifeStat.MELEE_DAMAGE;
import static info.faceland.strife.stats.StrifeStat.MELEE_MULT;
import static info.faceland.strife.stats.StrifeStat.OVERCHARGE;
import static info.faceland.strife.stats.StrifeStat.RANGED_DAMAGE;
import static info.faceland.strife.stats.StrifeStat.RANGED_MULT;
import static info.faceland.strife.stats.StrifeStat.REGENERATION;
import static info.faceland.strife.stats.StrifeStat.REGEN_MULT;
import static info.faceland.strife.stats.StrifeStat.WARDING;
import static info.faceland.strife.stats.StrifeStat.WARD_MULT;
import static info.faceland.strife.stats.StrifeStat.WARD_PENETRATION;
import static info.faceland.strife.stats.StrifeStat.WPEN_MULT;
import static org.bukkit.potion.PotionEffectType.FAST_DIGGING;

import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.math.NumberUtils;
import com.tealcube.minecraft.bukkit.shade.google.common.base.CharMatcher;
import info.faceland.strife.StrifePlugin;
import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.stats.StrifeStat;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

public class StatUtil {

  private static final double BASE_ATTACK_SECONDS = 2.0D;
  private static final double BASE_EVASION_MULT = 0.8D;
  private static final double EVASION_ACCURACY_MULT = 0.6D;

  public static double getRegen(StrifeMob ae) {
    return ae.getAttribute(REGENERATION) * (1 + ae.getAttribute(REGEN_MULT) / 100);
  }

  public static double getHealth(StrifeMob ae) {
    return ae.getAttribute(HEALTH) * (1 + ae.getAttribute(HEALTH_MULT) / 100);
  }

  public static double getMaximumBarrier(StrifeMob ae) {
    return ae.getAttribute(BARRIER);
  }

  public static double getBarrierPerSecond(StrifeMob ae) {
    return (4 + (ae.getAttribute(BARRIER) * 0.08)) * (1 + (ae.getAttribute(BARRIER_SPEED) / 100));
  }

  public static double getDamageMult(StrifeMob ae) {
    return 1 + ae.getAttribute(DAMAGE_MULT) / 100;
  }

  public static double getMeleeDamage(StrifeMob ae) {
    return ae.getAttribute(MELEE_DAMAGE) * (1 + ae.getAttribute(MELEE_MULT) / 100);
  }

  public static double getRangedDamage(StrifeMob ae) {
    return ae.getAttribute(RANGED_DAMAGE) * (1 + ae.getAttribute(RANGED_MULT) / 100);
  }

  public static double getMagicDamage(StrifeMob ae) {
    return ae.getAttribute(MAGIC_DAMAGE) * (1 + ae.getAttribute(MAGIC_MULT) / 100);
  }

  public static double getBaseMeleeDamage(StrifeMob attacker, StrifeMob defender) {
    double rawDamage = getMeleeDamage(attacker);
    return rawDamage * getArmorMult(attacker, defender);
  }

  public static double getBaseRangedDamage(StrifeMob attacker, StrifeMob defender) {
    double rawDamage = getRangedDamage(attacker);
    return rawDamage * getArmorMult(attacker, defender);
  }

  public static double getBaseMagicDamage(StrifeMob attacker, StrifeMob defender) {
    double rawDamage = getMagicDamage(attacker);
    return rawDamage * getWardingMult(attacker, defender);
  }

  public static double getBaseExplosionDamage(StrifeMob attacker,
      StrifeMob defender) {
    double rawDamage = getMagicDamage(attacker);
    return rawDamage * getArmorMult(attacker, defender);
  }

  public static double getAttackTime(StrifeMob ae) {
    double attackTime = BASE_ATTACK_SECONDS;
    double attackBonus = ae.getAttribute(StrifeStat.ATTACK_SPEED);
    if (itemCanUseRage(ae.getEntity().getEquipment().getItemInMainHand())) {
      attackBonus += StrifePlugin.getInstance().getRageManager().getRage(ae.getEntity());
    }
    if (ae.getEntity().hasPotionEffect(FAST_DIGGING)) {
      attackBonus += 15 * (1 + ae.getEntity().getPotionEffect(FAST_DIGGING).getAmplifier());
    }
    if (attackBonus > 0) {
      attackTime /= 1 + attackBonus / 100;
    } else {
      attackTime *= 1 + Math.abs(attackBonus / 100);
    }
    return attackTime;
  }

  public static double getOverchargeMultiplier(StrifeMob ae) {
    return 1 + (ae.getAttribute(OVERCHARGE) / 100);
  }

  public static double getCriticalMultiplier(StrifeMob ae) {
    return 1 + (ae.getAttribute(CRITICAL_DAMAGE) / 100);
  }

  public static double getArmor(StrifeMob ae) {
    return ae.getAttribute(ARMOR) * (1 + ae.getAttribute(ARMOR_MULT) / 100);
  }

  public static double getWarding(StrifeMob ae) {
    return ae.getAttribute(WARDING) * (1 + ae.getAttribute(WARD_MULT) / 100);
  }

  public static double getMinimumEvasionMult(StrifeMob ae) {
    return getFlatEvasion(ae) * (1 + ae.getAttribute(EVASION_MULT) / 100);
  }

  public static double getFlatEvasion(StrifeMob ae) {
    return ae.getAttribute(EVASION);
  }

  public static double getArmorPen(StrifeMob ae) {
    return ae.getAttribute(ARMOR_PENETRATION) * (1 + (ae.getAttribute(APEN_MULT) / 100));
  }

  public static double getWardPen(StrifeMob ae) {
    return ae.getAttribute(WARD_PENETRATION) * (1 + (ae.getAttribute(WPEN_MULT) / 100));
  }

  public static double getAccuracy(StrifeMob ae) {
    return ae.getAttribute(ACCURACY) * (1 + (ae.getAttribute(ACCURACY_MULT) / 100));
  }

  public static double getArmorMult(StrifeMob attacker, StrifeMob defender) {
    double adjustedArmor = Math.max(getArmor(defender) - getArmorPen(attacker), 1);
    return Math.min(1, 80 / (80 + adjustedArmor));
  }

  public static double getWardingMult(StrifeMob attacker, StrifeMob defender) {
    double adjustedWarding = Math.max(getWarding(defender) - getWardPen(attacker), 1);
    return Math.min(1, 80 / (80 + adjustedWarding));
  }

  public static double getMinimumEvasionMult(StrifeMob attacker, StrifeMob defender) {
    double evasion = getMinimumEvasionMult(defender);
    double accuracy = getAccuracy(attacker);
    double bonusMultiplier = EVASION_ACCURACY_MULT * ((evasion - accuracy) / (1 + accuracy));
    return Math.min(1, BASE_EVASION_MULT - bonusMultiplier);
  }

  public static double getFireResist(StrifeMob ae) {
    double amount = ae.getAttribute(FIRE_RESIST) + ae.getAttribute(ALL_RESIST);
    return Math.min(amount, ae.getEntity() instanceof Player ? 80 : 99);
  }

  public static double getIceResist(StrifeMob ae) {
    double amount = ae.getAttribute(ICE_RESIST) + ae.getAttribute(ALL_RESIST);
    return Math.min(amount, ae.getEntity() instanceof Player ? 80 : 99);
  }

  public static double getLightningResist(StrifeMob ae) {
    double amount = ae.getAttribute(LIGHTNING_RESIST) + ae.getAttribute(ALL_RESIST);
    return Math.min(amount, ae.getEntity() instanceof Player ? 80 : 99);
  }

  public static double getEarthResist(StrifeMob ae) {
    double amount = ae.getAttribute(EARTH_RESIST) + ae.getAttribute(ALL_RESIST);
    return Math.min(amount, ae.getEntity() instanceof Player ? 80 : 99);
  }

  public static double getLightResist(StrifeMob ae) {
    double amount = ae.getAttribute(LIGHT_RESIST) + ae.getAttribute(ALL_RESIST);
    return Math.min(amount, ae.getEntity() instanceof Player ? 80 : 99);
  }

  public static double getShadowResist(StrifeMob ae) {
    double amount = ae.getAttribute(DARK_RESIST) + ae.getAttribute(ALL_RESIST);
    return Math.min(amount, ae.getEntity() instanceof Player ? 80 : 99);
  }

  public static double getLifestealPercentage(StrifeMob attacker) {
    return attacker.getAttribute(LIFE_STEAL) / 100;
  }

  public static double getFireDamage(StrifeMob attacker) {
    return attacker.getAttribute(FIRE_DAMAGE) * getElementalMult(attacker);
  }

  public static double getIceDamage(StrifeMob attacker) {
    return attacker.getAttribute(ICE_DAMAGE) * getElementalMult(attacker);
  }

  public static double getLightningDamage(StrifeMob attacker) {
    return attacker.getAttribute(LIGHTNING_DAMAGE) * getElementalMult(attacker);
  }

  public static double getEarthDamage(StrifeMob attacker) {
    return attacker.getAttribute(EARTH_DAMAGE) * getElementalMult(attacker);
  }

  public static double getLightDamage(StrifeMob attacker) {
    return attacker.getAttribute(LIGHT_DAMAGE) * getElementalMult(attacker);
  }

  public static double getShadowDamage(StrifeMob attacker) {
    return attacker.getAttribute(DARK_DAMAGE) * getElementalMult(attacker);
  }

  public static double getBaseFireDamage(StrifeMob attacker, StrifeMob defender) {
    double damage = attacker.getAttribute(StrifeStat.FIRE_DAMAGE);
    if (damage == 0) {
      return 0D;
    }
    damage *= 1 + attacker.getAttribute(StrifeStat.ELEMENTAL_MULT) / 100;
    damage *= 1 - getFireResist(defender) / 100;
    return damage;
  }

  public static double getBaseIceDamage(StrifeMob attacker, StrifeMob defender) {
    double damage = attacker.getAttribute(StrifeStat.ICE_DAMAGE);
    if (damage == 0) {
      return 0D;
    }
    damage *= 1 + attacker.getAttribute(StrifeStat.ELEMENTAL_MULT) / 100;
    damage *= 1 - getIceResist(defender) / 100;
    return damage;
  }

  public static double getBaseLightningDamage(StrifeMob attacker,
      StrifeMob defender) {
    double damage = attacker.getAttribute(StrifeStat.LIGHTNING_DAMAGE);
    if (damage == 0) {
      return 0D;
    }
    damage *= 1 + attacker.getAttribute(StrifeStat.ELEMENTAL_MULT) / 100;
    damage *= 1 - getLightningResist(defender) / 100;
    return damage;
  }

  public static double getBaseEarthDamage(StrifeMob attacker, StrifeMob defender) {
    double damage = attacker.getAttribute(StrifeStat.EARTH_DAMAGE);
    if (damage == 0) {
      return 0D;
    }
    damage *= 1 + attacker.getAttribute(StrifeStat.ELEMENTAL_MULT) / 100;
    damage *= 1 - getEarthResist(defender) / 100;
    return damage;
  }

  public static double getBaseLightDamage(StrifeMob attacker, StrifeMob defender) {
    double damage = attacker.getAttribute(StrifeStat.LIGHT_DAMAGE);
    if (damage == 0) {
      return 0D;
    }
    damage *= 1 + attacker.getAttribute(StrifeStat.ELEMENTAL_MULT) / 100;
    damage *= 1 - getLightResist(defender) / 100;
    return damage;
  }

  public static double getBaseShadowDamage(StrifeMob attacker, StrifeMob defender) {
    double damage = attacker.getAttribute(StrifeStat.DARK_DAMAGE);
    if (damage == 0) {
      return 0D;
    }
    damage *= 1 + attacker.getAttribute(StrifeStat.ELEMENTAL_MULT) / 100;
    damage *= 1 - getShadowResist(defender) / 100;
    return damage;
  }

  public static int getMobLevel(LivingEntity livingEntity) {
    int level;
    if (livingEntity instanceof Player) {
      return ((Player) livingEntity).getLevel();
    }
    if (livingEntity.hasMetadata("LVL")) {
      level = livingEntity.getMetadata("LVL").get(0).asInt();
    } else {
      if (StringUtils.isBlank(livingEntity.getCustomName())) {
        level = -1;
      } else {
        String lev = CharMatcher.DIGIT.or(CharMatcher.is('-')).negate()
            .collapseFrom(ChatColor.stripColor(livingEntity.getCustomName()), ' ').trim();
        level = NumberUtils.toInt(lev.split(" ")[0], 0);
      }
      livingEntity.setMetadata("LVL", new FixedMetadataValue(StrifePlugin.getInstance(), level));
    }
    return level;
  }

  private static double getElementalMult(StrifeMob pStats) {
    return 1 + (pStats.getAttribute(ELEMENTAL_MULT) / 100);
  }

  private static boolean itemCanUseRage(ItemStack item) {
    if (item.getType() == Material.BOW) {
      return false;
    }
    if (!ItemUtil.isMeleeWeapon(item.getType())) {
      return false;
    }
    if (ItemUtil.isWand(item)) {
      return false;
    }
    return true;
  }
}
