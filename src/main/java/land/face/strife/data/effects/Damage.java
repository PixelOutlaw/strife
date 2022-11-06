package land.face.strife.data.effects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import land.face.strife.StrifePlugin;
import land.face.strife.data.BonusDamage;
import land.face.strife.data.DamageModifiers;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.TargetResponse;
import land.face.strife.events.StrifeDamageEvent;
import land.face.strife.events.StrifePreDamageEvent;
import land.face.strife.util.DamageUtil;
import land.face.strife.util.DamageUtil.AbilityMod;
import land.face.strife.util.DamageUtil.AttackType;
import land.face.strife.util.DamageUtil.DamageType;
import land.face.strife.util.ProjectileUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class Damage extends Effect {

  private float attackMultiplier;
  private float healMultiplier;
  private float damageReductionRatio;
  private final Map<DamageType, Float> damageMultipliers = new HashMap<>();
  private final List<BonusDamage> bonusDamages = new ArrayList<>();
  private final Map<AbilityMod, Float> abilityMods = new HashMap<>();
  private AttackType attackType;
  private boolean canBeEvaded;
  private boolean canBeBlocked;
  private boolean canSneakAttack;
  private boolean isBlocking;
  private boolean applyOnHitEffects;
  private boolean showPopoffs;
  private boolean bypassBarrier;
  @Getter @Setter
  private boolean guardBreak;
  private boolean selfInflict;
  private final List<Effect> hitEffects = new ArrayList<>();
  private final List<Effect> killEffects = new ArrayList<>();

  public int currentProjectileId = -1;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {

    if (!target.getEntity().isValid()) {
      return;
    }

    StrifePreDamageEvent preDamageEvent = new StrifePreDamageEvent(caster.getEntity(), target.getEntity());
    Bukkit.getPluginManager().callEvent(preDamageEvent);

    if (preDamageEvent.isCancelled()) {
      return;
    }

    DamageModifiers mods = new DamageModifiers();
    mods.setAttackType(attackType);
    mods.setAttackMultiplier(attackMultiplier);
    mods.setHealMultiplier(healMultiplier);
    mods.setDamageReductionRatio(damageReductionRatio);
    mods.setCanBeEvaded(canBeEvaded);
    mods.setCanBeBlocked(canBeBlocked);
    mods.setApplyOnHitEffects(applyOnHitEffects);
    mods.setShowPopoffs(showPopoffs);
    mods.setBypassBarrier(bypassBarrier);
    mods.setGuardBreak(guardBreak);
    mods.setScaleChancesWithAttack(false);
    if (canSneakAttack && caster.getEntity() instanceof Player && getPlugin().getStealthManager()
        .canSneakAttack((Player) caster.getEntity())) {
      mods.setSneakAttack(true);
    }
    mods.setBlocking(isBlocking);
    mods.getBonusDamages().addAll(bonusDamages);
    mods.getDamageMultipliers().putAll(damageMultipliers);
    mods.getAbilityMods().putAll(abilityMods);
    mods.setBasicAttack(false);

    mods.setAttackMultiplier(mods.getAttackMultiplier());
    boolean attackSuccess = DamageUtil.preDamage(caster, target, mods);

    if (!attackSuccess) {
      return;
    }

    float multishotRatio = 1;
    if (currentProjectileId != -1) {
      multishotRatio = target.getMultishotRatio(currentProjectileId);
      currentProjectileId = -1;
      if (multishotRatio < 0.05) {
        return;
      }
    }

    Map<DamageType, Float> damage = DamageUtil.buildDamage(caster, target, mods);
    float multi = applyMultipliers(caster, 1) * multishotRatio;
    if (multi != 1) {
      damage.replaceAll((type, amount) -> amount * multi);
    }
    if (selfInflict) {
      target = caster;
    }
    DamageUtil.applyElementalEffects(caster, target, damage, mods);
    DamageUtil.reduceDamage(caster, target, damage, mods);
    float finalDamage = DamageUtil.calculateFinalDamage(caster, target, damage, mods);

    StrifeDamageEvent strifeDamageEvent = new StrifeDamageEvent(caster, target, mods);
    strifeDamageEvent.setFinalDamage(finalDamage);

    Bukkit.getPluginManager().callEvent(strifeDamageEvent);

    if (strifeDamageEvent.isCancelled()) {
      return;
    }

    target.trackDamage(caster, (float) strifeDamageEvent.getFinalDamage());

    StrifePlugin.getInstance().getDamageManager().dealDamage(caster, target,
        (float) strifeDamageEvent.getFinalDamage(), mods);

    Set<LivingEntity> entities = new HashSet<>();
    entities.add(target.getEntity());
    TargetResponse response = new TargetResponse(entities);

    getPlugin().getEffectManager().executeEffectList(caster, response, hitEffects);
    if (target.getEntity().isDead()) {
      getPlugin().getEffectManager().executeEffectList(caster, response, killEffects);
    }

    if (damage.containsKey(DamageType.PHYSICAL)) {
      DamageUtil.attemptBleed(caster, target, damage.get(DamageType.PHYSICAL), mods, false);
    }

    StrifeMob finalTarget = target;
    if (mods.isApplyOnHitEffects()) {
      Bukkit.getScheduler().runTaskLater(StrifePlugin.getInstance(), () ->
          DamageUtil.postDamage(caster, finalTarget, mods), 0L);
    }
  }

  public float getDamageReductionRatio() {
    return damageReductionRatio;
  }

  public void setDamageReductionRatio(float damageReductionRatio) {
    this.damageReductionRatio = damageReductionRatio;
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

  public Map<DamageType, Float> getDamageMultipliers() {
    return damageMultipliers;
  }

  public List<BonusDamage> getBonusDamages() {
    return bonusDamages;
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

  public boolean isApplyOnHitEffects() {
    return applyOnHitEffects;
  }

  public void setApplyOnHitEffects(boolean applyOnHitEffects) {
    this.applyOnHitEffects = applyOnHitEffects;
  }

  public boolean isShowPopoffs() {
    return showPopoffs;
  }

  public void setShowPopoffs(boolean showPopoffs) {
    this.showPopoffs = showPopoffs;
  }

  public boolean isBypassBarrier() {
    return bypassBarrier;
  }

  public void setBypassBarrier(boolean bypassBarrier) {
    this.bypassBarrier = bypassBarrier;
  }

  public void setSelfInflict(boolean selfInflict) {
    this.selfInflict = selfInflict;
  }

  public List<Effect> getHitEffects() {
    return hitEffects;
  }

  public List<Effect> getKillEffects() {
    return killEffects;
  }

  public boolean isBlocking() {
    return isBlocking;
  }

  public void setBlocking(boolean blocking) {
    isBlocking = blocking;
  }
}