package info.faceland.strife.data.conditions;

import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.util.PlayerDataUtil;

public class HealthCondition extends Condition {

  private final boolean percentage;

  public HealthCondition(boolean percentage) {
    this.percentage = percentage;
  }

  public boolean isMet(StrifeMob attacker, StrifeMob target) {
    double healthValue;
    if (percentage) {
      healthValue = getCompareTarget() == CompareTarget.SELF ?
          attacker.getEntity().getHealth() / attacker.getEntity().getMaxHealth() :
          target.getEntity().getHealth() / target.getEntity().getMaxHealth();
    } else {
      healthValue = getCompareTarget() == CompareTarget.SELF ?
          attacker.getEntity().getHealth() : target.getEntity().getHealth();
      healthValue = Math.round(healthValue);
    }
    return PlayerDataUtil.conditionCompare(getComparison(), healthValue, getValue());
  }
}
