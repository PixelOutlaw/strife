package land.face.strife.util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.entity.Player;

public class MoveUtil {

  private final static Map<UUID, Long> LAST_MOVED = new HashMap<>();
  private final static Map<UUID, Long> LAST_GROUNDED = new HashMap<>();
  private final static Map<UUID, Long> SNEAK_START = new HashMap<>();

  public static void setLastMoved(Player player) {
    LAST_MOVED.put(player.getUniqueId(), System.currentTimeMillis());
  }

  public static void setLastGrounded(Player player) {
    LAST_GROUNDED.put(player.getUniqueId(), System.currentTimeMillis());
  }

  public static boolean hasMoved(Player player) {
    return System.currentTimeMillis() - LAST_MOVED.getOrDefault(player.getUniqueId(), 0L) < 100;
  }

  public static long timeOffGround(Player player) {
    return System.currentTimeMillis() - LAST_GROUNDED.getOrDefault(player.getUniqueId(), 1L);
  }

  public static void setSneak(Player player) {
    if (player.isSneaking()) {
      if (!SNEAK_START.containsKey(player.getUniqueId())) {
        SNEAK_START.put(player.getUniqueId(), System.currentTimeMillis());
      }
    } else {
      SNEAK_START.remove(player.getUniqueId());
    }
  }

  public static int getLastSneak(UUID uuid) {
    if (!SNEAK_START.containsKey(uuid)) {
      return -1;
    }
    return Math.toIntExact(System.currentTimeMillis() - SNEAK_START.get(uuid));
  }
}
