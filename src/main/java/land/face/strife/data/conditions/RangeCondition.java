package land.face.strife.data.conditions;

import land.face.strife.data.StrifeMob;
import land.face.strife.util.PlayerDataUtil;

public class RangeCondition extends Condition {

  // For CPU usage, this condition is intentionally basic
  // and only cares about the horizontal axis
  public boolean isMet(StrifeMob attacker, StrifeMob target) {
    double xRange = attacker.getEntity().getLocation().getX() - target.getEntity().getLocation().getX();
    double zRange = attacker.getEntity().getLocation().getZ() - target.getEntity().getLocation().getZ();
    double range = Math.abs(xRange) + Math.abs(zRange);
    return PlayerDataUtil.conditionCompare(getComparison(), range, getValue());
  }
}
