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
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class CreateModelAnimation extends LocationEffect {

  public static Set<Entity> CURRENT_MODELS = new HashSet<>();
  private final Map<String, ModelBlueprint> cachedAnimationModels = new HashMap<>();

  @Setter
  private String modelId;
  @Setter
  private String boneId;
  @Setter
  private String animationId;
  @Setter
  private int lerpIn;
  @Setter
  private int lerpOut;
  @Setter
  private double speed = 1;
  @Setter
  private int lifespan;
  @Setter
  private boolean targetLock;
  @Setter
  private boolean randomRotation;
  @Setter
  private boolean rotationLock;
  @Setter
  private boolean forceGrounded;
  @Setter
  private Color color = null;
  @Setter
  private Map<Integer, List<Triple<String, String, String>>> complexPart = null;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    ActiveModel model = ModelEngineAPI.createActiveModel(modelId);
    if (model == null) {
      Bukkit.getLogger().warning("[Strife] Failed to create model animation! No model!" + getId());
      return;
    }
    Location loc = TargetingUtil.getOriginLocation(target.getEntity(), getOrigin());

    if (randomRotation) {
      loc.setYaw(StrifePlugin.RNG.nextFloat() * 360f);
    } else {
      if (rotationLock) {
        loc.setYaw(caster.getEntity().getEyeLocation().getYaw());
      } else {
        loc.setYaw(StrifePlugin.RNG.nextFloat() * 360f);
      }
    }
    loc.setPitch(0);

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
      ChunkUtil.setDespawnOnUnload(e);
    });
    CURRENT_MODELS.add(stand);

    if (targetLock) {
      Vector relativePos = loc.subtract(target.getEntity().getLocation()).toVector();
      Vector direction = caster.getEntity().getLocation().clone().getDirection();
      BukkitRunnable runnable = new BukkitRunnable() {
        @Override
        public void run() {
          if (stand.isValid()) {
            Location newLoc = target.getEntity().getLocation().clone();
            if (rotationLock) {
              newLoc.setDirection(stand.getLocation().getDirection());
            } else {
              newLoc.setDirection(direction);
            }
            stand.teleport(newLoc.add(relativePos));
          } else {
            this.cancel();
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

    if (randomRotation) {
      loc.setYaw(StrifePlugin.RNG.nextFloat() * 360);
    } else {
      if (rotationLock) {
        loc.setYaw(caster.getEntity().getEyeLocation().getYaw());
      } else {
        loc.setYaw(StrifePlugin.RNG.nextFloat() * 360);
      }
    }
    loc.setPitch(0);

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
      ChunkUtil.setDespawnOnUnload(e);
    });
    CURRENT_MODELS.add(stand);

    Bukkit.getScheduler().runTaskLater(getPlugin(), () -> {
      stand.remove();
      CURRENT_MODELS.remove(stand);
    }, lifespan);

    applyModelStuff(stand, model, loc.getYaw());
  }

  private void applyModelStuff(ArmorStand stand, ActiveModel model, float yaw) {
    Bukkit.getScheduler().runTaskLater(getPlugin(), () -> {
      ModeledEntity modeledEntity = ModelEngineAPI.createModeledEntity(stand);
      if (modeledEntity == null) {
        Bukkit.getLogger().warning("Failed to create modelled entity");
      } else {
        modeledEntity.addModel(model, true);
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
}