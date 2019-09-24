package info.faceland.strife.data.conditions;

import info.faceland.strife.data.StrifeMob;

public class HeightCondition extends Condition {

  public boolean isMet(StrifeMob attacker, StrifeMob target) {
    if (getCompareTarget() == CompareTarget.SELF) {
      return attacker.getEntity().getLocation().getY() > target.getEntity().getLocation().getY();
    } else {
      return attacker.getEntity().getLocation().getY() < target.getEntity().getLocation().getY();
    }
  }
}
