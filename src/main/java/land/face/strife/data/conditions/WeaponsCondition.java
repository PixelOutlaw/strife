package land.face.strife.data.conditions;

import static land.face.dinvy.windows.equipment.EquipmentMenu.DeluxeSlot.OFF_HAND;

import java.util.List;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.util.ItemUtil;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

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
    if (entity instanceof Player p) {
      var playerData = StrifePlugin.getInstance().getDeluxeInvyPlugin().getPlayerManager().getPlayerData(p);
      var mainHand = equipment.getItemInMainHand();
      var offHand = playerData.getEquipmentItem(OFF_HAND);
      offHand = ItemUtil.isValidOffhand(mainHand, offHand) ? offHand : null;
      return strict ? hasAllWeaponsInSet(mainHand, offHand) : hasAtLeastOneWeaponInSet(mainHand, offHand);
    } else {
      return strict ? hasAllWeaponsInSet(equipment.getItemInMainHand(), equipment.getItemInOffHand()) :
          hasAtLeastOneWeaponInSet(equipment.getItemInMainHand(), equipment.getItemInOffHand());
    }
  }

  private boolean hasAtLeastOneWeaponInSet(ItemStack mainHand, ItemStack offHand) {
    return ItemUtil.isType(mainHand, validTiers) || ItemUtil.isType(offHand, validTiers);
  }

  private boolean hasAllWeaponsInSet(ItemStack mainHand, ItemStack offHand) {
    return ItemUtil.isType(mainHand, validTiers) && ItemUtil.isType(offHand, validTiers);
  }

  public CompareTarget getCompareTarget() {
    return CompareTarget.SELF;
  }
}
