package land.face.strife.data.conditions;

import land.face.strife.data.StrifeMob;
import land.face.strife.util.PlayerDataUtil;

public class HealthCondition extends Condition {

  private final boolean percentage;

  public HealthCondition(boolean percentage) {
    this.percentage = percentage;
  }

  public boolean isMet(StrifeMob attacker, StrifeMob target) {
    StrifeMob mob = getEntity(attacker, target);
    if (mob == null) {
      return false;
    }
    double healthValue;
    if (percentage) {
      healthValue = mob.getEntity().getHealth() / mob.getEntity().getMaxHealth();
    } else {
      healthValue = mob.getEntity().getHealth();
      healthValue = Math.round(healthValue);
    }
    return PlayerDataUtil.conditionCompare(getComparison(), healthValue, getValue());
  }
}
