package info.faceland.strife.conditions;

import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.stats.StrifeStat;
import info.faceland.strife.util.PlayerDataUtil;

public class StatCondition implements Condition {

  private final StrifeStat strifeStat;
  private final CompareTarget compareTarget;
  private final Comparison comparison;
  private final double value;

  public StatCondition(StrifeStat attribute, CompareTarget compareTarget,
      Comparison comparison, double value) {
    this.strifeStat = attribute;
    this.compareTarget = compareTarget;
    this.comparison = comparison;
    this.value = value;
  }

  public boolean isMet(StrifeMob attacker, StrifeMob target) {
    long attributeValue = compareTarget == CompareTarget.SELF ?
        Math.round(attacker.getAttribute(strifeStat)) :
        Math.round(target.getAttribute(strifeStat));
    return PlayerDataUtil.conditionCompare(comparison, (int) attributeValue, value);
  }
}
