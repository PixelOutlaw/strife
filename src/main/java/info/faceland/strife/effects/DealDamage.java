package info.faceland.strife.effects;

import static info.faceland.strife.stats.StrifeStat.DAMAGE_MULT;

import info.faceland.strife.StrifePlugin;
import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.stats.StrifeStat;
import info.faceland.strife.util.DamageUtil;
import info.faceland.strife.util.DamageUtil.AbilityMod;
import info.faceland.strife.util.DamageUtil.DamageScale;
import info.faceland.strife.util.DamageUtil.DamageType;
import info.faceland.strife.util.LogUtil;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.entity.Player;

public class DealDamage extends Effect {

  private float amount;
  private float flatBonus;
  private DamageScale damageScale;
  private DamageType damageType;
  private final Map<AbilityMod, Float> abilityMods = new HashMap<>();

  private static double pvpMult = StrifePlugin.getInstance().getSettings()
      .getDouble("config.mechanics.pvp-damage", 0.50);

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    float damage = amount;
    for (StrifeStat attr : getStatMults().keySet()) {
      damage += getStatMults().get(attr) * caster.getStat(attr);
    }
    LogUtil.printDebug("Damage Effect! " + damage + " | " + damageScale + " | " + damageType);
    damage = DamageUtil.applyDamageScale(caster, target, damage, damageScale, damageType);
    damage += flatBonus;
    LogUtil.printDebug(" [Pre-Mitigation] Dealing " + damage + " of type " + damageType);
    damage *= DamageUtil.getDamageReduction(damageType, caster, target, abilityMods);
    if (damageType != DamageType.TRUE_DAMAGE) {
      damage *= DamageUtil.getPotionMult(caster.getEntity(), target.getEntity());
      damage *= 1 + (caster.getStat(DAMAGE_MULT) / 100);
    }
    if (caster != target && caster.getEntity() instanceof Player && target
        .getEntity() instanceof Player) {
      damage *= pvpMult;
    }
    LogUtil.printDebug(" [Post-Mitigation] Dealing " + damage + " of type " + damageType);
    LogUtil.printDebug(" [Pre-Damage] Target Health: " + target.getEntity().getHealth());
    DamageUtil.dealDirectDamage(caster, target, damage);
    LogUtil.printDebug(" [Post-Damage] Target Health: " + target.getEntity().getHealth());
  }

  public void setAmount(float amount) {
    this.amount = amount;
  }

  public void setFlatBonus(float flatBonus) {
    this.flatBonus = flatBonus;
  }

  public void setDamageScale(DamageScale damageScale) {
    this.damageScale = damageScale;
  }

  public void setDamageType(DamageType damageType) {
    this.damageType = damageType;
  }

  public Map<AbilityMod, Float> getAbilityMods() {
    return abilityMods;
  }

}