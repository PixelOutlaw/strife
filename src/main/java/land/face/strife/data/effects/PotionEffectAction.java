package land.face.strife.data.effects;

import land.face.strife.data.StrifeMob;
import land.face.strife.listeners.PotionListener;
import land.face.strife.stats.StrifeStat;
import land.face.strife.util.LogUtil;
import lombok.Setter;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

@Setter
public class PotionEffectAction extends Effect {

  private PotionEffectType potionEffectType;
  private double duration;
  private int intensity;
  private boolean remove;
  private boolean bumpUpToIntensity;
  private boolean strictDuration;
  private boolean ignoreFx;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    if (remove) {
      target.getEntity().removePotionEffect(potionEffectType);
      return;
    }
    double duration = this.duration;
    if (!strictDuration) {
      duration *= 1 + caster.getStat(StrifeStat.EFFECT_DURATION) / 100;
    }
    if (ignoreFx) {
      PotionListener.ignoreFx = true;
    }
    if (bumpUpToIntensity) {
      bumpPotionEffect(target.getEntity(), duration);
      PotionListener.ignoreFx = false;
      return;
    }
    target.getEntity().addPotionEffect(new PotionEffect(potionEffectType, (int) duration, intensity));
    PotionListener.ignoreFx = false;
  }

  private void bumpPotionEffect(LivingEntity target, double duration) {
    LogUtil.printDebug(" Bumping potion effect");
    int level = 0;
    if (target.hasPotionEffect(potionEffectType)) {
      level = target.getPotionEffect(potionEffectType).getAmplifier();
      level = Math.min(level + 1, intensity);
      LogUtil.printDebug(" Bumped from " + level + " to " + (level + 1) + ". MAX: " + intensity);
    } else {
      LogUtil.printDebug(" Target missing potion effect - adding at intensity 0");
    }
    target.addPotionEffect(new PotionEffect(potionEffectType, (int) duration, level));
  }
}
