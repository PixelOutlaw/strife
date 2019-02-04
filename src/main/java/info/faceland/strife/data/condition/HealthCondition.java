package info.faceland.strife.data.condition;

import info.faceland.strife.data.AttributedEntity;
import info.faceland.strife.util.PlayerDataUtil;

public class HealthCondition implements Condition {

  private final CompareTarget compareTarget;
  private final Comparison comparison;
  private final double value;
  private final boolean percentage;

  public HealthCondition(CompareTarget compareTarget, Comparison comparison, double value, boolean percentage) {
    this.compareTarget = compareTarget;
    this.comparison = comparison;
    this.value = value;
    this.percentage = percentage;
  }

  public boolean isMet(AttributedEntity attacker, AttributedEntity target) {
    double healthValue;
    if (percentage) {
      healthValue = compareTarget == CompareTarget.SELF ?
          attacker.getEntity().getHealth() / attacker.getEntity().getMaxHealth() :
          target.getEntity().getHealth() / target.getEntity().getMaxHealth();
    } else {
      healthValue = compareTarget == CompareTarget.SELF ?
          attacker.getEntity().getHealth() : target.getEntity().getHealth();
      healthValue = Math.round(healthValue);
    }
    return PlayerDataUtil.conditionCompare(comparison, healthValue, value);
  }
}
