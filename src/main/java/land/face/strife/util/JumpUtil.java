package land.face.strife.util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.champion.LifeSkillType;
import land.face.strife.stats.StrifeStat;
import org.bukkit.entity.Player;

public class JumpUtil {

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

}
