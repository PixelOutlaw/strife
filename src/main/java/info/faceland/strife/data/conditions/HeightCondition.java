package info.faceland.strife.data.conditions;

import info.faceland.strife.data.StrifeMob;

public class HeightCondition implements Condition {

  private final CompareTarget compareTarget;

  public HeightCondition(CompareTarget compareTarget) {
    this.compareTarget = compareTarget;
  }

  public boolean isMet(StrifeMob attacker, StrifeMob target) {
    if (compareTarget == CompareTarget.SELF) {
      return attacker.getEntity().getLocation().getY() > target.getEntity().getLocation().getY();
    } else {
      return attacker.getEntity().getLocation().getY() < target.getEntity().getLocation().getY();
    }
  }

  public CompareTarget getCompareTarget() {
    return compareTarget;
  }
}
