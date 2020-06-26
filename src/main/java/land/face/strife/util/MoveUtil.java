package land.face.strife.util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.champion.LifeSkillType;
import land.face.strife.stats.StrifeStat;
import org.bukkit.entity.Player;

public class MoveUtil {

  private final static Map<UUID, Long> LAST_MOVED = new HashMap<>();
  private final static Map<UUID, Long> LAST_GROUNDED = new HashMap<>();
  private final static Map<UUID, Long> SNEAK_START = new HashMap<>();
  private final static Map<UUID, Integer> JUMP_MAP = new HashMap<>();

  public static int getMaxJumps(StrifeMob mob) {
    return getMaxJumps(mob.getChampion().getLifeSkillLevel(LifeSkillType.AGILITY)) +
        (int) mob.getStat(StrifeStat.AIR_JUMPS);
  }

  public static int getMaxJumps(int agilityLevel) {
    if (agilityLevel > 59) {
      return 2;
    }
    if (agilityLevel > 39) {
      return 1;
    }
    return 0;
  }

  public static void setJumps(Player player, int amount) {
    if (amount == 0) {
      JUMP_MAP.remove(player.getUniqueId());
    }
    JUMP_MAP.put(player.getUniqueId(), amount);
  }

  public static int getJumps(Player player) {
    return JUMP_MAP.getOrDefault(player.getUniqueId(), 0);
  }

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
