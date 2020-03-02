package land.face.strife.data.conditions;

import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;

public class StealthCondition extends Condition {

  public boolean isMet(StrifeMob attacker, StrifeMob target) {
    if (getCompareTarget() == CompareTarget.SELF) {
      target = attacker;
    }
    return StrifePlugin.getInstance().getStealthManager().isStealthed(target.getEntity());
  }
}
