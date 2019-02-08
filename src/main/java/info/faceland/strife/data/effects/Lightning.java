package info.faceland.strife.data.effects;

import info.faceland.strife.data.AttributedEntity;
import org.bukkit.entity.LivingEntity;

public class Lightning extends Effect {

  @Override
  public void apply(AttributedEntity caster, AttributedEntity target) {
    target.getEntity().getWorld().strikeLightningEffect(target.getEntity().getLocation());
  }
}