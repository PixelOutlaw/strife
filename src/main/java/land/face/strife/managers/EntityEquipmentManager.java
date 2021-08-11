package land.face.strife.managers;

import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import io.pixeloutlaw.minecraft.spigot.garbage.StringExtensionsKt;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import land.face.strife.util.ItemUtil;
import land.face.strife.util.LogUtil;
import me.arcaniax.hdb.api.HeadDatabaseAPI;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

public class EntityEquipmentManager {

  private static final EquipmentSlot[] SLOTS = EquipmentSlot.values();
  private HeadDatabaseAPI headDatabaseAPI = new HeadDatabaseAPI();
  private final Map<String, ItemStack> itemMap;

  public EntityEquipmentManager() {
    this.itemMap = new HashMap<>();
  }

  public ItemStack getItem(String key) {
    return itemMap.getOrDefault(key, null);
  }

  public Map<EquipmentSlot, ItemStack> getEquipmentMap(Map<EquipmentSlot, String> data) {
    Map<EquipmentSlot, ItemStack> equipmentMap = new HashMap<>();
    if (data == null) {
      return equipmentMap;
    }
    for (EquipmentSlot slot : data.keySet()) {
      equipmentMap.put(slot, getItem(data.get(slot)));
    }
    return equipmentMap;
  }

  public Map<EquipmentSlot, String> buildEquipmentFromConfigSection(ConfigurationSection cs) {
    Map<EquipmentSlot, String> equipmentMap = new HashMap<>();
    if (cs == null) {
      return equipmentMap;
    }
    for (EquipmentSlot slot : EntityEquipmentManager.SLOTS) {
      equipmentMap.put(slot, cs.getString(slot.toString(), null));
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

    ItemStack stack = new ItemStack(material);
    if (material == Material.PLAYER_HEAD) {
      String base64 = cs.getString("base64");
      String hdbId = cs.getString("head-db-id");
      if (StringUtils.isNotBlank(base64)) {
        stack = ItemUtil.withBase64(stack, base64);
      } else if (StringUtils.isNotBlank(hdbId) && headDatabaseAPI != null) {
        try {
          stack = headDatabaseAPI.getItemHead(hdbId);
          LogUtil.printDebug("Loaded HDB Head " + hdbId + " successfully!");
        } catch (NullPointerException e) {
          stack = null;
        }
      }
      if (stack == null) {
        Bukkit.getLogger().warning("Null head stack! Aborting... Key:" + key);
        return;
      }
    } else {
      stack = new ItemStack(material);
    }

    if (stack.getItemMeta() instanceof LeatherArmorMeta) {
      int rgb = cs.getInt("dye-red", -1);
      if (rgb != -1) {
        LeatherArmorMeta meta = ((LeatherArmorMeta) stack.getItemMeta());
        meta.setColor(Color
            .fromRGB(cs.getInt("dye-red", 0), cs.getInt("dye-green", 0), cs.getInt("dye-blue", 0)));
        stack.setItemMeta(meta);
      }
    }

    String name = cs.getString("name", "");
    if (StringUtils.isNotBlank(name)) {
      ItemStackExtensionsKt.setDisplayName(stack, StringExtensionsKt.chatColorize(name));
    }
    List<String> lore = new ArrayList<>();
    for (String line : cs.getStringList("lore")) {
      lore.add(StringExtensionsKt.chatColorize(line));
    }
    int data = cs.getInt("custom-model-data", -1);
    if (data != -1) {
      ItemStackExtensionsKt.setCustomModelData(stack, data);
    }
    stack.setLore(lore);
    ItemStackExtensionsKt.setUnbreakable(stack, true);
    itemMap.put(key, stack);
  }

  public void setHeadDatabaseAPI(HeadDatabaseAPI api) {
    headDatabaseAPI = api;
  }
}
