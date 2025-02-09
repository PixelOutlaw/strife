package land.face.strife.tasks;

import com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor;
import de.oliver.fancyholograms.api.FancyHologramsPlugin;
import de.oliver.fancyholograms.api.data.DisplayHologramData;
import de.oliver.fancyholograms.api.data.HologramData;
import de.oliver.fancyholograms.api.data.TextHologramData;
import de.oliver.fancyholograms.api.hologram.Hologram;
import de.oliver.fancyholograms.api.hologram.HologramType;
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

  private boolean hideFromTarget = false;

  @Setter
  private FaceColor color = null;
  private Hologram display;
  private BukkitTask task;

  public DisplayRunner(DisplayContainer displayContainer, Location location) {
    this.displayContainer = displayContainer;
    this.location = location;
    setup();
  }

  public DisplayRunner(DisplayContainer displayContainer, LivingEntity entity,
      boolean hideFromTarget, float offset, float verticalOffset) {
    this.displayContainer = displayContainer;
    this.offset = offset;
    this.verticalOffset = verticalOffset;
    this.livingEntity = entity;
    this.hideFromTarget = hideFromTarget;
    setup();
  }

  private void setup() {

    Location loc = null;
    if (location != null) {
      loc = location;
    } else if (livingEntity != null) {
      loc = TargetingUtil.getOriginLocation(livingEntity, OriginLocation.CENTER);
    }

    var manager = FancyHologramsPlugin.get().getHologramManager();

    TextHologramData textData = new TextHologramData(UUID.randomUUID().toString(), loc);
    textData.setText(List.of(""));
    textData.setBackground(Hologram.TRANSPARENT);
    textData.setBillboard(Billboard.CENTER);
    textData.setBrightness(new Brightness(10, 10));
    textData.setTextShadow(false);
    textData.setPersistent(false);

    Hologram holo = manager.create(textData);
    //holo.createHologram();

    var players = Bukkit.getOnlinePlayers();
    Bukkit.getScheduler().runTaskAsynchronously(StrifePlugin.getInstance(), () -> {
      if (hideFromTarget && livingEntity != null) {
        players.removeIf(p -> p.getUniqueId().equals(livingEntity.getUniqueId()));
      }
      for (var p : players) {
        holo.forceShowHologram(p);
      }
    });
    display = holo;
    task = Bukkit.getScheduler().runTaskTimer(StrifePlugin.getInstance(), this::tick, 0L, 1L);
  }

  private void tick() {
    if (frame >= displayContainer.getFrames().size()) {
      frame = 0;
      loop++;
    }
    if (loop > displayContainer.getLoops()) {
      var pls = Bukkit.getOnlinePlayers();
      Bukkit.getScheduler().runTaskAsynchronously(StrifePlugin.getInstance(), () -> {
        for (var p : pls) {
          display.forceHideHologram(p);
        }
        display.deleteHologram();
      });
      task.cancel();
      return;
    }
    DisplayFrame displayFrame = displayContainer.getFrames().get(frame);
    if (livingEntity != null) {
      if (!livingEntity.isValid()) {
        var pls = Bukkit.getOnlinePlayers();
        Bukkit.getScheduler().runTaskAsynchronously(StrifePlugin.getInstance(), () -> {
          for (var p : pls) {
            display.forceHideHologram(p);
          }
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
      display.getData().setLocation(loc);
    }
    frame++;
    displayFrame.applyToDisplay(display, color);
    display.refreshForViewers();
  }
}
