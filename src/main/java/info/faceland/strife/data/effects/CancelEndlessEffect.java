package info.faceland.strife.data.effects;

import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.timers.EndlessEffectTimer;
import info.faceland.strife.util.LogUtil;

public class CancelEndlessEffect extends Effect {

  private EndlessEffect endlessEffect;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    if (endlessEffect == null) {
      LogUtil.printError("Null EndlessEffect found in CancelEndlessEffect... Check your configs!");
      return;
    }
    EndlessEffectTimer timer = endlessEffect.getEndlessTimer(target);
    if (timer == null) {
      return;
    }
    timer.doExpiry();
  }

  public void setEndlessEffect(EndlessEffect endlessEffect) {
    this.endlessEffect = endlessEffect;
  }
}