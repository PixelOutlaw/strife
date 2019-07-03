package info.faceland.strife.conditions;

import info.faceland.strife.data.StrifeMob;

public interface Condition {

  boolean isMet(StrifeMob attacker, StrifeMob target);

  enum Comparison {
    GREATER_THAN,
    LESS_THAN,
    EQUAL
  }

  enum CompareTarget {
    SELF,
    OTHER
  }

  enum ConditionType {
    ATTRIBUTE,
    CHANCE,
    STAT,
    HEALTH,
    BARRIER,
    POTION_EFFECT,
    LEVEL,
    BONUS_LEVEL,
    ITS_OVER_ANAKIN,
    ENTITY_TYPE,
    BLEEDING,
    DARKNESS,
    BURNING
  }
}
