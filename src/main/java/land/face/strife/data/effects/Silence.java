package land.face.strife.data.effects;

import land.face.strife.data.StrifeMob;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class Silence extends Effect {

  private int duration;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    if (target.getEntity() instanceof Player) {
      ((Player) target.getEntity()).setCooldown(Material.GOLDEN_CHESTPLATE, Math.max(duration,
          ((Player) target.getEntity()).getCooldown(Material.GOLDEN_CHESTPLATE)));
    }
  }

  public int getDuration() {
    return duration;
  }

  public void setDuration(int duration) {
    this.duration = duration;
  }

}
