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

  private float attackMultiplier;
  private float healMultiplier;
  private final Map<DamageType, Float> damageModifiers = new HashMap<>();
  private final Map<DamageType, Float> damageBonuses = new HashMap<>();
  private final Map<AbilityMod, Float> abilityMods = new HashMap<>();
  private AttackType attackType;
  private boolean canBeEvaded;
  private boolean canBeBlocked;
  private boolean isBlocking;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    StrifeDamageEvent event = new StrifeDamageEvent(caster, target, attackType, attackMultiplier);
    event.setHealMultiplier(healMultiplier);
    event.getDamageModifiers().putAll(damageModifiers);
    event.getFlatDamageBonuses().putAll(damageBonuses);
    event.getAbilityMods().putAll(abilityMods);
    event.setCanBeBlocked(false);
    event.setCanBeEvaded(false);
    Bukkit.getPluginManager().callEvent(event);
    if (!event.isCancelled()) {
      DamageUtil.forceCustomDamage(caster.getEntity(), target.getEntity(), event.getFinalDamage());
    }
  }

  public float getHealMultiplier() {
    return healMultiplier;
  }

  public void setHealMultiplier(float healMultiplier) {
    this.healMultiplier = healMultiplier;
  }

  public double getAttackMultiplier() {
    return attackMultiplier;
  }

  public void setAttackMultiplier(float attackMultiplier) {
    this.attackMultiplier = attackMultiplier;
  }

  public AttackType getAttackType() {
    return attackType;
  }

  public void setAttackType(AttackType attackType) {
    this.attackType = attackType;
  }

  public Map<DamageType, Float> getDamageModifiers() {
    return damageModifiers;
  }

  public Map<DamageType, Float> getDamageBonuses() {
    return damageBonuses;
  }

  public Map<AbilityMod, Float> getAbilityMods() {
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