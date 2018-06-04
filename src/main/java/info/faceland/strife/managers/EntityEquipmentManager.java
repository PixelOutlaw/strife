package info.faceland.strife.managers;

import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

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
}
