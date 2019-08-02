package info.faceland.strife.conditions;

import info.faceland.strife.data.StrifeMob;

public class GroundedCondition implements Condition {

  private CompareTarget compareTarget;
  private boolean inverted;

  public GroundedCondition(CompareTarget compareTarget, boolean inverted) {
    this.compareTarget = compareTarget;
    this.inverted = inverted;
  }

  public boolean isMet(StrifeMob caster, StrifeMob target) {
    if (compareTarget == CompareTarget.SELF) {
      return caster.getEntity().isOnGround() == !inverted;
    } else {
      return target.getEntity().isOnGround() == !inverted;
    }
  }
}
