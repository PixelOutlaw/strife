package land.face.strife.data.conditions;

import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.stats.StrifeStat;
import land.face.strife.util.PlayerDataUtil;
import org.bukkit.entity.Player;

public class EnergyCondition extends Condition {

  private final boolean percentage;

  public EnergyCondition(boolean percentage) {
    this.percentage = percentage;
  }

  public boolean isMet(StrifeMob attacker, StrifeMob target) {
    double energy;
    if (!(target.getEntity() instanceof Player)) {
      return true;
    }
    if (percentage) {
      if (getCompareTarget() == CompareTarget.SELF) {
        if (attacker.getStat(StrifeStat.ENERGY) == 0D) {
          return PlayerDataUtil.conditionCompare(getComparison(), 0D, getValue());
        }
        energy = StrifePlugin.getInstance().getEnergyManager().getEnergy(attacker) / attacker
            .getStat(StrifeStat.ENERGY);
      } else {
        if (target.getStat(StrifeStat.ENERGY) == 0D) {
          return PlayerDataUtil.conditionCompare(getComparison(), 0D, getValue());
        }
        energy = StrifePlugin.getInstance().getEnergyManager().getEnergy(target) / target
            .getStat(StrifeStat.ENERGY);
      }
    } else {
      energy = getCompareTarget() == CompareTarget.SELF ?
          StrifePlugin.getInstance().getEnergyManager().getEnergy(attacker) :
          StrifePlugin.getInstance().getEnergyManager().getEnergy(target);
    }
    return PlayerDataUtil.conditionCompare(getComparison(), energy, getValue());
  }
}
