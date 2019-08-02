package info.faceland.strife.effects;

import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.stats.StrifeStat;
import info.faceland.strife.util.DamageUtil;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffectType;

public class PotionEffectAction extends Effect {

  private PotionEffectType potionEffectType;
  private double duration;
  private int intensity;
  private boolean bumpUpToIntensity;
  private boolean strictDuration;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    double duration = this.duration;
    if (!strictDuration) {
      duration *= 1 + caster.getStat(StrifeStat.EFFECT_DURATION) / 100;
    }
    if (bumpUpToIntensity) {
      bumpPotionEffect(caster.getEntity(), target.getEntity(), duration);
      return;
    }
    applyPotionEffect(caster.getEntity(), target.getEntity(), duration);
  }

  private void bumpPotionEffect(LivingEntity caster, LivingEntity target, double duration) {
    int level = 0;
    if (isForceTargetCaster()) {
      if (caster.hasPotionEffect(potionEffectType)) {
        level = caster.getPotionEffect(potionEffectType).getAmplifier();
        level = Math.min(level + 1, intensity);
      }
      DamageUtil.applyPotionEffect(caster, potionEffectType, level, (int) duration);
      return;
    }
    if (target.hasPotionEffect(potionEffectType)) {
      level = target.getPotionEffect(potionEffectType).getAmplifier();
      level = Math.min(level + 1, intensity);
    }
    DamageUtil.applyPotionEffect(target, potionEffectType, level, (int) duration);
  }

  private void applyPotionEffect(LivingEntity caster, LivingEntity target, double duration) {
    if (isForceTargetCaster()) {
      DamageUtil.applyPotionEffect(caster, potionEffectType, intensity, (int) duration);
      return;
    }
    DamageUtil.applyPotionEffect(target, potionEffectType, intensity, (int) duration);
  }

  public void setPotionEffectType(PotionEffectType potionEffectType) {
    this.potionEffectType = potionEffectType;
  }

  public void setDuration(double duration) {
    this.duration = duration;
  }

  public void setIntensity(int intensity) {
    this.intensity = intensity;
  }

  public void setStrictDuration(boolean strictDuration) {
    this.strictDuration = strictDuration;
  }

  public void setBumpUpToIntensity(boolean bumpUpToIntensity) {
    this.bumpUpToIntensity = bumpUpToIntensity;
  }
}
