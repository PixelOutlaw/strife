package land.face.strife.util;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Shulker;
import org.bukkit.util.Vector;

public class DirectionUtil {

  public static boolean isBlocking(LivingEntity livingEntity) {
    if (livingEntity instanceof Shulker s) {
      return s.getPeek() < 0.05;
    }
    if (livingEntity instanceof Player p) {
      return p.isBlocking();
    }
    return false;
  }

  public static HitDirection getDirection(LivingEntity le, Vector direction) {
    direction = direction.normalize();
    // Hey dipshit, there's 6.28319 radians in 360 degrees! But only half cuz spheres
    float angle = le.getEyeLocation().getDirection().angle(direction);
    //Bukkit.getLogger().info("angle " + angle);
    if (angle < 0.625) {
      return HitDirection.BEHIND;
    }
    if (angle > 2.1) {
      return HitDirection.FRONT;
    }
    return HitDirection.NORMAL;
  }

  public static Vector getDodgeRollVelocity(Player player) {
    var input = player.getCurrentInput();
    var vec = new Vector(
        (input.isRight() ? 0 : 1) - (input.isLeft() ? 0 : 1),
        0,
        (input.isForward() ? 1 : 0) - (input.isBackward() ? 1 : 0)
    );
    if (vec.getX() == 0 && vec.getZ() == 0) {
      vec.setZ(-1);
    }
    vec.normalize();
    vec.rotateAroundY(Math.toRadians(-player.getYaw()));
    return vec;
  }

  public enum HitDirection {
    FRONT,
    NORMAL,
    BEHIND
  }
}
