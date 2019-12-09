package land.face.strife.data.conditions;

import java.util.HashMap;
import java.util.Map;
import land.face.strife.data.StrifeMob;
import land.face.strife.stats.StrifeStat;

public abstract class Condition {

  private CompareTarget compareTarget;
  private Comparison comparison;
  private ConditionType type;
  private ConditionUser conditionUser;
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

  public ConditionUser getConditionUser() {
    return conditionUser;
  }

  public void setConditionUser(ConditionUser conditionUser) {
    this.conditionUser = conditionUser;
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

  public enum ConditionUser {
    PLAYER,
    MOB,
    ANY
  }

  public enum ConditionType {
    ATTRIBUTE,
    WEAPONS,
    BUFF,
    LORE,
    ENDLESS_EFFECT,
    BLOCKING,
    MOVING,
    NEARBY_ENTITIES,
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
    UNIQUE_ID,
    FACTION_MEMBER,
    VELOCITY,
    GROUNDED,
    BLEEDING,
    DARKNESS,
    RANGE,
    BURNING,
    EARTH_RUNES
  }
}
