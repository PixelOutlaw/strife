package land.face.strife.data;

import com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor;
import land.face.strife.events.LocationChangeEvent;
import land.face.strife.managers.BossBarManager;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@Getter
public class TopBarData {

  private String cachedEventStr = "";
  private String cachedCompassStr = "";
  private String cachedLocationStr = "";
  private String cachedClockStr = "";
  private String cachedSkillsStr = "";

  private TextComponent event = Component.empty();
  private TextComponent compass = Component.empty();
  private TextComponent location = Component.empty();
  private TextComponent clock = Component.empty();
  private TextComponent skills = Component.empty();

  private static final TextComponent buffer = Component.text("\uF808");

  public TextComponent getFinalTitle() {
    return Component.textOfChildren(event, compass, location, clock, skills, buffer);
  }

  public boolean setEvent(String str) {
    if (cachedEventStr.equals(str) || str == null) {
      return false;
    }
    cachedEventStr = str;
    event = BossBarManager.covertStringToRetardComponent(str);
    return true;
  }

  public boolean setCompass(String str) {
    if (cachedCompassStr.equals(str) || str == null) {
      return false;
    }
    cachedCompassStr = str;
    compass = BossBarManager.covertStringToRetardComponent(str);
    return true;
  }

  public boolean setLocation(Player player, String location) {
    if (cachedLocationStr.equals(location) || location == null) {
      return false;
    }
    Bukkit.getPluginManager().callEvent(new LocationChangeEvent(player, location));
    cachedLocationStr = location;
    this.location = BossBarManager.covertStringToRetardComponent(FaceColor.YELLOW + location);
    return true;
  }

  public boolean setClock(String str) {
    if (cachedClockStr.equals(str) || str == null) {
      return false;
    }
    cachedClockStr = str;
    clock = BossBarManager.covertStringToRetardComponent(str);
    return true;
  }

  public boolean setSkills(String str) {
    if (cachedSkillsStr.equals(str) || str == null) {
      return false;
    }
    cachedSkillsStr = str;
    skills = BossBarManager.covertStringToRetardComponent(str);
    return true;
  }

}
