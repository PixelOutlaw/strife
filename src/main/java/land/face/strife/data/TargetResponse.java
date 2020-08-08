package land.face.strife.data;

import java.util.Set;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

public class TargetResponse {

  private Set<LivingEntity> entities = null;
  private Location location = null;

  public Set<LivingEntity> getEntities() {
    return entities;
  }

  public void setEntities(Set<LivingEntity> entities) {
    this.entities = entities;
  }

  public Location getLocation() {
    return location;
  }

  public void setLocation(Location location) {
    this.location = location;
  }

}
