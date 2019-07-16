package info.faceland.strife.effects;

import info.faceland.strife.conditions.Condition;
import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.stats.StrifeStat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Effect {

  private String name;
  private boolean forceTargetCaster;
  private boolean friendly;
  private double range;

  private final Map<StrifeStat, Double> statMults = new HashMap<>();
  private final Set<Condition> conditions = new HashSet<>();

  public void apply(StrifeMob caster, StrifeMob target) {

  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public boolean isForceTargetCaster() {
    return forceTargetCaster;
  }

  public void setForceTargetCaster(boolean forceTargetCaster) {
    this.forceTargetCaster = forceTargetCaster;
  }

  public boolean isFriendly() {
    return friendly;
  }

  public void setFriendly(boolean friendly) {
    this.friendly = friendly;
  }

  public double getRange() {
    return range;
  }

  public void setRange(double range) {
    this.range = range;
  }

  public Map<StrifeStat, Double> getStatMults() {
    return statMults;
  }

  public void setStatMults(Map<StrifeStat, Double> statMults) {
    this.statMults.clear();
    this.statMults.putAll(statMults);
  }

  public void addCondition(Condition condition) {
    conditions.add(condition);
  }

  public Set<Condition> getConditions() {
    return conditions;
  }
}
