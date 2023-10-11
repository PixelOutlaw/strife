package land.face.strife.data;

import com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor;
import de.oliver.fancyholograms.api.Hologram;
import java.util.List;
import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.entity.Display.Brightness;
import org.bukkit.util.Vector;
import org.joml.Vector3f;

@Data
public class DisplayFrame {

  private String text = null;
  private Brightness brightness = null;
  private Vector3f scale = null;
  private Vector3f translation = null;
  private Vector rotation = null;

  public static DisplayFrame fromString(String str) {
    DisplayFrame displayFrame = new DisplayFrame();
    String[] segments = str.split("\\|");
    for (String s : segments) {
      if (s.startsWith("text:")) {
        displayFrame.setText(s.replace("text:", ""));
      } else if (s.startsWith("brightness:")) {
        int brightness = Integer.parseInt(s.replace("brightness:", ""));
        displayFrame.setBrightness(new Brightness(brightness, brightness));
      } else if (s.startsWith("scale:")) {
        String[] strs = s.replace("scale:", "").split(",");
        displayFrame.setScale(
            new Vector3f(Float.parseFloat(strs[0]), Float.parseFloat(strs[1]), Float.parseFloat(strs[2])));
      } else if (s.startsWith("translation:")) {
        String[] strs = s.replace("translation:", "").split(",");
        displayFrame.setTranslation(
            new Vector3f(Float.parseFloat(strs[0]), Float.parseFloat(strs[1]), Float.parseFloat(strs[2])));
      }
      if (s.startsWith("rotation:")) {
        String[] strs = s.replace("rotation:", "").split(",");
        displayFrame.setRotation(
            new Vector(Float.parseFloat(strs[0]), Float.parseFloat(strs[1]), Float.parseFloat(strs[2])));
        displayFrame.getRotation().normalize();
      }
    }
    return displayFrame;
  }

  public void applyToDisplay(Hologram hologram) {
    applyToDisplay(hologram, null);
  }

  public void applyToDisplay(Hologram hologram, FaceColor color) {
    if (text != null) {
      hologram.getData().setText(List.of(text));
    }
    if (brightness != null) {
      hologram.getData().setBrightness(brightness);
    }
    if (rotation != null) {
      hologram.getData().getLocation().setDirection(rotation);
    }
    if (scale != null) {
      hologram.getData().getScale().set(scale.x, scale.y, scale.z);
    }
    if (translation != null) {
      hologram.getData().getTranslation().set(translation.x, translation.y, translation.z);
    }
    hologram.updateHologram();
    hologram.refreshHologram(Bukkit.getOnlinePlayers());
  }
}
