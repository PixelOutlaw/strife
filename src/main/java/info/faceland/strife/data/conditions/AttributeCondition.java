package info.faceland.strife.data.conditions;

import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.data.champion.StrifeAttribute;
import info.faceland.strife.util.PlayerDataUtil;

public class AttributeCondition extends Condition {

  private final StrifeAttribute strifeAttribute;

  public AttributeCondition(StrifeAttribute strifeAttribute) {
    this.strifeAttribute = strifeAttribute;
  }

  public boolean isMet(StrifeMob attacker, StrifeMob target) {
    if (getCompareTarget() == CompareTarget.SELF && attacker.getChampion() == null ||
        getCompareTarget() == CompareTarget.OTHER && target.getChampion() == null) {
      return false;
    }
    int statValue = getCompareTarget() == CompareTarget.SELF ?
        attacker.getChampion().getLevelMap().get(strifeAttribute)
            : target.getChampion().getLevelMap().get(strifeAttribute);
    return PlayerDataUtil.conditionCompare(getComparison(), statValue, getValue());
  }
}
