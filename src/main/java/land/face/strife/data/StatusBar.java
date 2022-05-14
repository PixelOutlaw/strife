package land.face.strife.data;

import java.lang.ref.WeakReference;
import land.face.strife.StrifePlugin;
import land.face.strife.util.GlowUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

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

  public void clearGlow(Player player) {
    if (target.get() != null) {
      GlowUtil.setGlow(player, target.get().getEntity(), null);
    }
  }

  public void refreshGlow(Player player, ChatColor color) {
    if (target.get() != null) {
      GlowUtil.setGlow(player, target.get().getEntity(), color);
    }
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
      barrierBar.setVisible(!hidden && target.get().getMaxBarrier() > 1);
    }
    Bukkit.getScheduler()
        .runTaskLater(StrifePlugin.getInstance(), () -> healthBar.setVisible(!hidden), 0L);
  }
}
