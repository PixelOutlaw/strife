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
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
@Builder
public class Ability {

  private final String id;
  private final String name;
  private final TargetType targetType;
  private final AbilityType castType;
  private final boolean raycastsTargetEntities, requireTarget, sneakSelfTarget, cancelStealth, passiveStatsOnCooldown,
      friendly, deathUntoggle, showMessages, healthCost, fullRecharge;
  private boolean hidden = true;
  private final float range, cost, sneakCost, cooldown, sneakCooldown, minCooldown, weightScaling;
  private final int maxCharges, globalCooldownTicks, sneakGlobalCooldownTicks, maxTargets;
  private final List<Effect> effects;
  private final List<Effect> sneakEffects;
  private final List<Effect> toggleOffEffects;
  private final Set<String> tags;
  private final Set<Condition> conditions;
  private final Set<Condition> sneakConditions;
  private final Set<Condition> filterConditions = new HashSet<>();
  private final Map<StrifeStat, Float> passiveStats = new HashMap<>();
  private final Map<StrifeStat, Float> togglePassiveStats = new HashMap<>();
  private final Map<Condition, Float> cooldownModifiers = new HashMap<>();
  private final Map<Condition, Float> costModifiers = new HashMap<>();
  private TargetingPriority targetingPriority;

  private final AbilityIconData abilityIconData;

  @NotNull
  public String getId() {
    return id;
  }

  public float calcRealCost(StrifeMob caster, StrifeMob target) {
    float cost = caster.getEntity().isSneaking() ? sneakCost : this.cost;
    if (cost >= 0) {
      // nothin
    } else {
      cost *= -1; // changes it to positive, because negative denotes percent
      float percentCost = cost % 1; // decimal remainder, for percent
      float flatCost = cost - percentCost;
      float resource = healthCost ? caster.getMaxLife() : caster.getMaxEnergy();
      cost = resource * percentCost + flatCost;
    }
    if (weightScaling != 0) {
      float weightRatio = caster.getStat(StrifeStat.WEIGHT) / 100;
      float difference = cost - (cost * weightRatio);
      cost += difference * weightScaling;
    }
    for (var entry : costModifiers.entrySet()) {
      if (entry.getKey().isMet(caster, target)) {
        cost *= entry.getValue();
      }
    }
    return cost;
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
