package info.faceland.strife.effects;

import info.faceland.strife.attributes.StrifeAttribute;
import info.faceland.strife.data.AttributedEntity;
import org.bukkit.entity.LivingEntity;

public class RestoreHealth extends Effect {

  @Override
  public void execute(AttributedEntity caster, LivingEntity target) {
    for (LivingEntity le : getTargets(caster.getEntity(), target, range)) {
      double amount = flatValue;
      for (StrifeAttribute attr : statMults.keySet()) {
        amount += statMults.get(attr) * caster.getAttributes().getOrDefault(attr, 0D);
      }
      le.setHealth(Math.min(le.getMaxHealth(), le.getHealth() + amount));
    }
  }
}