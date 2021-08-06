package land.face.strife.data.conditions;

import land.face.strife.data.StrifeMob;

public class FlyingCondition extends Condition {

  public boolean isMet(StrifeMob attacker, StrifeMob target) {
    if (getCompareTarget() == CompareTarget.SELF) {
      target = attacker;
    }
    return target.getEntity().isGliding();
  }
}
