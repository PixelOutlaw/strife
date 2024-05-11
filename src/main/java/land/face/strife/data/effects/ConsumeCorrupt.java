package land.face.strife.data.effects;

import land.face.strife.data.StrifeMob;
import land.face.strife.util.DamageUtil;

public class ConsumeCorrupt extends Effect {

  private double damageRatio;
  private double healRatio;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    double value = target.getCorruption();
    if (value <= 0) {
      return;
    }
    target.setCorruption(0);
    if (damageRatio > 0.01) {
      target.getEntity().damage(value * damageRatio, caster.getEntity());
    }
    DamageUtil.restoreHealth(caster.getEntity(),  value * healRatio, true, false);
  }

  public double getDamageRatio() {
    return damageRatio;
  }

  public void setDamageRatio(double damageRatio) {
    this.damageRatio = damageRatio;
  }

  public double getHealRatio() {
    return healRatio;
  }

  public void setHealRatio(double healRatio) {
    this.healRatio = healRatio;
  }
}