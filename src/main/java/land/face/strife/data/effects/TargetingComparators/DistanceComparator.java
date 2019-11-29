package land.face.strife.data.effects.TargetingComparators;

import java.util.Comparator;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

public class DistanceComparator implements Comparator<LivingEntity> {

  private Location loc;

  public int compare(LivingEntity le1, LivingEntity le2) {
    return Double
        .compare(le1.getLocation().distanceSquared(loc), le2.getLocation().distanceSquared(loc));
  }

  public void setLoc(Location loc) {
    this.loc = loc;
  }
}
