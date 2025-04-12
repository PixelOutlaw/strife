package land.face.strife.data.conditions;

import java.util.List;
import java.util.Set;
import land.face.strife.data.StrifeMob;
import land.face.strife.util.ItemUtil;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;

public class WeaponsCondition extends Condition {

  private final List<String> validTiers;
  private final boolean strict;

  public WeaponsCondition(List<String> validTiers, boolean strict) {
    this.validTiers = validTiers;
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
    return ItemUtil.isType(equipment.getItemInMainHand(), validTiers) ||
        ItemUtil.isType(equipment.getItemInOffHand(), validTiers);
  }

  private boolean hasAllWeaponsInSet(EntityEquipment equipment) {
    return ItemUtil.isType(equipment.getItemInMainHand(), validTiers) &&
        ItemUtil.isType(equipment.getItemInOffHand(), validTiers);
  }

  public CompareTarget getCompareTarget() {
    return CompareTarget.SELF;
  }
}
