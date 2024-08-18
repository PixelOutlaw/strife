package land.face.strife.util;

import org.bukkit.Bukkit;
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

  public enum HitDirection {
    FRONT,
    NORMAL,
    BEHIND
  }
}
