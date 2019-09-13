package info.faceland.strife.data.conditions;

import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.util.PlayerDataUtil;

public class BuffCondition implements Condition {

  private final CompareTarget compareTarget;
  private final Comparison comparison;
  private final String buffId;
  private int stacks;

  public BuffCondition(CompareTarget target, Comparison comparison, String buffId, int stacks) {
    this.compareTarget = target;
    this.comparison = comparison;
    this.buffId = buffId;
    this.stacks = stacks;
  }

  public boolean isMet(StrifeMob attacker, StrifeMob target) {
    StrifeMob actualTarget = compareTarget == CompareTarget.SELF ? attacker : target;
    int buffStacks = actualTarget.getBuffStacks(buffId);
    return PlayerDataUtil.conditionCompare(comparison, buffStacks, stacks);
  }

  public CompareTarget getCompareTarget() {
    return compareTarget;
  }
}
