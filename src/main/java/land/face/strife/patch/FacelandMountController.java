package land.face.strife.patch;

import com.tealcube.minecraft.bukkit.facecore.utilities.MoveUtil;
import com.ticxo.modelengine.api.animation.StateProperty;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import com.ticxo.modelengine.api.model.mount.controller.AbstractMountController;
import com.ticxo.modelengine.api.model.mount.handler.IMountHandler;
import com.ticxo.modelengine.api.nms.WrapperLookController;
import com.ticxo.modelengine.api.nms.WrapperMoveController;
import land.face.strife.data.LoadedMount;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class FacelandMountController extends AbstractMountController {

  private final ActiveModel model;
  private final LoadedMount loadedMount;
  private boolean flying;

  private long launchStamp = 0L;

  public FacelandMountController(ActiveModel model, LoadedMount loadedMount) {
    this.flying = false;
    this.loadedMount = loadedMount;
    this.model = model;
  }

  @Override
  public void updateMovement(final WrapperMoveController wrapperMoveController, final ModeledEntity modeledEntity) {
    if (getInput().sneak) {
      final IMountHandler mountHandler = modeledEntity.getMountHandler();
      mountHandler.setCanDamageMount(mountHandler.getDriver(), true);
      mountHandler.setDriver(null);
      return;
    }
    if (flying) {
      final Vector velocity = wrapperMoveController.getVelocity();
      wrapperMoveController.setVelocity(
          velocity.getX() * 0.4, velocity.getY() * 0.3, velocity.getZ() * 0.4);
      wrapperMoveController.nullifyFallDistance();
      modeledEntity.setJumping(true);
      if (modeledEntity.getEntity().isOnGround()) {
        modeledEntity.getEntity().setGravity(true);
        modeledEntity.setJumping(false);
        flying = false;
        if (loadedMount.getFlyAnimation() != null) {
          model.removeState(loadedMount.getFlyAnimation(), false);
        }
        if (loadedMount.getLandAnimation() != null) {
          model.addState(loadedMount.getLandAnimation(), 2, 5, 3);
        }
        return;
      }
      if (launchStamp < System.currentTimeMillis()) {
        if (loadedMount.getFlyAnimation() != null &&
            !model.getStates().contains(loadedMount.getFlyAnimation())) {
          StateProperty flyProp = new StateProperty(loadedMount.getFlyAnimation(),
              model.getBlueprint().getAnimation(loadedMount.getFlyAnimation()), 5, 5, 1.2f);
          flyProp.setLoop(true);
          model.addState(flyProp);
        }
      }
      if (getInput().front == 0.0f) {
        MoveUtil.setLastMoved((Player) getEntity());
        modeledEntity.setWalking(false);
        /*
        Vector direction = getEntity().getLocation().getDirection().clone();
        direction.multiply(0.005);
        wrapperMoveController.addVelocity(direction.getX(), direction.getY(), direction.getZ());
        */
      } else {
        modeledEntity.setWalking(true);
        Vector direction = getEntity().getLocation().getDirection().clone();
        direction.multiply(modeledEntity.getEntity().getMovementSpeed() * 0.7);
        wrapperMoveController.addVelocity(direction.getX(), direction.getY(), direction.getZ());
      }
      if (getEntity().getLocation().getY() > 240) {
        wrapperMoveController.addVelocity(0, -2, 0);
      }
      modeledEntity.getEntity().getLocation()
          .setDirection(wrapperMoveController.getVelocity().normalize());
      return;
    }
    if (getInput().side == 0.0f && getInput().front == 0.0f) {
      modeledEntity.setWalking(false);
    } else {
      modeledEntity.setWalking(true);
      final double radians = Math.toRadians(getEntity().getLocation().getYaw());
      final double sin = Math.sin(radians);
      final double cos = Math.cos(radians);
      double n = getInput().side * cos - getInput().front * sin;
      double n2 = getInput().side * sin + getInput().front * cos;
      if (getInput().side != 0.0f && getInput().front != 0.0f) {
        n *= com.ticxo.modelengine.api.model.mount.controller.FlyingMountController_v16.diag;
        n2 *= com.ticxo.modelengine.api.model.mount.controller.FlyingMountController_v16.diag;
      }
      wrapperMoveController.move(n, n2, modeledEntity.getEntity().getMovementSpeed());
    }
    if (loadedMount.isFlying() && getEntity().getLocation().getPitch() < -60) {
      wrapperMoveController.addVelocity(0.0, 2, 0.0);
      modeledEntity.getEntity().setGravity(false);
      modeledEntity.setJumping(true);
      flying = true;
      launchStamp = System.currentTimeMillis() + 660;
      if (loadedMount.getLaunchAnimation() != null) {
        model.addState(loadedMount.getLaunchAnimation(), 2, 5, 3);
      }
      return;
    }
    if (getInput().jump) {
      wrapperMoveController.jump();
      modeledEntity.setJumping(true);
      if (loadedMount.getLaunchAnimation() != null) {
        model.addState(loadedMount.getLaunchAnimation(), 2, 5, 3);
      }
    }
  }

  @Override
  public void updatePassengerMovement(final WrapperMoveController wrapperMoveController, final ModeledEntity modeledEntity) {
    final IMountHandler mountHandler = modeledEntity.getMountHandler();
    if (getInput().sneak) {
      mountHandler.setCanDamageMount(getEntity(), true);
      modeledEntity.clearModels();
      mountHandler.removePassenger(getEntity());
    }
  }

  @Override
  public void updateDirection(final WrapperLookController wrapperLookController, final ModeledEntity modeledEntity) {
    wrapperLookController.setYaw(getEntity().getLocation().getYaw());
    wrapperLookController.setPitch(getEntity().getLocation().getPitch() / 2.0f);
  }

  @Override
  public FacelandMountController getInstance() {
    return new FacelandMountController(model, loadedMount);
  }

  public void setFlying(ModeledEntity modeledEntity) {
    if (!loadedMount.isFlying() || getEntity().isOnGround()) {
      return;
    }
    modeledEntity.getEntity().setGravity(false);
    modeledEntity.setJumping(true);
    modeledEntity.setWalking(false);
    flying = true;
  }
}
