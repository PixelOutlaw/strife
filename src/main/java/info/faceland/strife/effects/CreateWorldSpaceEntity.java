package info.faceland.strife.effects;

import info.faceland.strife.StrifePlugin;
import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.data.WorldSpaceEffectEntity;
import info.faceland.strife.util.DamageUtil;
import info.faceland.strife.util.LogUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Location;

public class CreateWorldSpaceEntity extends Effect {

  private final Map<Integer, List<Effect>> cachedEffectSchedule = new HashMap<>();
  private Map<Integer, List<String>> effectSchedule;
  private int maxTicks;
  private double velocity;
  private int lifespan;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    cacheEffects();
    Location loc;
    if (getRange() == 0) {
      loc = target.getEntity().getEyeLocation();
    } else if (target == null) {
      loc = DamageUtil.getTargetArea(caster.getEntity(), null, getRange());
    } else {
      loc = DamageUtil.getTargetArea(caster.getEntity(), target.getEntity(), getRange());
    }
    LogUtil.printDebug(" Creating world space entity with effects " + cachedEffectSchedule);
    WorldSpaceEffectEntity entity = new WorldSpaceEffectEntity(caster, cachedEffectSchedule, loc,
        caster.getEntity().getEyeLocation().getDirection().multiply(velocity), maxTicks, lifespan);
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