package info.faceland.strife.effects;

import info.faceland.strife.attributes.StrifeAttribute;
import info.faceland.strife.data.AttributedEntity;
import info.faceland.strife.util.DamageUtil;
import org.bukkit.entity.LivingEntity;

public class DealDamage extends Effect {

  private double amount;
  private DamageScale damageScale;

  @Override
  public void apply(AttributedEntity caster, LivingEntity target) {
    double damage = amount;
    for (StrifeAttribute attr : statMults.keySet()) {
      damage += statMults.get(attr) * caster.getAttributes().getOrDefault(attr, 0D);
    }
    switch (damageScale) {
      case FLAT:
        target.damage(damage, caster.getEntity());
        break;
      case CURRENT_HP:
        damage = damage * (target.getHealth() / target.getMaxHealth());
        target.damage(damage * target.getHealth(), caster.getEntity());
      case MISSING_HP:
        damage = damage * (1 - target.getHealth() / target.getMaxHealth());
        target.damage(damage, caster.getEntity());
      case MAXIMUM_HP:
        target.damage(damage * target.getMaxHealth(), caster.getEntity());
    }
  }

  public void setAmount(double amount) {
    this.amount = amount;
  }

  public void setDamageScale(DamageScale damageScale) {
    this.damageScale = damageScale;
  }

  public enum DamageType {
    TRUE,
    PHYSICAL,
    MAGICAL,
    FIRE,
    ICE,
    LIGHTNING,
    DARK
  }
  
  public enum DamageScale {
    FLAT,
    MAXIMUM_HP,
    CURRENT_HP,
    MISSING_HP
  }
}