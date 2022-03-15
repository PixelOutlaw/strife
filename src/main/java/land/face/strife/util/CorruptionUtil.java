package land.face.strife.util;

import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.util.DamageUtil.OriginLocation;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;

public class CorruptionUtil {

  private static StrifePlugin plugin;

  private static float flatCorruptPerTick;
  private static float percentCorruptPerTick;

  public static void refresh() {
    plugin = StrifePlugin.getInstance();
    flatCorruptPerTick = (float) StrifePlugin.getInstance().getSettings()
        .getDouble("config.mechanics.base-corrupt-loss", 0.15);
    percentCorruptPerTick = 1f - (float) StrifePlugin.getInstance().getSettings()
        .getDouble("config.mechanics.config.mechanics.percent-corrupt-loss", 0.05);
  }

  public static void applyCorrupt(StrifeMob target, float amount, boolean silent) {
    target.addCorruption(amount);
    if (!silent) {
      target.getEntity().getWorld().playSound(target.getEntity().getLocation(),
          Sound.ENTITY_WITHER_SHOOT, 0.7f, 2f);
    }
    target.getEntity().getWorld().spawnParticle(Particle.SMOKE_NORMAL,
        target.getEntity().getLocation(), 10, 0.4, 1, 0.5, 0.1);
  }

  public static float getCorruptionMultiplier(StrifeMob mob) {
    return mob.getCorruption() > 0 ? 1 + 0.01f * mob.getCorruption() : 1;
  }

  public static void tickCorruption(StrifeMob mob) {
    float newCorruption = mob.getCorruption() * percentCorruptPerTick;
    newCorruption -= flatCorruptPerTick;
    mob.setCorruption(newCorruption);
    spawnCorruptionParticles(mob.getEntity(), mob.getCorruption());
  }

  public static void spawnCorruptionParticles(LivingEntity target, float corruption) {
    double particleAmount = Math.min(5 + corruption / 3, 30);
    target.getWorld().spawnParticle(Particle.SMOKE_NORMAL,
        TargetingUtil.getOriginLocation(target, OriginLocation.CENTER),
        (int) particleAmount,
        0.4, 0.4, 0.5,
        0.03
    );
  }
}
