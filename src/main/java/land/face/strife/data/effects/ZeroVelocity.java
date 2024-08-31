package land.face.strife.data.effects;

import com.tealcube.minecraft.bukkit.facecore.utilities.MoveUtil;
import land.face.strife.data.StrifeMob;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class ZeroVelocity extends Effect {

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    target.getEntity().setVelocity(new Vector(0, 0, 0));
    if (target.isPlayer()) {
      MoveUtil.setVelocity((Player) target.getEntity());
    }
  }
}