package land.face.strife.data.effects;

import land.face.strife.data.StrifeMob;
import land.face.strife.data.buff.LoadedBuff;
import land.face.strife.stats.StrifeStat;
import land.face.strife.util.DamageUtil;
import land.face.strife.util.LogUtil;

public class BuffEffect extends Effect {

  private LoadedBuff loadedBuff;
  private boolean strictDuration;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    double durationMult = 1;
    LogUtil.printDebug("Applying BuffEffect to " + target.getEntity().getName());
    if (!strictDuration) {
      durationMult *= 1 + caster.getStat(StrifeStat.EFFECT_DURATION) / 100;
    }
    DamageUtil.applyBuff(loadedBuff, target, durationMult);
  }

  public void setLoadedBuff(String buffId) {
    this.loadedBuff = DamageUtil.getBuff(buffId);
  }

  public void setStrictDuration(boolean strictDuration) {
    this.strictDuration = strictDuration;
  }
}