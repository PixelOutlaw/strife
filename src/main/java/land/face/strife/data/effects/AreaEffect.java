package land.face.strife.data.effects;

import static land.face.strife.listeners.StrifeDamageListener.buildMissIndicator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import land.face.strife.StrifePlugin;
import land.face.strife.data.HitData;
import land.face.strife.data.StrifeMob;
import land.face.strife.util.DamageUtil;
import land.face.strife.util.DamageUtil.AbilityMod;
import land.face.strife.util.DamageUtil.AttackType;
import land.face.strife.util.TargetingUtil;
import org.bukkit.entity.Player;

public class AreaEffect extends Effect {

  private Map<UUID, List<HitData>> targetDelay = new HashMap<>();
  private long lastApplication = System.currentTimeMillis();

  private List<Effect> effects = new ArrayList<>();
  private AreaType areaType;
  private TargetingPriority priority;
  private double range;
  private float maxConeRadius;
  private int maxTargets;
  private boolean scaleTargetsWithMultishot;
  private boolean isLineOfSight;
  private boolean canBeEvaded;
  private boolean canBeBlocked;
  private final Map<AbilityMod, Float> attackModifiers = new HashMap<>();
  private long targetingCooldown;

  public void apply(StrifeMob caster, StrifeMob target) {
    if (targetingCooldown > 0) {
      if (lastApplication < System.currentTimeMillis()) {
        targetDelay.clear();
      }
      lastApplication = System.currentTimeMillis() + targetingCooldown * 4L;
      if (!canTargetBeHit(caster.getEntity().getUniqueId(), target.getEntity().getUniqueId())) {
        return;
      }
    }
    if (!TargetingUtil.isFriendly(caster, target) && isCancelled(caster, target)) {
      return;
    }
    for (Effect effect : effects) {
      StrifePlugin.getInstance().getEffectManager().execute(effect, caster, target.getEntity());
    }
  }

  private boolean isCancelled(StrifeMob caster, StrifeMob target) {
    if (canBeEvaded) {
      float evasionMultiplier = DamageUtil.getFullEvasionMult(caster, target, attackModifiers);
      if (evasionMultiplier < DamageUtil.EVASION_THRESHOLD) {
        DamageUtil.doEvasion(caster.getEntity(), target.getEntity());
        return true;
      }
    }
    if (canBeBlocked) {
      if (StrifePlugin.getInstance().getCounterManager()
          .executeCounters(caster.getEntity(), target.getEntity())) {
        return true;
      }
      boolean blocked = StrifePlugin.getInstance().getBlockManager()
          .isAttackBlocked(caster, target, 1.0f, AttackType.MAGIC, false);
      if (blocked) {
        if (caster.getEntity() instanceof Player) {
          StrifePlugin.getInstance().getIndicatorManager().addIndicator(caster.getEntity(),
              target.getEntity(), buildMissIndicator((Player) caster.getEntity()), "Blocked");
        }
      }
      return blocked;
    }
    return false;
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

  public AreaType getAreaType() {
    return areaType;
  }

  public void setAreaType(AreaType areaType) {
    this.areaType = areaType;
  }

  public TargetingPriority getPriority() {
    return priority;
  }

  public void setPriority(TargetingPriority priority) {
    this.priority = priority;
  }

  public void setCanBeEvaded(boolean canBeEvaded) {
    this.canBeEvaded = canBeEvaded;
  }

  public void setCanBeBlocked(boolean canBeBlocked) {
    this.canBeBlocked = canBeBlocked;
  }

  public boolean isLineOfSight() {
    return isLineOfSight;
  }

  public void setLineOfSight(boolean lineOfSight) {
    isLineOfSight = lineOfSight;
  }

  public double getRange() {
    return range;
  }

  public void setRange(double range) {
    this.range = range;
  }

  public float getMaxConeRadius() {
    if (areaType != AreaType.CONE) {
      throw new IllegalStateException("You cannot get cone radius on non-cone aoes dingus");
    }
    return maxConeRadius;
  }

  public void setMaxConeRadius(float maxConeRadius) {
    this.maxConeRadius = maxConeRadius;
  }

  public int getMaxTargets() {
    return maxTargets;
  }

  public void setMaxTargets(int maxTargets) {
    this.maxTargets = maxTargets;
  }

  public boolean isScaleTargetsWithMultishot() {
    return scaleTargetsWithMultishot;
  }

  public void setScaleTargetsWithMultishot(boolean scaleTargetsWithMultishot) {
    this.scaleTargetsWithMultishot = scaleTargetsWithMultishot;
  }

  public Map<AbilityMod, Float> getAttackModifiers() {
    return attackModifiers;
  }

  public void setTargetingCooldown(long targetingCooldown) {
    this.targetingCooldown = targetingCooldown;
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
