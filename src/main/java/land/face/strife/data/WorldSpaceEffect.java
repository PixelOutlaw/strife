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
import land.face.strife.data.effects.Damage;
import land.face.strife.data.effects.Effect;
import land.face.strife.data.effects.LocationEffect;
import land.face.strife.data.effects.Push;
import land.face.strife.util.LogUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.util.Vector;

public class WorldSpaceEffect {

  private final Map<Integer, List<Effect>> effectSchedule;
  private final int maxTicks;
  private final float maxDisplacement;
  private final int maxFallTicks;
  private final StrifeMob caster;
  private final float gravity;
  private final float friction;
  @Getter
  private ModeledEntity modeledEntity = null;
  @Getter
  private ArmorStand stand = null;
  @Getter @Setter
  private Location nextLocation;
  @Getter
  private Vector velocity;
  private int lifespan;
  private int currentTick = 0;
  private int currentFallTicks = 0;
  private boolean destroyOnContact;

  public WorldSpaceEffect(final StrifeMob caster, final Map<Integer, List<Effect>> effectSchedule,
      Location nextLocation, final Vector velocity, final float gravity, final float friction,
      final float maxDisplacement, final int maxTicks, final int lifespan, String modelEffect,
      int maxFallTicks, boolean destroyOnContact) {
    this.caster = caster;
    this.effectSchedule = effectSchedule;
    this.maxDisplacement = maxDisplacement;
    this.velocity = velocity;
    this.gravity = gravity;
    this.friction = friction;
    this.maxTicks = maxTicks;
    this.lifespan = lifespan;
    this.nextLocation = nextLocation;
    this.maxFallTicks = maxFallTicks;
    this.destroyOnContact = destroyOnContact;
    if (modelEffect != null) {
      ActiveModel model = ModelEngineAPI.createActiveModel(modelEffect);
      if (model == null) {
        Bukkit.getLogger().warning("[Strife] (WSE) No valid model for " + modelEffect);
      } else {
        stand = nextLocation.getWorld().spawn(nextLocation, ArmorStand.class);
        stand.setGravity(false);
        stand.setAI(false);
        stand.setInvulnerable(true);
        ChunkUtil.setDespawnOnUnload(stand);
        modeledEntity = ModelEngineAPI.createModeledEntity(stand);
        if (modeledEntity != null) {
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
      runEffects(newLocation);
    }

    if (gravity > 0) {
      Block blockBelow = newLocation.clone().add(0, Math.min(-0.5, velocity.getY()), 0).getBlock();
      if (!blockBelow.getType().isSolid()) {
        velocity.setY(Math.max(-0.99, velocity.getY() - gravity));
        currentFallTicks++;
        if (currentFallTicks == maxFallTicks) {
          LogUtil.printDebug("WSE effect cannot fall this far! Removing...");
          return false;
        }
      } else {
        if (destroyOnContact) {
          LogUtil.printDebug("WSE destroyed on contact! Removing...");
          return false;
        }
        velocity.setY(0);
        newLocation.setY(blockBelow.getY() + 1.3);
        if (currentFallTicks != 0) {
          currentFallTicks = 0;
        }
      }
    }

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
          return false;
        }
        newLocation.setY(newLocation.getY() + 0.4);
        block = newLocation.getBlock();
      }
    } else if (block.getType().isSolid()) {
      LogUtil.printDebug("WSE effect has hit a wall! Removing...");
      return false;
    }

    nextLocation = newLocation;
    if (modeledEntity != null) {
      stand.teleport(nextLocation);
    }

    lifespan -= 1;
    if (lifespan < 0) {
      LogUtil.printDebug(" - WSE ran out of time! Removing...");
      return false;
    }
    currentTick += 1;
    if (currentTick > maxTicks) {
      currentTick = 1;
    }
    return true;
  }

  private void runEffects(Location newLocation) {
    List<Effect> effects = effectSchedule.get(currentTick);
    // TODO PASS DIRECTION CONTEXT TO EFFECTS DIRECTLY SO DELAYED ONES WORK
    for (Effect effect : effects) {
      applyDirectionToPushEffects(this, effect);
    }
    TargetResponse response = new TargetResponse(newLocation);
    StrifePlugin.getInstance().getEffectManager().processEffectList(caster, response, effects);
  }

  private void applyDirectionToPushEffects(WorldSpaceEffect wse, Effect effect) {
    if (effect instanceof Damage) {
      modifyDamageEffect(wse, (Damage) effect);
    } else if (effect instanceof AreaEffect) {
      for (Effect e : ((AreaEffect) effect).getEffects()) {
        if (e instanceof Push) {
          ((Push) e).setTempOrigin(wse.getNextLocation());
          ((Push) e).setTempDirection(wse.getVelocity().clone().setY(0.001).normalize());
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
        ((Push) e2).setTempOrigin(wse.getNextLocation());
        ((Push) e2).setTempDirection(wse.getVelocity().clone().setY(0.001).normalize());
      }
    }
  }
}
