package info.faceland.strife.data.conditions;

import info.faceland.strife.StrifePlugin;
import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.util.PlayerDataUtil;

public class CorruptionCondition extends Condition {

  public boolean isMet(StrifeMob attacker, StrifeMob target) {
    double stacks;
    if (getCompareTarget() == CompareTarget.SELF) {
      stacks = StrifePlugin.getInstance().getDarknessManager().getCorruption(attacker.getEntity());
    } else {
      stacks = StrifePlugin.getInstance().getDarknessManager().getCorruption(target.getEntity());
    }
    return PlayerDataUtil.conditionCompare(getComparison(), stacks, getValue());
  }
}
