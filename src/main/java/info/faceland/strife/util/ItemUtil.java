package info.faceland.strife.util;

import info.faceland.strife.attributes.StrifeTrait;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemUtil {

  public static boolean isArmor(Material material) {
    String name = material.name();
    return name.endsWith("HELMET") || name.endsWith("CHESTPLATE") || name.endsWith("LEGGINGS") ||
        name.endsWith("BOOTS");
  }

  public static boolean isMeleeWeapon(Material material) {
    String name = material.name();
    return name.endsWith("SWORD") || name.endsWith("_AXE") || name.endsWith("HOE");
  }

  public static boolean isDualWield(EntityEquipment equipment) {
    return isMeleeWeapon(equipment.getItemInMainHand().getType()) && isMeleeWeapon(
        equipment.getItemInOffHand().getType());
  }

  public static boolean isValidOffhand(EntityEquipment entityEquipment) {
    ItemStack mainItem = entityEquipment.getItemInMainHand();
    ItemStack offItem = entityEquipment.getItemInOffHand();
    if (mainItem.getType() == Material.AIR) {
      return true;
    }
    if (offItem.getType() == Material.AIR) {
      return true;
    }
    if (isMeleeWeapon(mainItem.getType())) {
      if (isMeleeWeapon(offItem.getType())) {
        return true;
      }
      return isValidMageOffhand(offItem);
    }
    if (isWand(mainItem)) {
      return isValidMageOffhand(offItem);
    }
    if (mainItem.getType() == Material.BOW) {
      return offItem.getType() == Material.ARROW;
    }
    if (mainItem.getType() == Material.SHIELD) {
      return offItem.getType() == Material.SHIELD;
    }
    return !isArmor(offItem.getType());
  }

  public static boolean isWand(ItemStack is) {
    if (is.getType() != Material.WOODEN_SWORD) {
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
        stack.getType() == Material.POTATO;
  }

  public static ItemStack getItem(EntityEquipment equipment, EquipmentSlot slot) {
    switch (slot) {
      case HAND:
        return equipment.getItemInMainHand();
      case OFF_HAND:
        return equipment.getItemInOffHand();
      case HEAD:
        return equipment.getHelmet();
      case CHEST:
        return equipment.getChestplate();
      case LEGS:
        return equipment.getLeggings();
      case FEET:
        return equipment.getBoots();
      default:
        return null;
    }
  }

  public static List<String> getLore(ItemStack stack) {
    if (stack == null || stack.getType() == Material.AIR) {
      return new ArrayList<>();
    }
    if (stack.getItemMeta() == null || stack.getItemMeta().getLore() == null) {
      return new ArrayList<>();
    }
    return stack.getItemMeta().getLore();
  }

  public static Set<StrifeTrait> getTraits(ItemStack stack) {
    List<String> lore = getLore(stack);
    Set<StrifeTrait> traits = new HashSet<>();
    if (lore.isEmpty()) {
      return traits;
    }
    for (String s : lore) {
      StrifeTrait trait = StrifeTrait.fromName(ChatColor.stripColor(s));
      if (trait != null) {
        LogUtil.printDebug("Added Trait: " + s);
        traits.add(trait);
      }
    }
    return traits;
  }

  public static int hashItem(ItemStack itemStack) {
    if (itemStack == null || itemStack.getType() == Material.AIR) {
      return -1;
    }
    return itemStack.hashCode();
  }

  public static boolean doesHashMatch(ItemStack itemStack, int hash) {
    if (itemStack == null || itemStack.getType() == Material.AIR) {
      return hash == -1;
    }
    return itemStack.hashCode() == hash;
  }

  public static void removeAttributes(ItemStack item) {
    if (item == null || item.getType() == Material.AIR) {
      return;
    }
    if (item.getType().getMaxDurability() < 15) {
      return;
    }
    ItemMeta meta = item.getItemMeta();
    if (meta.getAttributeModifiers() == null) {
      return;
    }
    for (Attribute attr : meta.getAttributeModifiers().asMap().keySet()) {
      meta.removeAttributeModifier(attr);
    }
    item.setItemMeta(meta);
  }
}
