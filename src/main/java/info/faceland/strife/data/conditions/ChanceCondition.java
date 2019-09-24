package info.faceland.strife.data.conditions;

import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.stats.StrifeStat;
import info.faceland.strife.util.DamageUtil;
import org.bukkit.potion.PotionEffectType;

public class ChanceCondition extends Condition {

  private final float chance;

  public ChanceCondition(float chance) {
    this.chance = chance;
  }

  public boolean isMet(StrifeMob attacker, StrifeMob target) {
    float bonusChance = 1f;
    for (StrifeStat attr : getStatMults().keySet()) {
      bonusChance += attacker.getStat(attr) * getStatMults().get(attr);
    }
    return DamageUtil.rollBool(chance * bonusChance,
        attacker.getEntity().hasPotionEffect(PotionEffectType.LUCK));
  }

  public CompareTarget getCompareTarget() {
    return CompareTarget.SELF;
  }
}
