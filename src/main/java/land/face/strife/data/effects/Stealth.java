package land.face.strife.data.effects;

import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import org.bukkit.entity.Player;

public class Stealth extends Effect {

  private boolean removeStealth;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    if (!(target.getEntity() instanceof Player)) {
      return;
    }
    if (removeStealth) {
      StrifePlugin.getInstance().getStealthManager().unstealthPlayer((Player) target.getEntity());
    } else {
      StrifePlugin.getInstance().getStealthManager().stealthPlayer((Player) target.getEntity());
    }
  }

  public void setRemoveStealth(boolean removeStealth) {
    this.removeStealth = removeStealth;
  }
}