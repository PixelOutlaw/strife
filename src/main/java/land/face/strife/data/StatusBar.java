package land.face.strife.data;

import java.lang.ref.WeakReference;
import java.util.Objects;
import land.face.strife.util.StatUtil;
import org.bukkit.boss.BossBar;

public class StatusBar {

  private WeakReference<StrifeMob> target;
  private final BossBar barrierBar;
  private final BossBar healthBar;

  private int lifeTicks;
  private boolean dead;
  private boolean hidden;

  public StatusBar(StrifeMob target, BossBar healthBar, BossBar barrierBar) {
    this.target = new WeakReference<>(target);
    this.healthBar = healthBar;
    this.barrierBar = barrierBar;
    this.lifeTicks = 400;
    this.dead = false;
    this.hidden = false;
  }

  public StrifeMob getTarget() {
    return target.get();
  }

  public void setTarget(StrifeMob target) {
    this.target = new WeakReference<>(target);
  }

  public BossBar getBarrierBar() {
    return barrierBar;
  }

  public BossBar getHealthBar() {
    return healthBar;
  }

  public int getLifeTicks() {
    return lifeTicks;
  }

  public void setLifeTicks(int lifeTicks) {
    this.lifeTicks = lifeTicks;
  }

  public boolean isDead() {
    return dead;
  }

  public void setDead(boolean dead) {
    this.dead = dead;
  }

  public boolean isHidden() {
    return hidden;
  }

  public void setHidden(boolean hidden) {
    this.hidden = hidden;
    if (target.get() == null) {
      barrierBar.setVisible(!hidden);
    } else {
      barrierBar.setVisible(!hidden && StatUtil.getMaximumBarrier(
          Objects.requireNonNull(target.get())) > 1);
    }
    healthBar.setVisible(!hidden);
  }
}
