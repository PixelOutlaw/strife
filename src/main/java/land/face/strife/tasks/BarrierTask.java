package land.face.strife.tasks;

import java.lang.ref.WeakReference;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.stats.StrifeStat;
import land.face.strife.util.DamageUtil;
import land.face.strife.util.StatUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;

public class BarrierTask extends BukkitRunnable {

  private static final BlockData BLOCK_DATA = Bukkit.getServer().createBlockData(Material.WHITE_STAINED_GLASS);

  private final WeakReference<StrifeMob> parentMob;
  private int delayTicks = 0;
  @Getter @Setter
  private boolean canBreak = true;

  private final float tickMultiplier;

  public BarrierTask(StrifeMob parentMob) {
    this.parentMob = new WeakReference<>(parentMob);
    this.runTaskTimer(StrifePlugin.getInstance(), 40L, DamageUtil.TICK_RATE);
    tickMultiplier = (1 / (20f / DamageUtil.TICK_RATE)) * 0.1f;
  }

  @Override
  public void run() {
    StrifeMob mob = parentMob.get();
    if (mob == null || mob.getEntity() == null || !mob.getEntity().isValid()) {
      cancel();
      return;
    }
    if (mob.getMaxBarrier() < 0.1) {
      delayTicks = 10;
      canBreak = false;
      return;
    }
    if (mob.getBarrier() >= mob.getMaxBarrier()) {
      mob.setBarrier(mob.getMaxBarrier());
      delayTicks = 10;
      canBreak = true;
      return;
    }
    if (delayTicks > 0) {
      delayTicks--;
      mob.restoreBarrier(StatUtil.getStat(mob, StrifeStat.BARRIER_REGEN) * tickMultiplier * 0.1f);
      if (mob.getBarrier() > mob.getMaxBarrier() / 25) {
        canBreak = true;
      }
      return;
    }
    float barrierGain = DamageUtil.TICK_RATE * StatUtil.getBarrierPerSecond(mob) / 20;
    barrierGain += StatUtil.getStat(mob, StrifeStat.BARRIER_REGEN) * tickMultiplier * 0.1f;
    canBreak = true;
    mob.restoreBarrier(barrierGain);
  }

  public void bumpBarrierTime(float barrierDelayMult) {
    delayTicks = (int) ((float) DamageUtil.DELAY_TICKS / barrierDelayMult);
  }

  public void setDelayTicks(int delayTicks) {
    this.delayTicks = Math.min(delayTicks, (int) ((float) DamageUtil.BASE_RECHARGE_TICKS / DamageUtil.TICK_RATE));
  }

  public static void spawnBarrierParticles(LivingEntity entity, float amount) {
    int particleAmount = (int) Math.ceil(amount / 5);
    entity.getWorld().spawnParticle(
        Particle.BLOCK,
        entity.getLocation().clone().add(0, entity.getEyeHeight() / 2, 0),
        particleAmount,
        0.0, 0.0, 0.0,
        1.3,
        BLOCK_DATA
    );
  }
}
