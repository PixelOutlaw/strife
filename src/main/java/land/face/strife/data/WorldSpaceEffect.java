package land.face.strife.data;

import com.tealcube.minecraft.bukkit.facecore.utilities.ChunkUtil;
import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import land.face.strife.StrifePlugin;
import land.face.strife.data.effects.AreaEffect;
import land.face.strife.data.effects.CreateModelAnimation;
import land.face.strife.data.effects.Damage;
import land.face.strife.data.effects.Effect;
import land.face.strife.data.effects.Push;
import land.face.strife.util.LogUtil;
import lombok.Builder;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.util.Vector;

@Builder
@Getter
public class WorldSpaceEffect {

  private final Map<Integer, List<Effect>> effectSchedule;
  private List<Effect> contactEffects;
  private List<Effect> expiryEffects;

  private StrifeMob caster;
  private ModeledEntity modeledEntity;
  private ArmorStand stand;
  private Location nextLocation;
  private Vector velocity;
  private float maxDisplacement, gravity, friction, verticalOffset, originalSpeed;
  private int maxTicks, lifespan, currentTick, currentFallTicks, maxFallTicks;
  private boolean destroyOnContact, restoreSpeedOnGroundContact, destroyOnSharpAngle;

  public void setupModel(String modelEffect, Color modelColor, double modelSize) {
    if (modelEffect != null) {
      ActiveModel model = ModelEngineAPI.createActiveModel(modelEffect);
      if (model == null) {
        Bukkit.getLogger().warning("[Strife] (WSE) No valid model for " + modelEffect);
      } else {
        stand = nextLocation.getWorld().spawn(nextLocation, ArmorStand.class);
        stand.setGravity(false);
        stand.setAI(false);
        stand.setInvulnerable(true);
        stand.setMarker(true);
        ChunkUtil.setDespawnOnUnload(stand);
        modeledEntity = ModelEngineAPI.createModeledEntity(stand);
        if (modeledEntity != null) {
          if (modelColor != null) {
            model.setDefaultTint(modelColor);
          }
          model.setScale(modelSize);
          modeledEntity.getBase().getBodyRotationController().setYBodyRot(stand.getLocation().getYaw());
          modeledEntity.addModel(model, true);
          //modeledEntity.detectPlayers();
          modeledEntity.setBaseEntityVisible(false);
        }
      }
    }
  }

  public boolean tick() {
    Location newLocation = nextLocation.clone();
    Vector velocity = this.velocity.clone();

    if (effectSchedule.containsKey(currentTick)) {
      runEffects(effectSchedule.get(currentTick), newLocation);
    }

    if (gravity > 0) {
      Block blockBelow = newLocation.clone().add(0, Math.min(-0.5, velocity.getY()), 0).getBlock();
      if (!blockBelow.getType().isSolid()) {
        velocity.setY(Math.max(-0.99, velocity.getY() - gravity));
        currentFallTicks++;
        if (currentFallTicks == maxFallTicks) {
          LogUtil.printDebug("WSE effect cannot fall this far! Removing...");
          runEffects(expiryEffects, newLocation.clone().add(velocity.clone().multiply(-1)));
          return false;
        }
      } else {
        if (destroyOnContact) {
          LogUtil.printDebug("WSE destroyed on contact! Removing...");
          runEffects(contactEffects, newLocation.clone().add(velocity.clone().multiply(-1)));
          return false;
        }
        if (destroyOnSharpAngle && newLocation.getPitch() > 45) {
          LogUtil.printDebug("WSE destroyed on high pitch contact! Removing...");
          runEffects(contactEffects, newLocation.clone().add(velocity.clone().multiply(-1)));
          return false;
        }
        if (velocity.getY() <= 0) {
          if (restoreSpeedOnGroundContact) {
            double oldSpeed = velocity.length();
            velocity.setY(0.0001);
            velocity.normalize();
            velocity.multiply(oldSpeed);
          } else {
            velocity.setY(0);
          }
          newLocation.setY(blockBelow.getY() + 1.3);
          if (currentFallTicks != 0) {
            currentFallTicks = 0;
          }
        }
      }
    }

    if (originalSpeed > 0.01) {
      velocity.multiply(friction);
      this.velocity = velocity;
      newLocation.add(velocity);
      newLocation.setDirection(velocity);

      Block block = newLocation.getBlock();

      if (maxDisplacement > 0) {
        float displacement = 0;
        while (block.getType().isSolid()) {
          displacement += 0.4F;
          if (displacement >= maxDisplacement) {
            LogUtil.printDebug("WSE effect has hit a wall! Removing...");
            runEffects(contactEffects, newLocation.clone().add(velocity.clone().multiply(-1)));
            return false;
          }
          newLocation.setY(newLocation.getY() + 0.4);
          block = newLocation.getBlock();
        }
      } else if (block.getType().isSolid()) {
        LogUtil.printDebug("WSE effect has hit a wall! Removing...");
        runEffects(contactEffects, newLocation.clone().add(velocity.clone().multiply(-1)));
        return false;
      }
    }

    nextLocation = newLocation;
    if (modeledEntity != null) {
      stand.teleport(nextLocation.clone().add(0, verticalOffset, 0));
      stand.setVelocity(velocity);
    }

    lifespan -= 1;
    if (lifespan < 0) {
      LogUtil.printDebug(" - WSE ran out of time! Removing...");
      runEffects(expiryEffects, newLocation.clone().add(velocity.clone().multiply(-1)));
      return false;
    }
    currentTick += 1;
    if (currentTick > maxTicks) {
      currentTick = 1;
    }
    return true;
  }

  private void runEffects(List<Effect> fx, Location newLocation) {
    if (fx.isEmpty()) {
      return;
    }
    newLocation = newLocation.clone().add(0, verticalOffset, 0);
    // TODO PASS DIRECTION CONTEXT TO EFFECTS DIRECTLY SO DELAYED ONES WORK
    for (Effect effect : fx) {
      applyDirectionToPushEffects(this, effect);
    }
    TargetResponse response = new TargetResponse(newLocation);
    // TODO PASS DIRECTION CONTEXT TO EFFECTS DIRECTLY SO DELAYED ONES WORK
    CreateModelAnimation.TEMP_LOC = newLocation.clone();
    StrifePlugin.getInstance().getEffectManager().processEffectList(caster, response, fx);
    CreateModelAnimation.TEMP_LOC = null;
  }

  private void applyDirectionToPushEffects(WorldSpaceEffect wse, Effect effect) {
    if (effect instanceof Damage) {
      modifyDamageEffect(wse, (Damage) effect);
    } else if (effect instanceof AreaEffect) {
      for (Effect e : ((AreaEffect) effect).getEffects()) {
        if (e instanceof Push) {
          Push.TEMP_ORIGIN = wse.getNextLocation().clone();
          Push.TEMP_DIRECTION = wse.getVelocity().clone().setY(0.001).normalize();
        } else if (e instanceof Damage) {
          modifyDamageEffect(wse, (Damage) e);
        }
      }
    }
  }

  private void modifyDamageEffect(WorldSpaceEffect wse, Damage e) {
    Set<Effect> allEffects = new HashSet<>(e.getHitEffects());
    allEffects.addAll(e.getKillEffects());
    for (Effect e2 : allEffects) {
      if (e2 instanceof Push) {
        Push.TEMP_ORIGIN = wse.getNextLocation().clone();
        Push.TEMP_DIRECTION = wse.getVelocity().clone().setY(0.001).normalize();
      }
    }
  }
}
