package land.face.strife.managers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.WorldSpaceEffect;
import land.face.strife.data.effects.CreateWorldSpaceEntity;
import land.face.strife.stats.StrifeStat;
import land.face.strife.util.LogUtil;
import org.bukkit.Bukkit;
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

    Collections.shuffle(cse.getStartAnimations());
    Collections.shuffle(cse.getMoveAnimations());
    Collections.shuffle(cse.getEndAnimations());
    var entity = WorldSpaceEffect.builder()
        .caster(caster)
        .effectSchedule(cse.getCachedEffectSchedule())
        .returnEffectSchedule(cse.getCachedReturnEffectSchedule())
        .contactEffects(cse.getContactEffects())
        .expiryEffects(cse.getExpiryEffects())
        .nextLocation(location)
        .velocity(direction)
        .originalSpeed((float) direction.length())
        .startAnimation(cse.getStartAnimations().isEmpty() ? null : cse.getStartAnimations().getFirst())
        .moveAnimation(cse.getMoveAnimations().isEmpty() ? null : cse.getMoveAnimations().getFirst())
        .endAnimation(cse.getEndAnimations().isEmpty() ? null : cse.getEndAnimations().getFirst())
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
    worldSpaceEffects.forEach((wse) -> {
      if (!wse.tick()) {
        wse.destroyed = true;
        expiredEffects.add(wse);
        if (wse.getStand() != null) {
          if (wse.getEndAnimation() != null) {
            wse.getModel().getAnimationHandler().forceStopAllAnimations();
            var len = (int) (20D * wse.getModel().getBlueprint().getAnimations()
                .get(wse.getEndAnimation()).getLength());
            wse.getModel().getAnimationHandler().playAnimation(wse.getEndAnimation(), 0, 0, 1, true);
            Bukkit.getScheduler().runTaskLater(StrifePlugin.getInstance(), () -> wse.getStand().setRemoved(true), len);
          } else {
            wse.getStand().setRemoved(true);
          }
        }
      }
    });
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
