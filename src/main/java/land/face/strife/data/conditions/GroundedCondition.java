package land.face.strife.data.conditions;

import land.face.strife.data.StrifeMob;
import land.face.strife.util.MoveUtil;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class GroundedCondition extends Condition {

  private boolean strict;

  public GroundedCondition(boolean strict) {
    this.strict = strict;
  }

  public boolean isMet(StrifeMob caster, StrifeMob target) {
    if (getCompareTarget() == CompareTarget.SELF) {
      if (strict) {
        return caster.getEntity().isOnGround();
      } else {
        return isGroundedOrCloseToGround(caster.getEntity());
      }
    } else {
      if (strict) {
        return target.getEntity().isOnGround();
      } else {
        return isGroundedOrCloseToGround(target.getEntity());
      }
    }
  }

  // For fluid gameplay, treat being recently on the ground
  // as grounded for players, so small falls and knockbacks
  // don't disable ground based conditions
  private boolean isGroundedOrCloseToGround(LivingEntity le) {
    return le.isOnGround() || (le instanceof Player && MoveUtil.timeOffGround((Player) le) <= 151);
  }
}
