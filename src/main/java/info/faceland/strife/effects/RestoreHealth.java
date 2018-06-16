package info.faceland.strife.effects;

import info.faceland.strife.attributes.StrifeAttribute;
import info.faceland.strife.data.AttributedEntity;
import org.bukkit.entity.LivingEntity;

public class RestoreHealth extends Effect {

  @Override
  public void apply(AttributedEntity caster, LivingEntity target) {
    double amount = flatValue;
    for (StrifeAttribute attr : statMults.keySet()) {
      amount += statMults.get(attr) * caster.getAttributes().getOrDefault(attr, 0D);
    }
    target.setHealth(Math.min(target.getMaxHealth(), target.getHealth() + amount));
  }
}