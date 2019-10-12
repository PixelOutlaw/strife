package land.face.strife.data.effects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.util.DamageUtil.OriginLocation;
import land.face.strife.util.TargetingUtil;
import org.bukkit.entity.LivingEntity;

public class CreateWorldSpaceEntity extends Effect {

  private final Map<Integer, List<Effect>> cachedEffectSchedule = new HashMap<>();
  private Map<Integer, List<String>> effectSchedule;
  private OriginLocation originLocation;
  private int maxTicks;
  private double velocity;
  private int lifespan;
  private boolean strictDuration;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    cacheEffects();
    StrifePlugin.getInstance().getWseManager().createAtTarget(caster,
        TargetingUtil.getOriginLocation(target.getEntity(), originLocation), lifespan, maxTicks,
            velocity, cachedEffectSchedule, strictDuration);
  }

  public void apply(StrifeMob caster, LivingEntity target) {
    cacheEffects();
    StrifePlugin.getInstance().getWseManager().createAtTarget(caster,
        TargetingUtil.getOriginLocation(target, originLocation), lifespan, maxTicks,
        velocity, cachedEffectSchedule, strictDuration);
  }

  public void setEffectSchedule(Map<Integer, List<String>> effectSchedule) {
    this.effectSchedule = effectSchedule;
  }

  public void setMaxTicks(int maxTicks) {
    this.maxTicks = maxTicks;
  }

  public void setVelocity(double velocity) {
    this.velocity = velocity;
  }

  public void setLifespan(int lifespan) {
    this.lifespan = lifespan;
  }

  public void setOriginLocation(OriginLocation originLocation) {
    this.originLocation = originLocation;
  }

  public void setStrictDuration(boolean strictDuration) {
    this.strictDuration = strictDuration;
  }

  private void cacheEffects() {
    if (cachedEffectSchedule.isEmpty() && !effectSchedule.isEmpty()) {
      for (int i : effectSchedule.keySet()) {
        List<Effect> effectList = new ArrayList<>();
        for (String s : effectSchedule.get(i)) {
          effectList.add(StrifePlugin.getInstance().getEffectManager().getEffect(s));
        }
        cachedEffectSchedule.put(i, effectList);
      }
      effectSchedule.clear();
    }
  }
}