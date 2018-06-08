package info.faceland.strife.util;

import info.faceland.strife.StrifePlugin;
import info.faceland.strife.data.Champion;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
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
    if (stack.getType() == Material.BOOK || stack.getType() == Material.SHIELD
        || stack.getType() == Material.POTATO_ITEM) {
      return true;
    }
    return false;
  }

  public static double getDualWieldEfficiency(ItemStack mainHandItemStack, ItemStack offHandItemStack) {
    if (mainHandItemStack == null || mainHandItemStack.getType() == Material.AIR) {
      return 1.0;
    }
    if (isWand(mainHandItemStack)) {
      return isValidMageOffhand(offHandItemStack) ? 1D : 0D;
    }
    if (isMeleeWeapon(mainHandItemStack.getType())) {
      if (offHandItemStack.getType() == Material.POTATO || offHandItemStack.getType() == Material.SHIELD) {
        return 1D;
      }
      if (isMeleeWeapon(offHandItemStack.getType()) || offHandItemStack.getType() == Material.BOW) {
        return 0.3D;
      }
      return 0D;
    }
    if (mainHandItemStack.getType() == Material.BOW) {
      return offHandItemStack.getType() == Material.ARROW ? 1D : 0D;
    }
    return 0D;
  }

  public static void updateHashes(LivingEntity entity) {
    if (!(entity instanceof Player)) {
      return;
    }
    Champion champion = StrifePlugin.getInstance().getChampionManager().getChampion(entity.getUniqueId());
    if (champion.isEquipmentHashMatching()) {
      return;
    }
    champion.updateHashedEquipment();
    StrifePlugin.getInstance().getChampionManager().updateAll(champion);
  }
}
