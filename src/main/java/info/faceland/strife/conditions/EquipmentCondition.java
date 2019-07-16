package info.faceland.strife.conditions;

import info.faceland.strife.data.StrifeMob;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.inventory.EntityEquipment;

public class EquipmentCondition implements Condition {

  private final Set<Material> validMaterials;
  private final boolean strict;

  public EquipmentCondition(Set<Material> validMaterials, boolean strict) {
    this.validMaterials = validMaterials;
    this.strict = strict;
  }

  public boolean isMet(StrifeMob attacker, StrifeMob target) {
    if (target.getEntity().getEquipment() == null) {
      return false;
    }
    EntityEquipment equipment = target.getEntity().getEquipment();
    if (strict) {
      if (equipment.getItemInMainHand() == null || equipment.getItemInOffHand() == null) {
        return false;
      }
      return validMaterials.contains(equipment.getItemInMainHand().getType()) && validMaterials
          .contains(equipment.getItemInOffHand().getType());
    } else {
      if (equipment.getItemInMainHand() == null) {
        return equipment.getItemInOffHand() != null && validMaterials.contains(equipment.getItemInOffHand().getType());
      }
      if (equipment.getItemInOffHand() == null) {
        return equipment.getItemInMainHand() != null && validMaterials.contains(equipment.getItemInMainHand().getType());
      }
      return false;
    }
  }
}
