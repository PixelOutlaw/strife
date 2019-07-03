package info.faceland.strife.conditions;

import info.faceland.strife.StrifePlugin;
import info.faceland.strife.attributes.StrifeAttribute;
import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.managers.BarrierManager;
import info.faceland.strife.util.PlayerDataUtil;

public class BarrierCondition implements Condition {

  private final CompareTarget compareTarget;
  private final Comparison comparison;
  private final double value;
  private final boolean percentage;

  private static final BarrierManager barrierManager = StrifePlugin.getInstance()
      .getBarrierManager();

  public BarrierCondition(CompareTarget compareTarget, Comparison comparison, double value,
      boolean percentage) {
    this.compareTarget = compareTarget;
    this.comparison = comparison;
    this.value = value;
    this.percentage = percentage;
  }

  public boolean isMet(StrifeMob attacker, StrifeMob target) {
    double barrierValue;
    if (percentage) {
      if (compareTarget == CompareTarget.SELF) {
        if (attacker.getAttribute(StrifeAttribute.BARRIER) == 0D) {
          return PlayerDataUtil.conditionCompare(comparison, 0D, value);
        }
        barrierValue = barrierManager.getCurrentBarrier(attacker) / attacker.getAttribute(StrifeAttribute.BARRIER);
      } else {
        if (target.getAttribute(StrifeAttribute.BARRIER) == 0D) {
          return PlayerDataUtil.conditionCompare(comparison, 0D, value);
        }
        barrierValue = barrierManager.getCurrentBarrier(target) / target.getAttribute(StrifeAttribute.BARRIER);
      }
    } else {
      barrierValue = compareTarget == CompareTarget.SELF ?
          barrierManager.getCurrentBarrier(attacker) : barrierManager.getCurrentBarrier(target);
    }
    return PlayerDataUtil.conditionCompare(comparison, barrierValue, value);
  }
}
