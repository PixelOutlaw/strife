package land.face.strife.data.effects;

import java.util.HashMap;
import java.util.Map;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.UniqueEntity;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class EquipmentSwap extends Effect {

  private final Map<EquipmentSlot, String> itemMap = new HashMap<>();

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
      for (EquipmentSlot slot : itemMap.keySet()) {
        ItemStack stack = getItem(target, slot);
        if (stack != null) {
          equipment.setItem(slot, stack);
        }
      }
    }, 1L);
  }

  public void addItem(EquipmentSlot slot, String itemId) {
    itemMap.put(slot, itemId);
  }

  private ItemStack getItem(StrifeMob mob, EquipmentSlot slot) {
    String key = itemMap.get(slot);
    if (key.equalsIgnoreCase("reset")) {
      if (mob.getUniqueEntityId() == null) {
        return null;
      }
      UniqueEntity ue = getPlugin().getUniqueEntityManager().getUnique(mob.getUniqueEntityId());
      return getPlugin().getEquipmentManager().getItem(ue.getEquipment().get(slot));
    }
    return getPlugin().getEquipmentManager().getItem(key);
  }
}
