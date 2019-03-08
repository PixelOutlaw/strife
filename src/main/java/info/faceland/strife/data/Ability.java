package info.faceland.strife.data;

import info.faceland.strife.conditions.Condition;
import info.faceland.strife.effects.Effect;
import java.util.List;
import java.util.Set;

public class Ability {

  private final String id;
  private final String name;
  private final TargetType targetType;
  private final double range;
  private final List<Effect> effects;
  private final int cooldown;
  private final boolean displayCd;
  private final Set<Condition> conditions;

  public Ability(String id, String name, List<Effect> effects, TargetType targetType, double range,
      int cooldown, boolean displayCd, Set<Condition> conditions) {
    this.id = id;
    this.name = name;
    this.cooldown = cooldown;
    this.effects = effects;
    this.targetType = targetType;
    this.range = range;
    this.displayCd = displayCd;
    this.conditions = conditions;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public TargetType getTargetType() {
    return targetType;
  }

  public double getRange() {
    return range;
  }

  public List<Effect> getEffects() {
    return effects;
  }

  public int getCooldown() {
    return cooldown;
  }

  public boolean isDisplayCd() {
    return displayCd;
  }

  public Set<Condition> getConditions() {
    return conditions;
  }

  public enum TargetType {
    SELF, OTHER, RANGE, NONE
  }
}
