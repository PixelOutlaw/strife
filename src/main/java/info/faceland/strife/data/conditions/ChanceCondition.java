package info.faceland.strife.data.conditions;

import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.stats.StrifeStat;
import info.faceland.strife.util.DamageUtil;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.potion.PotionEffectType;

public class ChanceCondition implements Condition {

  private final float chance;
  private final Map<StrifeStat, Float> statMults = new HashMap<>();

  public ChanceCondition(float chance) {
    this.chance = chance;
  }

  public boolean isMet(StrifeMob attacker, StrifeMob target) {
    float bonusChance = 1f;
    for (StrifeStat attr : statMults.keySet()) {
      bonusChance += attacker.getStat(attr) * statMults.get(attr);
    }
    return DamageUtil.rollBool(chance * bonusChance,
        attacker.getEntity().hasPotionEffect(PotionEffectType.LUCK));
  }

  public CompareTarget getCompareTarget() {
    return CompareTarget.SELF;
  }
}
