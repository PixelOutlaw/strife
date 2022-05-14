package land.face.strife.data.effects;

import land.face.strife.data.StrifeMob;
import land.face.strife.stats.StrifeStat;
import lombok.Setter;

public class Invincible extends Effect {

  @Setter
  private int duration = 0;
  @Setter
  private boolean strictDuration;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    float trueDuration = (float) duration;
    if (!strictDuration) {
      trueDuration *= 1 + (caster.getStat(StrifeStat.EFFECT_DURATION) / 100);
    }
    target.applyInvincible((int) trueDuration);
  }
}
