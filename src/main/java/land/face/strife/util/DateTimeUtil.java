package land.face.strife.util;

import org.bukkit.World;

public class DateTimeUtil {

  public static boolean isWorldTimeInRange(World world, long startTime, long endTime) {
    long time = world.getTime();
    if (startTime == -1 || endTime == -1) {
      return true;
    }
    if (startTime < endTime) {
      return time >= startTime && time <= endTime;
    } else {
      return time >= startTime || time <= endTime;
    }
  }
}
