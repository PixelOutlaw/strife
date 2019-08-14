package info.faceland.strife.effects;

import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.events.StrifeDamageEvent;
import info.faceland.strife.util.DamageUtil;
import info.faceland.strife.util.DamageUtil.AbilityMod;
import info.faceland.strife.util.DamageUtil.AttackType;
import info.faceland.strife.util.DamageUtil.DamageType;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Bukkit;

public class StandardDamage extends Effect {

  private double attackMultiplier;
  private final Map<DamageType, Double> damageModifiers = new HashMap<>();
  private final Map<DamageType, Double> damageBonuses = new HashMap<>();
  private final Map<AbilityMod, Double> abilityMods = new HashMap<>();
  private AttackType attackType;
  private boolean canBeEvaded;
  private boolean canBeBlocked;
  private boolean isBlocking;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    StrifeDamageEvent event = new StrifeDamageEvent(caster, target, attackType, attackMultiplier);
    event.getDamageModifiers().putAll(damageModifiers);
    event.getDamageModifiers().putAll(damageBonuses);
    event.getAbilityMods().putAll(abilityMods);
    event.setCanBeBlocked(false);
    event.setCanBeEvaded(false);
    Bukkit.getPluginManager().callEvent(event);
    if (!event.isCancelled()) {
      DamageUtil.forceCustomDamage(caster.getEntity(), target.getEntity(), event.getFinalDamage());
    }
  }

  public double getAttackMultiplier() {
    return attackMultiplier;
  }

  public void setAttackMultiplier(double attackMultiplier) {
    this.attackMultiplier = attackMultiplier;
  }

  public AttackType getAttackType() {
    return attackType;
  }

  public void setAttackType(AttackType attackType) {
    this.attackType = attackType;
  }

  public Map<DamageType, Double> getDamageModifiers() {
    return damageModifiers;
  }

  public Map<DamageType, Double> getDamageBonuses() {
    return damageBonuses;
  }

  public Map<AbilityMod, Double> getAbilityMods() {
    return abilityMods;
  }

  public boolean isCanBeEvaded() {
    return canBeEvaded;
  }

  public void setCanBeEvaded(boolean canBeEvaded) {
    this.canBeEvaded = canBeEvaded;
  }

  public boolean isCanBeBlocked() {
    return canBeBlocked;
  }

  public void setCanBeBlocked(boolean canBeBlocked) {
    this.canBeBlocked = canBeBlocked;
  }

  public boolean isBlocking() {
    return isBlocking;
  }

  public void setBlocking(boolean blocking) {
    isBlocking = blocking;
  }
}