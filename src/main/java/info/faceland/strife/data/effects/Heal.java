package info.faceland.strife.data.effects;

import info.faceland.strife.attributes.StrifeAttribute;
import info.faceland.strife.data.AttributedEntity;
import info.faceland.strife.data.effects.DealDamage.DamageScale;
import info.faceland.strife.util.DamageUtil;
import org.bukkit.entity.LivingEntity;
import org.w3c.dom.Attr;

public class Heal extends Effect {

  private double amount;
  private DamageScale damageScale;

  @Override
  public void apply(AttributedEntity caster, AttributedEntity attributedTarget) {
    double heal = amount;
    for (StrifeAttribute attr : statMults.keySet()) {
      heal += statMults.get(attr) * caster.getAttributes().getOrDefault(attr, 0D);
    }
    LivingEntity target = attributedTarget.getEntity();
    switch (damageScale) {
      case FLAT:
        DamageUtil.restoreHealth(target, heal);
        break;
      case CURRENT_HP:
        heal = heal * (target.getHealth() / target.getMaxHealth());
        DamageUtil.restoreHealth(target, heal * target.getHealth());
      case MISSING_HP:
        heal = heal * (1 - target.getHealth() / target.getMaxHealth());
        DamageUtil.restoreHealth(target, heal);
      case MAXIMUM_HP:
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