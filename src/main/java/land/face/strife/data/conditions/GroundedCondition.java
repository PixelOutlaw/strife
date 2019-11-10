package land.face.strife.data.conditions;

import land.face.strife.data.StrifeMob;
import land.face.strife.util.MoveUtil;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class GroundedCondition extends Condition {

  private boolean inverted;

  public GroundedCondition(boolean inverted) {
    this.inverted = inverted;
  }

  public boolean isMet(StrifeMob caster, StrifeMob target) {
    if (getCompareTarget() == CompareTarget.SELF) {
      return isGroundedOrCloseToGround(caster.getEntity()) == !inverted;
    } else {
      return isGroundedOrCloseToGround(target.getEntity()) == !inverted;
    }
  }

  // For fluid gameplay, treat being recently on the ground
  // as grounded for players, so small falls and knockbacks
  // don't disable ground based conditions
  private boolean isGroundedOrCloseToGround(LivingEntity le) {
    return le.isOnGround() || (le instanceof Player && MoveUtil.lastGroundTime((Player) le) < 100);
  }
}
