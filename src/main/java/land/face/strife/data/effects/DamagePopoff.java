package land.face.strife.data.effects;

import de.oliver.fancyholograms.api.hologram.Hologram;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

@Getter
@Setter
public class DamagePopoff {

  private Player viewer;
  private float[] sizeArray;
  private int maxLife;
  private int midLife;
  private Hologram hologram;
  private Vector velocity;
  private double gravity;
  private int life;

  public void setMaxLife(int amount) {
    this.maxLife = amount;
    this.midLife = amount / 2;
  }

}
