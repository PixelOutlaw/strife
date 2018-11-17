package info.faceland.strife.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ItemUtil {

  public static boolean isArmor(Material material) {
    String name = material.name();
    return name.endsWith("HELMET") || name.endsWith("CHESTPLATE") || name.endsWith("LEGGINGS") ||
        name.endsWith("BOOTS");
  }

  public static boolean isMeleeWeapon(Material material) {
    String name = material.name();
    return name.endsWith("SWORD") || name.endsWith("AXE") || name.endsWith("HOE");
  }

  public static boolean isWand(ItemStack is) {
    if (is.getType() != Material.WOOD_SWORD) {
      return false;
    }
    if (!is.hasItemMeta()) {
      return false;
    }
    if (!is.getItemMeta().hasLore()) {
      return false;
    }
    if (is.getItemMeta().getLore().get(1) == null) {
      return false;
    }
    return is.getItemMeta().getLore().get(1).endsWith("Wand");
  }

  public static boolean isValidMageOffhand(ItemStack stack) {
    return stack.getType() == Material.BOOK || stack.getType() == Material.SHIELD ||
        stack.getType() == Material.POTATO_ITEM;
  }

  public static double getDualWieldEfficiency(ItemStack mainItem, ItemStack offItem) {
    if (mainItem == null || mainItem.getType() == Material.AIR) {
      return 1.0;
    }
    if (isWand(mainItem)) {
      return isValidMageOffhand(offItem) ? 1D : 0D;
    }
    if (isMeleeWeapon(mainItem.getType())) {
      if (isValidMageOffhand(offItem)) {
        return 1D;
      }
      if (isMeleeWeapon(offItem.getType()) || offItem.getType() == Material.BOW) {
        return 0.35D;
      }
      return 0D;
    }
    if (mainItem.getType() == Material.BOW) {
      return offItem.getType() == Material.ARROW ? 1D : 0D;
    }
    if (mainItem.getType() == Material.SHIELD) {
      return offItem.getType() == Material.SHIELD ? 1D : 0D;
    }
    if (isArmor(offItem.getType())) {
      return 0D;
    }
    return 1.0D;
  }
}
