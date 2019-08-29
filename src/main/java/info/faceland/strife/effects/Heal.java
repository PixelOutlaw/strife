package info.faceland.strife.effects;

import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.effects.DealDamage.DamageScale;
import info.faceland.strife.stats.StrifeStat;
import info.faceland.strife.util.DamageUtil;

public class Heal extends Effect {

  private double amount;
  private DamageScale damageScale;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    double heal = amount;
    for (StrifeStat attr : getStatMults().keySet()) {
      heal += getStatMults().get(attr) * caster.getStat(attr);
    }
    DamageUtil.restoreHealth(target.getEntity(),
        DamageUtil.applyDamageScale(caster, target, heal, damageScale, null));
  }

  public void setAmount(double amount) {
    this.amount = amount;
  }

  public void setDamageScale(DamageScale damageScale) {
    this.damageScale = damageScale;
  }
}