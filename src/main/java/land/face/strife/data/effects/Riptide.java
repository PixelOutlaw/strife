package land.face.strife.data.effects;

import land.face.strife.data.StrifeMob;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Riptide extends Effect {

  @Setter
  private int ticks;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    if (target.getEntity() instanceof Player p) {
      p.startRiptideAttack(ticks, 0f, new ItemStack(Material.TRIDENT));
    }
  }


}