package land.face.strife.data.conditions;

import land.face.strife.data.StrifeMob;
import land.face.strife.stats.StrifeStat;
import land.face.strife.stats.StrifeTrait;
import land.face.strife.util.PlayerDataUtil;
import land.face.strife.util.StatUtil;

public class BarrierCondition extends Condition {

  private final boolean percentage;

  public BarrierCondition(boolean percentage) {
    this.percentage = percentage;
  }

  public boolean isMet(StrifeMob caster, StrifeMob target) {
    double barrierValue;
    if (percentage) {
      if (getCompareTarget() == CompareTarget.SELF) {
        if (caster.getMaxBarrier() < 0.1 || caster.hasTrait(StrifeTrait.NO_BARRIER_ALLOWED)) {
          return PlayerDataUtil.conditionCompare(getComparison(), 0D, getValue());
        }
        barrierValue = caster.getBarrier() / caster.getMaxBarrier();
      } else {
        if (target.getMaxBarrier() < 0.1 || target.hasTrait(StrifeTrait.NO_BARRIER_ALLOWED)) {
          return PlayerDataUtil.conditionCompare(getComparison(), 0D, getValue());
        }
        barrierValue = target.getBarrier() / target.getMaxBarrier();
      }
    } else {
      barrierValue = getCompareTarget() == CompareTarget.SELF ? caster.getBarrier() : target.getBarrier();
    }
    return PlayerDataUtil.conditionCompare(getComparison(), barrierValue, getValue());
  }
}
