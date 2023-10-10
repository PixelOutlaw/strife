package land.face.strife.tasks;

import com.tealcube.minecraft.bukkit.facecore.utilities.ChunkUtil;
import com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor;
import land.face.strife.StrifePlugin;
import land.face.strife.data.DisplayFrame;
import land.face.strife.data.pojo.DisplayContainer;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Display.Billboard;
import org.bukkit.entity.Display.Brightness;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.TextDisplay;
import org.bukkit.scheduler.BukkitTask;

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
  private TextDisplay display;
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
    if (location != null) {
      display = location.getWorld().spawn(location, TextDisplay.class, (e) -> {
        e.setInterpolationDelay(0);
        e.setInterpolationDuration(1);
        e.setBrightness(new Brightness(15, 15));
        e.setText("");
        e.setBillboard(Billboard.CENTER);
        e.setShadowed(false);
        e.setBackgroundColor(Color.fromARGB(0,0,0,0));
      });
      ChunkUtil.setDespawnOnUnload(display);
    } else if (livingEntity != null) {
      display = location.getWorld().spawn(location, TextDisplay.class, (e) -> {
        e.setInterpolationDelay(0);
        e.setInterpolationDuration(1);
        e.setBrightness(new Brightness(15, 15));
        e.setText("");
        e.setBillboard(Billboard.CENTER);
        e.setShadowed(false);
        e.setBackgroundColor(Color.fromARGB(0,0,0,0));
      });
      ChunkUtil.setDespawnOnUnload(display);
    }
    task = Bukkit.getScheduler().runTaskTimer(StrifePlugin.getInstance(), this::tick, 0L, 1L);
  }

  private void tick() {
    if (frame >= displayContainer.getFrames().size()) {
      frame = 0;
      loop++;
    }
    if (loop > displayContainer.getLoops() || !display.isValid()) {
      display.remove();
      task.cancel();
      return;
    }
    DisplayFrame displayFrame = displayContainer.getFrames().get(frame);
    if (livingEntity != null) {
      if (!livingEntity.isValid()) {
        display.remove();
        task.cancel();
        return;
      }
      Location loc = livingEntity.getLocation().clone();
      if (verticalOffset != 0) {
        loc.setY(loc.getY() + verticalOffset);
      }
      if (offset != 0) {
        loc.add(livingEntity.getLocation().getDirection().multiply(offset));
      }
      display.teleport(loc);
    }
    frame++;
    displayFrame.applyToDisplay(display, color);
  }

}
