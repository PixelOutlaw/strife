package info.faceland.strife.effects;

import info.faceland.strife.data.AttributedEntity;

public class Lightning extends Effect {

  @Override
  public void apply(AttributedEntity caster, AttributedEntity target) {
    target.getEntity().getWorld().strikeLightningEffect(target.getEntity().getLocation());
  }
}