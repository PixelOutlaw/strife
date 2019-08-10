package info.faceland.strife.data.ability;

import info.faceland.strife.conditions.Condition;
import info.faceland.strife.data.AbilityIconData;
import info.faceland.strife.effects.Effect;
import info.faceland.strife.effects.PlaySound;
import info.faceland.strife.effects.SpawnParticle;
import java.util.List;
import java.util.Set;

public class Ability {

  private final String id;
  private final String name;
  private final TargetType targetType;
  private final double range;
  private final double radius;
  private final List<Effect> effects;
  private final int cooldown;
  private final boolean showMessages;
  private final Set<Condition> conditions;
  private final AbilityIconData abilityIconData;
  private final SpawnParticle abilityParticle;
  private final PlaySound castSound;

  public Ability(String id, String name, List<Effect> effects, TargetType targetType, double range,
      double radius, int cooldown, boolean showMsgs, Set<Condition> conditions,
      AbilityIconData abilityIconData, SpawnParticle abilityParticle, PlaySound castSound) {
    this.id = id;
    this.name = name;
    this.cooldown = cooldown;
    this.effects = effects;
    this.targetType = targetType;
    this.range = range;
    this.radius = radius;
    this.showMessages = showMsgs;
    this.conditions = conditions;
    this.abilityIconData = abilityIconData;
    this.abilityParticle = abilityParticle;
    this.castSound = castSound;
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

  public boolean isShowMessages() {
    return showMessages;
  }

  public Set<Condition> getConditions() {
    return conditions;
  }

  public AbilityIconData getAbilityIconData() {
    return abilityIconData;
  }

  public SpawnParticle getAbilityParticle() {
    return abilityParticle;
  }

  public double getRadius() {
    return radius;
  }

  public PlaySound getCastSound() {
    return castSound;
  }

  public enum TargetType {
    SELF, MINIONS, PARTY, SINGLE_OTHER, AREA_LINE, TARGET_AREA, TARGET_GROUND, AREA_RADIUS, NONE
  }
}
