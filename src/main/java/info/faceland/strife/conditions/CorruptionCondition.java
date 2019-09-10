package info.faceland.strife.conditions;

import info.faceland.strife.StrifePlugin;
import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.util.PlayerDataUtil;

public class CorruptionCondition implements Condition {

  private final CompareTarget compareTarget;
  private final Comparison comparison;
  private final double value;

  public CorruptionCondition(CompareTarget compareTarget, Comparison comparison, double value) {
    this.compareTarget = compareTarget;
    this.comparison = comparison;
    this.value = value;
  }

  public boolean isMet(StrifeMob attacker, StrifeMob target) {
    double stacks;
    if (compareTarget == CompareTarget.SELF) {
      stacks = StrifePlugin.getInstance().getDarknessManager().getCorruption(attacker.getEntity());
    } else {
      stacks = StrifePlugin.getInstance().getDarknessManager().getCorruption(target.getEntity());
    }
    return PlayerDataUtil.conditionCompare(comparison, stacks, value);
  }

  public CompareTarget getCompareTarget() {
    return compareTarget;
  }
}
