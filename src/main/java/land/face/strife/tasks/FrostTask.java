package land.face.strife.tasks;

import java.lang.ref.WeakReference;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.util.DamageUtil;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class FrostTask extends BukkitRunnable {

  private final WeakReference<StrifeMob> parentMob;

  public FrostTask(StrifeMob parentMob) {
    this.parentMob = new WeakReference<>(parentMob);
    this.runTaskTimer(StrifePlugin.getInstance(), 20L, 13L);
  }

  @Override
  public void run() {

    StrifeMob mob = parentMob.get();
    if (mob == null || mob.getEntity() == null || !mob.getEntity().isValid()) {
      cancel();
      return;
    }
    if (mob.getEntity().getType() != EntityType.PLAYER) {
      lowerFrostNormally(mob);
      return;
    }
    Player player = (Player) mob.getEntity();
    if (player.getGameMode() != GameMode.ADVENTURE) {
      return;
    }

    Block block = player.getLocation().getBlock();
    boolean isCold = isLocationCold(block);

    if (isCold) {
      mob.addFrost(1);
      mob.setFrostGraceTicks(4);
      playFrostParticles(mob.getEntity());
    } else {
      lowerFrostNormally(mob);
    }
  }

  private void lowerFrostNormally(StrifeMob mob) {
    if (mob.getFrost() > 0) {
      if (mob.getFrostGraceTicks() > 0) {
        mob.setFrostGraceTicks(mob.getFrostGraceTicks() - 1);
      } else {
        mob.removeFrost(2);
      }
      playFrostParticles(mob.getEntity());
    }
  }

  private static void playFrostParticles(LivingEntity livingEntity) {
    livingEntity.getWorld().spawnParticle(Particle.SNOWFLAKE,
        livingEntity.getEyeLocation(),
        1 + livingEntity.getFreezeTicks() / 25,
        0.5, 0.8, 0.5,
        0);
  }

  private static boolean isLocationCold(Block block) {
    if (block.getTemperature() > 0.1) {
      return false;
    }
    if (block.getType() == Material.WATER) {
      return true;
    }
    if (block.getLightFromSky() > 12) {
      if (block.getLightLevel() > 5) {
        return false;
      }
      return block.getWorld().hasStorm()
          || (block.getWorld().getTime() > 13000 && block.getWorld().getTime() < 23000);
    }
    return false;
  }
}
