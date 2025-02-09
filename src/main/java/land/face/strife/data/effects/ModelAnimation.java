package land.face.strife.data.effects;

import com.ticxo.modelengine.api.model.ActiveModel;
import land.face.strife.data.StrifeMob;
import lombok.Setter;
import org.bukkit.Bukkit;

@Setter
public class ModelAnimation extends Effect {

  private String animationId;
  private float lerpIn;
  private float lerpOut;
  private float speed = 1f;
  private int unionDelay = 0;
  private int lockTicks = 0;
  private boolean force = true;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    if (target.getModelEntity() != null) {
      if (lockTicks > 0) {
        target.getModelEntity().getBase().getBodyRotationController().setYBodyRot(target.getModelEntity().getYHeadRot());
        target.getModelEntity().setModelRotationLocked(true);
        Bukkit.getScheduler().runTaskLater(getPlugin(), () ->
            target.getModelEntity().setModelRotationLocked(false), lockTicks);
      }
      for (ActiveModel model : target.getModelEntity().getModels().values()) {
        model.getAnimationHandler().playAnimation(animationId, lerpIn, lerpOut, speed, force);
      }
      return;
    }
    if (getPlugin().getUnionManager().hasActiveUnion(target)) {
      getPlugin().getUnionManager().playUnionAnimation(target, animationId, speed, unionDelay, lockTicks);
    }
  }
}