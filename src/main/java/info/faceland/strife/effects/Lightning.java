package info.faceland.strife.effects;

import info.faceland.strife.data.AttributedEntity;
import org.bukkit.entity.LivingEntity;

public class Lightning extends Effect {

  @Override
  public void apply(AttributedEntity caster, LivingEntity target) {
    target.getWorld().strikeLightningEffect(target.getLocation());
  }
}