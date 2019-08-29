package info.faceland.strife.effects;

import info.faceland.strife.StrifePlugin;
import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.data.WorldSpaceEffectEntity;
import info.faceland.strife.stats.StrifeStat;
import info.faceland.strife.util.DamageUtil.OriginLocation;
import info.faceland.strife.util.LogUtil;
import info.faceland.strife.util.TargetingUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

public class CreateWorldSpaceEntity extends Effect {

  private final Map<Integer, List<Effect>> cachedEffectSchedule = new HashMap<>();
  private Map<Integer, List<String>> effectSchedule;
  private OriginLocation originLocation;
  private int maxTicks;
  private double velocity;
  private int lifespan;
  private boolean lockedToEntity;
  private boolean strictDuration;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    createAtEntity(caster, target.getEntity());
  }

  public void createAtEntity(StrifeMob caster, LivingEntity target) {
    cacheEffects();
    LogUtil.printDebug(" Creating world space entity with effects " + cachedEffectSchedule);
    double newLifeSpan = lifespan;
    if (!strictDuration) {
      newLifeSpan *= 1 + caster.getStat(StrifeStat.EFFECT_DURATION) / 100;
    }
    Location location;
    Vector direction;
    if (lockedToEntity) {
      location = TargetingUtil.getOriginLocation(target, originLocation);
      direction = null;
    } else {
      location = TargetingUtil.getOriginLocation(target, originLocation);
      direction = caster.getEntity().getEyeLocation().getDirection().multiply(velocity);
    }
    WorldSpaceEffectEntity entity = new WorldSpaceEffectEntity(caster, cachedEffectSchedule,
        location, lockedToEntity, direction, maxTicks, (int) newLifeSpan);
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

  public void setLockedToEntity(boolean lockedToEntity) {
    this.lockedToEntity = lockedToEntity;
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