package land.face.strife.data.conditions;

import java.util.Set;
import land.face.strife.data.EquipmentItemData;
import land.face.strife.data.StrifeMob;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;

public class WeaponsCondition extends Condition {

  private final Set<EquipmentItemData> validMaterials;
  private final boolean strict;

  public WeaponsCondition(Set<EquipmentItemData> validMaterials, boolean strict) {
    this.validMaterials = validMaterials;
    this.strict = strict;
  }

  public boolean isMet(StrifeMob attacker, StrifeMob target) {
    LivingEntity entity;
    if (getCompareTarget() == CompareTarget.SELF) {
      entity = attacker.getEntity();
    } else {
      entity = target.getEntity();
    }
    if (entity.getEquipment() == null) {
      return false;
    }
    EntityEquipment equipment = entity.getEquipment();
    return strict ? hasAllWeaponsInSet(equipment) : hasAtLeastOneWeaponInSet(equipment);
  }

  private boolean hasAtLeastOneWeaponInSet(EntityEquipment equipment) {
    return EquipmentItemData.contains(validMaterials, equipment.getItemInMainHand())
        || EquipmentItemData.contains(validMaterials, equipment.getItemInOffHand());
  }

  private boolean hasAllWeaponsInSet(EntityEquipment equipment) {
    return EquipmentItemData.contains(validMaterials, equipment.getItemInMainHand())
        && EquipmentItemData.contains(validMaterials, equipment.getItemInOffHand());
  }

  public CompareTarget getCompareTarget() {
    return CompareTarget.SELF;
  }
}
