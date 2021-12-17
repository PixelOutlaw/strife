package land.face.strife.data.conditions;

import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.util.PlayerDataUtil;

public class CorruptionCondition extends Condition {

  public boolean isMet(StrifeMob attacker, StrifeMob target) {
    double stacks;
    if (getCompareTarget() == CompareTarget.SELF) {
      stacks = attacker.getCorruption();
    } else {
      stacks = target.getCorruption();
    }
    return PlayerDataUtil.conditionCompare(getComparison(), stacks, getValue());
  }
}
