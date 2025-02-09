package land.face.strife.data.effects;

import java.util.UUID;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.buff.LoadedBuff;
import land.face.strife.stats.StrifeStat;
import land.face.strife.util.LogUtil;
import lombok.Setter;

@Setter
public class BuffEffect extends Effect {

  private LoadedBuff loadedBuff;
  private boolean strictDuration;
  private boolean universal;
  private int stacks;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    double durationMult = 1;
    LogUtil.printDebug("Applying BuffEffect to " + target.getEntity().getName());
    if (!strictDuration) {
      durationMult *= 1 + caster.getStat(StrifeStat.EFFECT_DURATION) / 100;
    }
    UUID source = universal ? null : caster.getEntity().getUniqueId();
    target.addBuff(loadedBuff, source, stacks, (float) (loadedBuff.getSeconds() * durationMult));
  }
}