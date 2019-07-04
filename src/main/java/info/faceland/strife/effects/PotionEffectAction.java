package info.faceland.strife.effects;

import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.stats.StrifeStat;
import info.faceland.strife.util.DamageUtil;
import org.bukkit.potion.PotionEffectType;

public class PotionEffectAction extends Effect {

  private PotionEffectType potionEffectType;
  private double duration;
  private int intensity;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    int effectDuration = (int) (duration * (1 + (
        caster.getAttribute(StrifeStat.EFFECT_DURATION) / 100)));
    if (isForceTargetCaster()) {
      DamageUtil.applyPotionEffect(caster.getEntity(), potionEffectType, intensity, effectDuration);
    } else {
      DamageUtil.applyPotionEffect(target.getEntity(), potionEffectType, intensity, effectDuration);
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
}
