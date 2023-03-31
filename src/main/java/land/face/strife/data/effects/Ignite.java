package land.face.strife.data.effects;

import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.stats.StrifeStat;
import lombok.Setter;

public class Ignite extends Effect {

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
      setFlames(target, (int) trueDuration);
    } else if (addDuration) {
      setFlames(target, target.getEntity().getFireTicks() + (int) trueDuration);
    } else {
      setFlames(target, Math.max(target.getEntity().getFireTicks(), (int) trueDuration));
    }
  }

  public static boolean setFlames(StrifeMob mob, int ticks) {
    int frost = (int) mob.getFrost();
    if (frost == 0) {
      mob.getEntity().setFireTicks(ticks);
      return true;
    }
    if (frost >= ticks) {
      mob.removeFrost(ticks);
      return false;
    }
    mob.removeFrost(100);
    mob.getEntity().setFireTicks(ticks - frost);
    return true;
  }

}
