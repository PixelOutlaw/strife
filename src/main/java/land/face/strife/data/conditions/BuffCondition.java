package land.face.strife.data.conditions;

import land.face.strife.data.StrifeMob;
import land.face.strife.util.PlayerDataUtil;

public class BuffCondition extends Condition {

  private final String buffId;
  private int stacks;

  public BuffCondition(String buffId, int stacks) {
    this.buffId = buffId;
    this.stacks = stacks;
  }

  public boolean isMet(StrifeMob attacker, StrifeMob target) {
    StrifeMob actualTarget = getCompareTarget() == CompareTarget.SELF ? attacker : target;
    int buffStacks = actualTarget.getBuffStacks(buffId, attacker.getEntity().getUniqueId());
    return PlayerDataUtil.conditionCompare(getComparison(), buffStacks, stacks);
  }
}
