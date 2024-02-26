package land.face.strife.patch;

import com.tealcube.minecraft.bukkit.facecore.utilities.MoveUtil;
import com.ticxo.modelengine.api.animation.BlueprintAnimation.LoopMode;
import com.ticxo.modelengine.api.animation.ModelState;
import com.ticxo.modelengine.api.animation.property.SimpleProperty;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.bone.manager.MountManager;
import com.ticxo.modelengine.api.model.bone.type.Mount;
import com.ticxo.modelengine.api.mount.controller.MountControllerType;
import com.ticxo.modelengine.api.mount.controller.impl.AbstractMountController;
import com.ticxo.modelengine.api.nms.entity.wrapper.MoveController;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class AirMountController extends AbstractMountController {

  public static final MountControllerType CUSTOM = new MountControllerType(AirMountController::new);

  private final float landSpeed;
  private final float airVelocity;

  public AirMountController(Entity entity, Mount mount) {
    super(entity, mount);
    landSpeed = 1.25f;
    airVelocity = 0.225f;
  }

  @Override
  public void updateDriverMovement(MoveController controller, ActiveModel model) {

    if (isFlying(model)) {
      controller.nullifyFallDistance();
      var original = controller.getVelocity();
      controller.setVelocity(original.getX(), 0, original.getZ());

      if (input.isSneak()) {
        model.getMountManager().ifPresent(MountManager::dismountAll);
        if (getEntity().getLocation().getBlock().getType().isSolid()) {
          getEntity().teleport(model.getModeledEntity().getBase().getLocation());
        }
        model.getModeledEntity().destroy();
        return;
      }

      if (input.getFront() != 0) {
        //model.getAnimationHandler().stopAnimation("walk");
        Vector direction = getEntity().getLocation().getDirection().clone();
        if (direction.getY() < 0 && !model.getModeledEntity().getBase()
            .getLocation().clone().add(0, -0.5, 0).getBlock().getType().isAir()) {
          model.getAnimationHandler().stopAnimation("fly_loop");
          model.getAnimationHandler().playAnimation("walk", 0.25, 0.25, 1, false);
          model.getModeledEntity().getBase().setRenderRadius(33);
          return;
        }
        direction.multiply(airVelocity);
        controller.addVelocity(direction.getX(), direction.getY(), direction.getZ());
        MoveUtil.setLastMoved((Player) getEntity());
      }
      if (getEntity().getLocation().getY() > 240) {
        controller.addVelocity(0, -6, 0);
      }
    } else {
      if (input.isSneak()) {
        model.getMountManager().ifPresent(MountManager::dismountAll);
        if (getEntity().getLocation().getBlock().getType().isSolid()) {
          getEntity().teleport(model.getModeledEntity().getBase().getLocation());
        }
        model.getModeledEntity().destroy();
        return;
      }

      if (getEntity().getLocation().getPitch() < -60) {
        controller.jump();
        SimpleProperty anim = new SimpleProperty(
            model,
            model.getBlueprint().getAnimations().get("fly_loop"),
            0.25,
            0.25,
            1
        );
        //anim.setForceLoopMode(LoopMode.LOOP);
        model.getAnimationHandler().playAnimation(anim, true);
        model.getModeledEntity().getBase().setRenderRadius(34);
        return;
      }
      if (input.getSide() != 0 || input.getFront() != 0) {
        MoveUtil.setLastMoved((Player) getEntity());
        //model.setState(ModelState.WALK);
      } else {
        //model.setState(ModelState.IDLE);
      }

      float move = controller.isOnGround() ? landSpeed : landSpeed * 0.8f;
      controller.move(input.getSide(), 0, input.getFront(), move);

      if (input.isJump() && controller.isOnGround()) {
        controller.jump();
        MoveUtil.setLastMoved((Player) getEntity());
        //model.setState(ModelState.JUMP);
      }
    }
  }

  @Override
  public void updatePassengerMovement(MoveController controller, ActiveModel model) {
    if (input.isSneak()) {
      model.getMountManager().ifPresent(MountManager::dismountAll);
    }
  }

  public boolean isFlying(ActiveModel model) {
    return model.getModeledEntity().getBase().getRenderRadius() == 34;
  }
}
