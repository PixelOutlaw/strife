package land.face.strife.data.effects;

import land.face.strife.data.StrifeMob;
import lombok.Setter;

public class SetPitch extends Effect {

  @Setter
  private float angle;
  @Setter
  private boolean minimum;
  @Setter
  private boolean add;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    float pitch = target.getEntity().getEyeLocation().getPitch();
    if (add) {
      pitch += angle;
      pitch = Math.min(Math.max(-90, pitch), 90);
      target.getEntity().getLocation().setPitch(pitch);
    } else {
      if (pitch > 0) {
        if (!minimum || pitch < angle) {
          target.getEntity().getLocation().setPitch(angle);
        }
      } else {
        if (!minimum || pitch > angle) {
          target.getEntity().getLocation().setPitch(angle);
        }
      }
    }
  }
}