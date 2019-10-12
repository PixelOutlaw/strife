package land.face.strife.util;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.bukkit.entity.Player;

public class MoveUtil {

  private final static Set<UUID> HAS_MOVED = new HashSet<>();

  public static void setHasMoved(Player player) {
    HAS_MOVED.add(player.getUniqueId());
  }

  public static boolean hasMoved(Player player) {
    return HAS_MOVED.contains(player.getUniqueId());
  }

  public static void clearMoved() {
    HAS_MOVED.clear();
  }
}
