package land.face.strife.data.effects;

import eu.decentsoftware.holograms.api.holograms.Hologram;
import org.bukkit.util.Vector;

public class DamagePopoff {

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

}
