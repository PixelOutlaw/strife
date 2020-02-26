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
  private boolean canBeEvaded;
  private boolean canBeBlocked;
  private final Map<AbilityMod, Float> abilityMods = new HashMap<>();
  private float damageReductionRatio;

  private static float pvpMult = (float) StrifePlugin.getInstance().getSettings()
      .getDouble("config.mechanics.pvp-damage", 0.50);

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    float evasionMultiplier = 1.0f;
    if (canBeEvaded) {
      evasionMultiplier = DamageUtil.getFullEvasionMult(caster, target, abilityMods);
      if (evasionMultiplier < DamageUtil.EVASION_THRESHOLD) {
        DamageUtil.doEvasion(caster, target);
        LogUtil.printDebug(" [Pre-Damage] Direct damage EVADED!");
        return;
      }
    }
    if (canBeBlocked) {
      if (StrifePlugin.getInstance().getCounterManager()
          .executeCounters(caster.getEntity(), target.getEntity())) {
        LogUtil.printDebug(" [Pre-Damage] Direct damage COUNTERED!");
        return;
      }
      if (StrifePlugin.getInstance().getBlockManager()
          .isAttackBlocked(caster, target, damageReductionRatio, attackType, false)) {
        LogUtil.printDebug(" [Pre-Damage] Direct damage BLOCKED!");
        return;
      }
    }
    float damage = 0;
    float damageMult = DamageUtil.getDamageMult(caster, target) * evasionMultiplier;
    for (DamageContainer container : damages) {
      float addDamage = DamageUtil.applyDamageScale(caster, target, container, attackType);
      addDamage *= DamageUtil.getDamageReduction(container.getDamageType(), caster, target,
          abilityMods);
      if (container.getDamageType() != DamageType.TRUE_DAMAGE) {
        addDamage *= damageMult;
      }
      damage += addDamage;
    }
    for (StrifeStat attr : getStatMults().keySet()) {
      damage *= 1 + getStatMults().get(attr) * caster.getStat(attr);
    }
    if (caster != target && caster.getEntity() instanceof Player && target
        .getEntity() instanceof Player) {
      damage *= pvpMult;
    }
    damage = Math.max(0,
        damage - target.getStat(StrifeStat.DAMAGE_REDUCTION) * damageReductionRatio * pvpMult);
    LogUtil.printDebug(" [Pre-Damage] Target Health: " + target.getEntity().getHealth());

    target.trackDamage(caster, damage);
    StrifePlugin.getInstance().getDamageManager().dealDamage(caster, target, damage, true);
    LogUtil.printDebug(" [Post-Damage] Target Health: " + target.getEntity().getHealth());
  }

  public void setDamageReductionRatio(float damageReductionRatio) {
    this.damageReductionRatio = damageReductionRatio;
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

  public void setCanBeEvaded(boolean canBeEvaded) {
    this.canBeEvaded = canBeEvaded;
  }

  public void setCanBeBlocked(boolean canBeBlocked) {
    this.canBeBlocked = canBeBlocked;
  }
}