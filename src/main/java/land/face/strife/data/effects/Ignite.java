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
    boolean trackBurning;
    if (override) {
      trackBurning = setFlames(target, (int) trueDuration);
    } else if (addDuration) {
      trackBurning = setFlames(target, target.getEntity().getFireTicks() + (int) trueDuration);
    } else {
      trackBurning = setFlames(target, Math.max(target.getEntity().getFireTicks(), (int) trueDuration));
    }
    if (trackBurning) {
      StrifePlugin.getInstance().getDamageOverTimeTask().trackBurning(target.getEntity());
    }
  }

  public static boolean setFlames(StrifeMob mob, int ticks) {
    int frost = mob.getFrost();
    if (frost == 0) {
      mob.getEntity().setFireTicks(ticks);
      return true;
    }
    if (frost >= ticks) {
      mob.removeFrost(ticks);
      return false;
    }
    mob.removeFrost(1000000);
    mob.getEntity().setFireTicks(ticks - frost);
    return true;
  }

}
