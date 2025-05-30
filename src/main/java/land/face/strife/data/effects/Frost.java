package land.face.strife.data.effects;

import land.face.strife.data.StrifeMob;
import lombok.Setter;

public class Frost extends Effect {

  @Setter
  private int duration = 0;
  @Setter
  private boolean override;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {

    float trueDuration = (float) duration;
    if (override) {
      target.setFrost(trueDuration);
    } else {
      if (trueDuration > 0) {
        target.addFrost(trueDuration);
        if (target.isInCombat() && caster != target && caster.isPlayer()) {
          target.getCombatCountdownTask().setPvp();
        }
      } else {
        target.removeFrost(-trueDuration);
      }
    }
  }
}
