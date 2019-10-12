package land.face.strife.util;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.util.Vector;

public class FireworkUtil {

  public static void spawnFirework(Location loc, Type type, Color color, Color fade, boolean trail,
      boolean flicker) {
    Firework fw = (Firework) loc.getWorld().spawnEntity(loc, EntityType.FIREWORK);

    fade = fade == null ? Color.BLACK : fade;

    FireworkMeta fwm = fw.getFireworkMeta();
    fwm.setPower(0);
    fwm.addEffect(
        FireworkEffect.builder().with(type).withColor(color).flicker(flicker).withFade(fade)
            .trail(trail).build());

    fw.setFireworkMeta(fwm);
    fw.detonate();
  }

  public static void spawnLevelupFireworks(Player player, Color color) {
    Location location = player.getEyeLocation().clone().add(new Vector(0, 5, 0));
    spawnFirework(
        location.clone().add(new Vector(Math.random() - 0.5, 0, Math.random() - 0.5).multiply(3)),
        Type.BALL, color, Color.WHITE, false, true);
    spawnFirework(
        location.clone().add(new Vector(Math.random() - 0.5, 0, Math.random() - 0.5).multiply(3)),
        Type.BALL, color, Color.WHITE, false, true);
    spawnFirework(
        location.clone().add(new Vector(Math.random() - 0.5, 0, Math.random() - 0.5).multiply(3)),
        Type.BALL, color, Color.WHITE, false, true);
  }
}
