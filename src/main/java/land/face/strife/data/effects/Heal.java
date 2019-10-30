package land.face.strife.data.effects;

import land.face.strife.data.DamageContainer;
import land.face.strife.data.StrifeMob;
import land.face.strife.stats.StrifeStat;
import land.face.strife.util.DamageUtil;
import land.face.strife.util.DamageUtil.DamageScale;

public class Heal extends Effect {

  private float amount;
  private DamageScale damageScale;
  private float flatBonus;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    float heal = amount;
    for (StrifeStat attr : getStatMults().keySet()) {
      heal += getStatMults().get(attr) * caster.getStat(attr);
    }
    DamageContainer container = new DamageContainer(damageScale, null, null, heal);
    heal = DamageUtil.applyDamageScale(caster, target, container, null);
    heal += flatBonus;
    DamageUtil.restoreHealth(target.getEntity(), heal);
  }

  public void setAmount(float amount) {
    this.amount = amount;
  }

  public void setDamageScale(DamageScale damageScale) {
    this.damageScale = damageScale;
  }

  public void setFlatBonus(float flatBonus) {
    this.flatBonus = flatBonus;
  }
}