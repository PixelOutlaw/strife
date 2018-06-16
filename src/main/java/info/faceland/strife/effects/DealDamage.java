package info.faceland.strife.effects;

import info.faceland.strife.attributes.StrifeAttribute;
import info.faceland.strife.data.AttributedEntity;
import org.bukkit.entity.LivingEntity;

public class DealDamage extends Effect {

  @Override
  public void apply(AttributedEntity caster, LivingEntity target) {
    double damage = flatValue;
    for (StrifeAttribute attr : statMults.keySet()) {
      damage += statMults.get(attr) * caster.getAttributes().getOrDefault(attr, 0D);
    }
    target.damage(damage, caster.getEntity());
  }

  public enum DamageType {
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