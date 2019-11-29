package land.face.strife.data.effects;

import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;

public class ConsumeCorrupt extends Effect {

  private double damageRatio;
  private double healRatio;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    double value = StrifePlugin.getInstance().getCorruptionManager()
        .getCorruption(target.getEntity());
    if (value <= 0) {
      return;
    }
    StrifePlugin.getInstance().getCorruptionManager().clearCorrupt(target.getEntity());
    if (damageRatio > 0.01) {
      target.getEntity().damage(value * damageRatio, caster.getEntity());
    }
    caster.getEntity().setHealth(Math.min(caster.getEntity().getHealth() + value * healRatio,
        caster.getEntity().getMaxHealth()));
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