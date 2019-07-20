package info.faceland.strife.effects;

import info.faceland.strife.StrifePlugin;
import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.stats.StrifeStat;

public class IncreaseRage extends Effect {

  private double amount;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    if (target.getStat(StrifeStat.MAXIMUM_RAGE) == 0) {
      return;
    }
    double restoreAmount = amount;
    for (StrifeStat attr : getStatMults().keySet()) {
      restoreAmount += getStatMults().get(attr) * caster.getStat(attr);
    }
    StrifePlugin.getInstance().getRageManager().addRage(target, restoreAmount);
  }

  public void setAmount(double amount) {
    this.amount = amount;
  }
}