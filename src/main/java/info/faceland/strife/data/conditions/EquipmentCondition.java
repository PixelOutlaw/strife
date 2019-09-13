package info.faceland.strife.data.conditions;

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
    return strict ? hasAllWeaponsInSet(equipment) : hasAtLeastOneWeaponInSet(equipment);
  }

  private boolean hasAtLeastOneWeaponInSet(EntityEquipment equipment) {
    Material typeOne = equipment.getItemInMainHand() == null ? Material.AIR
        : equipment.getItemInMainHand().getType();
    Material typeTwo = equipment.getItemInOffHand() == null ? Material.AIR
        : equipment.getItemInOffHand().getType();
    return validMaterials.contains(typeOne) || validMaterials.contains(typeTwo);
  }

  private boolean hasAllWeaponsInSet(EntityEquipment equipment) {
    Material typeOne = equipment.getItemInMainHand() == null ? Material.AIR
        : equipment.getItemInMainHand().getType();
    Material typeTwo = equipment.getItemInOffHand() == null ? Material.AIR
        : equipment.getItemInOffHand().getType();
    return validMaterials.contains(typeOne) && validMaterials.contains(typeTwo);
  }

  public CompareTarget getCompareTarget() {
    return CompareTarget.SELF;
  }
}
