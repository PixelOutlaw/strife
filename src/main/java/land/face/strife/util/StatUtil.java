package land.face.strife.util;

import static org.bukkit.potion.PotionEffectType.FAST_DIGGING;

import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.math.NumberUtils;
import com.tealcube.minecraft.bukkit.shade.google.common.base.CharMatcher;
import java.util.HashMap;
import java.util.Map;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.stats.StrifeStat;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

public class StatUtil {

  private static final float BASE_ATTACK_SECONDS = 1.6f;
  private static final float BASE_EVASION_MULT = 0.8f;
  private static final float EVASION_ACCURACY_MULT = 0.6f;

  public static double getRegen(StrifeMob ae) {
    return ae.getStat(StrifeStat.REGENERATION) * (1 + ae.getStat(StrifeStat.REGEN_MULT) / 100);
  }

  public static float getHealth(StrifeMob ae) {
    return ae.getStat(StrifeStat.HEALTH) * (1 + ae.getStat(StrifeStat.HEALTH_MULT) / 100);
  }

  public static float getBarrier(StrifeMob ae) {
    return StrifePlugin.getInstance().getBarrierManager().getCurrentBarrier(ae);
  }

  public static float getMaximumBarrier(StrifeMob ae) {
    return ae.getStat(StrifeStat.BARRIER);
  }

  public static float getBarrierPerSecond(StrifeMob ae) {
    return (4 + (ae.getStat(StrifeStat.BARRIER) * 0.08f)) * (1 + (ae.getStat(
        StrifeStat.BARRIER_SPEED) / 100));
  }

  public static double getDamageMult(StrifeMob ae) {
    return 1 + ae.getStat(StrifeStat.DAMAGE_MULT) / 100;
  }

  public static double getMeleeDamage(StrifeMob ae) {
    return ae.getStat(StrifeStat.PHYSICAL_DAMAGE) * (1 + ae.getStat(StrifeStat.MELEE_PHYSICAL_MULT) / 100);
  }

  public static double getRangedDamage(StrifeMob ae) {
    return ae.getStat(StrifeStat.PHYSICAL_DAMAGE) * (1 + ae.getStat(StrifeStat.RANGED_PHYSICAL_MULT) / 100);
  }

  public static double getMagicDamage(StrifeMob ae) {
    return ae.getStat(StrifeStat.MAGIC_DAMAGE) * (1 + ae.getStat(StrifeStat.MAGIC_MULT) / 100);
  }

  public static float getCriticalChance(StrifeMob attacker, float attackMult, float bonusCrit) {
    float totalCrit = attackMult * 1.2f * (attacker.getStat(StrifeStat.CRITICAL_RATE) + bonusCrit);
    return totalCrit / 100;
  }

