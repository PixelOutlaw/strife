package land.face.strife.util;

import static org.bukkit.potion.PotionEffectType.FAST_DIGGING;
import static org.bukkit.potion.PotionEffectType.SLOW_DIGGING;

import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.math.NumberUtils;
import com.tealcube.minecraft.bukkit.shade.google.common.base.CharMatcher;
import java.util.HashMap;
import java.util.Map;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.champion.LifeSkillType;
import land.face.strife.data.champion.StrifeAttribute;
import land.face.strife.events.PropertyUpdateEvent;
import land.face.strife.stats.StrifeStat;
import land.face.strife.stats.StrifeTrait;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class StatUtil {

  private static final float BASE_ATTACK_SECONDS = 1.6f;
  private static final float BASE_EVASION_MULT = 0.8f;
  private static final float EVASION_ACCURACY_MULT = 0.6f;

  public static float getRegen(StrifeMob ae) {
    return ae.getStat(StrifeStat.REGENERATION) * (1 + ae.getStat(StrifeStat.REGEN_MULT) / 100);
  }

  public static float getBarrierRegen(StrifeMob ae) {
    return ae.getStat(StrifeStat.BARRIER_REGEN);
  }

  public static float getHealth(StrifeMob ae) {
    float amount = ae.getStat(StrifeStat.HEALTH) * (1 + ae.getStat(StrifeStat.HEALTH_MULT) / 100);
    PropertyUpdateEvent event = new PropertyUpdateEvent(ae, "life", amount);
    Bukkit.getPluginManager().callEvent(event);
    return event.getAppliedValue();
  }

  public static float updateMaxEnergy(StrifeMob ae) {
    float amount = ae.getStat(StrifeStat.ENERGY) * (1 + ae.getStat(StrifeStat.ENERGY_MULT) / 100);
    PropertyUpdateEvent event = new PropertyUpdateEvent(ae, "energy", amount);
    Bukkit.getPluginManager().callEvent(event);
    ae.setMaxEnergy(event.getAppliedValue());
    return event.getAppliedValue();
  }

  public static float getMaximumRage(StrifeMob ae) {
    float amount = ae.getStat(StrifeStat.MAXIMUM_RAGE);
    PropertyUpdateEvent event = new PropertyUpdateEvent(ae, "rage", amount);
    Bukkit.getPluginManager().callEvent(event);
    return event.getAppliedValue();
  }

  public static void changeEnergy(StrifeMob mob, float amount) {
    mob.setEnergy(mob.getEnergy() + amount);
  }

  public static float getEnergy(StrifeMob ae) {
    return ae.getEnergy();
  }

  public static float getMaximumBarrier(StrifeMob ae) {
    if (ae.hasTrait(StrifeTrait.NO_BARRIER_ALLOWED)) {
      return 0;
    }
    float amount = ae.getStat(StrifeStat.BARRIER) * (1 + ae.getStat(StrifeStat.BARRIER_MULT) / 100);
    ae.setMaxBarrier(amount);
    return amount;
  }

  public static float getBarrierPerSecond(StrifeMob ae) {
    float baseRestoreSpeed = 4 + (getMaximumBarrier(ae) * 0.08f);
    return baseRestoreSpeed * (1 + (ae.getStat(StrifeStat.BARRIER_SPEED) / 100));
  }

  public static float getDamageMult(StrifeMob ae) {
    return 1 + ae.getStat(StrifeStat.DAMAGE_MULT) / 100;
  }

  public static double getMeleeDamage(StrifeMob ae) {
    float multiplier = ae.getStat(StrifeStat.MELEE_PHYSICAL_MULT) + ae.getStat(StrifeStat.PHYSICAL_MULT);
    return ae.getStat(StrifeStat.PHYSICAL_DAMAGE) * (1 + multiplier / 100);
  }

  public static double getRangedDamage(StrifeMob ae) {
    float multiplier = ae.getStat(StrifeStat.RANGED_PHYSICAL_MULT) + ae.getStat(StrifeStat.PHYSICAL_MULT);
    return ae.getStat(StrifeStat.PHYSICAL_DAMAGE) * (1 + multiplier / 100);
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

    if (ItemUtil.isMeleeWeapon(ae.getEntity().getEquipment().getItemInMainHand().getType())) {
      attackBonus += StrifePlugin.getInstance().getRageManager().getRage(ae.getEntity());
    }

    if (ae.getEntity().hasPotionEffect(FAST_DIGGING)) {
      attackBonus += 10 * (1 + ae.getEntity().getPotionEffect(FAST_DIGGING).getAmplifier());
    }
    if (ae.getEntity().hasPotionEffect(SLOW_DIGGING)) {
      attackBonus -= 10 * (1 + ae.getEntity().getPotionEffect(SLOW_DIGGING).getAmplifier());
    }
    if (ae.getEntity().getFreezeTicks() > 0) {
      attackBonus *= 1 - 0.5 * ((float) ae.getEntity().getFreezeTicks() / ae.getEntity().getMaxFreezeTicks());
    }

    if (attackBonus > 0) {
      attackTime /= 1 + attackBonus / 100;
    } else {
      attackTime *= 1 + Math.abs(attackBonus / 100);
    }

    return attackTime;
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

  public static float getFireResist(StrifeMob ae, boolean soulFlame) {
    float amount = ae.getStat(StrifeStat.FIRE_RESIST) + ae.getStat(StrifeStat.ALL_RESIST);
    if (amount > 0 && soulFlame) {
      amount *= 0.5;
    }
    return Math.min(amount, ae.getEntity() instanceof Player ? 80 : 99);
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

  public static Map<LifeSkillType, Float> getSkillMapFromSection(ConfigurationSection skillSection) {
    Map<LifeSkillType, Float> skillMap = new HashMap<>();
    if (skillSection == null) {
      return skillMap;
    }
    for (String skillString : skillSection.getKeys(false)) {
      LifeSkillType strifeStat;
      try {
        strifeStat = LifeSkillType.valueOf(skillString);
      } catch (Exception e) {
        LogUtil.printWarning("Invalid skill " + skillString + ". Skipping...");
        continue;
      }
      skillMap.put(strifeStat, (float) skillSection.getDouble(skillString));
    }
    return skillMap;
  }

  public static Map<StrifeAttribute, Float> getAttributeMapFromSection(ConfigurationSection attrSection) {
    Map<StrifeAttribute, Float> attributeMap = new HashMap<>();
    if (attrSection == null) {
      return attributeMap;
    }
    for (String attrString : attrSection.getKeys(false)) {
      StrifeAttribute strifeAttr = StrifePlugin.getInstance()
          .getAttributeManager().getAttribute(attrString);
      if (strifeAttr == null) {
        LogUtil.printWarning("Invalid attribute " + attrString + ". Skipping...");
      } else {
        attributeMap.put(strifeAttr, (float) attrSection.getDouble(attrString));
      }
    }
    return attributeMap;
  }

  public static int getMobLevel(LivingEntity livingEntity) {
    if (livingEntity instanceof Player) {
      return ((Player) livingEntity).getLevel();
    }
    int level = SpecialStatusUtil.getMobLevel(livingEntity);
    if (level == -1) {
      if (StringUtils.isBlank(livingEntity.getCustomName())) {
        SpecialStatusUtil.setMobLevel(livingEntity, 1);
        return 1;
      }
      String lev = CharMatcher.digit().or(CharMatcher.is('-')).negate()
          .collapseFrom(ChatColor.stripColor(livingEntity.getCustomName()), ' ').trim();
      level = NumberUtils.toInt(lev.split(" ")[0], 1);
      SpecialStatusUtil.setMobLevel(livingEntity, level);
      return level;
    }
    return level;
  }

  private static double getElementalMult(StrifeMob pStats) {
    return 1 + (pStats.getStat(StrifeStat.ELEMENTAL_MULT) / 100);
  }
}
