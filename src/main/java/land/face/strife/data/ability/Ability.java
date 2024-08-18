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

@Getter @Setter
public class Ability {

  private final String id;
  private final String name;
  private boolean hidden = true;
  private final TargetType targetType;
  private final AbilityType castType;
  private final boolean raycastsTargetEntities;
  private final boolean requireTarget;
  private final boolean sneakSelfTarget;
  private final boolean cancelStealth;
  private final float range;
  private final float cost;
  private final float sneakCost;
  private final List<Effect> effects;
  private final List<Effect> sneakEffects;
  private final List<Effect> toggleOffEffects;
  private final boolean deathUntoggle;
  private final float cooldown;
  private final float sneakCooldown;
  private final float minCooldown;
  private final float weightScaling;
  private final int maxCharges;
  private final int globalCooldownTicks;
  private final boolean showMessages;
  private final Set<Condition> conditions;
  private final Set<Condition> sneakConditions;
  private final Set<Condition> filterConditions = new HashSet<>();
  private final AbilityIconData abilityIconData;
  private final Map<StrifeStat, Float> passiveStats = new HashMap<>();
  private final Map<StrifeStat, Float> togglePassiveStats = new HashMap<>();
  private final boolean passiveStatsOnCooldown;
  private final boolean friendly;
  private TargetingPriority targetingPriority;
  private int maxTargets = 1;

  public Ability(String id, String name, List<Effect> effects, List<Effect> sneakEffects, List<Effect> toggleOffEffects,
      AbilityType castType, TargetType targetType, boolean sneakSelfTarget, float range, float cost, float sneakCost,
      float cooldown, float sneakCooldown, int maxCharges, int globalCooldownTicks, boolean showMsgs,
      boolean requireTarget, boolean raycastsTargetEntities, Set<Condition> conditions, Set<Condition> sneakConditions,
      boolean passiveStatsOnCooldown, boolean friendly, AbilityIconData abilityIconData, boolean cancelStealth,
      boolean deathUntoggle, float minCooldown, float weightScaling) {
    this.id = id;
    this.name = name;
    this.cooldown = cooldown;
    this.sneakCooldown = sneakCooldown;
    this.maxCharges = maxCharges;
    this.globalCooldownTicks = globalCooldownTicks;
    this.effects = effects;
    this.toggleOffEffects = toggleOffEffects;
    this.targetType = targetType;
    this.castType = castType;
    this.sneakEffects = sneakEffects;
    this.requireTarget = requireTarget;
    this.sneakSelfTarget = sneakSelfTarget;
    this.raycastsTargetEntities = raycastsTargetEntities;
    this.range = range;
    this.cost = cost;
    this.sneakCost = sneakCost;
    this.showMessages = showMsgs;
    this.conditions = conditions;
    this.sneakConditions = sneakConditions;
    this.passiveStatsOnCooldown = passiveStatsOnCooldown;
    this.abilityIconData = abilityIconData;
    this.friendly = friendly;
    this.cancelStealth = cancelStealth;
    this.deathUntoggle = deathUntoggle;
    this.minCooldown = minCooldown;
    this.weightScaling = weightScaling;
  }

  @NotNull
  public String getId() {
    return id;
  }

  public float calcRealEnergyCost(StrifeMob mob) {
    float cost = mob.getEntity().isSneaking() ? sneakCost : this.cost;
    if (cost >= 0) {
      // nothin
    } else {
      cost *= -1; // changes it to positive, because negative denotes percent
      float percentCost = cost % 1; // decimal remainder, for percent
      float flatCost = cost - percentCost;
      cost = mob.getMaxEnergy() * percentCost + flatCost;
    }
    if (weightScaling != 0) {
      float weightDiff = mob.getStat(StrifeStat.WEIGHT) - 30;
      //mob.getEntity().sendMessage("og cost: " + cost);
      //mob.getEntity().sendMessage("weight: " + weightDiff);
      if (weightDiff > 0) {
        float ratio = weightDiff / 30;
        cost *= 1f + (weightScaling * ratio);
        //mob.getEntity().sendMessage("ratio: " + ratio);
        //mob.getEntity().sendMessage("heavy scaling: " + cost);
      } else {
        float ratio = -weightDiff / 30;
        cost *= 1f - Math.min(0.75f, (weightScaling * ratio));
        //mob.getEntity().sendMessage("light scaling: " + cost);
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
