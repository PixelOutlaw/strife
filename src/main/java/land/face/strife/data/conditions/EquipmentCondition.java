package land.face.strife.data.conditions;

import java.util.Set;
import land.face.strife.data.StrifeMob;
import org.bukkit.Material;
import org.bukkit.inventory.EntityEquipment;

public class EquipmentCondition extends Condition {

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
    Material typeOne = equipment.getItemInMainHand().getType();
    Material typeTwo = equipment.getItemInOffHand().getType();
    return validMaterials.contains(typeOne) || validMaterials.contains(typeTwo);
  }

  private boolean hasAllWeaponsInSet(EntityEquipment equipment) {
    Material typeOne = equipment.getItemInMainHand().getType();
    Material typeTwo = equipment.getItemInOffHand().getType();
    return validMaterials.contains(typeOne) && validMaterials.contains(typeTwo);
  }

  public CompareTarget getCompareTarget() {
    return CompareTarget.SELF;
  }
}
