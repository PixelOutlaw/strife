package land.face.strife.util;

import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.util.DamageUtil.OriginLocation;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;

public class CorruptionUtil {

  public static void applyCorrupt(StrifeMob target, float amount, boolean silent) {
    StrifePlugin.getInstance().getCorruptionManager().addCorruption(target, amount, silent);
  }

  public static float getCorruptionMultiplier(StrifeMob mob) {
    return StrifePlugin.getInstance().getCorruptionManager().getCorruptionMultiplier(mob);
  }
}
