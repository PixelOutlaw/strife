package land.face.strife.data.effects;

import land.face.strife.data.StrifeMob;
import land.face.strife.stats.StrifeStat;
import land.face.strife.util.DamageUtil;
import lombok.Setter;

public class Frost extends Effect {

  @Setter
  private int duration = 0;
  @Setter
  private boolean strictDuration;
  @Setter
  private boolean addDuration;
  @Setter
  private boolean override;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {

    float trueDuration = (float) duration;
    if (!strictDuration) {
      trueDuration *= 1 + (caster.getStat(StrifeStat.EFFECT_DURATION) / 100);
    }
    if (override) {
      target.setFrost((int) trueDuration);
    } else if (addDuration) {
      DamageUtil.addFrost(caster, target, trueDuration);
    } else {
      target.setFrost(Math.max(target.getFrost(), (int) trueDuration));
    }
  }
}
