package land.face.strife.patch;

import com.tealcube.minecraft.bukkit.facecore.utilities.MoveUtil;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.bone.manager.MountManager;
import com.ticxo.modelengine.api.model.bone.type.Mount;
import com.ticxo.modelengine.api.mount.controller.MountControllerType;
import com.ticxo.modelengine.api.mount.controller.impl.AbstractMountController;
import com.ticxo.modelengine.api.nms.entity.wrapper.MoveController;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class GroundMountController extends AbstractMountController {

  public static final MountControllerType CUSTOM = new MountControllerType(GroundMountController::new);
  private final float landSpeed;

  public GroundMountController(Entity entity, Mount mount) {
    super(entity, mount);
    landSpeed = 1.25f;
  }

  @Override
  public void updateDriverMovement(MoveController controller, ActiveModel model) {
    if (input.isSneak()) {
      model.getMountManager().ifPresent(MountManager::dismountAll);
      if (getEntity().getLocation().getBlock().getType().isSolid()) {
        getEntity().teleport(model.getModeledEntity().getBase().getLocation());
      }
      model.getModeledEntity().destroy();
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

  @Override
  public void updatePassengerMovement(MoveController controller, ActiveModel model) {

  }
}
