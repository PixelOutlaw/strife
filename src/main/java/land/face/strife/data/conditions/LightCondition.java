package land.face.strife.data.conditions;

import land.face.strife.data.StrifeMob;
import land.face.strife.util.PlayerDataUtil;

public class LightCondition extends Condition {

  public boolean isMet(StrifeMob attacker, StrifeMob target) {
    int lightLevel = getCompareTarget() == CompareTarget.SELF ? attacker.getEntity().getLocation().getBlock().getLightLevel() : target.getEntity().getLocation().getBlock().getLightLevel();
    return PlayerDataUtil.conditionCompare(getComparison(), lightLevel, Math.round(getValue()));
  }
}
