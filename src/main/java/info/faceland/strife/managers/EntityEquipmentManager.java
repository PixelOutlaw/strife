package info.faceland.strife.managers;

import com.tealcube.minecraft.bukkit.TextUtils;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import info.faceland.strife.util.LogUtil;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class EntityEquipmentManager {

  public static final EquipmentSlot[] SLOTS = EquipmentSlot.values();
  private final Map<String, ItemStack> itemMap;

  public EntityEquipmentManager() {
    this.itemMap = new HashMap<>();
  }

  public Map<String, ItemStack> getItemMap() {
    return itemMap;
  }

  public ItemStack getItem(String key) {
    return itemMap.getOrDefault(key, null);
  }

  public Map<EquipmentSlot, ItemStack> buildEquipmentFromConfigSection(ConfigurationSection cs) {
    Map<EquipmentSlot, ItemStack> equipmentMap = new HashMap<>();
    if (cs == null) {
      return equipmentMap;
    }
    for (EquipmentSlot slot : EntityEquipmentManager.SLOTS) {
      equipmentMap.put(slot, getItem(cs.getString(slot.toString(), "")));
    }
    return equipmentMap;
  }

  public void loadEquipmentItem(String key, ConfigurationSection cs) {
    Material material;
    String type = cs.getString("material");
    try {
      material = Material.valueOf(type);
    } catch (Exception e) {
      LogUtil.printWarning("Skipping item " + key + " for invalid material");
      return;
    }

    ItemStack itemStack = new ItemStack(material);

    String name = cs.getString("name", "");
    if (StringUtils.isNotBlank(name)) {
      ItemStackExtensionsKt.setDisplayName(itemStack, TextUtils.color(name));
    }
    List<String> lore = new ArrayList<>();
    for (String line : cs.getStringList("lore")) {
      lore.add(TextUtils.color(line));
    }
    ItemStackExtensionsKt.setLore(itemStack, lore);
    ItemStackExtensionsKt.setUnbreakable(itemStack, true);

    String uuidString = cs.getString("uuid", "b4064ecc-5508-4848-ae4d-f12fbebd0a52");
    if (material == Material.PLAYER_HEAD && StringUtils.isNotBlank(uuidString)) {
      UUID uuid = UUID.fromString(uuidString);
      SkullMeta skullMeta = (SkullMeta) itemStack.getItemMeta();
      skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(uuid));
      itemStack.setItemMeta(skullMeta);
    }
    getItemMap().put(key, itemStack);
  }
}
