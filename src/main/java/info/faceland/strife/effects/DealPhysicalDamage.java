package info.faceland.strife.effects;

import info.faceland.strife.attributes.StrifeAttribute;
import info.faceland.strife.data.AttributedEntity;
import org.bukkit.entity.LivingEntity;

public class DealPhysicalDamage extends Effect {

  @Override
  public void execute(AttributedEntity caster, LivingEntity target) {
    for (LivingEntity le : getTargets(caster.getEntity(), target, range)) {
      double damage = flatValue;
      for (StrifeAttribute attr : statMults.keySet()) {
        damage += statMults.get(attr) * caster.getAttributes().getOrDefault(attr, 0D);
      }
      le.damage(damage, caster.getEntity());
    }
  }
}