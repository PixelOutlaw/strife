package land.face.strife.managers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.WorldSpaceEffect;
import land.face.strife.data.effects.AreaEffect;
import land.face.strife.data.effects.Effect;
import land.face.strife.data.effects.LocationEffect;
import land.face.strife.data.effects.Push;
import land.face.strife.stats.StrifeStat;
import land.face.strife.util.LogUtil;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

public class WSEManager {

  private final Set<WorldSpaceEffect> worldSpaceEffects;

  public WSEManager() {
    this.worldSpaceEffects = new HashSet<>();
  }

  public void createAtTarget(StrifeMob caster, Location location, int lifespan, float gravity, float friction,
      int maxTicks, double speed, float maxDisplacement, Map<Integer, List<Effect>> effects, boolean strictDuration,
      boolean zeroVertical) {
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
    WorldSpaceEffect entity = new WorldSpaceEffect(caster, effects, location, direction,
        gravity, friction, maxDisplacement, maxTicks, (int) newLifeSpan);
    addWorldSpaceEffectEntity(entity);
  }

  public void tickAllWorldSpaceEffects() {
    List<WorldSpaceEffect> expiredEffects = new ArrayList<>();
    for (WorldSpaceEffect effect : worldSpaceEffects) {
      boolean isAlive = tick(effect);
      if (!isAlive) {
        expiredEffects.add(effect);
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

  private boolean tick(WorldSpaceEffect wse) {

    Location location = wse.getLocation().clone();
    Vector velocity = wse.getVelocity().clone();

    if (wse.getGravity() > 0) {
      Block blockBelow = location.clone().add(0, Math.min(-0.5, velocity.getY()), 0).getBlock();
      if (!blockBelow.getType().isSolid()) {
        if (!blockBelow.getLocation().add(0, -1, 0).getBlock().getType().isSolid() && !blockBelow.getLocation()
            .add(0, -2, 0).getBlock().getType().isSolid()) {
          LogUtil.printDebug("WSE effect cannot fall this far! Removing...");
          return false;
        }
        velocity.setY(Math.max(-0.99, velocity.getY() - wse.getGravity()));
      } else {
        velocity.setY(0);
        location.setY(blockBelow.getY() + 1.3);
      }
    }

    velocity.multiply(wse.getFriction());
    wse.setVelocity(velocity);
    location.add(velocity);
    location.setDirection(velocity);

    Block block = location.getBlock();
    int displacement = 0;
    while (block.getType().isSolid()) {
      displacement += 0.4;
      if (displacement >= wse.getMaxDisplacement()) {
        LogUtil.printDebug("WSE effect has hit a wall! Removing...");
        return false;
      }
      location.setY(location.getY() + 0.4);
      block = location.getBlock();
    }

    wse.setLocation(location);

    if (wse.getEffectSchedule().containsKey(wse.getCurrentTick())) {
      List<Effect> effects = wse.getEffectSchedule().get(wse.getCurrentTick());
      for (Effect effect : effects) {
        if (effect == null) {
          LogUtil.printError("Null WSE effect! Tick:" + wse.getCurrentTick());
          continue;
        }
        if (!(effect instanceof LocationEffect)) {
          LogUtil.printError("WSEs can only use effects with location! invalid: " + effect.getId());
          continue;
        }
        applyDirectionToPushEffects(wse, effect);
        ((LocationEffect) effect).applyAtLocation(wse.getCaster(), location);
      }
    }
    wse.setLifespan(wse.getLifespan() - 1);
    if (wse.getLifespan() < 0) {
      LogUtil.printDebug(" - WSE ran out of time! Removing...");
      return false;
    }
    wse.setCurrentTick(wse.getCurrentTick() + 1);
    if (wse.getCurrentTick() > wse.getMaxTicks()) {
      wse.setCurrentTick(1);
    }
    return true;
  }

  private void applyDirectionToPushEffects(WorldSpaceEffect wse, Effect effect) {
    if (!(effect instanceof AreaEffect)) {
      return;
    }
    for (Effect areaEffect : ((AreaEffect) effect).getEffects()) {
      if (areaEffect instanceof Push) {
        ((Push) areaEffect).setTempVectorFromWSE(wse);
      }
    }
  }
}
