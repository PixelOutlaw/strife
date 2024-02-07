package land.face.strife.tasks;

import com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor;
import de.oliver.fancyholograms.api.FancyHologramsPlugin;
import de.oliver.fancyholograms.api.Hologram;
import de.oliver.fancyholograms.api.HologramType;
import de.oliver.fancyholograms.api.data.DisplayHologramData;
import de.oliver.fancyholograms.api.data.HologramData;
import de.oliver.fancyholograms.api.data.TextHologramData;
import java.util.List;
import java.util.UUID;
import land.face.strife.StrifePlugin;
import land.face.strife.data.DisplayFrame;
import land.face.strife.data.pojo.DisplayContainer;
import land.face.strife.util.DamageUtil.OriginLocation;
import land.face.strife.util.TargetingUtil;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Display.Billboard;
import org.bukkit.entity.Display.Brightness;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitTask;
import org.joml.Vector3f;

public class DisplayRunner {

  private final DisplayContainer displayContainer;
  private Location location = null;
  private LivingEntity livingEntity = null;
  private float verticalOffset = 0;
  private float offset = 0;

  private int frame = 0;
  private int loop = 1;

  @Setter
  private FaceColor color = null;
  private Hologram display;
  private BukkitTask task;

  public DisplayRunner(DisplayContainer displayContainer, Location location) {
    this.displayContainer = displayContainer;
    this.location = location;
    setup();
  }

  public DisplayRunner(DisplayContainer displayContainer, LivingEntity entity, float offset, float verticalOffset) {
    this.displayContainer = displayContainer;
    this.offset = offset;
    this.verticalOffset = verticalOffset;
    this.livingEntity = entity;
    setup();
  }

  private void setup() {

    Location loc = null;
    if (location != null) {
      loc = location;
    } else if (livingEntity != null) {
      loc = TargetingUtil.getOriginLocation(livingEntity, OriginLocation.CENTER);
    }
    DisplayHologramData displayData = DisplayHologramData.getDefault(loc);
    displayData.setTranslation(new Vector3f(0));
    displayData.setBillboard(Billboard.CENTER);
    displayData.setScale(new Vector3f(1));
    displayData.setBrightness(new Brightness(9, 9));

    TextHologramData textData = TextHologramData.getDefault("");
    textData.setBackground(Hologram.TRANSPARENT);
    textData.setTextShadow(false);

    HologramData data = new HologramData(UUID.randomUUID().toString(), displayData, HologramType.TEXT, textData);

    Hologram holo = FancyHologramsPlugin.get().getHologramManager().create(data);
    holo.createHologram();
    Bukkit.getScheduler().runTaskAsynchronously(StrifePlugin.getInstance(), () ->
        holo.showHologram(Bukkit.getOnlinePlayers()));
    display = holo;
    task = Bukkit.getScheduler().runTaskTimer(StrifePlugin.getInstance(), this::tick, 0L, 1L);
  }

  private void tick() {
    if (frame >= displayContainer.getFrames().size()) {
      frame = 0;
      loop++;
    }
    if (loop > displayContainer.getLoops()) {
      Bukkit.getScheduler().runTaskAsynchronously(StrifePlugin.getInstance(), () -> {
        display.hideHologram(Bukkit.getOnlinePlayers());
        display.deleteHologram();
      });
      task.cancel();
      return;
    }
    DisplayFrame displayFrame = displayContainer.getFrames().get(frame);
    if (livingEntity != null) {
      if (!livingEntity.isValid()) {
        Bukkit.getScheduler().runTaskAsynchronously(StrifePlugin.getInstance(), () -> {
          display.hideHologram(Bukkit.getOnlinePlayers());
          display.deleteHologram();
        });
        task.cancel();
        return;
      }
      Location loc = TargetingUtil.getOriginLocation(livingEntity, OriginLocation.CENTER);
      if (verticalOffset != 0) {
        loc.setY(loc.getY() + verticalOffset);
      }
      if (offset != 0) {
        loc.add(livingEntity.getLocation().getDirection().multiply(offset));
      }
      display.getData().getDisplayData().setLocation(loc);
    }
    frame++;
    displayFrame.applyToDisplay(display, color);
    display.updateHologram();
  }
}
