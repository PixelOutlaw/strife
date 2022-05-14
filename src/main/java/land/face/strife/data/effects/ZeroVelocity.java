package land.face.strife.data.effects;

import land.face.strife.data.StrifeMob;
import org.bukkit.util.Vector;

public class ZeroVelocity extends Effect {

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    target.getEntity().setVelocity(new Vector(0, 0, 0));
  }
}