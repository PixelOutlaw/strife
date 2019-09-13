package info.faceland.strife.data.effects;

import info.faceland.strife.StrifePlugin;
import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.stats.StrifeStat;

public class IncreaseRage extends Effect {

  private float amount;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    if (target.getStat(StrifeStat.MAXIMUM_RAGE) == 0) {
      return;
    }
    float rageAmount = amount;
    for (StrifeStat attr : getStatMults().keySet()) {
      rageAmount += getStatMults().get(attr) * caster.getStat(attr);
    }
    StrifePlugin.getInstance().getRageManager().addRage(target, rageAmount);
  }

  public void setAmount(float amount) {
    this.amount = amount;
  }
}