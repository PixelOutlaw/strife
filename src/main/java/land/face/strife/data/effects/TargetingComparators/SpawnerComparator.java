package land.face.strife.data.effects.TargetingComparators;

import java.util.Comparator;
import land.face.strife.data.Spawner;
import org.bukkit.Location;

public class SpawnerComparator implements Comparator<Spawner> {

  private Location loc;

  public int compare(Spawner s1, Spawner s2) {
    if (loc.getWorld() != s1.getLocation().getWorld() || loc.getWorld() != s2.getLocation()
        .getWorld()) {
      throw new IllegalArgumentException("Cannot compare spawners in differing worlds!");
    }
    return Double
        .compare(s1.getLocation().distanceSquared(loc), s2.getLocation().distanceSquared(loc));
  }

  public void setLoc(Location loc) {
    this.loc = loc;
  }
}
