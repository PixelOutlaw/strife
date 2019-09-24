package info.faceland.strife.data.conditions;

import info.faceland.strife.data.StrifeMob;

public class GroundedCondition extends Condition {

  private boolean inverted;

  public GroundedCondition(boolean inverted) {
    this.inverted = inverted;
  }

  public boolean isMet(StrifeMob caster, StrifeMob target) {
    if (getCompareTarget() == CompareTarget.SELF) {
      return caster.getEntity().isOnGround() == !inverted;
    } else {
      return target.getEntity().isOnGround() == !inverted;
    }
  }
}
