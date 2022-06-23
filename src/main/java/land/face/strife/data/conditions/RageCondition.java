package land.face.strife.data.conditions;

import land.face.strife.data.StrifeMob;
import land.face.strife.stats.StrifeStat;
import land.face.strife.util.PlayerDataUtil;

public class RageCondition extends Condition {

  private final boolean percentage;

  public RageCondition(boolean percentage) {
    this.percentage = percentage;
  }

  public boolean isMet(StrifeMob attacker, StrifeMob target) {
    double rageValue;
    if (percentage) {
      if (getCompareTarget() == CompareTarget.SELF) {
        if (attacker.getStat(StrifeStat.MAXIMUM_RAGE) == 0D) {
          return PlayerDataUtil.conditionCompare(getComparison(), 0D, getValue());
        }
        rageValue = attacker.getRage() / attacker.getMaxRage();
      } else {
        if (target.getStat(StrifeStat.MAXIMUM_RAGE) == 0D) {
          return PlayerDataUtil.conditionCompare(getComparison(), 0D, getValue());
        }
        rageValue = target.getRage() / target.getMaxRage();
      }
    } else {
      rageValue = getCompareTarget() == CompareTarget.SELF ? attacker.getRage() : target.getRage();
    }
    return PlayerDataUtil.conditionCompare(getComparison(), rageValue, getValue());
  }
}
