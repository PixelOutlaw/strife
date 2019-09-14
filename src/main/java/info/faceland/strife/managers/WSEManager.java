package info.faceland.strife.managers;

import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.data.WorldSpaceEffectEntity;
import info.faceland.strife.data.effects.AreaEffect;
import info.faceland.strife.data.effects.Effect;
import info.faceland.strife.data.effects.EvokerFangEffect;
import info.faceland.strife.data.effects.PlaySound;
import info.faceland.strife.data.effects.Push;
import info.faceland.strife.data.effects.SpawnParticle;
import info.faceland.strife.stats.StrifeStat;
import info.faceland.strife.util.DamageUtil.OriginLocation;
import info.faceland.strife.util.LogUtil;
import info.faceland.strife.util.TargetingUtil;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

public class WSEManager {

  private EffectManager effectManager;
  private final Set<WorldSpaceEffectEntity> worldSpaceEffects;

  public WSEManager(EffectManager effectManager) {
    this.effectManager = effectManager;
    this.worldSpaceEffects = new HashSet<>();
  }

  public void createAtTarget(StrifeMob caster, Location location, int lifespan, int maxTicks,
      double speed, Map<Integer, List<Effect>> effects, boolean lockedToEntity,
      boolean strictDuration) {
    LogUtil.printDebug(" Creating world space entity with effects " + effects);
    double newLifeSpan = lifespan;
    if (!strictDuration) {
      newLifeSpan *= 1 + caster.getStat(StrifeStat.EFFECT_DURATION) / 100;
    }
    Vector direction;
    if (lockedToEntity) {
      direction = null;
    } else {
      direction = caster.getEntity().getEyeLocation().getDirection().multiply(speed);
    }
    WorldSpaceEffectEntity entity = new WorldSpaceEffectEntity(caster, effects,
        location, lockedToEntity, direction, maxTicks, (int) newLifeSpan);
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
    StrifeMob caster = wse.getCaster();
    if (wse.isCasterLock()) {
      location = TargetingUtil.getOriginLocation(caster.getEntity(), OriginLocation.CENTER);
    } else {
      location.add(wse.getVelocity());
    }
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
        if (effect instanceof SpawnParticle) {
          ((SpawnParticle) effect).playAtLocation(location);
          continue;
        }
        if (effect instanceof PlaySound) {
          ((PlaySound) effect).playAtLocation(location);
          continue;
        }
        if (effect instanceof EvokerFangEffect) {
          ((EvokerFangEffect) effect).spawnAtLocation(wse.getCaster(), location);
          continue;
        }
        applyDirectionToPushEffects(wse, effect);
        effectManager.executeEffectAtLocation(effect, caster, location);
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
