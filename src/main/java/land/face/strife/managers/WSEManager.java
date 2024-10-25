package land.face.strife.managers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.WorldSpaceEffect;
import land.face.strife.data.effects.CreateWorldSpaceEntity;
import land.face.strife.stats.StrifeStat;
import land.face.strife.util.LogUtil;
import org.bukkit.Location;

public class WSEManager {

  private final Set<WorldSpaceEffect> worldSpaceEffects = new HashSet<>();

  public void createAtTarget(StrifeMob caster, Location location, CreateWorldSpaceEntity cse) {
    LogUtil.printDebug(" Creating world space entity with effects " + cse.getCachedEffectSchedule());
    double newLifeSpan = cse.getLifespan();
    double extension = 0;
    if (!cse.isStrictDuration()) {
      extension += caster.getStat(StrifeStat.EFFECT_DURATION);
    }
    if (cse.isReachScaling()) {
      extension += caster.getStat(StrifeStat.ATTACK_RANGE);
    }
    newLifeSpan *= 1 + extension / 100;

    var direction = location.getDirection();
    direction.multiply(cse.getVelocity());

    var entity = WorldSpaceEffect.builder()
        .caster(caster)
        .effectSchedule(cse.getCachedEffectSchedule())
        .contactEffects(cse.getContactEffects())
        .expiryEffects(cse.getExpiryEffects())
        .nextLocation(location)
        .velocity(direction)
        .originalSpeed((float) direction.length())
        .gravity(cse.getGravity())
        .friction(cse.getFriction())
        .verticalOffset(cse.getVerticalOffset())
        .maxDisplacement(cse.getMaxDisplacement())
        .maxTicks(cse.getMaxTicks())
        .lifespan((int) newLifeSpan)
        .maxFallTicks(cse.getMaxFallTicks())
        .destroyOnContact(cse.isDestroyOnContact())
        .restoreSpeedOnGroundContact(cse.isRestoreSpeedOnGroundContact())
        .destroyOnSharpAngle(cse.isDestroyOnSharpAngle())
        .build();

    entity.setupModel(cse.getModelEffect(), cse.getColor(), cse.getModelSize());

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
