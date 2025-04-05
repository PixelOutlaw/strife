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
      double gravity, int life, String text, boolean shadow) {
    return createPopoff(player, location, velocity, gravity, life, text, 1.0f, 1.0f, 1.0f, shadow);
  }

  public static DamagePopoff createPopoff(Player player, Location location, Vector velocity,
      double gravity, int life, String text, float startScale, float midScale, float endScale, boolean shadow) {

    TextHologramData textData = new TextHologramData(UUID.randomUUID().toString(), location);
    textData.setText(List.of(text));
    textData.setInterpolationDuration(1);
    textData.setBackground(Hologram.TRANSPARENT);
    textData.setBillboard(Billboard.CENTER);
    textData.setBrightness(new Brightness(13, 13));
    //textData.setIn(2);
    textData.setTextShadow(shadow);
    textData.setPersistent(false);
    textData.getScale().set(startScale);

    var manager = FancyHologramsPlugin.get().getHologramManager();

    Hologram holo = manager.create(textData);
    //holo.createHologram();

    Bukkit.getScheduler().runTaskAsynchronously(StrifePlugin.getInstance(), () -> holo.forceShowHologram(player));

    DamagePopoff indicator = new DamagePopoff();

    indicator.setSizeArray(createArray(startScale, midScale, endScale, life));

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
    float scale = indicator.getSizeArray()[indicator.getMaxLife() - indicator.getLife()];
    indicator.setLife(indicator.getLife() - 1);
    Hologram hologram = indicator.getHologram();

    Vector velocity = indicator.getVelocity();
    velocity.setY(Math.max(MAX_GRAVITY, velocity.getY() - indicator.getGravity()));
    indicator.setVelocity(velocity);
    ((DisplayHologramData) hologram.getData()).getTranslation().add((float) velocity.getX(), (float) velocity.getY(), 0);
    ((DisplayHologramData) hologram.getData()).getScale().set(scale);
    //hologram.forceShowHologram(indicator.getViewer());
    Bukkit.getScheduler().runTaskAsynchronously(LootPlugin.getInstance(), hologram::refreshForViewers);
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

  public static float[] createArray(float start, float middle, float end, int size) {
    // Check for valid size
    if (size < 3) {
      throw new IllegalArgumentException("Array size must be at least 3");
    }
    // Create the array
    float[] result = new float[size];
    // Calculate the position of the middle value (centered if size is odd)
    int midIndex = (size - 1) / 2;
    // Set the known values
    result[0] = start;
    result[midIndex] = middle;
    result[size - 1] = end;
    // Calculate step sizes for left and right halves
    double leftStep = (middle - start) / midIndex;
    double rightStep = (end - middle) / (size - 1 - midIndex);
    // Fill left half
    for (int i = 1; i < midIndex; i++) {
      result[i] = (float) (start + (i * leftStep));
    }
    // Fill right half
    for (int i = midIndex + 1; i < size - 1; i++) {
      result[i] = (float) (middle + ((i - midIndex) * rightStep));
    }
    return result;
  }
}
