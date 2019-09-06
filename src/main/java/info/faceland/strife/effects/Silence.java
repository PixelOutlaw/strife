package info.faceland.strife.effects;

import info.faceland.strife.data.StrifeMob;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class Silence extends Effect {

  private int duration;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    if (target.getEntity() instanceof Player) {
      ((Player) target.getEntity()).setCooldown(Material.DIAMOND_CHESTPLATE, Math.max(duration,
          ((Player) target.getEntity()).getCooldown(Material.DIAMOND_CHESTPLATE)));
    }
  }

  public int getDuration() {
    return duration;
  }

  public void setDuration(int duration) {
    this.duration = duration;
  }

}
