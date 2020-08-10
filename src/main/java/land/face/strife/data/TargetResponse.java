package land.face.strife.data;

import java.util.HashSet;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

public class TargetResponse {

  private final Set<LivingEntity> entities = new HashSet<>();
  private Location location = null;

  public TargetResponse(Set<LivingEntity> entities, Location location) {
    this.entities.addAll(entities);
    this.location = location;
  }

  public TargetResponse(Set<LivingEntity> entities) {
    this.entities.addAll(entities);
  }

  public TargetResponse(Location location) {
    this.location = location;
  }

  public Set<LivingEntity> getEntities() {
    return entities;
  }

  public Location getLocation() {
    return location;
  }

  public void setLocation(Location location) {
    this.location = location;
  }

}
