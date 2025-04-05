package land.face.strife.data.conditions;

import com.tealcube.minecraft.bukkit.facecore.utilities.MoveUtil;
import land.face.strife.data.StrifeMob;
import org.bukkit.entity.Player;

public class MovingCondition extends Condition {

  private final boolean state;
  private final int millis;

  public MovingCondition(boolean state, int millis) {
    this.state = state;
    this.millis = millis;
  }

  public boolean isMet(StrifeMob caster, StrifeMob target) {
    if (getCompareTarget() == CompareTarget.SELF) {
      if (!(caster.getEntity() instanceof Player)) {
        //throw new IllegalArgumentException("Move condition can only be used on players");
        return true;
      }
      return MoveUtil.hasMoved((Player) caster.getEntity(), millis) == state;
    } else {
      if (!(target.getEntity() instanceof Player)) {
        //throw new IllegalArgumentException("Move condition can only be used on players");
        return true;
      }
      return MoveUtil.hasMoved((Player) target.getEntity(), millis) == state;
    }
  }
}
