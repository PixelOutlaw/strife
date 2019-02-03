package info.faceland.strife.data.condition;

import info.faceland.strife.data.AttributedEntity;
import info.faceland.strife.stats.StrifeStat;

public class StatCondition implements Condition {

  private StrifeStat strifeStat;
  private Comparison comparison;
  private double value;

  public StatCondition(StrifeStat strifeStat, Comparison comparison, double value) {
    this.strifeStat = strifeStat;
    this.comparison = comparison;
    this.value = value;
  }

  public boolean isMet(AttributedEntity attacker, AttributedEntity target) {
    if (attacker.getChampion() == null) {
      return false;
    }
    switch (comparison) {
      case GREATER_THAN:
        return attacker.getChampion().getLevelMap().getOrDefault(strifeStat, 0) > value;
      case LESS_THAN:
        return attacker.getChampion().getLevelMap().getOrDefault(strifeStat, 0) < value;
      case EQUAL:
        return attacker.getChampion().getLevelMap().getOrDefault(strifeStat, 0) == value;
    }
    return false;
  }
}
