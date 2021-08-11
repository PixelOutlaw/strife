package land.face.strife.util;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import land.face.strife.stats.StrifeStat;
import land.face.strife.stats.StrifeTrait;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemUtil {

  // This is for integrations with Loot. It assumes default
  // settings for enchantments, and is kinda shitty
  public static Map<StrifeStat, Float> getEnchantmentStats(Player player) {
    Map<StrifeStat, Float> total = new HashMap<>();
    return total;
  }

  private static Map<StrifeStat, Float> getStats(ItemStack stack) {
    Map<StrifeStat, Float> total = new HashMap<>();
    if (stack.getLore() == null) {
      return total;
    }
    List<String> lore = stack.getLore();
    for (String s : lore) {

    }
    return null;
  }

  public static ItemStack withBase64(ItemStack item, String base64) {
    UUID hashAsId = new UUID(base64.hashCode(), base64.hashCode());
    return Bukkit.getUnsafe().modifyItemStack(item,
        "{SkullOwner:{Id:\"" + hashAsId + "\",Properties:{textures:[{Value:\"" + base64
            + "\"}]}}}");
  }

  public static boolean isArmor(Material material) {
    String name = material.name();
    return name.endsWith("HELMET") || name.endsWith("CHESTPLATE") || name.endsWith("LEGGINGS")
        || name.endsWith("BOOTS")
        || name.endsWith("SKULL") || name.endsWith("HEAD");
  }

  public static boolean isMeleeWeapon(Material material) {
    switch (material) {
      case WOODEN_AXE:
      case WOODEN_HOE:
      case WOODEN_SHOVEL:
      case STONE_AXE:
      case STONE_SWORD:
      case STONE_HOE:
      case STONE_SHOVEL:
      case IRON_AXE:
      case IRON_SWORD:
      case IRON_HOE:
      case IRON_SHOVEL:
      case GOLDEN_AXE:
      case GOLDEN_SWORD:
      case GOLDEN_HOE:
      case GOLDEN_SHOVEL:
      case DIAMOND_AXE:
      case DIAMOND_SWORD:
      case DIAMOND_HOE:
      case DIAMOND_SHOVEL:
        return true;
      default:
        return false;
    }
  }

  public static boolean isTool(ItemStack stack) {
    String material = stack.getType().toString();
    if (stack.getType() == Material.FISHING_ROD || stack.getType() == Material.SHEARS
        || material.endsWith("_PICKAXE") || material.endsWith("_HOE") || material.endsWith("_AXE")
        || material.endsWith("_SHOVEL")) {
      int modelData = getCustomData(stack);
      return modelData < 8000;
    }
    return false;
  }

  public static boolean isDualWield(EntityEquipment equipment) {
    return isMeleeWeapon(equipment.getItemInMainHand().getType()) && isMeleeWeapon(
        equipment.getItemInOffHand().getType());
  }

  public static boolean isValidOffhand(EntityEquipment entityEquipment) {
    ItemStack mainItem = entityEquipment.getItemInMainHand();
    ItemStack offItem = entityEquipment.getItemInOffHand();
    if (mainItem.getType() == Material.AIR || offItem.getType() == Material.AIR) {
      return true;
    }
    if (isTool(mainItem)) {
      return false;
    }
    if (isMeleeWeapon(mainItem.getType())) {
      if (isMeleeWeapon(offItem.getType())) {
        return true;
      }
      return isValidMageOffhand(offItem);
    }
    if (isWandOrStaff(mainItem)) {
      return isValidMageOffhand(offItem);
    }
    if (mainItem.getType() == Material.BOOK) {
      return isValidMageOffhand(offItem);
    }
    if (mainItem.getType() == Material.BOW) {
      if (isPistol(mainItem)) {
        return isBullets(offItem);
      }
      return offItem.getType() == Material.ARROW;
    }
    if (mainItem.getType() == Material.SHIELD) {
      return offItem.getType() == Material.SHIELD;
    }
    return !isArmor(offItem.getType());
  }

  public static boolean isPistol(ItemStack stack) {
    int itemData = getCustomData(stack);
    return stack.getType() == Material.BOW && itemData > 10999 && itemData < 12000;
  }

  public static boolean isQuiver(ItemStack stack) {
    int itemData = getCustomData(stack);
    return stack.getType() == Material.ARROW && itemData < 10000;
  }

  public static boolean isBullets(ItemStack stack) {
    int itemData = getCustomData(stack);
    return stack.getType() == Material.ARROW && itemData > 9999 && itemData < 11000;
  }

  public static boolean isWandOrStaff(ItemStack stack) {
    if (stack.getType() != Material.WOODEN_SWORD) {
      return false;
    }
    int itemData = getCustomData(stack);
    return itemData > 8999 && itemData < 11000;
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

  public static void equipMob(Map<EquipmentSlot, ItemStack> items, LivingEntity entity,
      boolean overwrite) {
    if (overwrite) {
      entity.getEquipment().clear();
    }
    entity.getEquipment().setHelmetDropChance(0f);
    entity.getEquipment().setChestplateDropChance(0f);
    entity.getEquipment().setLeggingsDropChance(0f);
    entity.getEquipment().setBootsDropChance(0f);
    entity.getEquipment().setItemInMainHandDropChance(0f);
    entity.getEquipment().setItemInOffHandDropChance(0f);
    entity.getEquipment().setHelmet(items.getOrDefault(EquipmentSlot.HEAD, null));
    entity.getEquipment().setChestplate(items.getOrDefault(EquipmentSlot.CHEST, null));
    entity.getEquipment().setLeggings(items.getOrDefault(EquipmentSlot.LEGS, null));
    entity.getEquipment().setBoots(items.getOrDefault(EquipmentSlot.FEET, null));
    entity.getEquipment().setItemInMainHand(items.getOrDefault(EquipmentSlot.HAND, null));
    entity.getEquipment().setItemInOffHand(items.getOrDefault(EquipmentSlot.OFF_HAND, null));
  }

  public static int getCustomData(ItemStack stack) {
    if (stack.getItemMeta() == null) {
      return -1;
    }
    if (!stack.getItemMeta().hasCustomModelData()) {
      return -1;
    }
    return stack.getItemMeta().getCustomModelData();
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

  public static short getPercentageDamage(ItemStack stack, double percent) {
    double maxDura = stack.getType().getMaxDurability();
    return (short) Math.min(maxDura, maxDura * percent);
  }

  public static void sendAbilityIconPacket(ItemStack stack, Player player, int slot,
      double percent, int charges, boolean toggled) {
    try {
      ProtocolLibrary.getProtocolManager().sendServerPacket(player,
          buildPacketContainer(36 + slot, stack, percent, charges, toggled));
    } catch (InvocationTargetException e) {
      e.printStackTrace();
    }
  }

  private static PacketContainer buildPacketContainer(int slot, ItemStack stack, double percent,
      int charges, boolean toggled) {
    ItemStack sentStack = stack.clone();
    ItemStackExtensionsKt.setUnbreakable(sentStack, false);
    sentStack.setAmount(charges);
    ItemMeta sentStackMeta = sentStack.getItemMeta();
    if (sentStackMeta instanceof Damageable && percent < 1) {
      ((Damageable) sentStackMeta).setDamage(getPercentageDamage(sentStack, percent));
      sentStack.setItemMeta(sentStackMeta);
    }
    if (toggled) {
      sentStackMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
      sentStack.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
    }
    PacketContainer packetContainer = new PacketContainer(PacketType.Play.Server.SET_SLOT);
    packetContainer.getIntegers().write(0, 0);
    packetContainer.getIntegers().write(1, slot);
    packetContainer.getItemModifier().write(0, sentStack);
    return packetContainer;
  }
}
