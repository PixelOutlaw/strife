package land.face.strife.data.pojo;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.Player;

@Getter
@Setter
public class ViolationData {

  private Location lastLocation;
  private long violationSleepTime = System.currentTimeMillis() + 200L;
  private int level = 0;
  private float multiplier = 1.0f;

  public ViolationData(Player player) {
    lastLocation = player.getLocation();
  }

}
