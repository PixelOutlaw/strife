package info.faceland.strife.data.effects;

import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.stats.StrifeStat;
import info.faceland.strife.util.DamageUtil;

public class Corrupt extends Effect {

  private float amount;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    float corruptionStacks = amount;
    for (StrifeStat attr : getStatMults().keySet()) {
      corruptionStacks += getStatMults().get(attr) * caster.getStat(attr);
    }
    DamageUtil.applyCorrupt(target.getEntity(), corruptionStacks);
  }

  public void setAmount(float amount) {
    this.amount = amount;
  }
}