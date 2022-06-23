package land.face.strife.data.effects;

import land.face.strife.data.StrifeMob;

public class ConsumeBleed extends Effect {

  private double damageRatio;
  private double healRatio;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    double value = target.getBleed();
    if (value <= 0) {
      return;
    }
    target.clearBleed();
    target.getEntity().damage(value * damageRatio, caster.getEntity());
    caster.getEntity().setHealth(caster.getEntity().getHealth() + value * healRatio);
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