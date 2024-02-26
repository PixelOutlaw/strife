package land.face.strife.util;

import de.oliver.fancyholograms.api.FancyHologramsPlugin;
import de.oliver.fancyholograms.api.Hologram;
import de.oliver.fancyholograms.api.HologramManager;
import de.oliver.fancyholograms.api.HologramType;
import de.oliver.fancyholograms.api.data.DisplayHologramData;
import de.oliver.fancyholograms.api.data.HologramData;
import de.oliver.fancyholograms.api.data.TextHologramData;
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

    DisplayHologramData displayData = DisplayHologramData.getDefault(location);
    displayData.setTranslation(new Vector3f(0));
    displayData.setBillboard(Billboard.CENTER);
    displayData.setScale(new Vector3f(startScale));
    displayData.setBrightness(new Brightness(10, 10));

    TextHologramData textData = TextHologramData.getDefault("");
    textData.setBackground(Hologram.TRANSPARENT);
    textData.setText(List.of(text));
    textData.setTextShadow(false);

    HologramData data = new HologramData(UUID.randomUUID().toString(), displayData, HologramType.TEXT, textData);

    Hologram holo = FancyHologramsPlugin.get().getHologramManager().create(data);
    Bukkit.getScheduler().runTaskAsynchronously(StrifePlugin.getInstance(), () ->
        holo.showHologram(List.of()));
    holo.createHologram();
    Bukkit.getScheduler().runTaskAsynchronously(StrifePlugin.getInstance(), () ->
        holo.showHologram(player));

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
    hologram.getData().getDisplayData().getTranslation().add((float) velocity.getX(), (float) velocity.getY(), 0);
    hologram.getData().getDisplayData().setScale(new Vector3f(scale));
    hologram.updateHologram();
    hologram.refreshHologram(indicator.getViewer());
    return false;
  }

  private static void deletePopoff(DamagePopoff indicator) {
    Hologram hologram = indicator.getHologram();
    if (indicator.getViewer() != null) {
      Bukkit.getScheduler().runTaskAsynchronously(StrifePlugin.getInstance(), () -> {
        hologram.hideHologram(indicator.getViewer());
        hologram.deleteHologram();
      });
    }
  }
}
