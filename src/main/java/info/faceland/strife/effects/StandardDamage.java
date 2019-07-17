package info.faceland.strife.effects;

import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.events.StrifeDamageEvent;
import info.faceland.strife.util.DamageUtil.AttackType;
import info.faceland.strife.util.DamageUtil.DamageType;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Bukkit;

public class StandardDamage extends Effect {

  private double attackMultiplier;
  private final Map<DamageType, Double> damageModifiers = new HashMap<>();
  private AttackType attackType;
  private boolean isBlocking;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    StrifeDamageEvent damageEvent = new StrifeDamageEvent(caster, target, attackType, attackMultiplier);
    damageEvent.getDamageModifiers().putAll(damageModifiers);
    Bukkit.getPluginManager().callEvent(damageEvent);
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

  public boolean isBlocking() {
    return isBlocking;
  }

  public void setBlocking(boolean blocking) {
    isBlocking = blocking;
  }
}