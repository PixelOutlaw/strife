package info.faceland.strife.effects;

import info.faceland.strife.StrifePlugin;
import info.faceland.strife.data.AttributedEntity;

public class ConsumeCorrupt extends Effect {

  private double damageRatio;
  private double healRatio;

  @Override
  public void apply(AttributedEntity caster, AttributedEntity target) {
    double value = StrifePlugin.getInstance().getDarknessManager()
        .getCorruptionStacks(target.getEntity());
    if (value <= 0) {
      return;
    }
    StrifePlugin.getInstance().getDarknessManager().removeEntity(target.getEntity());
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