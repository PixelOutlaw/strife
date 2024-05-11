package land.face.strife.data;

import java.util.HashSet;
import java.util.Set;
import land.face.strife.timers.SoulTimer;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

@Getter @Setter
public class TargetResponse {

  private final Set<LivingEntity> entities = new HashSet<>();
  private final Set<SoulTimer> souls = new HashSet<>();
  private Location location = null;
  private boolean cancelOnCasterDeath = false;
  private boolean force = false;

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

  public TargetResponse(Set<LivingEntity> entities, boolean cancelOnCasterDeath) {
    this.entities.addAll(entities);
    this.cancelOnCasterDeath = cancelOnCasterDeath;
  }

}
