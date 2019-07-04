package info.faceland.strife.effects;

import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.stats.StrifeStat;
import info.faceland.strife.util.DamageUtil;
import info.faceland.strife.util.StatUtil;

public class Bleed extends Effect {

  private double amount;
  private boolean ignoreArmor;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    double bleedAmount = amount;
    for (StrifeStat attr : getStatMults().keySet()) {
      bleedAmount += getStatMults().get(attr) * caster.getStat(attr);
    }
    // TODO: Add logic for ignore armor false
    if (!ignoreArmor) {
      bleedAmount *= StatUtil.getArmorMult(caster, target);
    }
    DamageUtil.applyBleed(target.getEntity(), bleedAmount);
  }

  public void setAmount(double amount) {
    this.amount = amount;
  }

  public void setIgnoreArmor(boolean ignoreArmor) {
    this.ignoreArmor = ignoreArmor;
  }
}