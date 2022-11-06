package land.face.strife.data.effects;

import land.face.strife.StrifePlugin;
import land.face.strife.data.BonusDamage;
import land.face.strife.data.StrifeMob;
import land.face.strife.managers.IndicatorManager.IndicatorStyle;
import land.face.strife.stats.StrifeStat;
import land.face.strife.util.DamageUtil;
import land.face.strife.util.DamageUtil.DamageScale;
import land.face.strife.util.PlayerDataUtil;
import lombok.Setter;
import org.bukkit.entity.Player;

public class Heal extends Effect {

  @Setter
  private float amount;
  @Setter
  private DamageScale damageScale;
  @Setter
  private float flatBonus;
  @Setter
  private int tickDuration;
  @Setter
  private boolean useHealingPower;
  @Setter
  private boolean healCaster;
  @Setter
  private boolean selfHealPenalty;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {

    if (healCaster ? caster.getEntity().isDead() : target.getEntity().isDead()) {
      return;
    }

    float heal = amount;

    BonusDamage container = new BonusDamage(damageScale, null, null, heal);
    heal = DamageUtil.applyDamageScale(caster, target, container);
    heal += flatBonus;

    if (useHealingPower) {
      heal *= 1 + caster.getStat(StrifeStat.HEALING_POWER) / 100;
    }

    if (selfHealPenalty && caster == target) {
      heal *= 0.5;
    }

    if (tickDuration == -1 && caster.getEntity() instanceof Player) {
      if (!healCaster && caster != target) {
        String healText = "&a" + DamageUtil.buildDamageString((int) heal);
        StrifePlugin.getInstance().getIndicatorManager().addIndicator(caster.getEntity(),
            target.getEntity(), IndicatorStyle.FLOAT_UP_MEDIUM, 8, healText);
      }
    }

    if (tickDuration == -1) {
      DamageUtil.restoreHealth(healCaster ?
          caster.getEntity() : target.getEntity(), applyMultipliers(caster, heal));
    } else {
      PlayerDataUtil.restoreHealthOverTime(healCaster ?
          caster.getEntity() : target.getEntity(), applyMultipliers(caster, heal), tickDuration);
    }
  }
}