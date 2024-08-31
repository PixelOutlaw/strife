package land.face.strife.data.effects;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.entity.Dummy;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.stats.StrifeStat;
import land.face.strife.util.TargetingUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

@Setter
@Getter
public class CreateModelAnimation extends LocationEffect {

  private String modelId;
  private String animationId;
  private String startAnimation;
  private String endAnimation;
  private float lerpIn;
  private float lerpOut;
  private float speed = 1;
  private float scale = 1;
  private float directionOffset = 0;
  private int forceGroundBlocks = 0;
  private int lifespan;
  private AnimationType animationType;
  private boolean entityLock;
  private boolean rotationLock;
  private boolean strictDuration;
  private Color color = null;

  public static Location TEMP_LOC;

  public enum AnimationType {
    LOOK_DIRECTION,
    LOOK_ROTATION,
    RANDOM_ROTATION,
  }

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    ActiveModel model = ModelEngineAPI.createActiveModel(modelId);
    if (model == null) {
      Bukkit.getLogger().warning("[Strife] Failed to create model animation! No model!" + getId());
      return;
    }
    Location loc = TargetingUtil.getOriginLocation(target.getEntity(), getOrigin());
    loc.setPitch(0);

    switch (animationType) {
      case LOOK_DIRECTION -> loc.setDirection(caster.getEntity().getEyeLocation().getDirection());
      case LOOK_ROTATION -> loc.setYaw(caster.getEntity().getEyeLocation().getYaw());
      case RANDOM_ROTATION -> loc.setYaw(StrifePlugin.RNG.nextFloat() * 360f);
    }


    if (forceGroundBlocks > 0) {
      int tries = forceGroundBlocks;
      loc = loc.clone();
      while (tries > 0) {
        var block = loc.getBlock();
        if (block.isLiquid()) {
          return;
        }
        if (block.getType().isSolid()) {
          loc.setY(loc.getBlockY() + block.getBoundingBox().getHeight() + 0.05);
          break;
        }
        loc.setY(loc.getY() - 0.5);
        tries--;
      }
      if (tries == 0) {
        return;
      }
    }
    applyModelStuff(caster, target, loc, model);
  }

  @Override
  public void applyAtLocation(StrifeMob caster, Location loc) {
    ActiveModel model = ModelEngineAPI.createActiveModel(modelId);
    if (model == null) {
      Bukkit.getLogger().warning("[Strife] Failed to create model animation! No model!" + getId());
      return;
    }
    if (TEMP_LOC == null) {
      loc.setPitch(0);
      switch (animationType) {
        case LOOK_DIRECTION -> loc.setDirection(caster.getEntity().getEyeLocation().getDirection());
        case LOOK_ROTATION -> loc.setYaw(caster.getEntity().getEyeLocation().getYaw());
        case RANDOM_ROTATION -> loc.setYaw(StrifePlugin.RNG.nextFloat() * 360f);
      }
    } else {
      loc.setPitch(TEMP_LOC.getPitch());
      loc.setYaw(TEMP_LOC.getYaw());
    }

    if (forceGroundBlocks > 0) {
      int tries = forceGroundBlocks;
      loc = loc.clone();
      while (tries > 0) {
        var block = loc.getBlock();
        if (block.isLiquid()) {
          return;
        }
        if (block.getType().isSolid()) {
          loc.setY(loc.getBlockY() + block.getBoundingBox().getHeight() + 0.05);
          break;
        }
        loc.setY(loc.getY() - 0.5);
        tries--;
      }
      if (tries == 0) {
        return;
      }
    }

    applyModelStuff(caster, null, loc, model);
  }

  private void applyModelStuff(StrifeMob caster, StrifeMob target, Location loc, ActiveModel model) {

    Dummy<?> dummy = new Dummy<>();
    dummy.setLocation(loc.clone().add(loc.getDirection().multiply(directionOffset)));
    ModeledEntity modeledEntity = ModelEngineAPI.createModeledEntity(dummy);
    if (modeledEntity == null) {
      dummy.setRemoved(true);
      Bukkit.getLogger().warning("Failed to create modelled entity");
      return;
    } else {
      if (color != null) {
        model.setDefaultTint(color);
      }
      modeledEntity.addModel(model, true);
      //modeledEntity.detectPlayers();
      dummy.getBodyRotationController().setRotationDuration(0);
      dummy.getBodyRotationController().setRotationDelay(0);
      dummy.setYBodyRot(loc.getYaw());
      dummy.setXHeadRot(loc.getPitch());
      dummy.setYHeadRot(loc.getYaw());
      dummy.setWalking(true);
    }
    model.setScale(scale);
    if (color != null) {
      model.setDefaultTint(color);
    }

    modeledEntity.addModel(model, false);
    modeledEntity.setModelRotationLocked(rotationLock);
    modeledEntity.setBaseEntityVisible(false);
    modeledEntity.getBase().setRenderRadius(36);

    float effectDuration = strictDuration ? 1f : 1f + caster.getStat(StrifeStat.EFFECT_DURATION) / 100;
    float realSpeed = speed / effectDuration;

    int realLifespan = lifespan;
    if (animationId != null) {
      if (lifespan == -1) {
        double animationDuration = model.getBlueprint().getAnimations().get(animationId).getLength() / realSpeed;
        realLifespan = (int) Math.floor(20 * animationDuration) + 1;
      } else {
        realLifespan = (int) (lifespan * effectDuration);
      }
    }
    if (realLifespan == -1) {
      Bukkit.getLogger().warning(
          "[Strife] You screwed up effect " + getId() + " only supply -1 lifespan if an animation-id is defined!");
      realLifespan = 20;
    }
    if (animationId != null) {
      model.getAnimationHandler().forceStopAllAnimations();
      model.getAnimationHandler().playAnimation(animationId, lerpIn, lerpOut, realSpeed, true);
    }
    if (startAnimation != null) {
      model.getAnimationHandler().playAnimation(startAnimation, 0, 0, 1, true);
    }
    if (endAnimation != null) {
      int endTicks = (int) (20f * model.getBlueprint().getAnimations().get(endAnimation).getLength());
      Bukkit.getScheduler().runTaskLater(getPlugin(), () ->
              model.getAnimationHandler().playAnimation(endAnimation, 0, 0, 1, true),
          Math.max(1, 1 + realLifespan - endTicks));
    }

    if (entityLock && target != null) {
      BukkitRunnable runnable = new BukkitRunnable() {
        @Override
        public void run() {
          if (dummy.isRemoved()) {
            this.cancel();
            return;
          }
          Location newLoc = TargetingUtil.getOriginLocation(target.getEntity(), getOrigin());
          Vector direction = target.getEntity().getEyeLocation().getDirection();
          float pitch = target.getEntity().getPitch();
          float yaw = target.getEntity().getYaw();
          if (!rotationLock) {
            newLoc.setDirection(direction);
            dummy.setLocation(newLoc);
            dummy.setYBodyRot(yaw);
            dummy.setXHeadRot(pitch);
            dummy.setYHeadRot(yaw);
          } else {
            dummy.setLocation(newLoc);
          }
        }
      };
      runnable.runTaskTimer(getPlugin(), 1L, 1L);
    }

    Bukkit.getScheduler().runTaskLater(getPlugin(), () -> dummy.setRemoved(true), 1 + realLifespan);
  }
}