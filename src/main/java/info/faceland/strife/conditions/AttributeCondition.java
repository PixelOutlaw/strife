package info.faceland.strife.conditions;

import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.data.champion.StrifeAttribute;
import info.faceland.strife.util.PlayerDataUtil;

public class AttributeCondition implements Condition {

  private final StrifeAttribute strifeAttribute;
  private final CompareTarget compareTarget;
  private final Comparison comparison;
  private final double value;

  public AttributeCondition(StrifeAttribute strifeAttribute, CompareTarget compareTarget, Comparison comparison,
      double value) {
    this.strifeAttribute = strifeAttribute;
    this.compareTarget = compareTarget;
    this.comparison = comparison;
    this.value = value;
  }

  public boolean isMet(StrifeMob attacker, StrifeMob target) {
    if (compareTarget == CompareTarget.SELF && attacker.getChampion() == null ||
        compareTarget == CompareTarget.OTHER && target.getChampion() == null) {
      return false;
    }
    int statValue =
        compareTarget == CompareTarget.SELF ? attacker.getChampion().getLevelMap().get(
            strifeAttribute)
            : target.getChampion().getLevelMap().get(strifeAttribute);
    return PlayerDataUtil.conditionCompare(comparison, statValue, value);
  }
}
