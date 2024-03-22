package land.face.strife.data.effects;

import com.tealcube.minecraft.bukkit.facecore.utilities.ChunkUtil;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.tuple.Triple;
import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.generator.blueprint.BlueprintBone;
import com.ticxo.modelengine.api.generator.blueprint.ModelBlueprint;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import com.ticxo.modelengine.api.model.bone.ModelBone;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.util.TargetingUtil;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

@Setter
public class CreateModelAnimation extends LocationEffect {

  public static Set<ArmorStand> CURRENT_MODELS = new HashSet<>();
  private final Map<String, ModelBlueprint> cachedAnimationModels = new HashMap<>();

  private String modelId;
  private String boneId;
  private String animationId;
  private List<String> animations;
  private float lerpIn;
  private float lerpOut;
  private float speed = 1;
  private float scale = 1;
  private int lifespan;
  private AnimationType animationType;
  private boolean entityLock;
  private boolean rotationLock;
  private boolean forceGrounded;
  private Color color = null;
  private Map<Integer, List<Triple<String, String, String>>> complexPart = null;

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

    int tries = 20;
    if (forceGrounded) {
      while (tries > 0) {
        if (loc.getBlock().getType().isSolid()) {
          loc.setY(loc.getBlockY() + loc.getBlock().getBoundingBox().getHeight() + 0.05);
          break;
        }
        loc.setY(loc.getY() - 0.5);
        tries--;
      }
    }

    ArmorStand stand = loc.getWorld().spawn(loc, ArmorStand.class, e -> {
      e.setInvisible(true);
      e.setInvulnerable(true);
      e.setCollidable(false);
      e.setAI(false);
      e.setGravity(false);
      e.setSilent(true);
      e.setMarker(true);
      e.setCanTick(false);
      if (animationType == AnimationType.LOOK_DIRECTION) {
        e.setVelocity(loc.getDirection().clone().multiply(0.03));
      }
      ChunkUtil.setDespawnOnUnload(e);
    });
    CURRENT_MODELS.add(stand);

    if (entityLock) {
      BukkitRunnable runnable = new BukkitRunnable() {
        @Override
        public void run() {
          if (!stand.isValid()) {
            this.cancel();
            return;
          }
          Location newLoc = target.getEntity().getLocation().clone();
          Vector direction = target.getEntity().getEyeLocation().getDirection();
          float pitch = target.getEntity().getPitch();
          float yaw = target.getEntity().getYaw();
          if (!rotationLock) {
            newLoc.setDirection(direction);
            stand.teleport(newLoc);
            stand.setRotation(yaw, pitch);
            stand.setVelocity(direction.multiply(0.03));
          } else {
            stand.teleport(newLoc);
          }
        }
      };
      runnable.runTaskTimer(getPlugin(), 1L, 1L);
    }

    Bukkit.getScheduler().runTaskLater(getPlugin(), () -> {
      stand.remove();
      CURRENT_MODELS.remove(stand);
    }, lifespan);

