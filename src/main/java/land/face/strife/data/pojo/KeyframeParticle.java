package land.face.strife.data.pojo;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Particle;

public class KeyframeParticle {

  private Particle particleEnum = null;
  @Getter @Setter
  private String particle = "FLAME";
  @Getter @Setter
  private int amount = 1;
  @Getter @Setter
  private double spread = 0;
  @Getter @Setter
  private double speed = 0;

  public void play(Location location) {
    if (particleEnum == null) {
      particleEnum = Particle.valueOf(particle);
    }
    location.getWorld().spawnParticle(particleEnum, location, amount, spread, spread, spread, speed);
  }

}
