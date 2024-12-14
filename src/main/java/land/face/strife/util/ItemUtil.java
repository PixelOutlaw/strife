package land.face.strife.util;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.tealcube.minecraft.bukkit.facecore.utilities.ItemUtils;
import com.tealcube.minecraft.bukkit.facecore.utilities.TextUtils;
import info.faceland.loot.data.pdc.EquipmentData;
import info.faceland.loot.utils.PersistHelper;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import land.face.dinvy.entity.PlayerData;
import land.face.dinvy.windows.equipment.EquipmentMenu.DeluxeSlot;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.champion.EquipmentCache;
import land.face.strife.data.champion.LifeSkillType;
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

  public static ItemStack withBase64(ItemStack item, String base64) {
    UUID hashAsId = new UUID(base64.hashCode(), base64.hashCode());
    return Bukkit.getUnsafe().modifyItemStack(item,
        "{SkullOwner:{Id:\"" + hashAsId + "\",Properties:{textures:[{Value:\"" + base64
            + "\"}]}}}");
  }

  public static boolean isArmor(Material material) {
    String name = material.name();
    return material == Material.SHEARS || name.endsWith("HELMET") || name.endsWith("CHESTPLATE") ||
        name.endsWith("LEGGINGS") || name.endsWith("BOOTS") || name.endsWith("SKULL") ||
        name.endsWith("HEAD");
  }

  public static boolean isAccessory(Material material) {
    return switch (material) {
      case DIAMOND_HORSE_ARMOR, IRON_HORSE_ARMOR, GOLDEN_HORSE_ARMOR -> true;
      default -> false;
    };
  }

  public static boolean isMeleeWeapon(Material material) {
    return switch (material) {
      case WOODEN_HOE, WOODEN_SHOVEL, STONE_AXE, STONE_SWORD, STONE_HOE, STONE_SHOVEL, IRON_AXE,
           IRON_SWORD, IRON_HOE, IRON_SHOVEL, GOLDEN_AXE, GOLDEN_SWORD, GOLDEN_HOE, GOLDEN_SHOVEL, DIAMOND_AXE,
           DIAMOND_SWORD, DIAMOND_HOE, DIAMOND_SHOVEL -> true;
      default -> false;
    };
  }

  public static boolean isTool(ItemStack stack) {
    if (stack == null) {
      return false;
    }
    var type = stack.getType();
    switch (type) {
      case STONE_AXE, IRON_AXE, STONE_HOE, IRON_HOE, IRON_PICKAXE, STONE_PICKAXE, FISHING_ROD -> {
        return getCustomData(stack) < 8000;
      }
    }
    return false;
  }

  public static boolean isDualWield(EntityEquipment equipment) {
    if (isMeleeWeapon(equipment.getItemInMainHand().getType())) {
      return isMeleeWeapon(equipment.getItemInOffHand().getType());
    }
    if (isWand(equipment.getItemInMainHand())) {
      return isWand(equipment.getItemInOffHand());
    }
    return false;
  }

  public static boolean isValidOffhand(ItemStack mainItem, ItemStack offItem) {
    if (offItem == null || mainItem.getType() == Material.AIR || offItem.getType() == Material.AIR) {
      return true;
    }
    if (mainItem.getType() == Material.FISHING_ROD && offItem.getType() == Material.WHEAT_SEEDS) {
      return true;
    }
    if (isAccessory(offItem.getType())) {
      return false;
    }
    if (mainItem.getType() == Material.SHIELD) {
      return offItem.getType() == Material.SHIELD;
    }
    if (mainItem.getType() == Material.BOOK ||
        mainItem.getType() == Material.SHIELD ||
        mainItem.getType() == Material.ARROW) {
      return false;
    }
    if (isTool(mainItem)) {
      return false;
    }
    if (isMeleeWeapon(mainItem.getType())) {
      if (isMeleeWeapon(offItem.getType())) {
        return true;
      }
      return (offItem.getType() == Material.SHIELD || offItem.getType() == Material.BOOK);
    }
    if (isWand(mainItem) && isWand(offItem)) {
      return true;
    }
    if (isWandOrStaff(mainItem)) {
      return (offItem.getType() == Material.SHIELD || offItem.getType() == Material.BOOK);
    }
    if (mainItem.getType() == Material.BOW) {
      if (isFlintlock(mainItem)) {
        return isBullets(offItem);
      }
      return offItem.getType() == Material.ARROW;
    }
    return !isArmor(offItem.getType());
  }

  public static boolean isFlintlock(ItemStack stack) {
    int itemData = getCustomData(stack);
    if (stack.getType() != Material.BOW) {
      return false;
    }
    return itemData > 10999 && itemData < 13000;
  }

  public static boolean isQuiver(ItemStack stack) {
    if (stack == null) {
      return false;
    }
    int itemData = getCustomData(stack);
    return stack.getType() == Material.ARROW && itemData < 10000;
  }

  public static boolean isBullets(ItemStack stack) {
    if (stack == null) {
      return false;
    }
    int itemData = getCustomData(stack);
    return stack.getType() == Material.ARROW && itemData > 9999 && itemData < 11000;
  }

  public static boolean isWandOrStaff(ItemStack stack) {
    if (stack == null) {
      return false;
    }
    if (stack.getType() != Material.WOODEN_SWORD) {
      return false;
    }
    int itemData = getCustomData(stack);
    return itemData > 8999 && itemData < 11000;
  }

  public static boolean isWand(ItemStack stack) {
    if (stack == null) {
      return false;
    }
    if (stack.getType() != Material.WOODEN_SWORD) {
      return false;
    }
    int itemData = getCustomData(stack);
    return itemData > 8999 && itemData < 10000;
  }

  public static boolean isValidMageOffhand(ItemStack stack) {
    if (stack == null) {
      return true;
    }
    return stack.getType() == Material.BOOK || stack.getType() == Material.SHIELD ||
        stack.getType() == Material.POTATO;
  }

  public static ItemStack getItem(EntityEquipment equipment, EquipmentSlot slot) {
    return switch (slot) {
      case HAND -> equipment.getItemInMainHand();
      case OFF_HAND -> equipment.getItemInOffHand();
      case HEAD -> equipment.getHelmet();
      case CHEST -> equipment.getChestplate();
      case LEGS -> equipment.getLeggings();
      case FEET -> equipment.getBoots();
      default -> throw new IllegalArgumentException("This is not a horse");
    };
  }

  public static ItemStack getItem(PlayerData data, DeluxeSlot slot) {
    return data.getEquipmentItem(slot);
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
      s = splitOnOffset(s);
      StrifeTrait trait = StrifeTrait.fromName(ChatColor.stripColor(s));
      if (trait != null) {
        LogUtil.printDebug("Added Trait: " + s);
        traits.add(trait);
      }
    }
    return traits;
  }

  public static String splitOnOffset(String s) {
    String[] split = s.split("\uF804\uF824");
    return split.length == 1 ? s : split[1];
  }

  public static void equipMob(Map<EquipmentSlot, ItemStack> items, LivingEntity entity,
      boolean clear, boolean overwrite) {
    if (clear) {
      entity.getEquipment().clear();
    }
    entity.getEquipment().setHelmetDropChance(0f);
    entity.getEquipment().setChestplateDropChance(0f);
    entity.getEquipment().setLeggingsDropChance(0f);
    entity.getEquipment().setBootsDropChance(0f);
    entity.getEquipment().setItemInMainHandDropChance(0f);
    entity.getEquipment().setItemInOffHandDropChance(0f);
    for (EquipmentSlot slot : EquipmentCache.EQUIPMENT_SLOTS) {
      if (!items.containsKey(slot)) {
        continue;
      }
      ItemStack currentItem = entity.getEquipment().getItem(slot);
      ItemStack newItem = items.get(slot);
      if (currentItem.getType() == Material.PLAYER_HEAD && newItem.getType() != Material.PLAYER_HEAD) {
        continue;
      }
      if (!overwrite && currentItem.getType() != Material.AIR) {
        continue;
      }
      entity.getEquipment().setItem(slot, newItem);
    }
  }

  @Deprecated
  public static int getCustomData(ItemStack stack) {
    return ItemUtils.getModelData(stack);
  }

  public static boolean doesHashMatch(ItemStack itemStack, EquipmentCache cache, String slotId) {
    int previousHash = cache.getSlotHash(slotId);
    if (itemStack == null || itemStack.getType() == Material.AIR) {
      cache.setSlotHash(slotId, -1);
      return previousHash == -1;
    }
    EquipmentData data = PersistHelper.getEquipmentData(itemStack);
    if (data != null) {
      int newHash = data.rebuild(previousHash);
      cache.setSlotHash(slotId, newHash);
      if (newHash != previousHash) {
        PersistHelper.setEquipmentData(itemStack, data, false);
        return false;
      }
      return true;
    } else {
      int newHash = itemStack.hashCode();
      cache.setSlotHash(slotId, newHash);
      return newHash == previousHash;
    }
  }

  public static boolean meetsRequirements(ItemStack stack, StrifeMob mob) {
    if (stack == null || stack.getType() == Material.AIR) {
      return true;
    }
    List<String> lore = TextUtils.getLore(stack);
    if (lore.isEmpty()) {
      return true;
    }
    EquipmentData data = PersistHelper.getEquipmentData(stack);
    if (data != null) {
      if (data.getTier() != null) {
        int itemLevel = data.getCustomLevel() - data.getUpgradeLevel() / 3;
        itemLevel += data.getLevelMod();
        itemLevel = Math.max(1, itemLevel);
        if (data.getTier().isSkillRequirement()) {
          String mat = stack.getType().toString();
          if (stack.getType() == Material.FISHING_ROD) {
            return mob.getChampion().getLifeSkillLevel(LifeSkillType.FISHING) >= itemLevel;
          } else if (mat.endsWith("_HOE")) {
            return mob.getChampion().getLifeSkillLevel(LifeSkillType.FARMING) >= itemLevel;
          } else if (mat.endsWith("_PICKAXE")) {
            return mob.getChampion().getLifeSkillLevel(LifeSkillType.MINING) >= itemLevel;
          } else if (mat.endsWith("_AXE")) {
            return mob.getChampion().getLifeSkillLevel(LifeSkillType.FARMING) >= itemLevel;
          }
          return true;
        } else {
          return mob.getLevel() >= itemLevel;
        }
      } else {
        return true;
      }
    }
    // TODO: DELETE BELOW
    String strippedLine = ChatColor.stripColor(lore.get(0));
    if (strippedLine.startsWith("Level Requirement: ")) {
      int itemLevel = Integer.parseInt(strippedLine.replace("Level Requirement: ", ""));
      return mob.getLevel() >= itemLevel;
    } else if (strippedLine.startsWith("Skill Requirement: ")) {
      int itemLevel = Integer.parseInt(strippedLine.replace("Skill Requirement: ", ""));
      String mat = stack.getType().toString();
      if (stack.getType() == Material.FISHING_ROD) {
        return mob.getChampion().getLifeSkillLevel(LifeSkillType.FISHING) >= itemLevel;
      } else if (mat.endsWith("_HOE")) {
        return mob.getChampion().getLifeSkillLevel(LifeSkillType.FARMING) >= itemLevel;
      } else if (mat.endsWith("_PICKAXE")) {
        return mob.getChampion().getLifeSkillLevel(LifeSkillType.MINING) >= itemLevel;
      } else if (mat.endsWith("_AXE")) {
        return mob.getChampion().getLifeSkillLevel(LifeSkillType.FARMING) >= itemLevel;
      }
      return false;
    }
    return true;
  }

  public static void sendAbilityIconPacket(ItemStack stack, Player player, int slot,
      double percent, int charges, int maxCharges, boolean toggled) {
    try {
      ProtocolLibrary.getProtocolManager().sendServerPacket(player,
          buildPacketContainer(36 + slot, stack, percent, charges, maxCharges, toggled));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static PacketContainer buildPacketContainer(int slot, ItemStack stack, double percent,
      int charges, int maxCharge, boolean toggled) {
    ItemStack sentStack = stack.clone();
    ItemStackExtensionsKt.setUnbreakable(sentStack, false);
    ItemMeta sentStackMeta = sentStack.getItemMeta();
    int damage;
    if (percent > 0.02) {
      damage = (int) (percent * 1000D);
    } else {
      damage = 0;
    }
    sentStack.editMeta(m -> {
      ((Damageable) m).setDamage(damage);
      m.setMaxStackSize(maxCharge * 10 + charges);
    });
    if (toggled) {
      sentStackMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
      sentStack.addUnsafeEnchantment(Enchantment.UNBREAKING, 1);
    }
    PacketContainer packetContainer = new PacketContainer(PacketType.Play.Server.SET_SLOT);
    packetContainer.getIntegers().write(0, 0);
    packetContainer.getIntegers().write(2, slot);
    packetContainer.getItemModifier().write(0, sentStack);
    return packetContainer;
  }
}
