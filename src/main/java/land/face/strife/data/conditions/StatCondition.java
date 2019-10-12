package land.face.strife.data.conditions;

import land.face.strife.data.StrifeMob;
import land.face.strife.stats.StrifeStat;
import land.face.strife.util.PlayerDataUtil;

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
