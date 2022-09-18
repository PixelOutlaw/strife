package land.face.strife.data.effects;

import com.tealcube.minecraft.bukkit.facecore.utilities.ChunkUtil;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.tuple.Triple;
import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.generator.blueprint.ModelBlueprint;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import com.ticxo.modelengine.api.model.PartEntity;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import land.face.strife.data.StrifeMob;
import land.face.strife.util.TargetingUtil;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Tadpole;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class CreateModelAnimation extends LocationEffect {

  public static Set<Entity> CURRENT_MODELS = new HashSet<>();
  private final Map<String, ModelBlueprint> cachedAnimationModels = new HashMap<>();

  @Setter
  private String modelId;
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
  private boolean rotationLock;
  @Setter
  private Color color = null;
  @Setter
  private Map<Integer, List<Triple<String, String, String>>> complexPart = null;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    ActiveModel model = ModelEngineAPI.api.getModelManager().createActiveModel(modelId);
    if (model == null) {
      Bukkit.getLogger().warning("[Strife] Failed to create model animation! No model!" + getId());
      return;
    }
    model.setClamp(0);
    Location loc = TargetingUtil.getOriginLocation(target.getEntity(), getOrigin());

    if (rotationLock) {
      loc.setYaw(caster.getEntity().getEyeLocation().getYaw());
    } else {
      loc.setYaw((float) (Math.random() * 180));
    }

    Tadpole stand = loc.getWorld().spawn(loc, Tadpole.class, e -> {
      e.setInvisible(true);
      e.setInvulnerable(true);
      e.setCollidable(false);
      e.setAI(false);
      e.setGravity(false);
      e.setSilent(true);
      ChunkUtil.setDespawnOnUnload(e);
    });

    CURRENT_MODELS.add(stand);

    ModeledEntity modeledEntity = ModelEngineAPI.api.getModelManager().createModeledEntity(stand);
    if (modeledEntity == null) {
      Bukkit.getLogger().warning("Failed to create modelled entity");
    } else {
      modeledEntity.addActiveModel(model);
      modeledEntity.detectPlayers();
      modeledEntity.setInvisible(true);
    }

    if (animationId != null) {
      model.addState(animationId, lerpIn, lerpOut, speed);
    }

    if (targetLock) {
      Vector relativePos = loc.subtract(target.getEntity().getLocation()).toVector();
      Vector direction = caster.getEntity().getLocation().clone().getDirection();
      BukkitRunnable runnable = new BukkitRunnable() {
        @Override
        public void run() {
          if (stand.isValid()) {
            Location newLoc = target.getEntity().getLocation().clone();
            if (rotationLock) {
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

    if (color != null) {
      for (PartEntity pe : model.getPartEntities().values()) {
        pe.setColor(color);
      }
    }

    Bukkit.getScheduler().runTaskLater(getPlugin(), () -> {
      stand.remove();
      CURRENT_MODELS.remove(stand);
    }, lifespan);

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
              blueprint = ModelEngineAPI.api.getModelManager().getModelRegistry()
                  .getModelBlueprint(base);
              cachedAnimationModels.put(base, blueprint);
            }
            final PartEntity partEntity = model.getPartEntity(targetPartId);
            final int id = blueprint.getItemId(newPartId);
            if (id <= 0) {
              return;
            }
            partEntity.updateDataId(id);
          }
        }, i);
      }
    }
  }

  @Override
  public void applyAtLocation(StrifeMob caster, Location loc) {
    ActiveModel model = ModelEngineAPI.api.getModelManager().createActiveModel(modelId);
    if (model == null) {
      Bukkit.getLogger().warning("[Strife] Failed to create model animation! No model!" + getId());
      return;
    }

    model.setClamp(0);
    if (rotationLock) {
      loc.setYaw(caster.getEntity().getEyeLocation().getYaw());
    } else {
      loc.setYaw((float) (Math.random() * 180));
    }

    Tadpole stand = loc.getWorld().spawn(loc, Tadpole.class, e -> {
      e.setInvisible(true);
      e.setInvulnerable(true);
      e.setCollidable(false);
      e.setAI(false);
      e.setInvisible(true);
      e.setGravity(false);
      e.setSilent(true);
      ChunkUtil.setDespawnOnUnload(e);
    });

    CURRENT_MODELS.add(stand);

    ModeledEntity modeledEntity = ModelEngineAPI.api.getModelManager().createModeledEntity(stand);
    if (modeledEntity == null) {
      Bukkit.getLogger().warning("Failed to create modelled entity");
    } else {
      modeledEntity.addActiveModel(model);
      modeledEntity.detectPlayers();
      modeledEntity.setInvisible(true);
    }

    if (animationId != null) {
      model.addState(animationId, lerpIn, lerpOut, speed);
    }

    if (color != null) {
      for (PartEntity pe : model.getPartEntities().values()) {
        pe.setColor(color);
      }
    }

    Bukkit.getScheduler().runTaskLater(getPlugin(), () -> {
      stand.remove();
      CURRENT_MODELS.remove(stand);
    }, lifespan);

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
              blueprint = ModelEngineAPI.api.getModelManager().getModelRegistry()
                  .getModelBlueprint(base);
              cachedAnimationModels.put(base, blueprint);
            }
            final PartEntity partEntity = model.getPartEntity(targetPartId);
            //if (color != null) {
            //  partEntity.setColor(color);
            //}
            final int id = blueprint.getItemId(newPartId);
            if (id <= 0) {
              return;
            }
            partEntity.updateDataId(id);
          }
        }, i);
      }
    }
  }
}