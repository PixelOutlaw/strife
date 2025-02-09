package land.face.strife.util;

import java.util.concurrent.TimeUnit;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class BorderEffectUtil {

  public static void sendBorder(Player p, double percentage, int fadeMillis) {
    if (percentage <= 0) {
      p.setWorldBorder(null);
      return;
    }
    var border = p.getWorldBorder();
    if (border == null || border.getWorld() != p.getWorld()) {
      border = Bukkit.getServer().createWorldBorder();
      border.setSize(4000);
      border.setDamageAmount(0);
      border.setWarningDistance(4000);
      p.setWorldBorder(border);
    }
    border.setCenter(p.getLocation());
    border.setSize(4000 - percentage * 3600D, TimeUnit.MILLISECONDS, fadeMillis);
  }
}
