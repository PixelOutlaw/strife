package info.faceland.strife.data.effects;

import info.faceland.strife.data.conditions.Condition;
import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.stats.StrifeStat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Effect {

  private String id;
  private boolean forceTargetCaster;
  private boolean friendly;

  private final Map<StrifeStat, Float> statMults = new HashMap<>();
  private final Set<Condition> conditions = new HashSet<>();

  public void apply(StrifeMob caster, StrifeMob target) {

  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
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

  public Map<StrifeStat, Float> getStatMults() {
    return statMults;
  }

  public void setStatMults(Map<StrifeStat, Float> statMults) {
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
