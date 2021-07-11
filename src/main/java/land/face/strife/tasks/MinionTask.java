package land.face.strife.tasks;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.stats.StrifeStat;
import org.bukkit.entity.Mob;
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
    if (!minionMob.getEntity().getPassengers().isEmpty()) {
      return;
    }
    if (((Mob) minionMob.getEntity()).getTarget() == mob.getEntity()) {
      ((Mob) minionMob.getEntity()).setTarget(null);
    }
    lifespan--;
    if (lifespan > 0) {
      return;
    }
    if (lifespan <= -15) {
      minionMob.getEntity().damage(minionMob.getEntity().getMaxHealth() * 10);
    } else {
      minionMob.getEntity().damage(minionMob.getEntity().getMaxHealth() / 10);
    }
  }

  public StrifeMob getMaster() {
    return master.get();
  }

  public int getLifespan() {
    return lifespan;
  }

  public void forceStartDeath() {
    lifespan = Math.min(0, lifespan);
  }

  public static void expireMinions(StrifeMob master) {
    List<StrifeMob> minionList = new ArrayList<>(master.getMinions());

    int excessMinions = minionList.size() - (int) master.getStat(StrifeStat.MAX_MINIONS);
    if (excessMinions > 0) {
      minionList.sort(Comparator.comparingDouble(StrifeMob::getMinionRating));
      while (excessMinions > 0) {
        minionList.get(excessMinions - 1).minionDeath();
        //Bukkit.getLogger().info("commit die: " + minionList.get(excessMinions - 1).getEntity().getName());
        excessMinions--;
      }
    }
  }
}
