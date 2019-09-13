package info.faceland.strife.data.effects;

import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.stats.StrifeStat;
import info.faceland.strife.util.DamageUtil;
import info.faceland.strife.util.DamageUtil.DamageScale;

public class Heal extends Effect {

  private float amount;
  private DamageScale damageScale;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    float heal = amount;
    for (StrifeStat attr : getStatMults().keySet()) {
      heal += getStatMults().get(attr) * caster.getStat(attr);
    }
    DamageUtil.restoreHealth(target.getEntity(),
        DamageUtil.applyDamageScale(caster, target, heal, damageScale, null));
  }

  public void setAmount(float amount) {
    this.amount = amount;
  }

  public void setDamageScale(DamageScale damageScale) {
    this.damageScale = damageScale;
  }
}