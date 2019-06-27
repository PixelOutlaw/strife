package info.faceland.strife.conditions;

import info.faceland.strife.data.AttributedEntity;
import info.faceland.strife.data.champion.StrifeStat;
import info.faceland.strife.util.PlayerDataUtil;

public class StatCondition implements Condition {

  private final StrifeStat strifeStat;
  private final CompareTarget compareTarget;
  private final Comparison comparison;
  private final double value;

  public StatCondition(StrifeStat strifeStat, CompareTarget compareTarget, Comparison comparison,
      double value) {
    this.strifeStat = strifeStat;
    this.compareTarget = compareTarget;
    this.comparison = comparison;
    this.value = value;
  }

  public boolean isMet(AttributedEntity attacker, AttributedEntity target) {
    if (compareTarget == CompareTarget.SELF && attacker.getChampion() == null ||
        compareTarget == CompareTarget.OTHER && target.getChampion() == null) {
      return false;
    }
    int statValue =
        compareTarget == CompareTarget.SELF ? attacker.getChampion().getLevelMap().get(strifeStat)
            : target.getChampion().getLevelMap().get(strifeStat);
    return PlayerDataUtil.conditionCompare(comparison, statValue, value);
  }
}
