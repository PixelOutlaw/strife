package info.faceland.strife.effects;

import info.faceland.strife.StrifePlugin;
import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.data.buff.LoadedBuff;

public class BuffEffect extends Effect {

  private LoadedBuff loadedBuff;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    if (isForceTargetCaster()) {
      StrifePlugin.getInstance().getBuffManager().applyBuff(loadedBuff, caster);
      return;
    }
    StrifePlugin.getInstance().getBuffManager().applyBuff(loadedBuff, target);
  }

  public void setLoadedBuff(String buffId) {
    this.loadedBuff = StrifePlugin.getInstance().getBuffManager().getBuffFromId(buffId);
  }
}