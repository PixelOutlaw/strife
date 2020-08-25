package land.face.strife.data.conditions;

import land.face.strife.data.StrifeMob;

public class AliveCondition extends Condition {

  public boolean isMet(StrifeMob caster, StrifeMob target) {
    return getCompareTarget() == CompareTarget.SELF ? caster.getEntity().isValid() : target.getEntity().isValid();
  }
}