    applyModelStuff(stand, model, loc.getYaw());
  }

  @Override
  public void applyAtLocation(StrifeMob caster, Location loc) {
    ActiveModel model = ModelEngineAPI.createActiveModel(modelId);
    if (model == null) {
      Bukkit.getLogger().warning("[Strife] Failed to create model animation! No model!" + getId());
      return;
    }
    loc.setPitch(0);
    switch (animationType) {
      case LOOK_DIRECTION -> loc.setDirection(caster.getEntity().getEyeLocation().getDirection());
      case LOOK_ROTATION -> loc.setYaw(caster.getEntity().getEyeLocation().getYaw());
      case RANDOM_ROTATION -> loc.setYaw(StrifePlugin.RNG.nextFloat() * 360f);
    }
    int tries = 20;
    if (forceGrounded) {
      while (tries > 0) {
        if (loc.getBlock().getType().isSolid()) {
          loc.setY(loc.getBlockY() + loc.getBlock().getBoundingBox().getHeight() + 0.05);
          break;
        }
        loc.setY(loc.getY() - 0.5);
        tries--;
      }
    }

    ArmorStand stand = loc.getWorld().spawn(loc, ArmorStand.class, e -> {
      e.setInvisible(true);
      e.setInvulnerable(true);
      e.setCollidable(false);
      e.setAI(false);
      e.setGravity(false);
      e.setSilent(true);
      e.setMarker(true);
      e.setCanTick(false);
      if (animationType == AnimationType.LOOK_DIRECTION) {
        e.setVelocity(loc.getDirection().multiply(0.03));
      }
      ChunkUtil.setDespawnOnUnload(e);
    });
    stand.getEyeLocation().setDirection(loc.getDirection());
    CURRENT_MODELS.add(stand);

    Bukkit.getScheduler().runTaskLater(getPlugin(), () -> {
      stand.remove();
      CURRENT_MODELS.remove(stand);
    }, lifespan);

    applyModelStuff(stand, model, loc.getYaw());
  }

  private void applyModelStuff(ArmorStand stand, ActiveModel model, float yaw) {
    model.setScale(scale);
    if (color != null) {
      model.setDefaultTint(color);
    }
    Bukkit.getScheduler().runTaskLater(getPlugin(), () -> {
      ModeledEntity modeledEntity = ModelEngineAPI.createModeledEntity(stand);
      if (modeledEntity == null) {
        Bukkit.getLogger().warning("Failed to create modelled entity");
      } else {
        modeledEntity.getBase().getBodyRotationController().setYBodyRot(stand.getLocation().getYaw());
        modeledEntity.getBase().getBodyRotationController().setYHeadRot(stand.getEyeLocation().getYaw());
        modeledEntity.getBase().getBodyRotationController().setXHeadRot(stand.getEyeLocation().getPitch());
        modeledEntity.addModel(model, false);
        modeledEntity.setModelRotationLocked(rotationLock);
        modeledEntity.setBaseEntityVisible(false);
      }

      if (animationId != null) {
        model.getAnimationHandler().playAnimation(animationId, lerpIn, lerpOut, speed, true);
      }

      if (complexPart != null) {
        for (int i : complexPart.keySet()) {
          Bukkit.getScheduler().runTaskLater(getPlugin(), () -> {
            if (!stand.isValid()) {
              return;
            }
            for (Triple<String, String, String> triple : complexPart.get(i)) {
              String base = triple.getLeft();
              String targetPartId = triple.getMiddle();
              String newPartId = triple.getRight();

              ModelBlueprint blueprint;
              if (cachedAnimationModels.containsKey(base)) {
                blueprint = cachedAnimationModels.get(base);
              } else {
                blueprint = ModelEngineAPI.getBlueprint(base);
                cachedAnimationModels.put(base, blueprint);
              }
              final ModelBone bone = model.getBone(targetPartId).get();
              BlueprintBone bpBone = blueprint.getBones().get(newPartId);
              if (bpBone == null) {
                continue;
              }
              if (bone.getParent() == null) {
                model.removeBone(targetPartId);
                model.forceGenerateBone(null, null, bpBone);
              } else {
                model.removeBone(targetPartId);
                model.forceGenerateBone(bone.getParent().getBoneId(), null, bpBone);
              }
            }
          }, i);
        }
      }
    }, 0L);
  }

  private static EulerAngle convertVectorToEulerAngle(Vector vec) {

    double x = vec.getX();
    double y = vec.getY();
    double z = vec.getZ();

    double xz = Math.sqrt(x * x + z * z);

    double eulX;
    if (x < 0) {
      if (y == 0) {
        eulX = Math.PI * 0.5;
      } else {
        eulX = Math.atan(xz / y) + Math.PI;
      }
    } else {
      eulX = Math.atan(y / xz) + Math.PI * 0.5;
    }

    double eulY;
    if (x == 0) {
      if (z > 0) {
        eulY = Math.PI;
      } else {
        eulY = 0;
      }
    } else {
      eulY = Math.atan(z / x) + Math.PI * 0.5;
    }

    return new EulerAngle(eulX, eulY, 0);

  }
}