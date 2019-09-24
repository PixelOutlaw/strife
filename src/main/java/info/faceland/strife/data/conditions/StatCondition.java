package info.faceland.strife.data.conditions;

import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.stats.StrifeStat;
import info.faceland.strife.util.PlayerDataUtil;

public class StatCondition extends Condition {

  private final StrifeStat strifeStat;

  public StatCondition(StrifeStat stat) {
    this.strifeStat = stat;
  }

  public boolean isMet(StrifeMob attacker, StrifeMob target) {
    long attributeValue = getCompareTarget() == CompareTarget.SELF ?
        Math.round(attacker.getStat(strifeStat)) :
        Math.round(target.getStat(strifeStat));
    return PlayerDataUtil.conditionCompare(getComparison(), (int) attributeValue, getValue());
  }
}
