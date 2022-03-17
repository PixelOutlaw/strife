package land.face.strife.data.ability;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import land.face.strife.data.conditions.Condition;
import land.face.strife.data.effects.Effect;
import land.face.strife.managers.AbilityManager.AbilityType;
import land.face.strife.stats.StrifeStat;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public class Ability {

  private final String id;
  private final String name;
  private final TargetType targetType;
  @Getter
  private final AbilityType castType;
  private final boolean raycastsTargetEntities;
  private final boolean requireTarget;
  private final boolean cancelStealth;
  private final float range;
  private final float cost;
  private final List<Effect> effects;
  private final List<Effect> toggleOffEffects;
  @Getter
  private final boolean deathUntoggle;
  private final int cooldown;
  private final int maxCharges;
  private final int globalCooldownTicks;
  private final boolean showMessages;
  private final Set<Condition> conditions;
  private final AbilityIconData abilityIconData;
  @Getter
  private final Map<StrifeStat, Float> passiveStats = new HashMap<>();
  @Getter
  private final Map<StrifeStat, Float> togglePassiveStats = new HashMap<>();
  @Getter
  private final boolean passiveStatsOnCooldown;
  private final boolean friendly;

  public Ability(String id, String name, List<Effect> effects, List<Effect> toggleOffEffects,
      AbilityType castType, TargetType targetType, float range, float cost, int cooldown,
      int maxCharges, int globalCooldownTicks, boolean showMsgs, boolean requireTarget,
      boolean raycastsTargetEntities, Set<Condition> conditions, boolean passiveStatsOnCooldown,
      boolean friendly, AbilityIconData abilityIconData, boolean cancelStealth,
      boolean deathUntoggle) {
    this.id = id;
    this.name = name;
    this.cooldown = cooldown;
    this.maxCharges = maxCharges;
    this.globalCooldownTicks = globalCooldownTicks;
    this.effects = effects;
    this.toggleOffEffects = toggleOffEffects;
    this.targetType = targetType;
    this.castType = castType;
    this.requireTarget = requireTarget;
    this.raycastsTargetEntities = raycastsTargetEntities;
    this.range = range;
    this.cost = cost;
    this.showMessages = showMsgs;
    this.conditions = conditions;
    this.passiveStatsOnCooldown = passiveStatsOnCooldown;
    this.abilityIconData = abilityIconData;
    this.friendly = friendly;
    this.cancelStealth = cancelStealth;
    this.deathUntoggle = deathUntoggle;
  }

  @NotNull
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

  public float getCost() {
    return cost;
  }

  public List<Effect> getEffects() {
    return effects;
  }

  public List<Effect> getToggleOffEffects() {
    return toggleOffEffects;
  }

  public boolean isCancelStealth() {
    return cancelStealth;
  }

  public boolean isRequireTarget() {
    return requireTarget;
  }

  public double getCooldown() {
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
