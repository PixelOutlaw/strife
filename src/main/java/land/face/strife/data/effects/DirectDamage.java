package land.face.strife.data.effects;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import land.face.strife.StrifePlugin;
import land.face.strife.data.DamageContainer;
import land.face.strife.data.StrifeMob;
import land.face.strife.stats.StrifeStat;
import land.face.strife.util.DamageUtil;
import land.face.strife.util.DamageUtil.AbilityMod;
import land.face.strife.util.DamageUtil.AttackType;
import land.face.strife.util.DamageUtil.DamageType;
import land.face.strife.util.LogUtil;
import org.bukkit.entity.Player;

public class DirectDamage extends Effect {

  private AttackType attackType;
  private Set<DamageContainer> damages = new HashSet<>();
  private final Map<AbilityMod, Float> abilityMods = new HashMap<>();

  private static double pvpMult = StrifePlugin.getInstance().getSettings()
      .getDouble("config.mechanics.pvp-damage", 0.50);

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    float damage = 0;
    for (DamageContainer container : damages) {
      float addDamage = DamageUtil.applyDamageScale(caster, target, container, attackType);
      addDamage *= DamageUtil.getDamageReduction(container.getDamageType(), caster, target,
          abilityMods);
      if (container.getDamageType() != DamageType.TRUE_DAMAGE) {
        addDamage *= DamageUtil.getPotionMult(caster.getEntity(), target.getEntity());
        addDamage *= 1 + (caster.getStat(StrifeStat.DAMAGE_MULT) / 100);
      }
      damage += addDamage;
    }
    for (StrifeStat attr : getStatMults().keySet()) {
      damage *= 1 + getStatMults().get(attr) * caster.getStat(attr);
    }
    if (caster != target && caster.getEntity() instanceof Player && target.getEntity() instanceof Player) {
      damage *= pvpMult;
    }
    LogUtil.printDebug(" [Pre-Damage] Target Health: " + target.getEntity().getHealth());
    DamageUtil.dealDirectDamage(caster, target, damage);
    LogUtil.printDebug(" [Post-Damage] Target Health: " + target.getEntity().getHealth());
  }

  public void setAttackType(AttackType attackType) {
    this.attackType = attackType;
  }

  public Map<AbilityMod, Float> getAbilityMods() {
    return abilityMods;
  }

  public Set<DamageContainer> getDamages() {
    return damages;
  }

}