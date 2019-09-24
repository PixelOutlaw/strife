package info.faceland.strife.data.conditions;

import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.util.PlayerDataUtil;

public class LightCondition extends Condition {

  public boolean isMet(StrifeMob attacker, StrifeMob target) {
    int lightLevel = getCompareTarget() == CompareTarget.SELF ? attacker.getEntity().getLocation().getBlock().getLightLevel() : target.getEntity().getLocation().getBlock().getLightLevel();
    return PlayerDataUtil.conditionCompare(getComparison(), lightLevel, Math.round(getValue()));
  }
}
