package land.face.strife.data.effects;

import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.stats.StrifeStat;
import lombok.Setter;

public class ChangeJumps extends Effect {

  @Setter
  private int amount;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    if (target.getStat(StrifeStat.AIR_JUMPS) == 0) {
      return;
    }
    int changeAmount = amount;
    target.setAirJumps(Math.min(target.getMaxAirJumps(), Math.max(0, target.getAirJumps() + changeAmount)));
    StrifePlugin.getInstance().getGuiManager().updateAirJumpDisplay(target);
  }
}
