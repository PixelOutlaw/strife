package land.face.strife.data.conditions;

import land.face.strife.data.StrifeMob;

public class HeightCondition extends Condition {

  public boolean isMet(StrifeMob attacker, StrifeMob target) {
    if (getCompareTarget() == CompareTarget.SELF) {
      return attacker.getEntity().getLocation().getY() > target.getEntity().getLocation().getY();
    } else {
      return attacker.getEntity().getLocation().getY() < target.getEntity().getLocation().getY();
    }
  }
}
