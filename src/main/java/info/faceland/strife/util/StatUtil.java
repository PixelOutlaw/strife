package info.faceland.strife.util;

import static info.faceland.strife.stats.StrifeStat.*;
import static org.bukkit.potion.PotionEffectType.FAST_DIGGING;

import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.math.NumberUtils;
import com.tealcube.minecraft.bukkit.shade.google.common.base.CharMatcher;
import info.faceland.strife.StrifePlugin;
import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.stats.StrifeStat;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

public class StatUtil {

  private static final double BASE_ATTACK_SECONDS = 2.0D;
  private static final double BASE_EVASION_MULT = 0.8D;
  private static final double EVASION_ACCURACY_MULT = 0.6D;

  public static double getTenacityMult(StrifeMob defender) {
    if (defender.getStat(TENACITY) < 1) {
      return 1.0D;
    }
    double percent = defender.getEntity().getHealth() / defender.getEntity().getMaxHealth();
    double maxReduction = 1 - Math.pow(0.5, defender.getStat(TENACITY) / 200);
    return 1 - (maxReduction * Math.pow(1 - percent, 1.5));
  }

  public static double getMinionMult(StrifeMob mob) {
    return 1 + mob.getStat(MINION_MULT_INTERNAL) / 100;
  }

  public static double getRegen(StrifeMob ae) {
    return ae.getStat(REGENERATION) * (1 + ae.getStat(REGEN_MULT) / 100);
  }

  public static double getHealth(StrifeMob ae) {
    return ae.getStat(HEALTH) * (1 + ae.getStat(HEALTH_MULT) / 100);
  }

  public static double getMaximumBarrier(StrifeMob ae) {
    return ae.getStat(BARRIER);
  }

  public static double getBarrierPerSecond(StrifeMob ae) {
    return (4 + (ae.getStat(BARRIER) * 0.08)) * (1 + (ae.getStat(BARRIER_SPEED) / 100));
  }

  public static double getDamageMult(StrifeMob ae) {
    return 1 + ae.getStat(DAMAGE_MULT) / 100;
  }

  public static double getMeleeDamage(StrifeMob ae) {
    return ae.getStat(PHYSICAL_DAMAGE) * (1 + ae.getStat(MELEE_PHYSICAL_MULT) / 100);
  }

  public static double getRangedDamage(StrifeMob ae) {
    return ae.getStat(PHYSICAL_DAMAGE) * (1 + ae.getStat(RANGED_PHYSICAL_MULT) / 100);
  }

  public static double getMagicDamage(StrifeMob ae) {
    return ae.getStat(MAGIC_DAMAGE) * (1 + ae.getStat(MAGIC_MULT) / 100);
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
    double attackBonus = ae.getStat(StrifeStat.ATTACK_SPEED);
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
    return 1 + (ae.getStat(OVERCHARGE) / 100);
  }

  public static double getCriticalMultiplier(StrifeMob ae) {
    return 1 + (ae.getStat(CRITICAL_DAMAGE) / 100);
  }

  public static double getArmor(StrifeMob ae) {
    return ae.getStat(ARMOR) * (1 + ae.getStat(ARMOR_MULT) / 100);
  }

  public static double getWarding(StrifeMob ae) {
    return ae.getStat(WARDING) * (1 + ae.getStat(WARD_MULT) / 100);
  }

  public static double getMinimumEvasionMult(StrifeMob ae) {
    return getFlatEvasion(ae) * (1 + ae.getStat(EVASION_MULT) / 100);
  }

  public static double getFlatEvasion(StrifeMob ae) {
    return ae.getStat(EVASION);
  }

  public static double getArmorPen(StrifeMob ae) {
    return ae.getStat(ARMOR_PENETRATION) * (1 + (ae.getStat(APEN_MULT) / 100));
  }

  public static double getWardPen(StrifeMob ae) {
    return ae.getStat(WARD_PENETRATION) * (1 + (ae.getStat(WPEN_MULT) / 100));
  }

  public static double getAccuracy(StrifeMob ae) {
    return ae.getStat(ACCURACY) * (1 + (ae.getStat(ACCURACY_MULT) / 100));
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
    double amount = ae.getStat(FIRE_RESIST) + ae.getStat(ALL_RESIST);
    return Math.min(amount, ae.getEntity() instanceof Player ? 80 : 99);
  }

  public static double getIceResist(StrifeMob ae) {
    double amount = ae.getStat(ICE_RESIST) + ae.getStat(ALL_RESIST);
    return Math.min(amount, ae.getEntity() instanceof Player ? 80 : 99);
  }

  public static double getLightningResist(StrifeMob ae) {
    double amount = ae.getStat(LIGHTNING_RESIST) + ae.getStat(ALL_RESIST);
    return Math.min(amount, ae.getEntity() instanceof Player ? 80 : 99);
  }

  public static double getEarthResist(StrifeMob ae) {
    double amount = ae.getStat(EARTH_RESIST) + ae.getStat(ALL_RESIST);
    return Math.min(amount, ae.getEntity() instanceof Player ? 80 : 99);
  }

