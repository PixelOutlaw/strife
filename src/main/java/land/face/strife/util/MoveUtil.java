package land.face.strife.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.entity.Player;

public class MoveUtil {

  private final static Set<UUID> HAS_MOVED = new HashSet<>();
  private final static Map<UUID, Long> LAST_GROUNDED = new HashMap<>();

  public static void setHasMoved(Player player) {
    HAS_MOVED.add(player.getUniqueId());
  }

  public static void setLastGrounded(Player player) {
    LAST_GROUNDED.put(player.getUniqueId(), System.currentTimeMillis());
  }

  public static boolean hasMoved(Player player) {
    return HAS_MOVED.contains(player.getUniqueId());
  }

  public static long timeOffGround(Player player) {
    return System.currentTimeMillis() - LAST_GROUNDED.getOrDefault(player.getUniqueId(), 1L);
  }

  public static void clearMoved() {
    HAS_MOVED.clear();
  }
}
