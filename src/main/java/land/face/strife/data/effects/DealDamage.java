package land.face.strife.data.effects;

import java.util.HashMap;
import java.util.Map;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.stats.StrifeStat;
import land.face.strife.util.DamageUtil;
import land.face.strife.util.DamageUtil.AbilityMod;
import land.face.strife.util.DamageUtil.AttackType;
import land.face.strife.util.DamageUtil.DamageScale;
import land.face.strife.util.DamageUtil.DamageType;
import land.face.strife.util.LogUtil;
import org.bukkit.entity.Player;

public class DealDamage extends Effect {

  private float amount;
  private float flatBonus;
  private DamageScale damageScale;
  private DamageType damageType;
  private AttackType attackType;
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
    damage = DamageUtil.applyDamageScale(caster, target, damage, damageScale, damageType,
        attackType);
    damage += flatBonus;
    LogUtil.printDebug(" [Pre-Mitigation] Dealing " + damage + " of type " + damageType);
    damage *= DamageUtil.getDamageReduction(damageType, caster, target, abilityMods);
    if (damageType != DamageType.TRUE_DAMAGE) {
      damage *= DamageUtil.getPotionMult(caster.getEntity(), target.getEntity());
      damage *= 1 + (caster.getStat(StrifeStat.DAMAGE_MULT) / 100);
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

  public void setAttackType(AttackType attackType) {
    this.attackType = attackType;
  }

  public Map<AbilityMod, Float> getAbilityMods() {
    return abilityMods;
  }

}