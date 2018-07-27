package info.faceland.strife.managers;

import com.tealcube.minecraft.bukkit.TextUtils;
import info.faceland.strife.util.LogUtil;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

public class EntityEquipmentManager {

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

  public void loadEquipmentItem(String key, ConfigurationSection cs) {
    Material material;
    String type = cs.getString("material");
    try {
      material = Material.getMaterial(type);
    } catch (Exception e) {
      LogUtil.printWarning("Skipping item " + key + " for invalid material");
      return;
    }

    ItemStack itemStack;
    SkullType skullType = null;
    if (material != Material.SKULL_ITEM) {
      itemStack = new ItemStack(material);
    } else {
      skullType = SkullType.valueOf(cs.getString("skull-type", "ZOMBIE"));
      itemStack = new ItemStack(Material.SKULL_ITEM, 1, (short) skullType.ordinal());
    }
    ItemMeta meta = itemStack.getItemMeta();

    String name = cs.getString("name", "");
    if (StringUtils.isNotBlank(name)) {
      meta.setDisplayName(TextUtils.color(name));
    }

    List<String> lore = new ArrayList<>();
    for (String line : cs.getStringList("lore")) {
      lore.add(TextUtils.color(line));
    }
    meta.setLore(lore);

    itemStack.setItemMeta(meta);

    if (material == Material.SKULL_ITEM && skullType == SkullType.PLAYER) {
      UUID uuid = UUID.fromString(cs.getString("uuid", "b4064ecc-5508-4848-ae4d-f12fbebd0a52"));
      SkullMeta skullMeta = (SkullMeta) itemStack.getItemMeta();
      skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(uuid));
      itemStack.setItemMeta(skullMeta);
    }

    getItemMap().put(key, itemStack);
  }
}
