package info.faceland.strife.util;

import info.faceland.strife.StrifePlugin;
import info.faceland.strife.conditions.Condition.Comparison;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class PlayerDataUtil {

  public static int getCraftSkill(Player player, Boolean updateEquipment) {
    return StrifePlugin.getInstance().getChampionManager().getChampion(player.getUniqueId())
        .getCraftSkill(updateEquipment);
  }

  public static int getCraftLevel(Player player) {
    return StrifePlugin.getInstance().getChampionManager().getChampion(player.getUniqueId())
        .getCraftingLevel();
  }

  public static float getCraftExp(Player player) {
    return StrifePlugin.getInstance().getChampionManager().getChampion(player.getUniqueId())
        .getCraftingExp();
  }

  public static float getCraftMaxExp(Player player) {
    int level = getCraftLevel(player);
    return StrifePlugin.getInstance().getCraftExperienceManager().getMaxExp(level);
  }

  public static int getEnchantSkill(Player player, Boolean updateEquipment) {
    return StrifePlugin.getInstance().getChampionManager().getChampion(player.getUniqueId())
        .getEnchantSkill(updateEquipment);
  }

  public static int getEnchantLevel(Player player) {
    return StrifePlugin.getInstance().getChampionManager().getChampion(player.getUniqueId())
        .getEnchantLevel();
  }

  public static float getEnchantExp(Player player) {
    return StrifePlugin.getInstance().getChampionManager().getChampion(player.getUniqueId())
        .getEnchantExp();
  }

  public static float getEnchantMaxExp(Player player) {
    int level = getEnchantLevel(player);
    return StrifePlugin.getInstance().getEnchantExperienceManager().getMaxExp(level);
  }

  public static int getFishSkill(Player player, Boolean updateEquipment) {
    return StrifePlugin.getInstance().getChampionManager().getChampion(player.getUniqueId())
        .getFishSkill(updateEquipment);
  }

  public static int getFishLevel(Player player) {
    return StrifePlugin.getInstance().getChampionManager().getChampion(player.getUniqueId())
        .getFishingLevel();
  }

  public static float getFishExp(Player player) {
    return StrifePlugin.getInstance().getChampionManager().getChampion(player.getUniqueId())
        .getFishingExp();
  }

  public static float getFishMaxExp(Player player) {
    int level = getFishLevel(player);
    return StrifePlugin.getInstance().getFishExperienceManager().getMaxExp(level);
  }

  public static int getMineSkill(Player player, Boolean updateEquipment) {
    return StrifePlugin.getInstance().getChampionManager().getChampion(player.getUniqueId())
        .getMineSkill(updateEquipment);
  }

  public static int getMiningLevel(Player player) {
    return StrifePlugin.getInstance().getChampionManager().getChampion(player.getUniqueId())
        .getMiningLevel();
  }

  public static float getMiningExp(Player player) {
    return StrifePlugin.getInstance().getChampionManager().getChampion(player.getUniqueId())
        .getMiningExp();
  }

  public static float getMiningMaxExp(Player player) {
    int level = getMiningLevel(player);
    return StrifePlugin.getInstance().getMiningExperienceManager().getMaxExp(level);
  }

  public static void updatePlayerEquipment(Player player) {
    StrifePlugin.getInstance().getChampionManager().updateEquipmentAttributes(
        StrifePlugin.getInstance().getChampionManager().getChampion(player.getUniqueId()));
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
    }
    return false;
  }

  // TODO: Something better with the crap below here...
  public static int getMaxItemDestroyLevel(Player player) {
    return getMaxItemDestroyLevel(
        StrifePlugin.getInstance().getChampionManager().getChampion(player.getUniqueId())
            .getCraftingLevel());
  }

  private static int getMaxItemDestroyLevel(int craftLvl) {
    return 10 + (int) Math.floor((double) craftLvl / 3) * 5;
  }

  public static int getMaxCraftItemLevel(Player player) {
    return getMaxCraftItemLevel(
        StrifePlugin.getInstance().getChampionManager().getChampion(player.getUniqueId())
            .getCraftingLevel());
  }

  public static int getMaxCraftItemLevel(int craftLvl) {
    return 5 + (int) Math.floor((double) craftLvl / 5) * 8;
  }

  public static String getName(LivingEntity livingEntity) {
    if (livingEntity instanceof Player) {
      return ((Player) livingEntity).getDisplayName();
    }
    return livingEntity.getCustomName() == null ? livingEntity.getName() : livingEntity.getCustomName();
  }
}
