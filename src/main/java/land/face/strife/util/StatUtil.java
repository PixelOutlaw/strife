package land.face.strife.util;

import com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor;
import com.tealcube.minecraft.bukkit.facecore.utilities.UnicodeUtil;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.math.NumberUtils;
import com.tealcube.minecraft.bukkit.shade.google.common.base.CharMatcher;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import land.face.strife.StrifePlugin;
import land.face.strife.data.LoreAbility;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.champion.LifeSkillType;
import land.face.strife.data.champion.StrifeAttribute;
import land.face.strife.managers.PrayerManager.Prayer;
import land.face.strife.stats.StrifeStat;
import land.face.strife.stats.StrifeTrait;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class StatUtil {

  public static StrifePlugin plugin;
  public static String COMBAT_ENTER_TOAST;
  public static String COMBAT_EXIT_TOAST;

  public static float ARMOR_DENOMINATOR;
  public static float WARD_DENOMINATOR;

  public static void refreshPlugin(StrifePlugin strifePlugin) {
    plugin = strifePlugin;
    COMBAT_ENTER_TOAST = FaceColor.NO_SHADOW + UnicodeUtil.unicodePlacehold("<toast_enter_combat>");
    COMBAT_EXIT_TOAST = FaceColor.NO_SHADOW + UnicodeUtil.unicodePlacehold("<toast_exit_combat>");

    ARMOR_DENOMINATOR = (float) plugin.getSettings().getDouble("config.mechanics.defense.armor-denominator", 110);
    WARD_DENOMINATOR = (float) plugin.getSettings().getDouble("config.mechanics.defense.ward-denominator", 110);
  }

  public static float getStat(StrifeMob mob, StrifeStat stat) {
    Map<StrifeStat, Float> stats = mob.getStatCache();
    switch (stat) {
      case ARMOR -> {
        return stats.getOrDefault(StrifeStat.ARMOR, 0f) *
            (1 + stats.getOrDefault(StrifeStat.ARMOR_MULT, 0f) / 100);
      }
      case REGENERATION -> {
        float amount = stats.getOrDefault(StrifeStat.REGENERATION, 0f) *
            (1 + stats.getOrDefault(StrifeStat.REGEN_MULT, 0f) / 100);
        if (!mob.isInCombat() && plugin.getPrayerManager().isPrayerActive(mob.getEntity(), Prayer.THREE)) {
          amount += 10;
        }
        if (plugin.getPrayerManager().isPrayerActive(mob.getEntity(), Prayer.SIX)) {
          amount += 50;
          amount += (float) (mob.getMaxLife() - mob.getEntity().getHealth());
        }
        return amount;
      }
      case ENERGY_REGEN -> {
        float amount = stats.getOrDefault(StrifeStat.ENERGY_REGEN, 0f);
        if (!mob.isInCombat() && plugin.getPrayerManager().isPrayerActive(mob.getEntity(), Prayer.THREE)) {
          amount += 10;
        }
        return amount;
      }
      case HEALTH -> {
        float amount = stats.getOrDefault(StrifeStat.HEALTH, 1f) *
            (1 + stats.getOrDefault(StrifeStat.HEALTH_MULT, 0f) / 100);
        mob.setMaxLife(amount);
        return amount;
      }
      case ENERGY -> {
        float amount = stats.getOrDefault(StrifeStat.ENERGY, 0f) *
            (1 + stats.getOrDefault(StrifeStat.ENERGY_MULT, 0f) / 100);
        mob.setMaxEnergy(amount);
        return amount;
      }
      case MAXIMUM_RAGE -> {
        float amount = stats.getOrDefault(StrifeStat.MAXIMUM_RAGE, 0f);
        mob.setMaxRage(amount);
        return amount;
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
      case MAX_PRAYER_POINTS -> {
        if (mob.getChampion() != null) {
          float maxPrayer = stats.getOrDefault(StrifeStat.MAX_PRAYER_POINTS, 10F) +
              mob.getChampion().getLifeSkillLevel(LifeSkillType.PRAYER) * 5.102f;
          mob.setMaxPrayer(maxPrayer);
          mob.setPrayer(Math.min(mob.getChampion().getSaveData().getPrayerPoints(), maxPrayer));
          return maxPrayer;
        } else {
          mob.setMaxPrayer(1);
          return 1;
        }
      }
      case ATTACK_SPEED -> {
        return stats.getOrDefault(StrifeStat.ATTACK_SPEED, 0F) +
            (plugin.getPrayerManager().isPrayerActive(mob.getEntity(), Prayer.NINE) ? 10 : 0);
      }
      case COOLDOWN_REDUCTION -> {
        return Math.min(70, stats.getOrDefault(StrifeStat.COOLDOWN_REDUCTION, 0F) +
            (plugin.getPrayerManager().isPrayerActive(mob.getEntity(), Prayer.NINE) ? 10 : 0));
      }
      case DODGE_CHANCE -> {
        return stats.getOrDefault(StrifeStat.DODGE_CHANCE, 0F) +
            (plugin.getPrayerManager().isPrayerActive(mob.getEntity(), Prayer.SEVEN) ? 2 : 0);
      }
      case CRITICAL_RATE -> {
        return stats.getOrDefault(StrifeStat.CRITICAL_RATE, 0F) +
            (plugin.getPrayerManager().isPrayerActive(mob.getEntity(), Prayer.SEVEN) ? 2 : 0);
      }
      case HP_ON_KILL -> {
        return stats.getOrDefault(StrifeStat.HP_ON_KILL, 0F) +
            (plugin.getPrayerManager().isPrayerActive(mob.getEntity(), Prayer.FIVE) ? 4 : 0);
      }
      case ENERGY_ON_KILL -> {
        return stats.getOrDefault(StrifeStat.ENERGY_ON_KILL, 0F) +
            (plugin.getPrayerManager().isPrayerActive(mob.getEntity(), Prayer.FIVE) ? 2 : 0);
      }
      case ENCHANT_SKILL -> {
        return stats.getOrDefault(StrifeStat.ENCHANT_SKILL, 0F) +
            (plugin.getPrayerManager().isPrayerActive(mob.getEntity(), Prayer.ELEVEN) ? 4 : 0);
      }
      case CRAFT_SKILL -> {
        return stats.getOrDefault(StrifeStat.CRAFT_SKILL, 0F) +
            (plugin.getPrayerManager().isPrayerActive(mob.getEntity(), Prayer.ELEVEN) ? 4 : 0);
      }
      case FIRE_RESIST, ICE_RESIST, LIGHTNING_RESIST, LIGHT_RESIST, DARK_RESIST, EARTH_RESIST -> {
        float amount = stats.getOrDefault(stat, 0f) + stats.getOrDefault(StrifeStat.ALL_RESIST, 0f);
        return Math.min(amount, 95);
      }
      case BARRIER_REGEN -> {
        if (mob.hasTrait(StrifeTrait.OVERSHIELD) && mob.getEntity().getHealth() > mob.getMaxLife() - 0.01) {
          float amount = stats.getOrDefault(StrifeStat.BARRIER_REGEN, 0f);
          amount += getStat(mob, StrifeStat.REGENERATION) * 0.3f;
          return amount;
        } else
          return stats.getOrDefault(StrifeStat.BARRIER_REGEN, 0f);
      }
      case ACCURACY -> {
        float amount = stats.getOrDefault(StrifeStat.ACCURACY, 0f) *
            (1 + stats.getOrDefault(StrifeStat.ACCURACY_MULT, 0f) / 100);
        if (plugin.getPrayerManager().isPrayerActive(mob.getEntity(), Prayer.FOUR)) {
          amount *= 1.06f;
        }
        return amount;
      }
      case AIR_JUMPS -> {
        if (mob.getChampion() == null) {
          return 0;
        }
        float amount = stats.getOrDefault(StrifeStat.AIR_JUMPS, 0f);
        if (mob.getChampion().getLifeSkillLevel(LifeSkillType.AGILITY) >= 40) {
          amount++;
        }
        if (mob.getChampion().getLifeSkillLevel(LifeSkillType.AGILITY) >= 60) {
          amount++;
        }
        return amount;
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

  public static float getCriticalChance(StrifeMob attacker, float attackMult, float bonusCrit) {
    float totalCrit = attackMult * (attacker.getStat(StrifeStat.CRITICAL_RATE) + bonusCrit);
    return totalCrit / 100;
  }

  public static float getAttackTime(StrifeMob ae) {

    float attackTime = DamageUtil.BASE_ATTACK_SECONDS;

    attackTime *= (1f + ae.getFrost() / 10000f);

    float attackBonus = ae.getStat(StrifeStat.ATTACK_SPEED);

    if (ae.getEntity().getEquipment().getItemInMainHand().getType() != Material.WOODEN_SWORD &&
        ItemUtil.isMeleeWeapon(ae.getEntity().getEquipment().getItemInMainHand().getType())) {
      attackBonus += ae.getRage() * 0.5f;
    } else {
      attackBonus += ae.getRage() * 0.2f;
    }

    if (attackBonus > 0) {
      attackTime /= 1 + attackBonus / 100;
    } else {
      attackTime *= 1 + Math.abs(attackBonus / 100);
    }

    ae.getEntity().removePotionEffect(PotionEffectType.FAST_DIGGING);
    ae.getEntity().removePotionEffect(PotionEffectType.SLOW_DIGGING);
    if (attackTime > 1.6) {
      ae.getEntity().addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 10, 1, true, false));
    } else if (attackTime > 1.405) {
      ae.getEntity().addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 9, 0, true, false));
    } else if (attackTime > 1.15) {
      // do nothing
    } else if (attackTime > 0.85) {
      ae.getEntity().addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 8, 0, true, false));
    } else if (attackTime > 0.55) {
      ae.getEntity().addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 7, 1, true, false));
    } else {
      ae.getEntity().addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 6, 2, true, false));
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

  public static double getArmorMult(StrifeMob attacker, StrifeMob defender) {
    float armor = getDefenderArmor(attacker, defender);
    return getArmorMult(armor);
  }

  public static float getDefenderArmor(StrifeMob attacker, StrifeMob defender) {
    return getStat(defender, StrifeStat.ARMOR) - getArmorPen(attacker);
  }

  public static float getArmorMult(float armor) {
    return (float) Math.pow(0.5f, armor / ARMOR_DENOMINATOR);
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

  public static Set<StrifeTrait> getTraitsFromSection(ConfigurationSection traitSection) {
    Set<StrifeTrait> traits = new HashSet<>();
    if (traitSection == null) {
      return traits;
    }
    for (String s : traitSection.getKeys(false)) {
      try {
        traits.add(StrifeTrait.valueOf(s));
      } catch (Exception e) {
        LogUtil.printWarning("Invalid trait " + s + ". Skipping...");
      }
    }
    return traits;
  }

  public static Set<LoreAbility> getLoreAbilitiesFromSection(ConfigurationSection laSection) {
    Set<LoreAbility> abilities = new HashSet<>();
    if (laSection == null) {
      return abilities;
    }
    for (String s : laSection.getKeys(false)) {
      LoreAbility la = StrifePlugin.getInstance().getLoreAbilityManager().getLoreAbilityFromId(s);
      if (la != null) {
        abilities.add(la);
      }
    }
    return abilities;
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
}
