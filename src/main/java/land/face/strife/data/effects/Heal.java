package land.face.strife.data.effects;

import static land.face.strife.util.DamageUtil.buildFloatIndicator;

import java.text.DecimalFormat;
import land.face.strife.StrifePlugin;
import land.face.strife.data.DamageContainer;
import land.face.strife.data.StrifeMob;
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

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    float heal = amount;
    for (StrifeStat attr : getStatMults().keySet()) {
      heal += getStatMults().get(attr) * caster.getStat(attr);
    }

    DamageContainer container = new DamageContainer(damageScale, null, null, heal);
    heal = DamageUtil.applyDamageScale(caster, target, container, null);
    heal += flatBonus;

    if (useHealingPower) {
      heal *= 1 + caster.getStat(StrifeStat.HEALING_POWER) / 100;
    }

    for (StrifeStat attr : getStatMults().keySet()) {
      heal *= 1 + getStatMults().get(attr) * caster.getStat(attr);
    }

    if (caster != target && caster.getEntity() instanceof Player) {
      String healText = "&a&l+" + INT_FORMAT.format(heal);
      StrifePlugin.getInstance().getIndicatorManager().addIndicator(caster.getEntity(),
          target.getEntity(), buildFloatIndicator((Player) caster.getEntity()), healText);
    }

    DamageUtil.restoreHealth(target.getEntity(), heal);
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
}