package land.face.strife.data.effects;

import java.text.DecimalFormat;
import land.face.strife.StrifePlugin;
import land.face.strife.data.BonusDamage;
import land.face.strife.data.StrifeMob;
import land.face.strife.managers.IndicatorManager.IndicatorStyle;
import land.face.strife.stats.StrifeStat;
import land.face.strife.util.DamageUtil;
import land.face.strife.util.DamageUtil.DamageScale;
import org.bukkit.entity.Player;

public class Heal extends Effect {

  private static final DecimalFormat INT_FORMAT = new DecimalFormat("#");

  private float amount;
  private DamageScale damageScale;
  private float flatBonus;
  private boolean useHealingPower;
  private boolean healCaster;

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

    if (!healCaster && caster != target && caster.getEntity() instanceof Player) {
      String healText = "&a&l+" + INT_FORMAT.format(heal);
      StrifePlugin.getInstance().getIndicatorManager().addIndicator(caster.getEntity(),
          target.getEntity(), IndicatorStyle.FLOAT_UP_SLOW, 8, healText);
    }

    DamageUtil.restoreHealth(healCaster ? caster.getEntity() : target.getEntity(), applyMultipliers(caster, heal));
  }

  public void setAmount(float amount) {
    this.amount = amount;
  }

  public void setDamageScale(DamageScale damageScale) {
    this.damageScale = damageScale;
  }

  public void setFlatBonus(float flatBonus) {
    this.flatBonus = flatBonus;
  }

  public void setUseHealingPower(boolean useHealingPower) {
    this.useHealingPower = useHealingPower;
  }

  public void setHealCaster(boolean healCaster) {
    this.healCaster = healCaster;
  }
}