package land.face.strife.data.conditions;

import com.tealcube.minecraft.bukkit.facecore.utilities.MoveUtil;
import land.face.strife.data.StrifeMob;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Shulker;

public class GroundedCondition extends Condition {

  private final boolean strict;
  private final int leeway;
  private final boolean ignoreLiquids;

  public GroundedCondition(boolean strict, boolean ignoreLiquids, int leeway) {
    this.strict = strict;
    this.ignoreLiquids = ignoreLiquids;
    this.leeway = leeway;
  }

  public boolean isMet(StrifeMob caster, StrifeMob target) {
    if (getCompareTarget() == CompareTarget.SELF) {
      if (caster.getEntity() instanceof Shulker) {
        return true;
      }
      if (!ignoreLiquids && caster.getEntity().isInWater()) {
        return false;
      }
      if (strict) {
        return caster.getEntity().isOnGround();
      } else {
        return isGroundedOrCloseToGround(caster.getEntity(), leeway);
      }
    } else {
      if (target.getEntity() instanceof Shulker) {
        return true;
      }
      if (!ignoreLiquids && target.getEntity().isInWater()) {
        return false;
      }
      if (strict) {
        return target.getEntity().isOnGround();
      } else {
        return isGroundedOrCloseToGround(target.getEntity(), leeway);
      }
    }
  }

  // For fluid gameplay, treat being recently on the ground
  // as grounded for players, so small falls and knockbacks
  // don't disable ground based conditions
  public static boolean isGroundedOrCloseToGround(LivingEntity le, float leeway) {
    if (le.isOnGround() || (le instanceof Player && MoveUtil.timeOffGround((Player) le) <= 151)) {
      return true;
    }
    if (leeway == 0) {
      return false;
    }
    float check = 0;
    Location position = le.getLocation().clone().add(0, -0.1, 0);
    if (position.getBlock().isSolid()) {
      return true;
    }
    float interval = leeway / 4;
    while (check < leeway) {
      position.add(0, -interval, 0);
      if (position.getBlock().isSolid()) {
        return true;
      }
      check += interval;
    }
    return false;
  }
}
