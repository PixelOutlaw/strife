package info.faceland.strife.util;

import static info.faceland.strife.attributes.StrifeAttribute.*;

import info.faceland.strife.StrifePlugin;
import info.faceland.strife.attributes.StrifeAttribute;
import info.faceland.strife.data.AttributedEntity;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class StatUtil {

  public static double getRegen(AttributedEntity ae) {
    return ae.getAttribute(REGENERATION) * (1 + ae.getAttribute(REGEN_MULT) / 100);
  }

  public static double getHealth(AttributedEntity ae) {
    return ae.getAttribute(HEALTH) * (1 + ae.getAttribute(HEALTH_MULT) / 100);
  }

  public static double getBarrier(AttributedEntity ae) {
    return ae.getAttribute(BARRIER);
  }

  public static double getBarrierPerSecond(AttributedEntity ae) {
    return (4 + (ae.getAttribute(BARRIER) * 0.08)) * (1 + (ae.getAttribute(BARRIER_SPEED) / 100));
  }

  public static double getDamageMult(AttributedEntity ae) {
    return 1 + ae.getAttribute(DAMAGE_MULT) / 100;
  }

  public static double getMeleeDamage(AttributedEntity ae) {
    return ae.getAttribute(MELEE_DAMAGE) * (1 + ae.getAttribute(MELEE_MULT) / 100);
  }

  public static double getRangedDamage(AttributedEntity ae) {
    return ae.getAttribute(RANGED_DAMAGE) * (1 + ae.getAttribute(RANGED_MULT) / 100);
  }

  public static double getMagicDamage(AttributedEntity ae) {
    return ae.getAttribute(MAGIC_DAMAGE) * (1 + ae.getAttribute(MAGIC_MULT) / 100);
  }

  public static double getBaseMeleeDamage(AttributedEntity attacker, AttributedEntity defender) {
    double rawDamage = getMeleeDamage(attacker);
    return rawDamage * getArmorMult(attacker, defender);
  }

  public static double getBaseRangedDamage(AttributedEntity attacker, AttributedEntity defender) {
    double rawDamage = getRangedDamage(attacker);
    return rawDamage * getArmorMult(attacker, defender);
  }

  public static double getBaseMagicDamage(AttributedEntity attacker, AttributedEntity defender) {
    double rawDamage = getMagicDamage(attacker);
    return rawDamage * getWardingMult(attacker, defender);
  }

  public static double getBaseExplosionDamage(AttributedEntity attacker,
      AttributedEntity defender) {
    double rawDamage = getMagicDamage(attacker);
    return rawDamage * getArmorMult(attacker, defender);
  }

  public static double getAttackTime(AttributedEntity ae) {
    double attackTime = 2;
    double attackBonus = ae.getAttribute(StrifeAttribute.ATTACK_SPEED);
    if (itemCanUseRage(ae.getEntity().getEquipment().getItemInMainHand())) {
      attackBonus += StrifePlugin.getInstance().getRageManager().getRage(ae.getEntity());
    }
    if (attackBonus > 0) {
      attackTime /= 1 + attackBonus / 100;
    } else {
      attackTime *= 1 + Math.abs(attackBonus / 100);
    }
    return attackTime;
  }

  public static double getOverchargeMultiplier(AttributedEntity ae) {
    return 1 + (ae.getAttribute(OVERCHARGE) / 100);
  }

  public static double getCriticalMultiplier(AttributedEntity ae) {
    return 1 + (ae.getAttribute(CRITICAL_DAMAGE) / 100);
  }

  public static double getArmor(AttributedEntity ae) {
    return ae.getAttribute(ARMOR) * (1 + ae.getAttribute(ARMOR_MULT) / 100);
  }

  public static double getWarding(AttributedEntity ae) {
    return ae.getAttribute(WARDING) * (1 + ae.getAttribute(WARD_MULT) / 100);
  }

  public static double getEvasion(AttributedEntity ae) {
    return getFlatEvasion(ae) * (1 + ae.getAttribute(EVASION_MULT) / 100);
  }

  public static double getFlatEvasion(AttributedEntity ae) {
    if (ae.getChampion() == null) {
      return ae.getAttribute(EVASION);
    }
    return ae.getAttribute(EVASION) + ae.getChampion().getBonusLevels() * (1
        + ae.getAttribute(EVASION_PER_TEN_B_LEVEL) / 10);
  }

  public static double getArmorPen(AttributedEntity ae) {
    return ae.getAttribute(ARMOR_PENETRATION) * (1 + (ae.getAttribute(APEN_MULT) / 100));
  }

  public static double getWardPen(AttributedEntity ae) {
    return ae.getAttribute(WARD_PENETRATION) * (1 + (ae.getAttribute(WPEN_MULT) / 100));
  }

  public static double getAccuracy(AttributedEntity ae) {
    return ae.getAttribute(ACCURACY) * (1 + (ae.getAttribute(ACCURACY_MULT) / 100));
  }

  public static double getArmorMult(AttributedEntity attacker, AttributedEntity defender) {
    double adjustedArmor = Math.max(getArmor(defender) - getArmorPen(attacker), 1);
    return Math.min(1, 100 / (100 + adjustedArmor));
  }

  public static double getWardingMult(AttributedEntity attacker, AttributedEntity defender) {
    double adjustedWarding = Math.max(getWarding(defender) - getWardPen(attacker), 1);
    return Math.min(1, 80 / (80 + adjustedWarding));
  }

  //public static double getEvasionMultiplier(AttributedEntity attacker, AttributedEntity defender) {
  //  double adjustedEvasion = Math.max(getEvasion(defender) - getAccuracy(attacker), 1);
  //  return Math.min(1, 40 / (40 + adjustedEvasion));
  //}

  public static double getEvasion(AttributedEntity attacker, AttributedEntity defender) {
    double evasion = getEvasion(defender);
    double accuracy = getAccuracy(attacker);
    double minimumDamage = 0.8 - 0.65 * ((evasion - accuracy) / (1 + accuracy));
    return Math.min(1, minimumDamage);
  }

  //public static double getEvasionChance(AttributedEntity attacker, AttributedEntity defender) {
  //  double evasionAdvantage = Math.max(getEvasion(defender) - getAccuracy(attacker), 1);
  //  return Math.min(1, 100 / (100 + evasionAdvantage));
  //}

  public static double getFireResist(AttributedEntity ae) {
    double amount =
        ae.getAttribute(StrifeAttribute.FIRE_RESIST) + ae.getAttribute(StrifeAttribute.ALL_RESIST);
    return Math.min(amount, ae.getEntity() instanceof Player ? 80 : 99);
  }

  public static double getIceResist(AttributedEntity ae) {
    double amount =
        ae.getAttribute(StrifeAttribute.ICE_RESIST) + ae.getAttribute(StrifeAttribute.ALL_RESIST);
    return Math.min(amount, ae.getEntity() instanceof Player ? 80 : 99);
  }

  public static double getLightningResist(AttributedEntity ae) {
    double amount = ae.getAttribute(StrifeAttribute.LIGHTNING_RESIST) + ae
        .getAttribute(StrifeAttribute.ALL_RESIST);
    return Math.min(amount, ae.getEntity() instanceof Player ? 80 : 99);
  }

  public static double getEarthResist(AttributedEntity ae) {
    double amount =
        ae.getAttribute(StrifeAttribute.EARTH_RESIST) + ae.getAttribute(StrifeAttribute.ALL_RESIST);
    return Math.min(amount, ae.getEntity() instanceof Player ? 80 : 99);
  }

  public static double getLightResist(AttributedEntity ae) {
    double amount =
        ae.getAttribute(StrifeAttribute.LIGHT_RESIST) + ae.getAttribute(StrifeAttribute.ALL_RESIST);
    return Math.min(amount, ae.getEntity() instanceof Player ? 80 : 99);
  }

  public static double getShadowResist(AttributedEntity ae) {
    double amount =
        ae.getAttribute(StrifeAttribute.DARK_RESIST) + ae.getAttribute(StrifeAttribute.ALL_RESIST);
    return Math.min(amount, ae.getEntity() instanceof Player ? 80 : 99);
  }

  public static double getLifestealPercentage(AttributedEntity attacker) {
    return attacker.getAttribute(LIFE_STEAL) / 100;
  }

  public static double getFireDamage(AttributedEntity attacker) {
    return attacker.getAttribute(StrifeAttribute.FIRE_DAMAGE) * (1 + (
        attacker.getAttribute(StrifeAttribute.ELEMENTAL_MULT) / 100));
  }

  public static double getIceDamage(AttributedEntity attacker) {
    return attacker.getAttribute(StrifeAttribute.ICE_DAMAGE) * (1 + (
        attacker.getAttribute(StrifeAttribute.ELEMENTAL_MULT) / 100));
  }

  public static double getLightningDamage(AttributedEntity attacker) {
    return attacker.getAttribute(StrifeAttribute.LIGHTNING_DAMAGE) * (1 + (
        attacker.getAttribute(StrifeAttribute.ELEMENTAL_MULT) / 100));
  }

  public static double getEarthDamage(AttributedEntity attacker) {
    return attacker.getAttribute(StrifeAttribute.EARTH_DAMAGE) * (1 + (
        attacker.getAttribute(StrifeAttribute.ELEMENTAL_MULT) / 100));
  }

  public static double getLightDamage(AttributedEntity attacker) {
    return attacker.getAttribute(StrifeAttribute.LIGHT_DAMAGE) * (1 + (
        attacker.getAttribute(StrifeAttribute.ELEMENTAL_MULT) / 100));
  }

  public static double getShadowDamage(AttributedEntity attacker) {
    return attacker.getAttribute(StrifeAttribute.DARK_DAMAGE) * (1 + (
        attacker.getAttribute(StrifeAttribute.ELEMENTAL_MULT) / 100));
  }

  public static double getBaseFireDamage(AttributedEntity attacker, AttributedEntity defender) {
    double damage = attacker.getAttribute(StrifeAttribute.FIRE_DAMAGE);
    if (damage == 0) {
      return 0D;
    }
    damage *= 1 + attacker.getAttribute(StrifeAttribute.ELEMENTAL_MULT) / 100;
    damage *= 1 - getFireResist(defender) / 100;
    return damage;
  }

  public static double getBaseIceDamage(AttributedEntity attacker, AttributedEntity defender) {
    double damage = attacker.getAttribute(StrifeAttribute.ICE_DAMAGE);
    if (damage == 0) {
      return 0D;
    }
    damage *= 1 + attacker.getAttribute(StrifeAttribute.ELEMENTAL_MULT) / 100;
    damage *= 1 - getIceResist(defender) / 100;
    return damage;
  }

  public static double getBaseLightningDamage(AttributedEntity attacker,
      AttributedEntity defender) {
    double damage = attacker.getAttribute(StrifeAttribute.LIGHTNING_DAMAGE);
    if (damage == 0) {
      return 0D;
    }
    damage *= 1 + attacker.getAttribute(StrifeAttribute.ELEMENTAL_MULT) / 100;
    damage *= 1 - getLightningResist(defender) / 100;
    return damage;
  }

  public static double getBaseEarthDamage(AttributedEntity attacker, AttributedEntity defender) {
    double damage = attacker.getAttribute(StrifeAttribute.EARTH_DAMAGE);
    if (damage == 0) {
      return 0D;
    }
    damage *= 1 + attacker.getAttribute(StrifeAttribute.ELEMENTAL_MULT) / 100;
    damage *= 1 - getEarthResist(defender) / 100;
    return damage;
  }

  public static double getBaseLightDamage(AttributedEntity attacker, AttributedEntity defender) {
    double damage = attacker.getAttribute(StrifeAttribute.LIGHT_DAMAGE);
    if (damage == 0) {
      return 0D;
    }
    damage *= 1 + attacker.getAttribute(StrifeAttribute.ELEMENTAL_MULT) / 100;
    damage *= 1 - getLightResist(defender) / 100;
    return damage;
  }

  public static double getBaseShadowDamage(AttributedEntity attacker, AttributedEntity defender) {
    double damage = attacker.getAttribute(StrifeAttribute.DARK_DAMAGE);
    if (damage == 0) {
      return 0D;
    }
    damage *= 1 + attacker.getAttribute(StrifeAttribute.ELEMENTAL_MULT) / 100;
    damage *= 1 - getShadowResist(defender) / 100;
    return damage;
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
