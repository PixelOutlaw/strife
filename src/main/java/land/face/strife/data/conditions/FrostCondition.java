package land.face.strife.data.conditions;

import land.face.strife.data.StrifeMob;
import land.face.strife.util.PlayerDataUtil;

public class FrostCondition extends Condition {

  public boolean isMet(StrifeMob caster, StrifeMob target) {
    double frost = getCompareTarget() == CompareTarget.SELF ?
        caster.getFrost() : target.getFrost();
    return PlayerDataUtil.conditionCompare(getComparison(), frost, getValue());
  }
}
