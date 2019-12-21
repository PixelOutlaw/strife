package land.face.strife.managers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.WorldSpaceEffectEntity;
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

  private EffectManager effectManager;
  private Set<WorldSpaceEffectEntity> worldSpaceEffects;

  public WSEManager(EffectManager effectManager) {
    this.effectManager = effectManager;
    this.worldSpaceEffects = new HashSet<>();
  }

  public void createAtTarget(StrifeMob caster, Location location, int lifespan, int maxTicks,
      double speed, Map<Integer, List<Effect>> effects, boolean strictDuration) {
    LogUtil.printDebug(" Creating world space entity with effects " + effects);
    double newLifeSpan = lifespan;
    if (!strictDuration) {
      newLifeSpan *= 1 + caster.getStat(StrifeStat.EFFECT_DURATION) / 100;
    }
    Vector direction = caster.getEntity().getEyeLocation().getDirection().multiply(speed);
    WorldSpaceEffectEntity entity = new WorldSpaceEffectEntity(caster, effects, location, direction,
        maxTicks, (int) newLifeSpan);
    addWorldSpaceEffectEntity(entity);
  }

  public void tickAllWorldSpaceEffects() {
    List<WorldSpaceEffectEntity> expiredEffects = new ArrayList<>();
    for (WorldSpaceEffectEntity effect : worldSpaceEffects) {
      boolean isAlive = tick(effect);
      if (!isAlive) {
        expiredEffects.add(effect);
      }
    }
    for (WorldSpaceEffectEntity effect : expiredEffects) {
      LogUtil.printDebug(" - Remove expired worldspace entity from effect manager");
      worldSpaceEffects.remove(effect);
    }
  }

  private void addWorldSpaceEffectEntity(WorldSpaceEffectEntity worldSpaceEffectEntity) {
    LogUtil.printDebug(" - Added worldspace entity to effect manager");
    worldSpaceEffects.add(worldSpaceEffectEntity);
  }

  private boolean tick(WorldSpaceEffectEntity wse) {
    Location location = wse.getLocation();
    location.add(wse.getVelocity());
    Block block = location.getBlock();
    if (!block.getType().isTransparent()) {
      LogUtil.printDebug(" - WSE at solid block... removing");
      return false;
    }

    if (wse.getEffectSchedule().containsKey(wse.getCurrentTick())) {
      List<Effect> effects = wse.getEffectSchedule().get(wse.getCurrentTick());
      for (Effect effect : effects) {
        if (effect == null) {
          LogUtil.printError("Null WSE effect! Tick:" + wse.getCurrentTick());
          continue;
        }
        applyDirectionToPushEffects(wse, effect);
        if (effect instanceof LocationEffect) {
          ((LocationEffect) effect).applyAtLocation(wse.getCaster(), location);
        } else {
          LogUtil.printError("WSEs can only use effects with location! invalid: " + effect.getId());
        }
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

  private void applyDirectionToPushEffects(WorldSpaceEffectEntity wse, Effect effect) {
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
