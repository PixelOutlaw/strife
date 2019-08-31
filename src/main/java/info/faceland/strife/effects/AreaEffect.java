package info.faceland.strife.effects;

import info.faceland.strife.StrifePlugin;
import info.faceland.strife.data.HitData;
import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.util.DamageUtil.AbilityMod;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AreaEffect extends Effect {

  private Map<UUID, List<HitData>> targetDelay = new HashMap<>();
  private long lastApplication = System.currentTimeMillis();

  private List<Effect> cachedEffects = new ArrayList<>();
  private List<String> effects = new ArrayList<>();
  private double range;
  private int maxTargets;
  private boolean isLineOfSight;
  private boolean canBeEvaded;
  private boolean canBeBlocked;
  private final Map<AbilityMod, Double> attackModifiers = new HashMap<>();
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
    if (canBeBlocked) {

    }
    if (canBeEvaded) {

    }
    for (Effect effect : getEffects()) {
      StrifePlugin.getInstance().getEffectManager().execute(effect, caster, target.getEntity());
    }
  }

  public List<Effect> getEffects() {
    if (cachedEffects.isEmpty()) {
      for (String effect : effects) {
        cachedEffects.add(StrifePlugin.getInstance().getEffectManager().getEffect(effect));
      }
    }
    return cachedEffects;
  }

  public void setCanBeEvaded(boolean canBeEvaded) {
    this.canBeEvaded = canBeEvaded;
  }

  public void setCanBeBlocked(boolean canBeBlocked) {
    this.canBeBlocked = canBeBlocked;
  }

  public void setEffects(List<String> effects) {
    this.effects = effects;
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

  public Map<AbilityMod, Double> getAttackModifiers() {
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
}
