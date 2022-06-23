package land.face.strife.data.conditions;

import land.face.strife.data.StrifeMob;

public class BleedingCondition extends Condition {

  private final boolean isBleeding;

  public BleedingCondition(boolean isBleeding) {
    this.isBleeding = isBleeding;
  }

  public boolean isMet(StrifeMob attacker, StrifeMob target) {
    if (getCompareTarget() == CompareTarget.SELF) {
      return attacker.isBleeding() == isBleeding;
    } else {
      return target.isBleeding() == isBleeding;
    }
  }
}
