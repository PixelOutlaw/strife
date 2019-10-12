package land.face.strife.data.conditions;

import land.face.strife.data.StrifeMob;
import land.face.strife.util.PlayerDataUtil;

public class BonusLevelCondition extends Condition {

  public boolean isMet(StrifeMob attacker, StrifeMob target) {
    if (attacker.getChampion() != null) {
      return PlayerDataUtil.conditionCompare(getComparison(),
          attacker.getChampion().getBonusLevels(), Math.round(getValue()));
    }
    return false;
  }

  public CompareTarget getCompareTarget() {
    return CompareTarget.SELF;
  }
}
