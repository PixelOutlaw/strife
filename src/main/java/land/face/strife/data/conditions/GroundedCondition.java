package land.face.strife.data.conditions;

import com.tealcube.minecraft.bukkit.facecore.utilities.MoveUtil;
import land.face.strife.data.StrifeMob;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Shulker;

public class GroundedCondition extends Condition {

  private final boolean strict;

  public GroundedCondition(boolean strict) {
    this.strict = strict;
  }

  public boolean isMet(StrifeMob caster, StrifeMob target) {
    if (getCompareTarget() == CompareTarget.SELF) {
      if (caster.getEntity() instanceof Shulker) {
        return true;
      }
      if (strict) {
        return caster.getEntity().isOnGround();
      } else {
        return isGroundedOrCloseToGround(caster.getEntity());
      }
    } else {
      if (target.getEntity() instanceof Shulker) {
        return true;
      }
      if (strict) {
        return target.getEntity().isOnGround();
      } else {
        return isGroundedOrCloseToGround(target.getEntity());
      }
    }
  }

  // For fluid gameplay, treat being recently on the ground
  // as grounded for players, so small falls and knockbacks
  // don't disable ground based conditions
  private boolean isGroundedOrCloseToGround(LivingEntity le) {
    return le.isOnGround() || (le instanceof Player && MoveUtil.timeOffGround((Player) le) <= 151);
  }
}
