package info.faceland.strife.util;

import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;
import info.faceland.strife.StrifePlugin;
import info.faceland.strife.conditions.Condition;
import info.faceland.strife.conditions.Condition.Comparison;
import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.data.champion.Champion;
import info.faceland.strife.data.champion.ChampionSaveData.LifeSkillType;
import java.util.Set;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class PlayerDataUtil {

  public static void sendActionbarDamage(LivingEntity entity, double damage, double overBonus,
      double critBonus, double fireBonus, double iceBonus, double lightningBonus, double earthBonus,
      double lightBonus, boolean corrupt, boolean isBleedApplied, boolean isSneakAttack) {
    if (!(entity instanceof Player)) {
      return;
    }
    StringBuilder damageString = new StringBuilder("&f&l" + (int) Math.ceil(damage) + " Damage! ");
    if (overBonus > 0) {
      damageString.append("&e✦");
    }
    if (isSneakAttack) {
      damageString.append("&e&l!");
    }
    if (critBonus > 0) {
      damageString.append("&c✶");
    }
    if (fireBonus > 0) {
      damageString.append("&6☀");
    }
    if (iceBonus > 0) {
      damageString.append("&b❊");
    }
    if (lightningBonus > 0) {
      damageString.append("&7⚡");
    }
    if (earthBonus > 0) {
      damageString.append("&2▼");
    }
    if (lightBonus > 0) {
      damageString.append("&f❂");
    }
    if (corrupt) {
      damageString.append("&8❂");
    }
    if (isBleedApplied) {
      damageString.append("&4♦");
    }
    MessageUtils.sendActionBar((Player) entity, damageString.toString());
  }

  public static boolean areConditionsMet(StrifeMob caster, StrifeMob target,
      Set<Condition> conditions) {
    if (target == null && conditions.size() > 0) {
      return false;
    }
    for (Condition condition : conditions) {
      if (!condition.isMet(caster, target)) {
        return false;
      }
    }
    return true;
  }

  public static void updatePlayerEquipment(Player player) {
    StrifePlugin.getInstance().getChampionManager().updateEquipmentStats(
        StrifePlugin.getInstance().getChampionManager().getChampion(player));
  }

  public static void playExpSound(Player player) {
    player.playSound(player.getEyeLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.2f,
        0.8f + (float) Math.random() * 0.4f);
  }

  // TODO: Something less stupid, this shouldn't be in this Util
  public static boolean conditionCompare(Comparison comparison, double val1, double val2) {
    switch (comparison) {
      case GREATER_THAN:
        return val1 > val2;
      case LESS_THAN:
        return val1 < val2;
      case EQUAL:
        return val1 == val2;
      case NONE:
        throw new IllegalArgumentException("Compare condition is NONE! Invalid usage!");
    }
    return false;
  }

  public static int getMaxItemDestroyLevel(Player player) {
    return getMaxItemDestroyLevel(
        StrifePlugin.getInstance().getChampionManager().getChampion(player)
            .getLifeSkillLevel(LifeSkillType.CRAFTING));
  }

  private static int getMaxItemDestroyLevel(int craftLvl) {
    return 10 + (int) Math.floor((double) craftLvl / 3) * 5;
  }

  public static int getMaxCraftItemLevel(Player player) {
    return getMaxCraftItemLevel(
        StrifePlugin.getInstance().getChampionManager().getChampion(player)
            .getLifeSkillLevel(LifeSkillType.CRAFTING));
  }

  public static int getMaxCraftItemLevel(int craftLvl) {
    return 5 + (int) Math.floor((double) craftLvl / 5) * 8;
  }

  public static String getName(LivingEntity livingEntity) {
    if (livingEntity instanceof Player) {
      return ((Player) livingEntity).getDisplayName();
    }
    return livingEntity.getCustomName() == null ? livingEntity.getName()
        : livingEntity.getCustomName();
  }

  public static double getEffectiveLifeSkill(Player player, LifeSkillType type,
      Boolean updateEquipment) {
    return StrifePlugin.getInstance().getChampionManager().getChampion(player)
        .getEffectiveLifeSkillLevel(type, updateEquipment);
  }

  public static int getLifeSkillLevel(Player player, LifeSkillType type) {
    return StrifePlugin.getInstance().getChampionManager().getChampion(player)
        .getLifeSkillLevel(type);
  }

  public static float getLifeSkillExp(Player player, LifeSkillType type) {
    return StrifePlugin.getInstance().getChampionManager().getChampion(player)
        .getLifeSkillExp(type);
  }

  public static float getFishMaxExp(Player player, LifeSkillType type) {
    int level = getLifeSkillLevel(player, type);
    return StrifePlugin.getInstance().getSkillExperienceManager()
        .getMaxExp(type, level);
  }

  public static float getSkillProgress(Champion champion, LifeSkillType type) {
    return champion.getSaveData().getSkillExp(type) / StrifePlugin.getInstance()
        .getSkillExperienceManager().getMaxExp(type, champion.getSaveData().getSkillLevel(type));
  }

  public static String getSkillColor(LifeSkillType type) {
    switch (type) {
      case CRAFTING:
        return "&e";
      case ENCHANTING:
        return "&d";
      case FISHING:
        return "&b";
      case MINING:
        return "&2";
      case SNEAK:
        return "&7";
    }
    return "";
  }

  public static String getPrettySkillName(LifeSkillType type) {
    switch (type) {
      case CRAFTING:
        return "Crafting";
      case ENCHANTING:
        return "Enchanting";
      case FISHING:
        return "Fishing";
      case MINING:
        return "Mining";
      case SNEAK:
        return "Sneak";
    }
    return "";
  }
}
