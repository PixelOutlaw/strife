package land.face.strife.data.effects;

import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.stats.StrifeStat;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

@Getter
@Setter
public class Stealth extends Effect {

  private boolean removeStealth;
  private boolean strictDuration;
  private int ticks;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    if (!(target.getEntity() instanceof Player)) {
      return;
    }
    if (removeStealth) {
      StrifePlugin.getInstance().getStealthManager().unstealthPlayer((Player) target.getEntity(), false);
    } else {
      float seconds = (float) ticks / 20f;
      if (!strictDuration) {
        seconds *= 1f + caster.getStat(StrifeStat.EFFECT_DURATION) / 100;
      }
      StrifePlugin.getInstance().getStealthManager().stealthPlayer((Player) target.getEntity(), seconds);
    }
  }
}