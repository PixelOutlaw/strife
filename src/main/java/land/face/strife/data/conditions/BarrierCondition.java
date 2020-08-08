package land.face.strife.data.conditions;

import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.managers.BarrierManager;
import land.face.strife.stats.StrifeStat;
import land.face.strife.util.PlayerDataUtil;
import land.face.strife.util.StatUtil;

public class BarrierCondition extends Condition {

  private final boolean percentage;

  private static final BarrierManager barrierManager = StrifePlugin.getInstance()
      .getBarrierManager();

  public BarrierCondition(boolean percentage) {
    this.percentage = percentage;
  }

  public boolean isMet(StrifeMob attacker, StrifeMob target) {
    double barrierValue;
    if (percentage) {
      if (getCompareTarget() == CompareTarget.SELF) {
        if (attacker.getStat(StrifeStat.BARRIER) == 0D) {
          return PlayerDataUtil.conditionCompare(getComparison(), 0D, getValue());
        }
        barrierValue = barrierManager.getCurrentBarrier(attacker) / StatUtil.getMaximumBarrier(attacker);
      } else {
        if (target.getStat(StrifeStat.BARRIER) == 0D) {
          return PlayerDataUtil.conditionCompare(getComparison(), 0D, getValue());
        }
        barrierValue = barrierManager.getCurrentBarrier(target) / StatUtil.getMaximumBarrier(target);
      }
    } else {
      barrierValue = getCompareTarget() == CompareTarget.SELF ?
          barrierManager.getCurrentBarrier(attacker) : barrierManager.getCurrentBarrier(target);
    }
    return PlayerDataUtil.conditionCompare(getComparison(), barrierValue, getValue());
  }
}