  public static float getAttackTime(StrifeMob ae) {
    float attackTime = BASE_ATTACK_SECONDS;
    float attackBonus = ae.getStat(StrifeStat.ATTACK_SPEED);
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

  public static float getOverchargeMultiplier(StrifeMob ae) {
    return 1 + (ae.getStat(StrifeStat.OVERCHARGE) / 100);
  }

  public static float getCriticalMultiplier(StrifeMob ae) {
    return 1 + (ae.getStat(StrifeStat.CRITICAL_DAMAGE) / 100);
  }

  public static float getArmor(StrifeMob ae) {
    return ae.getStat(StrifeStat.ARMOR) * (1 + ae.getStat(StrifeStat.ARMOR_MULT) / 100);
  }

  public static float getWarding(StrifeMob ae) {
    return ae.getStat(StrifeStat.WARDING) * (1 + ae.getStat(StrifeStat.WARD_MULT) / 100);
  }

  public static float getEvasion(StrifeMob ae) {
    return getFlatEvasion(ae) * (1 + ae.getStat(StrifeStat.EVASION_MULT) / 100);
  }

  public static float getFlatEvasion(StrifeMob ae) {
    return ae.getStat(StrifeStat.EVASION);
  }

  public static float getArmorPen(StrifeMob ae) {
    return ae.getStat(StrifeStat.ARMOR_PENETRATION);
  }

  public static float getWardPen(StrifeMob ae) {
    return ae.getStat(StrifeStat.WARD_PENETRATION);
  }

  public static float getAccuracy(StrifeMob ae) {
    return ae.getStat(StrifeStat.ACCURACY) * (1f + (ae.getStat(StrifeStat.ACCURACY_MULT) / 100f));
  }

  public static double getArmorMult(StrifeMob attacker, StrifeMob defender) {
    float armor = getDefenderArmor(attacker, defender);
    return getArmorMult(armor);
  }

  public static float getDefenderArmor(StrifeMob attacker, StrifeMob defender) {
    return getArmor(defender) - getArmorPen(attacker);
  }

  public static float getArmorMult(float armor) {
    return armor > 0 ? 80 / (80 + armor) : 1 - (armor / 100);
  }

  public static float getWardingMult(StrifeMob attacker, StrifeMob defender) {
    float warding = getDefenderWarding(attacker, defender);
    return getWardingMult(warding);
  }

  public static float getDefenderWarding(StrifeMob attacker, StrifeMob defender) {
    return getWarding(defender) - getWardPen(attacker);
  }

  public static float getWardingMult(float warding) {
    return warding > 0 ? 80 / (80 + warding) : 1 - (warding / 100);
  }

  public static float getMinimumEvasionMult(float evasion, float accuracy) {
    evasion += 10;
    accuracy += 10;
    float bonusMultiplier = EVASION_ACCURACY_MULT * ((evasion - accuracy) / (accuracy));
    return Math.min(1.1f, BASE_EVASION_MULT - bonusMultiplier);
  }

  public static float getFireResist(StrifeMob ae) {
    double amount = ae.getStat(StrifeStat.FIRE_RESIST) + ae.getStat(StrifeStat.ALL_RESIST);
    return (float) Math.min(amount, ae.getEntity() instanceof Player ? 80 : 99);
  }

  public static float getIceResist(StrifeMob ae) {
    double amount = ae.getStat(StrifeStat.ICE_RESIST) + ae.getStat(StrifeStat.ALL_RESIST);
    return (float) Math.min(amount, ae.getEntity() instanceof Player ? 80 : 99);
  }

  public static float getLightningResist(StrifeMob ae) {
    double amount = ae.getStat(StrifeStat.LIGHTNING_RESIST) + ae.getStat(StrifeStat.ALL_RESIST);
    return (float) Math.min(amount, ae.getEntity() instanceof Player ? 80 : 99);
  }

  public static float getEarthResist(StrifeMob ae) {
    double amount = ae.getStat(StrifeStat.EARTH_RESIST) + ae.getStat(StrifeStat.ALL_RESIST);
    return (float) Math.min(amount, ae.getEntity() instanceof Player ? 80 : 99);
  }

  public static float getLightResist(StrifeMob ae) {
    double amount = ae.getStat(StrifeStat.LIGHT_RESIST) + ae.getStat(StrifeStat.ALL_RESIST);
    return (float) Math.min(amount, ae.getEntity() instanceof Player ? 80 : 99);
  }

  public static float getShadowResist(StrifeMob ae) {
    double amount = ae.getStat(StrifeStat.DARK_RESIST) + ae.getStat(StrifeStat.ALL_RESIST);
    return (float) Math.min(amount, ae.getEntity() instanceof Player ? 80 : 99);
  }

  public static float getLifestealPercentage(StrifeMob attacker) {
    return attacker.getStat(StrifeStat.LIFE_STEAL) / 100;
  }

  public static double getFireDamage(StrifeMob attacker) {
    return attacker.getStat(StrifeStat.FIRE_DAMAGE) * getElementalMult(attacker);
  }

  public static double getIceDamage(StrifeMob attacker) {
    return attacker.getStat(StrifeStat.ICE_DAMAGE) * getElementalMult(attacker);
  }

  public static double getLightningDamage(StrifeMob attacker) {
    return attacker.getStat(StrifeStat.LIGHTNING_DAMAGE) * getElementalMult(attacker);
  }

  public static double getEarthDamage(StrifeMob attacker) {
    return attacker.getStat(StrifeStat.EARTH_DAMAGE) * getElementalMult(attacker);
  }

  public static double getLightDamage(StrifeMob attacker) {
    return attacker.getStat(StrifeStat.LIGHT_DAMAGE) * getElementalMult(attacker);
  }

  public static double getShadowDamage(StrifeMob attacker) {
    return attacker.getStat(StrifeStat.DARK_DAMAGE) * getElementalMult(attacker);
  }

  public static Map<StrifeStat, Float> getStatMapFromSection(ConfigurationSection statSection) {
    Map<StrifeStat, Float> statMap = new HashMap<>();
    if (statSection == null) {
      return statMap;
    }
    for (String statString : statSection.getKeys(false)) {
      StrifeStat strifeStat;
      try {
        strifeStat = StrifeStat.valueOf(statString);
      } catch (Exception e) {
        LogUtil.printWarning("Invalid stat " + statString + ". Skipping...");
        continue;
      }
      statMap.put(strifeStat, (float) statSection.getDouble(statString));
    }
    return statMap;
  }

  public static int getMobLevel(LivingEntity livingEntity) {
    int level;
    if (livingEntity instanceof Player) {
      level = ((Player) livingEntity).getLevel();
    } else if (livingEntity.hasMetadata("LVL")) {
      level = livingEntity.getMetadata("LVL").get(0).asInt();
    } else if (StringUtils.isBlank(livingEntity.getCustomName())) {
      level = 0;
    } else {
      String lev = CharMatcher.digit().or(CharMatcher.is('-')).negate()
          .collapseFrom(ChatColor.stripColor(livingEntity.getCustomName()), ' ').trim();
      level = NumberUtils.toInt(lev.split(" ")[0], 0);
    }
    livingEntity.setMetadata("LVL", new FixedMetadataValue(StrifePlugin.getInstance(), level));
    return level;
  }

  private static double getElementalMult(StrifeMob pStats) {
    return 1 + (pStats.getStat(StrifeStat.ELEMENTAL_MULT) / 100);
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