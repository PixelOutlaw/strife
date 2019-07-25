package info.faceland.strife.effects;

import info.faceland.strife.StrifePlugin;
import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.data.WorldSpaceEffectEntity;
import info.faceland.strife.managers.EffectManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateWorldSpaceEntity extends Effect {

  private final static EffectManager EFFECT_MANAGER = StrifePlugin.getInstance().getEffectManager();

  private final Map<Integer, List<Effect>> cachedEffectSchedule = new HashMap<>();

  private Map<Integer, List<String>> effectSchedule;
  private int maxTicks;
  private double velocity;
  private int lifespan;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    if (cachedEffectSchedule.isEmpty() && !effectSchedule.isEmpty()) {
      for (int i : effectSchedule.keySet()) {
        List<Effect> effectList = new ArrayList<>();
        for (String s : effectSchedule.get(i)) {
          effectList.add(EFFECT_MANAGER.getEffect(s));
        }
        cachedEffectSchedule.put(i, effectList);
      }
      effectSchedule.clear();
    }
    WorldSpaceEffectEntity entity = new WorldSpaceEffectEntity(caster, cachedEffectSchedule,
        caster.getEntity().getEyeLocation(),
        caster.getEntity().getEyeLocation().getDirection().multiply(velocity), maxTicks, lifespan);
    EFFECT_MANAGER.addWorldSpaceEffectEntity(entity);
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
}