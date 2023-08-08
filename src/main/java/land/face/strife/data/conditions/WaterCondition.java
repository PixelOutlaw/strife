package land.face.strife.data.conditions;

import land.face.strife.data.StrifeMob;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;

public class WaterCondition extends Condition {

  private final boolean surfaceOnly;
  private final boolean checkHead;

  public WaterCondition(boolean surfaceOnly, boolean checkHead) {
    this.surfaceOnly = surfaceOnly;
    this.checkHead = checkHead;
  }

  public boolean isMet(StrifeMob caster, StrifeMob target) {
    if (getCompareTarget() == CompareTarget.SELF) {
      return checkInWater(caster.getEntity());
    } else {
      return checkInWater(target.getEntity());
    }
  }

  private boolean checkInWater(LivingEntity le) {
    if (le.getLocation().getBlock().getType() == Material.WATER) {
      if (checkHead) {
        return le.getEyeLocation().getBlock().getType() == Material.WATER;
      }
      if (surfaceOnly) {
        return le.getLocation().getBlock().getRelative(BlockFace.UP, 1).getType() != Material.WATER ||
            le.getLocation().getBlock().getRelative(BlockFace.UP, 2).getType() != Material.WATER;
      }
      return true;
    }
    return false;
  }
}
