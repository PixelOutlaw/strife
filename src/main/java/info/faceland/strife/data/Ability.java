package info.faceland.strife.data;

import info.faceland.strife.effects.Effect;
import java.util.List;

public class Ability {

  private final String id;
  private final String name;
  private final TargetType targetType;
  private final double range;
  private final List<Effect> effects;
  private final int cooldown;

  public Ability(String id, String name, List<Effect> effects, TargetType targetType, double range, int cooldown) {
    this.id = id;
    this.name = name;
    this.cooldown = cooldown;
    this.effects = effects;
    this.targetType = targetType;
    this.range = range;
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

  public enum TargetType {
    SELF, OTHER, RANGE, NONE
  }
}
