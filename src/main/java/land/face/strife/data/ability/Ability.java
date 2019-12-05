package land.face.strife.data.ability;

import java.util.List;
import java.util.Set;
import land.face.strife.data.conditions.Condition;
import land.face.strife.data.effects.Effect;

public class Ability {

  private final String id;
  private final String name;
  private final TargetType targetType;
  private final boolean raycastsTargetEntities;
  private final boolean requireTarget;
  private final float range;
  private final List<Effect> effects;
  private final List<Effect> toggleOffEffects;
  private final int cooldown;
  private final int maxCharges;
  private final int globalCooldownTicks;
  private final boolean showMessages;
  private final Set<Condition> conditions;
  private final AbilityIconData abilityIconData;
  private final boolean friendly;

  public Ability(String id, String name, List<Effect> effects, List<Effect> toggleOffEffects,
      TargetType targetType, float range, int cooldown, int maxCharges, int globalCooldownTicks,
      boolean showMsgs, boolean requireTarget, boolean raycastsTargetEntities,
      Set<Condition> conditions, boolean friendly, AbilityIconData abilityIconData) {
    this.id = id;
    this.name = name;
    this.cooldown = cooldown;
    this.maxCharges = maxCharges;
    this.globalCooldownTicks = globalCooldownTicks;
    this.effects = effects;
    this.toggleOffEffects = toggleOffEffects;
    this.targetType = targetType;
    this.requireTarget = requireTarget;
    this.raycastsTargetEntities = raycastsTargetEntities;
    this.range = range;
    this.showMessages = showMsgs;
    this.conditions = conditions;
    this.abilityIconData = abilityIconData;
    this.friendly = friendly;
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

  public float getRange() {
    return range;
  }

  public List<Effect> getEffects() {
    return effects;
  }

  public List<Effect> getToggleOffEffects() {
    return toggleOffEffects;
  }

  public boolean isRequireTarget() {
    return requireTarget;
  }

  public int getCooldown() {
    return cooldown;
  }

  public int getMaxCharges() {
    return maxCharges;
  }

  public int getGlobalCooldownTicks() {
    return globalCooldownTicks;
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

  public boolean isFriendly() {
    return friendly;
  }

  public boolean isRaycastsTargetEntities() {
    return raycastsTargetEntities;
  }

  public enum TargetType {
    SELF, TOGGLE, MASTER, MINIONS, PARTY, SINGLE_OTHER, TARGET_AREA, TARGET_GROUND, NEAREST_SOUL, NONE
  }
}
