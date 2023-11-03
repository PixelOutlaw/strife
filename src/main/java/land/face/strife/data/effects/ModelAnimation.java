package land.face.strife.data.effects;

import com.ticxo.modelengine.api.model.ActiveModel;
import land.face.strife.data.StrifeMob;
import lombok.Setter;
import org.bukkit.Bukkit;

public class ModelAnimation extends Effect {

  @Setter
  private String animationId;
  @Setter
  private int lerpIn;
  @Setter
  private int lerpOut;
  @Setter
  private double speed = 1;
  @Setter
  private int lockTicks = 0;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    if (target.getModelEntity() == null) {
      return;
    }
    if (lockTicks > 0) {
      target.getModelEntity().setModelRotationLocked(true);
      Bukkit.getScheduler().runTaskLater(getPlugin(), () ->
          target.getModelEntity().setModelRotationLocked(false), lockTicks);
    }
    for (ActiveModel model : target.getModelEntity().getModels().values()) {
      model.getAnimationHandler().playAnimation(animationId, lerpIn, lerpOut, speed, true);
    }
  }
}