package land.face.strife.data.effects;

import de.oliver.fancyholograms.api.Hologram;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class DamagePopoff {

  @Getter @Setter
  private Player viewer;
  @Getter @Setter
  private float startScale = 1;
  @Getter @Setter
  private float midScale = 1;
  @Getter @Setter
  private float endScale = 1;
  @Getter
  private int maxLife;
  @Getter
  private int midLife;
  private Hologram hologram;
  private Vector velocity;
  private double gravity;
  private int life;

  public Hologram getHologram() {
    return hologram;
  }

  public void setHologram(Hologram hologram) {
    this.hologram = hologram;
  }

  public Vector getVelocity() {
    return velocity;
  }

  public void setVelocity(Vector velocity) {
    this.velocity = velocity;
  }

  public double getGravity() {
    return gravity;
  }

  public void setGravity(double gravity) {
    this.gravity = gravity;
  }

  public int getLife() {
    return life;
  }

  public void setLife(int life) {
    this.life = life;
  }

  public void setMaxLife(int amount) {
    this.maxLife = amount;
    this.midLife = amount / 2;
  }

}
