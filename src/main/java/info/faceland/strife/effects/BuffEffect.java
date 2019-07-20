package info.faceland.strife.effects;

import info.faceland.strife.StrifePlugin;
import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.data.buff.LoadedBuff;
import info.faceland.strife.stats.StrifeStat;

public class BuffEffect extends Effect {

  private LoadedBuff loadedBuff;
  private boolean strictDuration;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    double durationMult = 1;
    if (!strictDuration) {
      durationMult *= caster.getStat(StrifeStat.EFFECT_DURATION) / 100;
    }
    if (isForceTargetCaster()) {
      StrifePlugin.getInstance().getBuffManager().applyBuff(loadedBuff, caster, durationMult);
      return;
    }
    StrifePlugin.getInstance().getBuffManager().applyBuff(loadedBuff, target, durationMult);
  }

  public void setLoadedBuff(String buffId) {
    this.loadedBuff = StrifePlugin.getInstance().getBuffManager().getBuffFromId(buffId);
  }

  public void setStrictDuration(boolean strictDuration) {
    this.strictDuration = strictDuration;
  }
}