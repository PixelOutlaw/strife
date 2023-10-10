package land.face.strife.data.effects;

import land.face.strife.data.StrifeMob;
import land.face.strife.timers.EndlessEffectTimer;
import land.face.strife.util.LogUtil;

public class CancelEndlessEffect extends Effect {

  private EndlessEffect endlessEffect;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    if (endlessEffect == null) {
      LogUtil.printError("Null EndlessEffect found in CancelEndlessEffect... Check your configs!");
      return;
    }
    EndlessEffectTimer timer = EndlessEffect.getEndlessEffect(target, endlessEffect);
    if (timer == null) {
      return;
    }
    timer.runCancelEffects();
  }

  public void setEndlessEffect(EndlessEffect endlessEffect) {
    this.endlessEffect = endlessEffect;
  }
}