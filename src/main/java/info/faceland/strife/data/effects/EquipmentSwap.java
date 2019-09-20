package info.faceland.strife.data.effects;

import info.faceland.strife.StrifePlugin;
import info.faceland.strife.data.StrifeMob;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;

public class EquipmentSwap extends Effect {

  private Map<EquipmentSlot, String> itemMap = new HashMap<>();

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    if (target.getEntity() instanceof Player) {
      throw new IllegalArgumentException("You cannot target a player with EquipmentSwap");
    }
    EntityEquipment equipment = target.getEntity().getEquipment();
    if (equipment == null) {
      return;
    }
    Bukkit.getScheduler().runTaskLater(StrifePlugin.getInstance(), () -> {
      if (itemMap.containsKey(EquipmentSlot.HEAD)) {
        equipment.setHelmet(StrifePlugin.getInstance().getEquipmentManager()
            .getItem(itemMap.get(EquipmentSlot.HEAD)));
      }
      if (itemMap.containsKey(EquipmentSlot.CHEST)) {
        equipment.setChestplate(StrifePlugin.getInstance().getEquipmentManager()
            .getItem(itemMap.get(EquipmentSlot.CHEST)));
      }
      if (itemMap.containsKey(EquipmentSlot.LEGS)) {
        equipment.setLeggings(StrifePlugin.getInstance().getEquipmentManager()
            .getItem(itemMap.get(EquipmentSlot.LEGS)));
      }
      if (itemMap.containsKey(EquipmentSlot.FEET)) {
        equipment.setBoots(StrifePlugin.getInstance().getEquipmentManager()
            .getItem(itemMap.get(EquipmentSlot.FEET)));
      }
      if (itemMap.containsKey(EquipmentSlot.HAND)) {
        equipment.setItemInMainHand(StrifePlugin.getInstance().getEquipmentManager()
            .getItem(itemMap.get(EquipmentSlot.HAND)));
      }
      if (itemMap.containsKey(EquipmentSlot.OFF_HAND)) {
        equipment.setItemInOffHand(StrifePlugin.getInstance().getEquipmentManager()
            .getItem(itemMap.get(EquipmentSlot.OFF_HAND)));
      }
    }, 1L);
  }

  public void addItem(EquipmentSlot slot, String itemId) {
    itemMap.put(slot, itemId);
  }
}
