package land.face.strife.data.conditions;

import land.face.strife.data.StrifeMob;
import land.face.strife.util.PlayerDataUtil;

public class AirJumpsCondition extends Condition {

  public boolean isMet(StrifeMob attacker, StrifeMob target) {
    int jumpz;
    if (getCompareTarget() == CompareTarget.SELF) {
      jumpz = attacker.getAirJumps();
    } else {
      jumpz = target.getAirJumps();
    }
    return PlayerDataUtil.conditionCompare(getComparison(), jumpz, Math.round(getValue()));
  }
}
