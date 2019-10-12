package land.face.strife.timers;

import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.util.LogUtil;
import org.bukkit.Particle;
import org.bukkit.entity.FallingBlock;
import org.bukkit.scheduler.BukkitRunnable;

public class FallingBlockTimer extends BukkitRunnable {

  private final StrifeMob caster;
  private final FallingBlock block;
  private int life = 0;

  public FallingBlockTimer(StrifeMob caster, FallingBlock block) {
    this.caster = caster;
    this.block = block;
    runTaskTimer(StrifePlugin.getInstance(), 0L, 2L);
    LogUtil.printDebug("Created FallingBlockTimer with id " + getTaskId());
  }

  @Override
  public void run() {
    if (block == null) {
      LogUtil.printDebug("Cancelled FallingBlockTimer " + getTaskId() + ", null entity");
      cancel();
      return;
    }
    if (block.isValid()) {
      if (life++ > 600) {
        LogUtil.printWarning("Handled FallingBlock got really old, something is wrong...");
        cancel();
      }
      return;
    }
    LogUtil.printDebug("SUCCESS! FallingBlockTimer " + getTaskId() + " entity not valid");
    block.getWorld().spawnParticle(Particle.BLOCK_DUST, block.getLocation(), 20, 0.7, 0.7, 0.7,
        block.getBlockData());
    block.setDropItem(false);
    String[] effects = block.getMetadata("EFFECT_PROJECTILE").get(0).asString().split("~");
    if (effects.length == 0) {
      LogUtil.printWarning("A handled FallingBlock was missing effect meta... something's wrong");
      cancel();
      return;
    }
    for (String s : effects) {
      StrifePlugin.getInstance().getEffectManager()
          .execute(StrifePlugin.getInstance().getEffectManager().getEffect(s), caster,
              block.getLocation());
    }
    cancel();
  }
}
