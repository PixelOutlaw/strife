package land.face.strife.data.conditions;

import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.managers.RageManager;
import land.face.strife.stats.StrifeStat;
import land.face.strife.util.PlayerDataUtil;

public class RageCondition extends Condition {

  private final boolean percentage;

  private final RageManager rageManager = StrifePlugin.getInstance().getRageManager();

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
        rageValue = rageManager.getRage(attacker.getEntity()) / attacker.getStat(StrifeStat.MAXIMUM_RAGE);
      } else {
        if (target.getStat(StrifeStat.MAXIMUM_RAGE) == 0D) {
          return PlayerDataUtil.conditionCompare(getComparison(), 0D, getValue());
        }
        rageValue = rageManager.getRage(target.getEntity()) / target.getStat(StrifeStat.MAXIMUM_RAGE);
      }
    } else {
      rageValue = getCompareTarget() == CompareTarget.SELF ? rageManager.getRage(attacker.getEntity())
          : rageManager.getRage(target.getEntity());
    }
    return PlayerDataUtil.conditionCompare(getComparison(), rageValue, getValue());
  }
}
