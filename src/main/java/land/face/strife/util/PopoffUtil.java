package land.face.strife.util;

import de.oliver.fancyholograms.api.FancyHologramsPlugin;
import de.oliver.fancyholograms.api.HologramManager;
import de.oliver.fancyholograms.api.data.DisplayHologramData;
import de.oliver.fancyholograms.api.data.HologramData;
import de.oliver.fancyholograms.api.data.TextHologramData;
import de.oliver.fancyholograms.api.hologram.Hologram;
import info.faceland.loot.LootPlugin;
import java.util.List;
import java.util.UUID;
import land.face.strife.StrifePlugin;
import land.face.strife.data.effects.DamagePopoff;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Display.Billboard;
import org.bukkit.entity.Display.Brightness;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.joml.Vector3f;

public class PopoffUtil {

  private static double MAX_GRAVITY = -2;
  private final HologramManager manager = FancyHologramsPlugin.get().getHologramManager();

  public static DamagePopoff createPopoff(Player player, Location location, Vector velocity,
      double gravity, int life, String text) {
    return createPopoff(player, location, velocity, gravity, life, text, 1.0f, 1.0f, 1.0f);
  }

  public static DamagePopoff createPopoff(Player player, Location location, Vector velocity,
      double gravity, int life, String text, float startScale, float midScale, float endScale) {

    TextHologramData textData = new TextHologramData(UUID.randomUUID().toString(), location);
    textData.setText(List.of(text));
    textData.setBackground(Hologram.TRANSPARENT);
    textData.setBillboard(Billboard.CENTER);
    textData.setBrightness(new Brightness(13, 13));
    textData.setTextShadow(false);
    textData.setPersistent(false);

    var manager = FancyHologramsPlugin.get().getHologramManager();

    Hologram holo = manager.create(textData);
    holo.createHologram();

    Bukkit.getScheduler().runTaskAsynchronously(LootPlugin.getInstance(), () -> {
      holo.forceShowHologram(player);
    });

    DamagePopoff indicator = new DamagePopoff();

    indicator.setStartScale(startScale);
    indicator.setMidScale(midScale);
    indicator.setEndScale(endScale);

    indicator.setViewer(player);
    indicator.setLife(life);
    indicator.setMaxLife(life);
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

    Vector velocity = indicator.getVelocity();
    velocity.setY(Math.max(MAX_GRAVITY, velocity.getY() - indicator.getGravity()));
    indicator.setVelocity(velocity);
    float percent = (float) indicator.getLife() / indicator.getMaxLife();
    float scale;
    if (percent > 0.5) {
      scale = indicator.getMidScale() + ((percent - 0.5f) * 2 * (indicator.getEndScale() - indicator.getMidScale()));
    } else {
      scale = indicator.getStartScale() + (percent * 2 * (indicator.getMidScale() - indicator.getStartScale()));
    }
    ((DisplayHologramData) hologram.getData()).getTranslation().add((float) velocity.getX(), (float) velocity.getY(), 0);
    ((DisplayHologramData) hologram.getData()).getScale().set(new Vector3f(scale));
    Bukkit.getScheduler().runTaskAsynchronously(LootPlugin.getInstance(), () -> {
      hologram.refreshForViewers();
      //hologram.forceShowHologram(indicator.getViewer());
    });
    return false;
  }

  private static void deletePopoff(DamagePopoff indicator) {
    Hologram hologram = indicator.getHologram();
    if (indicator.getViewer() != null) {
      Bukkit.getScheduler().runTaskAsynchronously(StrifePlugin.getInstance(), () -> {
        hologram.forceHideHologram(indicator.getViewer());
        hologram.deleteHologram();
        //hologram.forceUpdate();
      });
    }
  }
}
