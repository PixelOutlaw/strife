package land.face.strife.util;

import org.bukkit.entity.Player;

public class BorderEffectUtil {

  public static void sendBorder(Player p, double percentage, int fadeTime) {
    return;
    /*
    if (StrifePlugin.getInstance().getWorldBorderApi() == null) {
      return;
    }
    percentage = Math.max(0.05, Math.min(0.95, percentage));
    StrifePlugin.getInstance().getWorldBorderApi().getWorldBorder(p)
        .setWarningDistanceInBlocks(2000);
    StrifePlugin.getInstance().getWorldBorderApi()
        .setBorder(p, 2000, p.getLocation());
    StrifePlugin.getInstance().getWorldBorderApi()
        .setBorder(p, 2000 - percentage * 1800, fadeTime);
     */
  }
}
