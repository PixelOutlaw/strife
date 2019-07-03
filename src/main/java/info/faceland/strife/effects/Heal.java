package info.faceland.strife.effects;

import info.faceland.strife.attributes.StrifeAttribute;
import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.effects.DealDamage.DamageScale;
import info.faceland.strife.util.DamageUtil;
import org.bukkit.entity.LivingEntity;

public class Heal extends Effect {

  private double amount;
  private DamageScale damageScale;

  @Override
  public void apply(StrifeMob caster, StrifeMob attributedTarget) {
    double heal = amount;
    for (StrifeAttribute attr : getStatMults().keySet()) {
      heal += getStatMults().get(attr) * caster.getAttributes().getOrDefault(attr, 0D);
    }
    LivingEntity target = attributedTarget.getEntity();
    switch (damageScale) {
      case FLAT:
        DamageUtil.restoreHealth(target, heal);
        break;
      case CURRENT:
        heal = heal * (target.getHealth() / target.getMaxHealth());
        DamageUtil.restoreHealth(target, heal * target.getHealth());
      case MISSING:
        heal = heal * (1 - target.getHealth() / target.getMaxHealth());
        DamageUtil.restoreHealth(target, heal);
      case MAXIMUM:
        DamageUtil.restoreHealth(target, heal * target.getMaxHealth());
    }
  }

  public void setAmount(double amount) {
    this.amount = amount;
  }

  public void setDamageScale(DamageScale damageScale) {
    this.damageScale = damageScale;
  }
}