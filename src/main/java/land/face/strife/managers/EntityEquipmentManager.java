package land.face.strife.managers;

import com.tealcube.minecraft.bukkit.TextUtils;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import land.face.strife.util.LogUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

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

    ItemStack stack = new ItemStack(material);
    if (material == Material.PLAYER_HEAD) {
      String base64 = cs.getString("base64",
          "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTIyODRlMTMyYmZkNjU5YmM2YWRhNDk3YzRmYTMwOTRjZDkzMjMxYTZiNTA1YTEyY2U3Y2Q1MTM1YmE4ZmY5MyJ9fX0=");
      stack = withBase64(stack, base64);
    } else {
      stack = new ItemStack(material);
    }

    String name = cs.getString("name", "");
    if (StringUtils.isNotBlank(name)) {
      ItemStackExtensionsKt.setDisplayName(stack, TextUtils.color(name));
    }
    List<String> lore = new ArrayList<>();
    for (String line : cs.getStringList("lore")) {
      lore.add(TextUtils.color(line));
    }
    ItemStackExtensionsKt.setLore(stack, lore);
    ItemStackExtensionsKt.setUnbreakable(stack, true);
    getItemMap().put(key, stack);
  }

  public ItemStack withBase64(ItemStack item, String base64) {
    UUID hashAsId = new UUID(base64.hashCode(), base64.hashCode());
    return Bukkit.getUnsafe().modifyItemStack(item, "{SkullOwner:{Id:\"" + hashAsId + "\",Properties:{textures:[{Value:\"" + base64 + "\"}]}}}");
  }
}
