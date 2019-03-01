package info.faceland.strife.effects;

import info.faceland.strife.StrifePlugin;
import info.faceland.strife.attributes.StrifeAttribute;
import info.faceland.strife.data.AttributedEntity;
import info.faceland.strife.effects.DealDamage.DamageScale;
import info.faceland.strife.util.DamageUtil;

public class RestoreBarrier extends Effect {

  private double amount;
  private DamageScale damageScale;

  @Override
  public void apply(AttributedEntity caster, AttributedEntity attributedTarget) {
    if (attributedTarget.getAttribute(StrifeAttribute.BARRIER) == 0) {
      return;
    }
    double restoreAmount = amount;
    for (StrifeAttribute attr : statMults.keySet()) {
      restoreAmount += statMults.get(attr) * caster.getAttributes().getOrDefault(attr, 0D);
    }
    switch (damageScale) {
      case FLAT:
        DamageUtil.restoreBarrier(attributedTarget, restoreAmount);
        break;
      case CURRENT:
        double curBarrier = StrifePlugin.getInstance().getBarrierManager()
            .getCurrentBarrier(attributedTarget);
        DamageUtil.restoreBarrier(attributedTarget, restoreAmount * curBarrier);
      case MISSING:
        double curBarrier2 = StrifePlugin.getInstance().getBarrierManager()
            .getCurrentBarrier(attributedTarget);
        restoreAmount = restoreAmount * (1 - curBarrier2 / attributedTarget
            .getAttribute(StrifeAttribute.BARRIER));
        DamageUtil.restoreBarrier(attributedTarget, restoreAmount);
      case MAXIMUM:
        DamageUtil.restoreBarrier(attributedTarget,
            restoreAmount * attributedTarget.getAttribute(StrifeAttribute.BARRIER));
    }
  }

  public void setAmount(double amount) {
    this.amount = amount;
  }

  public void setDamageScale(DamageScale damageScale) {
    this.damageScale = damageScale;
  }
}