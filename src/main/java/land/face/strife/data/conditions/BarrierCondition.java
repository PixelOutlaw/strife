package land.face.strife.data.conditions;

import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.managers.BarrierManager;
import land.face.strife.stats.StrifeStat;
import land.face.strife.stats.StrifeTrait;
import land.face.strife.util.PlayerDataUtil;
import land.face.strife.util.StatUtil;

public class BarrierCondition extends Condition {

  private final boolean percentage;

  private static final BarrierManager barrierManager = StrifePlugin.getInstance()
      .getBarrierManager();

  public BarrierCondition(boolean percentage) {
    this.percentage = percentage;
  }

  public boolean isMet(StrifeMob caster, StrifeMob target) {
    double barrierValue;
    if (percentage) {
      if (getCompareTarget() == CompareTarget.SELF) {
        if (caster.getStat(StrifeStat.BARRIER) < 0.1 || caster.hasTrait(StrifeTrait.NO_BARRIER_ALLOWED)) {
          return PlayerDataUtil.conditionCompare(getComparison(), 0D, getValue());
        }
        barrierValue = barrierManager.getCurrentBarrier(caster) / StatUtil.getMaximumBarrier(caster);
      } else {
        if (target.getStat(StrifeStat.BARRIER) < 0.1 || target.hasTrait(StrifeTrait.NO_BARRIER_ALLOWED)) {
          return PlayerDataUtil.conditionCompare(getComparison(), 0D, getValue());
        }
        barrierValue = barrierManager.getCurrentBarrier(target) / StatUtil.getMaximumBarrier(target);
      }
    } else {
      barrierValue = getCompareTarget() == CompareTarget.SELF ?
          barrierManager.getCurrentBarrier(caster) : barrierManager.getCurrentBarrier(target);
    }
    return PlayerDataUtil.conditionCompare(getComparison(), barrierValue, getValue());
  }
}
