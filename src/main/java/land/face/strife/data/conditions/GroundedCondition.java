package land.face.strife.data.conditions;

import com.tealcube.minecraft.bukkit.facecore.utilities.MoveUtil;
import land.face.strife.data.StrifeMob;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
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
    if (le.isOnGround()) {
      return true;
    }
    if (le instanceof Player p) {
      if (MoveUtil.timeOffGround(p) <= 75 + p.getPing() / 2) {
        return true;
      }
    }
    if (leeway == 0) {
      return false;
    }
    Location position = le.getLocation().clone();

    int attempts = Math.max((int) Math.floor(leeway), 3);
    float interval = leeway / attempts;

    int cachedBlockY = -10000;
    position.add(0, -interval, 0);
    while (attempts > 0) {
      int blockY = position.getBlockY();
      if (blockY != cachedBlockY) {
        cachedBlockY = blockY;
        var block = position.getBlock();
        if (block.getType().isSolid() || block.getType() == Material.LILY_PAD ||
            block.getType() == Material.BIG_DRIPLEAF) {
          var shape = block.getCollisionShape();
          var highestPoint = 0d;
          for (var bb : shape.getBoundingBoxes()) {
            highestPoint = Math.max(highestPoint, bb.getMaxY());
          }
          if (highestPoint >= position.getY()) {
            return true;
          }
        }
      }
      position.add(0, -interval, 0);
      attempts--;
    }
    return false;
  }
}
