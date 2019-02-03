package info.faceland.strife.data.condition;

import info.faceland.strife.data.AttributedEntity;

public interface Condition {

  boolean isMet(AttributedEntity attacker, AttributedEntity target);

  enum Comparison {
    GREATER_THAN,
    LESS_THAN,
    EQUAL
  }

  enum ConditionType {
    ATTRIBUTE,
    STAT,
    HEALTH,
    BARRIER,
    HEALTH_PERCENT,
    BARRIER_PERCENT,
    POTION_EFFECT,
    LEVEL,
    BONUS_LEVEL,
    ITS_OVER_ANAKIN
  }
}
