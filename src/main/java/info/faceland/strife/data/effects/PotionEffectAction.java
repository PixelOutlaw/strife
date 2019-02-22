package info.faceland.strife.data.effects;

import info.faceland.strife.data.AttributedEntity;
import info.faceland.strife.util.DamageUtil;
import org.bukkit.potion.PotionEffectType;

public class PotionEffectAction extends Effect {

  private PotionEffectType potionEffectType;
  private int duration;
  private int intensity;
  private boolean targetCaster;

  @Override
  public void apply(AttributedEntity caster, AttributedEntity target) {
    if (targetCaster) {
      DamageUtil.applyPotionEffect(caster.getEntity(), potionEffectType, intensity, duration);
    } else {
      DamageUtil.applyPotionEffect(target.getEntity(), potionEffectType, intensity, duration);
    }
  }

  public PotionEffectType getPotionEffectType() {
    return potionEffectType;
  }

  public void setPotionEffectType(PotionEffectType potionEffectType) {
    this.potionEffectType = potionEffectType;
  }

  public int getDuration() {
    return duration;
  }

  public void setDuration(int duration) {
    this.duration = duration;
  }

  public int getIntensity() {
    return intensity;
  }

  public void setIntensity(int intensity) {
    this.intensity = intensity;
  }

  public void setTargetCaster(boolean targetCaster) {
    this.targetCaster = targetCaster;
  }
}
