package info.faceland.strife.effects;

import info.faceland.strife.StrifePlugin;
import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.data.WorldSpaceEffectEntity;
import info.faceland.strife.util.DamageUtil;
import info.faceland.strife.util.DamageUtil.OriginLocation;
import info.faceland.strife.util.LogUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateWorldSpaceEntity extends Effect {

  private final Map<Integer, List<Effect>> cachedEffectSchedule = new HashMap<>();
  private Map<Integer, List<String>> effectSchedule;
  private OriginLocation originLocation;
  private int maxTicks;
  private double velocity;
  private int lifespan;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    cacheEffects();
    LogUtil.printDebug(" Creating world space entity with effects " + cachedEffectSchedule);
    WorldSpaceEffectEntity entity = new WorldSpaceEffectEntity(caster, cachedEffectSchedule,
        DamageUtil.getOriginLocation(target.getEntity(), originLocation),
            caster.getEntity().getEyeLocation().getDirection().multiply(velocity), maxTicks,
            lifespan);
    StrifePlugin.getInstance().getEffectManager().addWorldSpaceEffectEntity(entity);
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