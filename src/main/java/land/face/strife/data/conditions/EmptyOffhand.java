package land.face.strife.data.conditions;

import static land.face.dinvy.windows.equipment.EquipmentMenu.DeluxeSlot.OFF_HAND;

import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;

public class EmptyOffhand extends Condition {

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
      var offhand = playerData.getEquipmentItem(OFF_HAND);
      return offhand == null || offhand.getType() == Material.AIR || offhand.getType() == Material.BARRIER;
    } else {
      var offhand = equipment.getItemInOffHand();
      return offhand == null || offhand.getType() == Material.AIR || offhand.getType() == Material.BARRIER;
    }
  }

  public CompareTarget getCompareTarget() {
    return CompareTarget.SELF;
  }
}
