package land.face.strife.tasks;

import java.lang.ref.WeakReference;
import java.util.Objects;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import org.bukkit.scheduler.BukkitRunnable;

public class MinionTask extends BukkitRunnable {

  private final WeakReference<StrifeMob> minion;
  private final WeakReference<StrifeMob> master;
  private int lifespan;

  public MinionTask(StrifeMob minion, StrifeMob master, int lifespanSeconds) {
    this.minion = new WeakReference<>(minion);
    this.master = new WeakReference<>(master);
    this.lifespan = lifespanSeconds;
    this.runTaskTimer(StrifePlugin.getInstance(), 20L, 20L);
  }

  @Override
  public void run() {
    StrifeMob mob = master.get();
    if (mob == null || mob.getEntity() == null) {
      cancel();
      return;
    }
    StrifeMob minionMob = minion.get();
    if (minionMob == null || minionMob.getEntity() == null || !minionMob.getEntity().isValid()) {
      Objects.requireNonNull(master.get()).removeMinion(minionMob);
      cancel();
      return;
    }
    if (!mob.getEntity().getPassengers().isEmpty()) {
      return;
    }
    if (lifespan > 0) {
      lifespan--;
      return;
    }
    if (lifespan <= -15) {
      minionMob.getEntity().damage(minionMob.getEntity().getMaxHealth() * 10);
    } else  {
      minionMob.getEntity().damage(minionMob.getEntity().getMaxHealth() / 10);
    }
  }

  public StrifeMob getMaster() {
    return master.get();
  }

}
