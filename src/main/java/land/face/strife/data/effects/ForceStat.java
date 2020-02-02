package land.face.strife.data.effects;

import java.util.Map;
import land.face.strife.data.StrifeMob;
import land.face.strife.stats.StrifeStat;

public class ForceStat extends Effect {

  private Map<StrifeStat, Float> stats;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    for (StrifeStat stat : stats.keySet()) {
      target.forceSetStat(stat, stats.get(stat));
    }
  }

  public void setStats(Map<StrifeStat, Float> stats) {
    this.stats = stats;
  }
}