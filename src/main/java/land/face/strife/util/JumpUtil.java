package land.face.strife.util;

import java.util.Map;
import java.util.WeakHashMap;
import land.face.strife.data.HoverData;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

public class JumpUtil {

  private final static Map<Player, HoverData> HOVER_MAP = new WeakHashMap<>();

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
      HoverData data = new HoverData((int) player.getLocation().getX(),
          (int) player.getLocation().getZ(),
          checkerLocation.getY());
      HOVER_MAP.put(player, data);
      return 20 - Math.min(distanceFromHoverGround(player, data), 20);
    }
    HoverData data = HOVER_MAP.get(player);
    if ((int) checkerLocation.getX() == data.getBlockX()
        && (int) checkerLocation.getZ() == data.getBlockZ()) {
      return 20 - Math.min(distanceFromHoverGround(player, data), 20);
    }
    data.setBlockX(player.getLocation().getBlockX());
    data.setBlockZ(player.getLocation().getBlockZ());
    while (checkerLocation.getY() > 0 && checkerLocation.getBlock().getType() == Material.AIR) {
      checkerLocation.setY(checkerLocation.getY() - 0.5);
    }
    data.setGroundBlockY(checkerLocation.getY());
    return 20 - Math.min(distanceFromHoverGround(player, data), 20);
  }

  public static double distanceFromHoverGround(Player player, HoverData data) {
    return player.getLocation().getY() - data.getGroundBlockY();
  }

  public static boolean isRooted(Player p) {
    if (p.hasPotionEffect(PotionEffectType.JUMP_BOOST)) {
      int amp = p.getPotionEffect(PotionEffectType.JUMP_BOOST).getAmplifier();
      return amp < 0 || amp > 100;
    }
    return false;
  }

}
