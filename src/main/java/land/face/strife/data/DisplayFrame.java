package land.face.strife.data;

import com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor;
import lombok.Data;
import org.bukkit.entity.Display.Brightness;
import org.bukkit.entity.TextDisplay;
import org.bukkit.util.Transformation;
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
        displayFrame.getScale().set(Float.parseFloat(strs[0]), Float.parseFloat(strs[1]), Float.parseFloat(strs[2]));
      } else if (s.startsWith("translation:")) {
        String[] strs = s.replace("translation:", "").split(",");
        displayFrame.setTranslation(
            new Vector3f(Float.parseFloat(strs[0]), Float.parseFloat(strs[1]), Float.parseFloat(strs[2])));
      } if (s.startsWith("rotation:")) {
        String[] strs = s.replace("rotation:", "").split(",");
        displayFrame.setRotation(
            new Vector(Float.parseFloat(strs[0]), Float.parseFloat(strs[1]), Float.parseFloat(strs[2])));
        displayFrame.getRotation().normalize();
      }
    }
    return displayFrame;
  }

  public void applyToDisplay(TextDisplay textDisplay) {
    applyToDisplay(textDisplay, null);
  }

  public void applyToDisplay(TextDisplay textDisplay, FaceColor color) {
    if (text != null) {
      textDisplay.setText(color == null ? "" : color + text);
    }
    if (brightness != null) {
      textDisplay.setBrightness(brightness);
    }
    if (rotation != null) {
      textDisplay.getLocation().setDirection(rotation);
    }
    if (scale != null || translation != null) {
      Transformation transformation = textDisplay.getTransformation();
      if (scale != null) {
        transformation.getScale().set(scale);
      }
      if (translation != null) {
        transformation.getTranslation().set(translation);
      }
      textDisplay.setTransformation(transformation);
    }
  }
}
