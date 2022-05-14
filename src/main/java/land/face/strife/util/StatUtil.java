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

  public static float getStat(StrifeMob mob, StrifeStat stat) {
    Map<StrifeStat, Float> stats = mob.getStatCache();
    switch (stat) {
      case ARMOR -> {
        return stats.getOrDefault(StrifeStat.ARMOR, 0f) *
            (1 + stats.getOrDefault(StrifeStat.ARMOR_MULT, 0f) / 100);
      }
      case WARDING -> {
        return stats.getOrDefault(StrifeStat.WARDING, 0f) *
            (1 + stats.getOrDefault(StrifeStat.WARD_MULT, 0f) / 100);
      }
      case REGENERATION -> {
        return stats.getOrDefault(StrifeStat.REGENERATION, 0f) *
            (1 + stats.getOrDefault(StrifeStat.REGEN_MULT, 0f) / 100);
      }
      case HEALTH -> {
        float amount = stats.getOrDefault(StrifeStat.HEALTH, 1f) *
            (1 + stats.getOrDefault(StrifeStat.HEALTH_MULT, 0f) / 100);
        PropertyUpdateEvent event = new PropertyUpdateEvent(mob, "life", amount);
        Bukkit.getPluginManager().callEvent(event);
        mob.setMaxLife(event.getAppliedValue());
        return event.getAppliedValue();
      }
      case ENERGY -> {
        float amount = stats.getOrDefault(StrifeStat.ENERGY, 0f) *
            (1 + stats.getOrDefault(StrifeStat.ENERGY_MULT, 0f) / 100);
        PropertyUpdateEvent event = new PropertyUpdateEvent(mob, "energy", amount);
        Bukkit.getPluginManager().callEvent(event);
        mob.setMaxEnergy(event.getAppliedValue());
        return event.getAppliedValue();
      }
      case MAXIMUM_RAGE -> {
        float amount = stats.getOrDefault(StrifeStat.MAXIMUM_RAGE, 0f);
        PropertyUpdateEvent event = new PropertyUpdateEvent(mob, "rage", amount);
        Bukkit.getPluginManager().callEvent(event);
        mob.setMaxRage(event.getAppliedValue());
        return event.getAppliedValue();
      }
      case MAX_EARTH_RUNES -> {
        float amount = stats.getOrDefault(StrifeStat.MAX_EARTH_RUNES, 0f);
        mob.setMaxEarthRunes(Math.round(amount));
        return amount;
      }
      case BARRIER -> {
        if (mob.hasTrait(StrifeTrait.NO_BARRIER_ALLOWED)) {
          return 0;
        }
        float amount = stats.getOrDefault(StrifeStat.BARRIER, 0f) *
            (1 + stats.getOrDefault(StrifeStat.BARRIER_MULT, 0f) / 100);
        mob.setMaxBarrier(amount);
        return amount;
      }
      case BLOCK -> {
        float amount = stats.getOrDefault(StrifeStat.BLOCK, 0f);
        mob.setMaxBlock(amount);
        if (!mob.isInCombat()) {
          mob.setBlock(amount);
        }
        return amount;
      }
      case FIRE_RESIST, ICE_RESIST, LIGHTNING_RESIST, LIGHT_RESIST, DARK_RESIST, EARTH_RESIST -> {
        float amount = stats.getOrDefault(stat, 0f) + stats.getOrDefault(StrifeStat.ALL_RESIST, 0f);
        return Math.min(amount, 95);
      }
      case BARRIER_REGEN -> {
        if (mob.hasTrait(StrifeTrait.OVERSHIELD) && mob.getEntity().getHealth() > mob.getMaxLife() - 0.01) {
          float amount = stats.getOrDefault(StrifeStat.BARRIER_REGEN, 0f);
          amount += getStat(mob, StrifeStat.REGENERATION) * 0.3;
          return amount;
        } else
          return stats.getOrDefault(StrifeStat.BARRIER_REGEN, 0f);
      }
    }
    return stats.getOrDefault(stat, 0f);
  }

  public static void changeEnergy(StrifeMob mob, float amount) {
    mob.setEnergy(mob.getEnergy() + amount);
  }

  public static float getEnergy(StrifeMob ae) {
    return ae.getEnergy();
  }

  public static float getBarrierPerSecond(StrifeMob ae) {
    float baseRestoreSpeed = DamageUtil.FLAT_BARRIER_PER_SECOND +
        (ae.getMaxBarrier() * DamageUtil.PERCENT_BARRIER_PER_SECOND);
    return baseRestoreSpeed * (1 + (ae.getStat(StrifeStat.BARRIER_SPEED) / 100));
  }

  public static float getDamageMult(StrifeMob ae) {
    return 1 + ae.getStat(StrifeStat.DAMAGE_MULT) / 100;
  }

  public static float getCriticalChance(StrifeMob attacker, float attackMult, float bonusCrit) {
    float totalCrit = attackMult * (attacker.getStat(StrifeStat.CRITICAL_RATE) + bonusCrit);
    return totalCrit / 100;
  }

  public static float getAttackTime(StrifeMob ae) {

    float attackTime = DamageUtil.BASE_ATTACK_SECONDS * (1f + ae.getFrost() / 10000f);
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

    if (attackBonus > 0) {
      attackTime /= 1 + attackBonus / 100;
    } else {
      attackTime *= 1 + Math.abs(attackBonus / 100);
    }

    return attackTime;
  }

  public static Map<StrifeStat, Float> buildStatsFromAttributes(Map<StrifeAttribute, Integer> m) {
    Map<StrifeStat, Float> attributeMap = new HashMap<>();
    for (StrifeAttribute stat : m.keySet()) {
      int statLevel = m.get(stat);
      if (statLevel == 0) {
        continue;
      }
      for (StrifeStat attr : stat.getAttributeMap().keySet()) {
        float amount = stat.getAttributeMap().get(attr) * statLevel;
        if (attributeMap.containsKey(attr)) {
          amount += attributeMap.get(attr);
        }
        attributeMap.put(attr, amount);
      }
    }
    return attributeMap;
  }

  public static float getCriticalMultiplier(StrifeMob ae) {
    return 1 + (ae.getStat(StrifeStat.CRITICAL_DAMAGE) / 100);
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
    return getStat(defender, StrifeStat.ARMOR) - getArmorPen(attacker);
  }

  public static float getArmorMult(float armor) {
    return (float) Math.pow(0.5f, armor / 100f);
  }

  public static float getWardingMult(StrifeMob attacker, StrifeMob defender) {
    float warding = getDefenderWarding(attacker, defender);
    return getWardingMult(warding);
  }

  public static float getDefenderWarding(StrifeMob attacker, StrifeMob defender) {
    return getStat(defender, StrifeStat.WARDING) - getWardPen(attacker);
  }

  public static float getWardingMult(float warding) {
    return (float) Math.pow(0.5f, warding / 100f);
  }

  public static float getMinimumEvasionMult(float evasion, float accuracy) {
    evasion += 10;
    accuracy += 10;
    float bonusMultiplier = DamageUtil.EVASION_ACCURACY_MULT * ((evasion - accuracy) / (accuracy));
    return Math.min(1.1f, DamageUtil.BASE_EVASION_MULT - bonusMultiplier);
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

  public static Map<LifeSkillType, Float> getSkillMapFromSection(
      ConfigurationSection skillSection) {
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

  public static Map<StrifeAttribute, Float> getAttributeMapFromSection(
      ConfigurationSection attrSection) {
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
