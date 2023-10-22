package land.face.strife.patch;

import com.tealcube.minecraft.bukkit.facecore.utilities.MoveUtil;
import com.ticxo.modelengine.api.animation.property.SimpleProperty;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import com.ticxo.modelengine.api.model.bone.type.Mount;
import com.ticxo.modelengine.api.mount.controller.impl.AbstractMountController;
import com.ticxo.modelengine.api.nms.entity.wrapper.MoveController;
import land.face.strife.data.LoadedMount;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class FacelandMountController extends AbstractMountController {

  private final ActiveModel model;
  private final LoadedMount loadedMount;
  private boolean flying;

  private final float landSpeed;
  private final float airSpeed;

  public FacelandMountController(ActiveModel model, LoadedMount loadedMount) {
    super(Entity entity, Mount mount);
    this.flying = false;
    this.loadedMount = loadedMount;
    this.model = model;

    landSpeed = 0.0025f + (loadedMount.getSpeed() / 100);
    airSpeed = 0.15f * (0.75f * loadedMount.getSpeed() / 100);
  }

  public FacelandMountController getInstance() {
    return new FacelandMountController(model, loadedMount);
  }

  @Override
  public void updateDriverMovement(MoveController controller, ModeledEntity model) {

    if (flying) {
      controller.nullifyFallDistance();
      var original = controller.getVelocity();
      controller.setVelocity(original.getX(), 0, original.getZ());

      if (input.isSneak()) {
        model.getMountManager().dismountAll();
        if (getEntity().getLocation().getBlock().getType().isSolid()) {
          getEntity().teleport(model.getBase().getLocation());
        }
        controller.move(0, 0, 0);
        return;
      }

      if (input.getFront() != 0) {
        Vector direction = getEntity().getLocation().getDirection().clone();
        if (direction.getY() < 0 &&
            !model.getBase().getLocation().clone().add(0, -0.5, 0).getBlock().getType().isAir()) {
          if (loadedMount.getFlyAnimation() != null) {
            this.model.getAnimationHandler().stopAnimation(loadedMount.getFlyAnimation());
          }
          this.model.getAnimationHandler().playAnimation("walk", 0.25, 0.25, 1, false);
          model.setState(ModelState.WALK);
          flying = false;
          return;
        }
        direction.multiply(airSpeed);
        controller.addVelocity(direction.getX(), direction.getY(), direction.getZ());
        MoveUtil.setLastMoved((Player) getEntity());
      }
      if (getEntity().getLocation().getY() > 240) {
        controller.addVelocity(0, -6, 0);
      }
    } else {
      if (input.isSneak()) {
        model.getMountManager().dismountAll();
        if (getEntity().getLocation().getBlock().getType().isSolid()) {
          getEntity().teleport(model.getBase().getLocation());
        }
        controller.move(0, 0, 0);
        return;
      }

      if (loadedMount.isFlying() && getEntity().getLocation().getPitch() < -60) {
        flying = true;
        controller.jump();
        if (loadedMount.getFlyAnimation() != null) {
          this.model.getAnimationHandler().forceStopAllAnimations();
          SimpleProperty anim = new SimpleProperty(
              this.model,
              this.model.getBlueprint().getAnimations().get(loadedMount.getFlyAnimation()),
              0.25,
              0.25,
              1
          );
          anim.setForceLoopMode(LoopMode.LOOP);
          this.model.getAnimationHandler().playAnimation(anim, true);
        }
        return;
      }

      controller.move(input.getSide() * landSpeed, input.getFront() * landSpeed, 1);
      if (input.getSide() != 0 || input.getFront() != 0) {
        MoveUtil.setLastMoved((Player) getEntity());
        model.setState(ModelState.WALK);
      } else {
        model.setState(ModelState.IDLE);
      }

      if (input.isJump() && controller.isOnGround()) {
        controller.jump();
        MoveUtil.setLastMoved((Player) getEntity());
        model.setState(ModelState.JUMP);
      }
    }
  }

  @Override
  public void updatePassengerMovement(MoveController controller, ModeledEntity model) {
    if (input.isSneak()) {
      model.getMountManager().removePassengers(entity);
    }
  }

  public void setFlying(ModeledEntity modeledEntity) {
    if (!loadedMount.isFlying() || getEntity().isOnGround()) {
      return;
    }
    ((Entity) modeledEntity.getBase().getOriginal()).setGravity(false);
    flying = true;
    if (loadedMount.getFlyAnimation() != null) {
      this.model.getAnimationHandler().forceStopAllAnimations();
      SimpleProperty anim = new SimpleProperty(
          this.model,
          this.model.getBlueprint().getAnimations().get(loadedMount.getFlyAnimation()),
          0.25,
          0.25,
          1
      );
      anim.setForceLoopMode(LoopMode.LOOP);
      this.model.getAnimationHandler().playAnimation(anim, true);
    }
  }
}
