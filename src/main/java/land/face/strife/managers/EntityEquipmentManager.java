package land.face.strife.managers;

import com.tealcube.minecraft.bukkit.TextUtils;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
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

  public static final EquipmentSlot[] SLOTS = EquipmentSlot.values();
  private HeadDatabaseAPI headDatabaseAPI = new HeadDatabaseAPI();
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

    ItemStack stack = new ItemStack(material);
    if (material == Material.PLAYER_HEAD) {
      String base64 = cs.getString("base64");
      String hdbId = cs.getString("head-db-id");
      if (StringUtils.isNotBlank(base64)) {
        stack = ItemUtil.withBase64(stack, base64);
      } else if (StringUtils.isNotBlank(hdbId)) {
        try {
          stack = headDatabaseAPI.getItemHead(hdbId);
        } catch (NullPointerException e) {
          Bukkit.getLogger().warning("Invalid HeadDatabaseID! " + key + " | " + hdbId);
          return;
        }
        Bukkit.getLogger().info("Loaded HDB Head " + hdbId + " successfully!");
      } else {
        Bukkit.getLogger().warning("Invalid head config for key " + key);
        return;
      }
      if (stack == null) {
        Bukkit.getLogger().warning("Null head stack! Aborting... Key:" + key);
        return;
      }
    } else {
      stack = new ItemStack(material);
    }

    if (stack.getItemMeta() instanceof LeatherArmorMeta) {
      int rgb = cs.getInt("dye-rgb", -1);
      if (rgb != -1) {
        LeatherArmorMeta meta = ((LeatherArmorMeta) stack.getItemMeta());
        meta.setColor(Color.fromRGB(rgb));
        stack.setItemMeta(meta);
      }
    }

    String name = cs.getString("name", "");
    if (StringUtils.isNotBlank(name)) {
      ItemStackExtensionsKt.setDisplayName(stack, TextUtils.color(name));
    }
    List<String> lore = new ArrayList<>();
    for (String line : cs.getStringList("lore")) {
      lore.add(TextUtils.color(line));
    }
    int data = cs.getInt("custom-model-data", -1);
    if (data != -1) {
      ItemStackExtensionsKt.setCustomModelData(stack, data);
    }
    ItemStackExtensionsKt.setLore(stack, lore);
    ItemStackExtensionsKt.setUnbreakable(stack, true);
    getItemMap().put(key, stack);
  }
}
