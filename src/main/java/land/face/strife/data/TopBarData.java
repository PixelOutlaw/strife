package land.face.strife.data;

import static com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor.YELLOW;
import static com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor.color;

import com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor;
import java.util.HashMap;
import java.util.Map;
import land.face.strife.events.LocationChangeEvent;
import land.face.strife.managers.BossBarManager;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.Style;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@Getter
public class TopBarData {

  private TextComponent event = Component.empty();
  private TextComponent compass = Component.empty();
  private TextComponent location = Component.empty();
  private TextComponent clock = Component.empty();
  private TextComponent skills = Component.empty();

  private static final TextComponent buffer = Component.text("\uF808");

  public TextComponent getFinalTitle() {
    return Component.textOfChildren(event, compass, location, clock, skills, buffer);
  }

  public boolean setEvent(TextComponent str) {
    if (str == null/* || str.content().equals(event.content()) */) {
      return false;
    }
    event = str;
    return true;
  }

  public boolean setCompass(TextComponent str) {
    if (str == null || str.content().equals(compass.content())) {
      return false;
    }
    compass = str;
    return true;
  }

  public boolean setLocation(Player player, String location) {
    if (location == null || location.equals(this.location.content())) {
      return false;
    }
    Bukkit.getPluginManager().callEvent(new LocationChangeEvent(player, location));
    this.location = color(Component.text(location), YELLOW);
    return true;
  }

  public boolean setClock(TextComponent str) {
    if (str == null || str.content().equals(clock.content())) {
      return false;
    }
    clock = str;
    return true;
  }

  public boolean setSkills(TextComponent str) {
    if (str == null/* || str.content().equals(skills.content())*/) {
      return false;
    }
    skills = str;
    return true;
  }

}
