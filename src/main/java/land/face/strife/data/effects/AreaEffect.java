package land.face.strife.data.effects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import land.face.strife.StrifePlugin;
import land.face.strife.data.HitData;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.TargetResponse;
import land.face.strife.data.conditions.Condition;
import land.face.strife.stats.StrifeStat;
import land.face.strife.util.DamageUtil;
import land.face.strife.util.DamageUtil.AbilityMod;
import land.face.strife.util.DamageUtil.AttackType;
import land.face.strife.util.PlayerDataUtil;
import land.face.strife.util.ProjectileUtil;
import land.face.strife.util.StatUtil;
import land.face.strife.util.TargetingUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;

public class AreaEffect extends LocationEffect {

  private final Map<UUID, List<HitData>> targetDelay = new HashMap<>();
  private final Map<AbilityMod, Float> attackModifiers = new HashMap<>();
  private final List<Effect> effects = new ArrayList<>();
  private final Set<Condition> filterConditions = new HashSet<>();

  private final Map<Integer, Float> circleMathCache = new HashMap<>();

  @Getter @Setter
  private AreaType areaType;
  @Getter @Setter
  private TargetingPriority priority;
  @Getter @Setter
  private LineOfSight lineOfSight;
  @Getter @Setter
  private boolean excludeCaster;
  @Getter
  private float range;
  @Getter @Setter
  private float radius;
  @Getter @Setter
  private float area;
  @Getter @Setter
  private int maxTargets;
  @Getter @Setter
  private boolean areaScaling;
  @Getter @Setter
  private boolean multishotScaling;
  @Getter @Setter
  private boolean canBeEvaded;
  @Getter @Setter
  private boolean canBeBlocked;
  @Getter @Setter
  private boolean canBeCountered;
  @Getter @Setter
  private long targetingCooldown;

  private long lastApplication = System.currentTimeMillis();

  public void apply(StrifeMob caster, StrifeMob target) {
    applyAtLocation(caster, TargetingUtil.getOriginLocation(target.getEntity(), getOrigin()));
  }

  @Override
  public void applyAtLocation(StrifeMob caster, Location location) {

    Set<LivingEntity> targets = getAreaEffectTargets(caster, location);
    if (excludeCaster) {
      targets.remove(caster.getEntity());
    }
    targets.removeIf(target -> ignoreEntity(caster, target));

    TargetResponse response = new TargetResponse(targets);
    for (Effect effect : effects) {
      if (effect instanceof ChaserEffect && ((ChaserEffect) effect).isCanLocationOverride()) {
        ((ChaserEffect) effect).setOverrideLocation(location);
      }
    }
    getPlugin().getEffectManager().processEffectList(caster, response, effects);
  }

  private boolean ignoreEntity(StrifeMob caster, LivingEntity le) {
    if (isDeflected(caster, StrifePlugin.getInstance().getStrifeMobManager().getStatMob(le))) {
      return true;
    }
    if (targetingCooldown > 0) {
      if (lastApplication < System.currentTimeMillis()) {
        targetDelay.clear();
      }
      lastApplication = System.currentTimeMillis() + targetingCooldown * 4L;
      return !canTargetBeHit(caster.getEntity().getUniqueId(), le.getUniqueId());
    }
    return false;
  }

