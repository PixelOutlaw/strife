package land.face.strife.data.conditions;

import land.face.strife.data.StrifeMob;
import land.face.strife.util.PlayerDataUtil;

public class EarthRunesCondition extends Condition {

  public boolean isMet(StrifeMob attacker, StrifeMob target) {
    int runes;
    if (getCompareTarget() == CompareTarget.SELF) {
      runes = attacker.getEarthRunes();
    } else {
      runes = target.getEarthRunes();
    }
    return PlayerDataUtil.conditionCompare(getComparison(), runes, Math.round(getValue()));
  }
}
