package land.face.strife.data.effects;

import java.util.HashMap;
import java.util.Map;
import land.face.strife.StrifePlugin;
import land.face.strife.data.DamageModifiers;
import land.face.strife.data.StrifeMob;
import land.face.strife.events.StrifeDamageEvent;
import land.face.strife.util.DamageUtil;
import land.face.strife.util.DamageUtil.AbilityMod;
import land.face.strife.util.DamageUtil.AttackType;
import land.face.strife.util.DamageUtil.DamageType;
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
  private boolean canSneakAttack;
  private boolean isBlocking;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {

    DamageModifiers mods = new DamageModifiers();
    mods.setAttackType(attackType);
    mods.setAttackMultiplier(attackMultiplier);
    mods.setHealMultiplier(healMultiplier);
    mods.setCanBeEvaded(canBeEvaded);
    mods.setCanBeBlocked(canBeBlocked);
    mods.setApplyOnHitEffects(attackMultiplier > 0.75);
    if (canSneakAttack && StrifePlugin.getInstance().getStealthManager().isStealthed(
        caster.getEntity())) {
      mods.setSneakAttack(true);
    }
    mods.setBlocking(isBlocking);
    mods.getFlatDamageBonuses().putAll(damageBonuses);
    mods.getDamageModifiers().putAll(damageModifiers);
    mods.getAbilityMods().putAll(abilityMods);

    boolean attackSuccess = DamageUtil.preDamage(caster, target, mods);

    if (!attackSuccess) {
      return;
    }

    Map<DamageType, Float> damage =  DamageUtil.buildDamage(caster, target, mods);
    DamageUtil.reduceDamage(caster, target, damage, mods);
    float finalDamage = DamageUtil.damage(caster, target, damage, mods);

    StrifeDamageEvent strifeDamageEvent = new StrifeDamageEvent(caster, target, mods);
    strifeDamageEvent.setFinalDamage(finalDamage);

    Bukkit.getPluginManager().callEvent(strifeDamageEvent);

    if (!strifeDamageEvent.isCancelled()) {
      DamageUtil.postDamage(caster, target, damage, mods);
      StrifePlugin.getInstance().getDamageManager().dealDamage(caster, target,
          (float) strifeDamageEvent.getFinalDamage(), false);
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

  public void setCanSneakAttack(boolean canSneakAttack) {
    this.canSneakAttack = canSneakAttack;
  }

  public boolean isBlocking() {
    return isBlocking;
  }

  public void setBlocking(boolean blocking) {
    isBlocking = blocking;
  }
}