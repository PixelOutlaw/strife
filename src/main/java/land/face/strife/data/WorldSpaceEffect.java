package land.face.strife.data;

import com.tealcube.minecraft.bukkit.facecore.utilities.ChunkUtil;
import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
      List<Effect> effects = effectSchedule.get(currentTick);
      for (Effect effect : effects) {
        if (effect == null) {
          LogUtil.printError("Null WSE effect! Tick:" + currentTick);
          continue;
        }
        if (!(effect instanceof LocationEffect)) {
          LogUtil.printError("WSEs can only use effects with location! invalid: " + effect.getId());
          continue;
        }
        applyDirectionToPushEffects(this, effect);
        ((LocationEffect) effect).applyAtLocation(caster, newLocation);
      }
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
        if (currentFallTicks != 0) {
          currentFallTicks = 0;
          newLocation.setY(blockBelow.getY() + 1.3);
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
        displacement += 0.4;
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

  private void applyDirectionToPushEffects(WorldSpaceEffect wse, Effect effect) {
    if (effect instanceof Damage) {
      Set<Effect> allEffects = new HashSet<>(((Damage) effect).getHitEffects());
      allEffects.addAll(((Damage) effect).getKillEffects());
      for (Effect e : allEffects) {
        if (e instanceof Push) {
          ((Push) e).setTempOrigin(wse.getNextLocation());
          ((Push) e).setTempDirection(wse.getNextLocation().getDirection().clone().normalize());
        }
      }
    } else if (effect instanceof AreaEffect) {
      for (Effect e : ((AreaEffect) effect).getEffects()) {
        if (e instanceof Push) {
          ((Push) e).setTempOrigin(wse.getNextLocation());
          ((Push) e).setTempDirection(wse.getNextLocation().getDirection().clone().normalize());
        }
      }
    }
  }
}
