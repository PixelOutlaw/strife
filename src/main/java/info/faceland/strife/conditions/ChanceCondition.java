package info.faceland.strife.conditions;

import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.stats.StrifeStat;
import info.faceland.strife.util.DamageUtil;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.potion.PotionEffectType;

public class ChanceCondition implements Condition {

  private final double chance;
  private final Map<StrifeStat, Double> statMults = new HashMap<>();

  public ChanceCondition(double chance) {
    this.chance = chance;
  }

  public boolean isMet(StrifeMob attacker, StrifeMob target) {
    double bonusChance = 1;
    for (StrifeStat attr : statMults.keySet()) {
      bonusChance += attacker.getStat(attr) * statMults.get(attr);
    }
    return DamageUtil.rollBool(chance * bonusChance,
        attacker.getEntity().hasPotionEffect(PotionEffectType.LUCK));
  }
}
