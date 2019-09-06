package info.faceland.strife.effects;

import static info.faceland.strife.util.DamageUtil.doEvasion;

import info.faceland.strife.StrifePlugin;
import info.faceland.strife.data.HitData;
import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.util.DamageUtil;
import info.faceland.strife.util.DamageUtil.AbilityMod;
import info.faceland.strife.util.DamageUtil.AttackType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AreaEffect extends Effect {

  private Map<UUID, List<HitData>> targetDelay = new HashMap<>();
  private long lastApplication = System.currentTimeMillis();

  private List<Effect> effects = new ArrayList<>();
  private AreaType areaType;
  private double range;
  private int maxTargets;
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
    if (canBeEvaded) {
      float evasionMultiplier = DamageUtil.getFullEvasionMult(caster, target, attackModifiers);
      if (evasionMultiplier < DamageUtil.EVASION_THRESHOLD) {
        doEvasion(caster.getEntity(), target.getEntity());
        return;
      }
    }
    if (canBeBlocked) {
      if (StrifePlugin.getInstance().getBlockManager()
          .isAttackBlocked(caster, target, 1.0f, AttackType.MAGIC, false)) {
        return;
      }
    }
    for (Effect effect : effects) {
      StrifePlugin.getInstance().getEffectManager().execute(effect, caster, target.getEntity());
    }
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

  public int getMaxTargets() {
    return maxTargets;
  }

  public void setMaxTargets(int maxTargets) {
    this.maxTargets = maxTargets;
  }

  public Map<AbilityMod, Float> getAttackModifiers() {
    return attackModifiers;
  }

  public void setTargetingCooldown(long targetingCooldown) {
    this.targetingCooldown = targetingCooldown;
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

  public enum AreaType {
    RADIUS,
    LINE
  }
}
