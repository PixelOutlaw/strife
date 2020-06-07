package land.face.strife.data.conditions;

import land.face.strife.data.StrifeMob;
import land.face.strife.util.PlayerDataUtil;

public class RangeCondition extends Condition {

  private double rangeSquared;

  public boolean isMet(StrifeMob attacker, StrifeMob target) {
    double distSquared = attacker.getEntity().getLocation().distanceSquared(target.getEntity().getLocation());
    return PlayerDataUtil.conditionCompare(getComparison(), distSquared, rangeSquared);
  }

  public void setRangeSquared(double rangeSquared) {
    this.rangeSquared = rangeSquared;
  }
}
