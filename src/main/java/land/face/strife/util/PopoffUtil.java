package land.face.strife.util;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.gmail.filoghost.holographicdisplays.api.VisibilityManager;
import io.pixeloutlaw.minecraft.spigot.garbage.StringExtensionsKt;
import land.face.strife.StrifePlugin;
import land.face.strife.data.effects.DamagePopoff;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class PopoffUtil {

  private static double MAX_GRAVITY = -2;

  public static DamagePopoff createPopoff(Player player, Location location, Vector velocity, double gravity,
      int life, String text) {
    Hologram hologram = HologramsAPI.createHologram(StrifePlugin.getInstance(), location);
    hologram.clearLines();
    hologram.appendTextLine(StringExtensionsKt.chatColorize(text));
    VisibilityManager visibilityManager = hologram.getVisibilityManager();
    visibilityManager.showTo(player);
    visibilityManager.setVisibleByDefault(false);
    DamagePopoff indicator = new DamagePopoff();

    indicator.setLife(life);
    indicator.setVelocity(velocity);
    indicator.setHologram(hologram);
    indicator.setGravity(gravity);

    return indicator;
  }

  public static boolean tickDamagePopoff(DamagePopoff indicator) {
    if (indicator.getLife() == 0) {
      deletePopoff(indicator);
      return true;
    }
    indicator.setLife(indicator.getLife() - 1);
    Hologram hologram = indicator.getHologram();
    Location location = hologram.getLocation().clone();
    Vector velocity = indicator.getVelocity();
    velocity.setY(Math.max(MAX_GRAVITY, velocity.getY() - indicator.getGravity()));
    indicator.setVelocity(velocity);
    location.add(velocity);
    hologram.teleport(location);
    return false;
  }

  private static void deletePopoff(DamagePopoff indicator) {
    Hologram hologram = indicator.getHologram();
    hologram.delete();
  }

}
