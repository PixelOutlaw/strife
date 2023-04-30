package land.face.strife.data.ability;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.conditions.Condition;
import land.face.strife.data.effects.AreaEffect.TargetingPriority;
import land.face.strife.data.effects.Effect;
import land.face.strife.managers.AbilityManager.AbilityType;
import land.face.strife.stats.StrifeStat;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

public class Ability {

  private final String id;
  private final String name;
  @Getter @Setter
  private boolean hidden = true;
  private final TargetType targetType;
  @Getter
  private final AbilityType castType;
  private final boolean raycastsTargetEntities;
  @Getter
  private final boolean requireTarget;
  private final boolean cancelStealth;
  private final float range;
  private final float cost;
  private final List<Effect> effects;
  private final List<Effect> toggleOffEffects;
  @Getter
  private final boolean deathUntoggle;
  @Getter
  private final float cooldown;
  @Getter
  private final float minCooldown;
  private final int maxCharges;
  private final int globalCooldownTicks;
  private final boolean showMessages;
  private final Set<Condition> conditions;
  @Getter
  private final Set<Condition> filterConditions = new HashSet<>();
  private final AbilityIconData abilityIconData;
  @Getter
  private final Map<StrifeStat, Float> passiveStats = new HashMap<>();
  @Getter
  private final Map<StrifeStat, Float> togglePassiveStats = new HashMap<>();
  @Getter
  private final boolean passiveStatsOnCooldown;
  private final boolean friendly;
  @Getter @Setter
  private TargetingPriority targetingPriority;
  @Getter @Setter
  private int maxTargets = 1;

  public Ability(String id, String name, List<Effect> effects, List<Effect> toggleOffEffects,
      AbilityType castType, TargetType targetType, float range, float cost, float cooldown,
      int maxCharges, int globalCooldownTicks, boolean showMsgs, boolean requireTarget,
      boolean raycastsTargetEntities, Set<Condition> conditions, boolean passiveStatsOnCooldown,
      boolean friendly, AbilityIconData abilityIconData, boolean cancelStealth,
      boolean deathUntoggle, float minCooldown) {
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
    this.minCooldown = minCooldown;
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

  public List<Effect> getEffects() {
    return effects;
  }

  public List<Effect> getToggleOffEffects() {
    return toggleOffEffects;
  }

  public boolean isCancelStealth() {
    return cancelStealth;
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

  public float calcRealEnergyCost(StrifeMob mob) {
    if (cost >= 0) {
      return cost;
    } else {
      float percentCost = -cost % 1;
      return (cost - percentCost) + (mob.getMaxEnergy() * percentCost);
    }
  }

  public enum TargetType {
    SELF,
    TOGGLE,
    MASTER,
    MINIONS,
    PARTY,
    NEARBY_ENEMIES,
    NEARBY_ALLIES,
    SINGLE_OTHER,
    TARGET_AREA,
    TARGET_GROUND,
    NEAREST_SOUL,
    NONE
  }
}
