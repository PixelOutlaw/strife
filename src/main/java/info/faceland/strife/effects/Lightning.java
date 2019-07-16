package info.faceland.strife.effects;

import info.faceland.strife.data.StrifeMob;

public class Lightning extends Effect {

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    target.getEntity().getWorld().strikeLightningEffect(target.getEntity().getLocation());
  }
}