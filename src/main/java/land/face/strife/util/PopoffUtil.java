package land.face.strife.util;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import io.pixeloutlaw.minecraft.spigot.garbage.StringExtensionsKt;
import java.util.List;
import java.util.UUID;
import land.face.strife.data.effects.DamagePopoff;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class PopoffUtil {

  private static double MAX_GRAVITY = -2;

  public static DamagePopoff createPopoff(Player player, Location location, Vector velocity,
      double gravity, int life, String text) {
    Hologram holo = DHAPI.createHologram(UUID.randomUUID().toString(),
        location.clone(),
        List.of(StringExtensionsKt.chatColorize(text))
    );
    holo.hideAll();
    holo.show(player, 0);
    DamagePopoff indicator = new DamagePopoff();

    indicator.setLife(life);
    indicator.setVelocity(velocity);
    indicator.setHologram(holo);
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
    DHAPI.moveHologram(hologram, location);
    return false;
  }

  private static void deletePopoff(DamagePopoff indicator) {
    Hologram hologram = indicator.getHologram();
    hologram.delete();
  }

}