  private Set<LivingEntity> getAreaEffectTargets(StrifeMob caster, Location location) {
    if (range < 0.1) {
      if (caster.getEntity() instanceof Mob) {
        range = (float) caster.getEntity().getAttribute(Attribute.GENERIC_FOLLOW_RANGE).getBaseValue();
      } else {
        range = 16;
      }
    }

    float searchRadius = range;
    float rangeModifier = 0f;
    if (multishotScaling) {
      rangeModifier += StatUtil.getStat(caster, StrifeStat.MULTISHOT) / 2;
    }
    if (areaScaling) {
      rangeModifier += StatUtil.getStat(caster, StrifeStat.AREA_OF_EFFECT);
    }
    if (rangeModifier != 0) {
      int bonus = (int) (rangeModifier - (rangeModifier % 5));
      if (!circleMathCache.containsKey(bonus)) {
        float bonusMult = 1 + ((float) bonus / 100);
        float newArea = area * bonusMult;
        float newRadius = (float) Math.sqrt(newArea / Math.PI);
        circleMathCache.put(bonus, newRadius);
        searchRadius = newRadius;
      } else {
        searchRadius = circleMathCache.get(bonus);
      }
    }

    Set<LivingEntity> areaTargets = new HashSet<>();
    switch (areaType) {
      case RADIUS -> areaTargets.addAll(TargetingUtil.getEntitiesInArea(location, searchRadius));
      case LINE -> areaTargets.addAll(TargetingUtil.getEntitiesInLine(location, searchRadius, radius));
      case CONE -> areaTargets.addAll(TargetingUtil
          .getEntitiesInCone(location, location.getDirection(), searchRadius, radius));
      case PARTY -> {
        if (caster.getEntity() instanceof Player) {
          areaTargets.addAll(StrifePlugin.getInstance().getSnazzyPartiesHook()
              .getNearbyPartyMembers((Player) caster.getEntity(), location, searchRadius));
        } else {
          areaTargets.addAll(
              TargetingUtil.getEntitiesInArea(caster.getEntity().getLocation(), searchRadius));
        }
      }
    }
    TargetingUtil.filterFriendlyEntities(areaTargets, caster, isFriendly());
    areaTargets.removeIf(e -> !PlayerDataUtil.areConditionsMet(caster,
        getPlugin().getStrifeMobManager().getStatMob(e), filterConditions));
    if (areaTargets.size() == 0) {
      return areaTargets;
    }
    switch (lineOfSight) {
      case CASTER -> areaTargets.removeIf(e ->
          !TargetingUtil.hasLineOfSight(caster.getEntity().getEyeLocation(),
              e.getEyeLocation(), e));
      case CENTER -> areaTargets.removeIf(
          e -> !TargetingUtil.hasLineOfSight(location, e.getEyeLocation(), e));
    }
    if (maxTargets > 0) {
      int numTargets = maxTargets;
      if (multishotScaling) {
        float mult = caster.getStat(StrifeStat.MULTISHOT) * (float) Math.pow(Math.random(), 1.15);
        numTargets = ProjectileUtil.getTotalProjectiles(numTargets, mult);
      }
      TargetingUtil.filterByTargetPriority(areaTargets, getPriority(), caster, Math.min(numTargets, areaTargets.size()));
    }
    return areaTargets;
  }

  private boolean isDeflected(StrifeMob caster, StrifeMob target) {
    if (canBeEvaded && DamageUtil.isEvaded(caster, target, attackModifiers)) {
      DamageUtil.doEvasion(caster, target);
      return true;
    }
    if (canBeCountered && getPlugin().getCounterManager()
        .executeCounters(caster.getEntity(), target.getEntity())) {
      return true;
    }
    return canBeBlocked && getPlugin().getBlockManager()
        .attemptBlock(caster, target, 1.0f, AttackType.AREA, false, false);
  }

  private boolean canTargetBeHit(UUID caster, UUID target) {
    if (!targetDelay.containsKey(caster)) {
      targetDelay.put(caster, new ArrayList<>());
      targetDelay.get(caster).add(new HitData(target, targetingCooldown));
      return true;
    }
    return bumpTargetData(caster, target);
  }

  private boolean bumpTargetData(UUID caster, UUID target) {
    for (HitData data : targetDelay.get(caster)) {
      if (data.getTarget() == target) {
        if (data.getTimeStamp() > System.currentTimeMillis()) {
          return false;
        }
        data.setTimeStamp(System.currentTimeMillis() + targetingCooldown);
        return true;
      }
    }
    targetDelay.get(caster).add(new HitData(target, targetingCooldown));
    return true;
  }

  public List<Effect> getEffects() {
    return effects;
  }

  public void setRange(double range) {
    this.range = (float) range;
    this.area = (float) (Math.PI * Math.pow(range, 2));
  }

  public Map<AbilityMod, Float> getAttackModifiers() {
    return attackModifiers;
  }

  public Set<Condition> getFilterConditions() {
    return filterConditions;
  }

  public enum LineOfSight {
    CASTER,
    CENTER,
    NONE
  }

  public enum AreaType {
    PARTY,
    RADIUS,
    LINE,
    CONE
  }

  public enum TargetingPriority {
    MOST_HEALTH,
    MOST_PERCENT_HEALTH,
    LEAST_HEALTH,
    LEAST_PERCENT_HEALTH,
    CLOSEST,
    FARTHEST,
    RANDOM
  }
}
