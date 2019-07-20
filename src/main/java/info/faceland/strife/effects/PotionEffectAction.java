package info.faceland.strife.effects;

import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.stats.StrifeStat;
import info.faceland.strife.util.DamageUtil;
import org.bukkit.potion.PotionEffectType;

public class PotionEffectAction extends Effect {

  private PotionEffectType potionEffectType;
  private double duration;
  private int intensity;
  private boolean strictDuration;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    double duration = this.duration;
    if (!strictDuration) {
      duration *= 1 + caster.getStat(StrifeStat.EFFECT_DURATION) / 100;
    }
    if (isForceTargetCaster()) {
      DamageUtil.applyPotionEffect(caster.getEntity(), potionEffectType, intensity, (int) duration);
    } else {
      DamageUtil.applyPotionEffect(target.getEntity(), potionEffectType, intensity, (int) duration);
    }
  }

  public PotionEffectType getPotionEffectType() {
    return potionEffectType;
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
}
