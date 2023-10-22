package land.face.strife.data;

import com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor;
import land.face.strife.events.LocationChangeEvent;
import land.face.strife.events.RuneChangeEvent;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@Getter
public class TopBarData {

  @Setter
  private String event = "";
  @Setter
  private String compass = "";
  private String location = "Somewhere, probably   ";
  @Setter
  private String clock = "";
  @Setter
  private String skills = "";

  public String getFinalTitle() {
    return event + compass + FaceColor.YELLOW + location + clock + skills + "\uF808";
  }

  public void setLocation(Player player, String location) {
    if (!this.location.equals(location)) {
      Bukkit.getPluginManager().callEvent(new LocationChangeEvent(player, location));
    }
    this.location = location;
  }

}
