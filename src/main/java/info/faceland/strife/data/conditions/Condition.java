package info.faceland.strife.data.conditions;

import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.stats.StrifeStat;
import java.util.HashMap;
import java.util.Map;

public abstract class Condition {

  private CompareTarget compareTarget;
  private Comparison comparison;
  private ConditionType type;
  private float value;

  private final Map<StrifeStat, Float> statMults = new HashMap<>();

  public boolean isMet(StrifeMob attacker, StrifeMob target) {
    return true;
  }

  public CompareTarget getCompareTarget() {
    return compareTarget;
  }

  public void setCompareTarget(CompareTarget compareTarget) {
    this.compareTarget = compareTarget;
  }

  public Comparison getComparison() {
    return comparison;
  }

  public void setComparison(Comparison comparison) {
    this.comparison = comparison;
  }

  public ConditionType getType() {
    return type;
  }

  public void setType(ConditionType type) {
    this.type = type;
  }

  public float getValue() {
    return value;
  }

  public void setValue(float value) {
    this.value = value;
  }

  public Map<StrifeStat, Float> getStatMults() {
    return statMults;
  }

  public void setStatMults(Map<StrifeStat, Float> statMults) {
    this.statMults.clear();
    this.statMults.putAll(statMults);
  }

  public enum Comparison {
    GREATER_THAN,
    LESS_THAN,
    EQUAL,
    NONE
  }

  public enum CompareTarget {
    SELF,
    OTHER
  }

  public enum ConditionType {
    ATTRIBUTE,
    EQUIPMENT,
    BUFF,
    ENDLESS_EFFECT,
    BLOCKING,
    MOVING,
    IN_COMBAT,
    CHANCE,
    STAT,
    LIGHT_LEVEL,
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
    BURNING,
    EARTH_RUNES
  }
}
