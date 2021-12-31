package land.face.strife.data.effects;

import com.tealcube.minecraft.bukkit.facecore.utilities.ChunkUtil;
import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import java.util.HashSet;
import java.util.Set;
import land.face.strife.data.StrifeMob;
import land.face.strife.util.SpecialStatusUtil;
import land.face.strife.util.TargetingUtil;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class CreateModelAnimation extends LocationEffect {

  public static Set<ArmorStand> CURRENT_STANDS = new HashSet<>();

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

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    ActiveModel model = ModelEngineAPI.api.getModelManager().createActiveModel(modelId);
    if (model == null) {
      return;
    }
    Location loc = TargetingUtil.getOriginLocation(target.getEntity(), getOrigin());

    ArmorStand stand = loc.getWorld().spawn(loc, ArmorStand.class);
    stand.setGravity(false);
    stand.setAI(false);
    stand.setInvulnerable(true);

    CURRENT_STANDS.add(stand);
    ChunkUtil.setDespawnOnUnload(stand);

    ModeledEntity modeledEntity = ModelEngineAPI.api.getModelManager().createModeledEntity(stand);
    if (modeledEntity == null) {
      Bukkit.getLogger().warning("Failed to create modelled entity");
    } else {
      modeledEntity.setNametagVisible(false);
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

    Bukkit.getScheduler().runTaskLater(getPlugin(), () -> {
      stand.remove();
      CURRENT_STANDS.remove(stand);
    }, lifespan);
  }

  @Override
  public void applyAtLocation(StrifeMob caster, Location loc) {
    ActiveModel model = ModelEngineAPI.api.getModelManager().createActiveModel(modelId);
    if (model == null) {
      return;
    }

    ArmorStand stand = loc.getWorld().spawn(loc, ArmorStand.class);
    stand.setGravity(false);
    stand.setAI(false);
    stand.setInvulnerable(true);

    ChunkUtil.setDespawnOnUnload(stand);

    ModeledEntity modeledEntity = ModelEngineAPI.api.getModelManager().createModeledEntity(stand);
    if (modeledEntity == null) {
      Bukkit.getLogger().warning("Failed to create modelled entity");
    } else {
      modeledEntity.setNametagVisible(false);
      modeledEntity.addActiveModel(model);
      modeledEntity.detectPlayers();
      modeledEntity.setInvisible(true);
    }

    if (animationId != null) {
      model.addState(animationId, lerpIn, lerpOut, speed);
    }

    Bukkit.getScheduler().runTaskLater(getPlugin(), () -> {
      stand.remove();
      CURRENT_STANDS.remove(stand);
    }, lifespan);
  }
}