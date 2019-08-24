package info.faceland.strife.conditions;

import info.faceland.strife.data.StrifeMob;

public interface Condition {

  boolean isMet(StrifeMob attacker, StrifeMob target);

  enum Comparison {
    GREATER_THAN,
    LESS_THAN,
    EQUAL,
    NONE
  }

  enum CompareTarget {
    SELF,
    OTHER
  }

  enum ConditionType {
    ATTRIBUTE,
    EQUIPMENT,
    BUFF,
    MOVING,
    CHANCE,
    STAT,
    HEALTH,
    BARRIER,
    POTION_EFFECT,
    TIME,
    LEVEL,
    BONUS_LEVEL,
    ITS_OVER_ANAKIN,
    ENTITY_TYPE,
    GROUNDED,
    BLEEDING,
    DARKNESS,
    BURNING
  }
}
