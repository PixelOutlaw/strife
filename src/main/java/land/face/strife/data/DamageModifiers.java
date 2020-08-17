package land.face.strife.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import land.face.strife.util.DamageUtil;
import land.face.strife.util.DamageUtil.AbilityMod;
import land.face.strife.util.DamageUtil.AttackType;
import land.face.strife.util.DamageUtil.DamageType;

public class DamageModifiers {

  private static final Map<DamageType, Float> baseDamageMults = buildBaseDamageMults();

  private AttackType attackType = AttackType.OTHER;
  private float attackMultiplier = 1f;
  private float healMultiplier = 1f;
  private float damageReductionRatio = 1f;
  private final Map<DamageType, Float> damageMultipliers = new HashMap<>(baseDamageMults);
  private final List<BonusDamage> bonusDamages = new ArrayList<>();
  private final Map<AbilityMod, Float> abilityMods = new HashMap<>();
  private final Set<ElementalStatus> elementalStatuses = new HashSet<>();
  private boolean isSneakAttack = false;
  private boolean isBlocking = false;
  private boolean canBeBlocked = true;
  private boolean canBeEvaded = true;
  private boolean applyOnHitEffects = true;
  private boolean consumeEarthRunes = false;
  private boolean scaleChancesWithAttack = false;
  private boolean showPopoffs = true;

  public AttackType getAttackType() {
    return attackType;
  }

  public void setAttackType(AttackType attackType) {
    this.attackType = attackType;
  }

  public float getAttackMultiplier() {
    return attackMultiplier;
  }

  public void setAttackMultiplier(float attackMultiplier) {
    this.attackMultiplier = attackMultiplier;
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

  public Map<DamageType, Float> getDamageMultipliers() {
    return damageMultipliers;
  }

  public List<BonusDamage> getBonusDamages() {
    return bonusDamages;
  }

  public Map<AbilityMod, Float> getAbilityMods() {
    return abilityMods;
  }

  public Set<ElementalStatus> getElementalStatuses() {
    return elementalStatuses;
  }

  public boolean isSneakAttack() {
    return isSneakAttack;
  }

  public void setSneakAttack(boolean sneakAttack) {
    isSneakAttack = sneakAttack;
  }

  public boolean isBlocking() {
    return isBlocking;
  }

  public void setBlocking(boolean blocking) {
    isBlocking = blocking;
  }

  public boolean isCanBeBlocked() {
    return canBeBlocked;
  }

  public void setCanBeBlocked(boolean canBeBlocked) {
    this.canBeBlocked = canBeBlocked;
  }

  public boolean isCanBeEvaded() {
    return canBeEvaded;
  }

  public void setCanBeEvaded(boolean canBeEvaded) {
    this.canBeEvaded = canBeEvaded;
  }

  public boolean isApplyOnHitEffects() {
    return applyOnHitEffects;
  }

  public void setApplyOnHitEffects(boolean applyOnHitEffects) {
    this.applyOnHitEffects = applyOnHitEffects;
  }

  public boolean isScaleChancesWithAttack() {
    return scaleChancesWithAttack;
  }

  public void setScaleChancesWithAttack(boolean scaleChancesWithAttack) {
    this.scaleChancesWithAttack = scaleChancesWithAttack;
  }

  public boolean isShowPopoffs() {
    return showPopoffs;
  }

  public void setShowPopoffs(boolean showPopoffs) {
    this.showPopoffs = showPopoffs;
  }

  public boolean isConsumeEarthRunes() {
    return consumeEarthRunes;
  }

  public void setConsumeEarthRunes(boolean consumeEarthRunes) {
    this.consumeEarthRunes = consumeEarthRunes;
  }

  private static Map<DamageType, Float> buildBaseDamageMults() {
    Map<DamageType, Float> base = new HashMap<>();
    for (DamageType type : DamageUtil.DMG_TYPES) {
      base.put(type, 1f);
    }
    return base;
  }

  public enum ElementalStatus {
    IGNITE,
    FREEZE,
    SHOCK,
    CORRUPT,
  }

}
