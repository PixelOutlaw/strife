package land.face.strife.data.effects;

import land.face.strife.data.StrifeMob;
import org.bukkit.entity.Player;

public class RemoveEntity extends Effect {

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    if (target.getEntity() instanceof Player) {
      target.getEntity().damage(100000000);
    }
    target.getEntity().remove();
  }
}
