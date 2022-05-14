package land.face.strife.data.effects;

import land.face.strife.data.StrifeMob;
import lombok.Setter;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.Slime;

public class ChangeSize extends Effect {

  @Setter
  private int amount;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    if (target.getEntity() instanceof Slime) {
      ((Slime) target.getEntity()).setSize(amount);
    } else if (target.getEntity() instanceof Phantom) {
      ((Phantom) target.getEntity()).setSize(amount);
    }
  }
}