  public static double getLightResist(StrifeMob ae) {
    double amount = ae.getStat(LIGHT_RESIST) + ae.getStat(ALL_RESIST);
    return Math.min(amount, ae.getEntity() instanceof Player ? 80 : 99);
  }

  public static double getShadowResist(StrifeMob ae) {
    double amount = ae.getStat(DARK_RESIST) + ae.getStat(ALL_RESIST);
    return Math.min(amount, ae.getEntity() instanceof Player ? 80 : 99);
  }

  public static double getLifestealPercentage(StrifeMob attacker) {
    return attacker.getStat(LIFE_STEAL) / 100;
  }

  public static double getFireDamage(StrifeMob attacker) {
    return attacker.getStat(FIRE_DAMAGE) * getElementalMult(attacker);
  }

  public static double getIceDamage(StrifeMob attacker) {
    return attacker.getStat(ICE_DAMAGE) * getElementalMult(attacker);
  }

  public static double getLightningDamage(StrifeMob attacker) {
    return attacker.getStat(LIGHTNING_DAMAGE) * getElementalMult(attacker);
  }

  public static double getEarthDamage(StrifeMob attacker) {
    return attacker.getStat(EARTH_DAMAGE) * getElementalMult(attacker);
  }

  public static double getLightDamage(StrifeMob attacker) {
    return attacker.getStat(LIGHT_DAMAGE) * getElementalMult(attacker);
  }

  public static double getShadowDamage(StrifeMob attacker) {
    return attacker.getStat(DARK_DAMAGE) * getElementalMult(attacker);
  }

  public static double getBasePhysicalDamage(StrifeMob attacker, StrifeMob defender) {
    double damage = attacker.getStat(StrifeStat.PHYSICAL_DAMAGE);
    if (damage == 0) {
      return 0D;
    }
    damage *= 1 - getArmorMult(attacker, defender) / 100;
    return damage;
  }

  public static double getBaseMagicalDamage(StrifeMob attacker, StrifeMob defender) {
    double damage = attacker.getStat(MAGIC_DAMAGE);
    if (damage == 0) {
      return 0D;
    }
    damage *= 1 - getWardingMult(attacker, defender) / 100;
    return damage;
  }

  public static double getBaseFireDamage(StrifeMob attacker, StrifeMob defender) {
    double damage = attacker.getStat(StrifeStat.FIRE_DAMAGE);
    if (damage == 0) {
      return 0D;
    }
    damage *= 1 - getFireResist(defender) / 100;
    return damage;
  }

  public static double getBaseIceDamage(StrifeMob attacker, StrifeMob defender) {
    double damage = attacker.getStat(StrifeStat.ICE_DAMAGE);
    if (damage == 0) {
      return 0D;
    }
    damage *= 1 - getIceResist(defender) / 100;
    return damage;
  }

  public static double getBaseLightningDamage(StrifeMob attacker,
      StrifeMob defender) {
    double damage = attacker.getStat(StrifeStat.LIGHTNING_DAMAGE);
    if (damage == 0) {
      return 0D;
    }
    damage *= 1 - getLightningResist(defender) / 100;
    return damage;
  }

  public static double getBaseEarthDamage(StrifeMob attacker, StrifeMob defender) {
    double damage = attacker.getStat(StrifeStat.EARTH_DAMAGE);
    if (damage == 0) {
      return 0D;
    }
    damage *= 1 - getEarthResist(defender) / 100;
    return damage;
  }

  public static double getBaseLightDamage(StrifeMob attacker, StrifeMob defender) {
    double damage = attacker.getStat(StrifeStat.LIGHT_DAMAGE);
    if (damage == 0) {
      return 0D;
    }
    damage *= 1 - getLightResist(defender) / 100;
    return damage;
  }

  public static double getBaseShadowDamage(StrifeMob attacker, StrifeMob defender) {
    double damage = attacker.getStat(StrifeStat.DARK_DAMAGE);
    if (damage == 0) {
      return 0D;
    }
    damage *= 1 - getShadowResist(defender) / 100;
    return damage;
  }

  public static Map<StrifeStat, Double> getStatMapFromSection(ConfigurationSection statSection) {
    Map<StrifeStat, Double> statMap = new HashMap<>();
    for (String statString : statSection.getKeys(false)) {
      StrifeStat strifeStat;
      try {
        strifeStat = StrifeStat.valueOf(statString);
      } catch (Exception e) {
        LogUtil.printWarning("Invalid stat " + statString + ". Skipping...");
        continue;
      }
      statMap.put(strifeStat, statSection.getDouble(statString));
    }
    return statMap;
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
        String lev = CharMatcher.digit().or(CharMatcher.is('-')).negate()
            .collapseFrom(ChatColor.stripColor(livingEntity.getCustomName()), ' ').trim();
        level = NumberUtils.toInt(lev.split(" ")[0], 0);
      }
      livingEntity.setMetadata("LVL", new FixedMetadataValue(StrifePlugin.getInstance(), level));
    }
    return level;
  }

  private static double getElementalMult(StrifeMob pStats) {
    return 1 + (pStats.getStat(ELEMENTAL_MULT) / 100);
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
