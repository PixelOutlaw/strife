package land.face.strife.managers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.WorldSpaceEffect;
import land.face.strife.data.effects.Effect;
import land.face.strife.stats.StrifeStat;
import land.face.strife.util.LogUtil;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public class WSEManager {

  private final Set<WorldSpaceEffect> worldSpaceEffects;

  public WSEManager() {
    this.worldSpaceEffects = new HashSet<>();
  }

  public void createAtTarget(StrifeMob caster, Location location, int lifespan, float gravity, float friction,
      int maxTicks, double speed, float maxDisplacement, Map<Integer, List<Effect>> effects, boolean strictDuration,
      boolean zeroVertical, String modelEffect, int maxFallTicks, boolean destroyOnContact) {
    LogUtil.printDebug(" Creating world space entity with effects " + effects);
    double newLifeSpan = lifespan;
    if (!strictDuration) {
      newLifeSpan *= 1 + caster.getStat(StrifeStat.EFFECT_DURATION) / 100;
    }
    Vector direction = caster.getEntity().getEyeLocation().getDirection().clone();
    if (zeroVertical) {
      direction.setY(0.00001);
      direction.normalize();
    }
    direction.multiply(speed);
    WorldSpaceEffect entity = new WorldSpaceEffect(caster, effects, location, direction, gravity,
        friction, maxDisplacement, maxTicks, (int) newLifeSpan, modelEffect, maxFallTicks, destroyOnContact);
    addWorldSpaceEffectEntity(entity);
  }

  public void tickAllWorldSpaceEffects() {
    List<WorldSpaceEffect> expiredEffects = new ArrayList<>();
    for (WorldSpaceEffect worldSpaceEffect : worldSpaceEffects) {
      boolean isAlive = worldSpaceEffect.tick();
      if (!isAlive) {
        if (worldSpaceEffect.getStand() != null) {
          worldSpaceEffect.getStand().remove();
        }
        expiredEffects.add(worldSpaceEffect);
      }
    }
    for (WorldSpaceEffect effect : expiredEffects) {
      LogUtil.printDebug(" - Remove expired worldspace entity from effect manager");
      worldSpaceEffects.remove(effect);
    }
  }

  private void addWorldSpaceEffectEntity(WorldSpaceEffect worldSpaceEffect) {
    LogUtil.printDebug(" - Added worldspace entity to effect manager");
    worldSpaceEffects.add(worldSpaceEffect);
  }
}
