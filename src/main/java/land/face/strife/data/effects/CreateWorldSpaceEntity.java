package land.face.strife.data.effects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.util.DamageUtil.OriginLocation;
import land.face.strife.util.TargetingUtil;
import lombok.Setter;
import org.bukkit.Location;

public class CreateWorldSpaceEntity extends LocationEffect {

  private final Map<Integer, List<Effect>> cachedEffectSchedule = new HashMap<>();
  private Map<Integer, List<String>> effectSchedule;
  private OriginLocation originLocation;
  private float gravity;
  private float friction;
  private int maxTicks;
  private double velocity;
  private int lifespan;
  private float maxDisplacement;
  private boolean strictDuration;
  private boolean zeroVerticalAxis;
  @Setter
  private String modelEffect;
  @Setter
  private int maxFallTicks;
  @Setter
  private boolean destroyOnContact;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    apply(caster, TargetingUtil.getOriginLocation(target.getEntity(), originLocation));
  }

  @Override
  public void applyAtLocation(StrifeMob caster, Location location) {
    apply(caster, location);
  }

  public void apply(StrifeMob caster, Location location) {
    cacheEffects();
    getPlugin().getWseManager().createAtTarget(caster, location, lifespan, gravity, friction,
        maxTicks, velocity, maxDisplacement, cachedEffectSchedule, strictDuration,
        zeroVerticalAxis, modelEffect, maxFallTicks, destroyOnContact);
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

  public void setGravity(float gravity) {
    this.gravity = gravity;
  }

  public void setFriction(float friction) {
    this.friction = friction;
  }

  public void setMaxDisplacement(float maxDisplacement) {
    this.maxDisplacement = maxDisplacement;
  }

  public void setStrictDuration(boolean strictDuration) {
    this.strictDuration = strictDuration;
  }

  public void setZeroVerticalAxis(boolean zeroVerticalAxis) {
    this.zeroVerticalAxis = zeroVerticalAxis;
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