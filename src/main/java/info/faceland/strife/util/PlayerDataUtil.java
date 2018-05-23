package info.faceland.strife.util;

import info.faceland.strife.StrifePlugin;
import org.bukkit.entity.Player;

public class PlayerDataUtil {

  public static int getCraftLevel(Player player) {
    return StrifePlugin.getInstance().getChampionManager().getChampion(player.getUniqueId()).getCraftingLevel();
  }

  public static float getCraftExp(Player player) {
    return StrifePlugin.getInstance().getChampionManager().getChampion(player.getUniqueId()).getCraftingExp();
  }

  public static float getCraftMaxExp(Player player) {
    int level = getCraftLevel(player);
    return StrifePlugin.getInstance().getCraftExperienceManager().getMaxCraftExp(level);
  }

  public static int getEnchantLevel(Player player) {
    return StrifePlugin.getInstance().getChampionManager().getChampion(player.getUniqueId()).getEnchantLevel();
  }

  public static float getEnchantExp(Player player) {
    return StrifePlugin.getInstance().getChampionManager().getChampion(player.getUniqueId()).getCraftingExp();
  }

  public static float getEnchantMaxExp(Player player) {
    int level = getEnchantLevel(player);
    return StrifePlugin.getInstance().getEnchantExperienceManager().getMaxExp(level);
  }

  public static int getFishLevel(Player player) {
    return StrifePlugin.getInstance().getChampionManager().getChampion(player.getUniqueId()).getFishingLevel();
  }

  public static float getFishExp(Player player) {
    return StrifePlugin.getInstance().getChampionManager().getChampion(player.getUniqueId()).getFishingExp();
  }

  public static float getFishMaxExp(Player player) {
    int level = getFishLevel(player);
    return StrifePlugin.getInstance().getFishExperienceManager().getMaxExp(level);
  }

  // TODO: Something better with the crap below here...
  public static int getMaxItemDestroyLevel(Player player) {
    return getMaxItemDestroyLevel(
        StrifePlugin.getInstance().getChampionManager().getChampion(player.getUniqueId()).getCraftingLevel());
  }

  private static int getMaxItemDestroyLevel(int craftLvl) {
    return 10 + (int)Math.floor((double)craftLvl/3) * 5;
  }

  public static int getMaxCraftItemLevel(Player player) {
    return getMaxCraftItemLevel(
        StrifePlugin.getInstance().getChampionManager().getChampion(player.getUniqueId()).getCraftingLevel());
  }

  public static int getMaxCraftItemLevel(int craftLvl) {
    return 5 + (int)Math.floor((double)craftLvl/5) * 8;
  }
}
