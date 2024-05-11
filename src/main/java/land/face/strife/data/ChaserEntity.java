package land.face.strife.data;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

@Getter
@Setter
public class ChaserEntity {

  private final StrifeMob caster;
  private final String chaserId;
  private final int lifespan;
  private final float speed;
  private final float maxSpeed;

  private Vector velocity;
  private Location location;
  private LivingEntity target;
  private int currentTick;

  public ChaserEntity(final StrifeMob caster, String chaserId, Location location,
      LivingEntity target, float speed, float maxSpeed, Vector velocity, int lifespan) {
    this.caster = caster;
    this.chaserId = chaserId;
    this.speed = speed;
    this.maxSpeed = maxSpeed;
    this.velocity = velocity;
    this.lifespan = lifespan;
    this.location = location;
    this.target = target;
    this.currentTick = 0;
  }

}
