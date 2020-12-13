package land.face.strife.util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import land.face.strife.data.HoverData;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.champion.LifeSkillType;
import land.face.strife.stats.StrifeStat;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class JumpUtil {

  private final static Map<UUID, Integer> JUMP_MAP = new HashMap<>();
  private final static Map<Player, HoverData> HOVER_MAP = new WeakHashMap<>();

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

  public static double determineHoverPower(Player player) {
    if (!player.isGliding()) {
      HOVER_MAP.remove(player);
      return 0;
    }
    Location checkerLocation = player.getLocation().clone();
    if (!HOVER_MAP.containsKey(player)) {
      while (checkerLocation.getY() > 0 && checkerLocation.getBlock().getType() == Material.AIR) {
        checkerLocation.setY(checkerLocation.getY() - 0.5);
      }
      HoverData data = new HoverData((int) player.getLocation().getX(), (int) player.getLocation().getZ(),
          checkerLocation.getY());
      HOVER_MAP.put(player, data);
      Bukkit.getLogger().info("New hover entry created");
      return 20 - Math.min(distanceFromHoverGround(player, data), 20);
    }
    HoverData data = HOVER_MAP.get(player);
    if ((int) checkerLocation.getX() == data.getBlockX() && (int) checkerLocation.getZ() == data.getBlockZ()) {
      Bukkit.getLogger().info("Using previous hover entry");
      return 20 - Math.min(distanceFromHoverGround(player, data), 20);
    }
    data.setBlockX(player.getLocation().getBlockX());
    data.setBlockZ(player.getLocation().getBlockZ());
    while (checkerLocation.getY() > 0 && checkerLocation.getBlock().getType() == Material.AIR) {
      checkerLocation.setY(checkerLocation.getY() - 0.5);
    }
    data.setGroundBlockY(checkerLocation.getY());
    Bukkit.getLogger().info("Updated hover entry");
    return 20 - Math.min(distanceFromHoverGround(player, data), 20);
  }

  public static double distanceFromHoverGround(Player player, HoverData data) {
    return player.getLocation().getY() - data.getGroundBlockY();
  }

}
