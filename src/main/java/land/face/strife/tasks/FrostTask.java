package land.face.strife.tasks;

import java.lang.ref.WeakReference;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.util.DamageUtil;
import org.bukkit.Bukkit;
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
  private int frostTick = 0;
  private boolean isCold;

  public FrostTask(StrifeMob parentMob) {
    this.parentMob = new WeakReference<>(parentMob);
    this.runTaskTimer(StrifePlugin.getInstance(), 20L, 1);
  }

  @Override
  public void run() {

    StrifeMob mob = parentMob.get();
    if (mob == null || mob.getEntity() == null || !mob.getEntity().isValid()) {
      cancel();
      return;
    }
    if (mob.getEntity().getType() == EntityType.PLAYER) {
      Player player = (Player) mob.getEntity();
      if (player.getGameMode() == GameMode.ADVENTURE) {
        frostTick++;
        frostTick = frostTick % 20;
        if (frostTick == 0) {
          Block block = player.getLocation().getBlock();
          isCold = isLocationCold(block);
        }
        if (isCold) {
          DamageUtil.addFrost(null, mob, 5);
          if (mob.getFrost() > 9900) {
            if (!mob.isInvincible()) {
              DamageUtil.dealRawDamage(mob, 1);
            }
          }
        } else if (mob.getFrost() > 0) {
          mob.setFrost(mob.getFrost() - 25);
          playFrostParticles(mob, mob.getEntity());
        }
        return;
      }
      mob.setFrost(0);
    } else {
      if (mob.getFrost() > 0) {
        mob.setFrost(mob.getFrost() - 25);
        playFrostParticles(mob, mob.getEntity());
      }
    }
  }

  private static void playFrostParticles(StrifeMob mob, LivingEntity livingEntity) {
    livingEntity.getWorld().spawnParticle(Particle.SNOWFLAKE,
        livingEntity.getEyeLocation(),
        1,
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
