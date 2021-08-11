package land.face.strife.data;

import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.UUID;
import land.face.strife.StrifePlugin;
import land.face.strife.util.StatUtil;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import ru.xezard.glow.data.glow.Glow;

public class StatusBar {

  private WeakReference<StrifeMob> target;
  private final BossBar barrierBar;
  private final BossBar healthBar;
  private final Glow glowContainer;

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
    glowContainer = Glow.builder()
        .animatedColor(ChatColor.WHITE)
        .name(UUID.randomUUID().toString().substring(0, 16))
        .build();
  }

  public void clearGlow(Player player) {
    glowContainer.removeHolders(glowContainer.getHolders().toArray(new Entity[0]));
    glowContainer.hideFrom(player);
  }

  public void refreshGlow(Player player, ChatColor color) {
    if (color == glowContainer.getColor() && glowContainer.getHolders()
        .contains(getTarget().getEntity())) {
      return;
    }
    glowContainer.removeHolders(glowContainer.getHolders().toArray(new Entity[0]));
    glowContainer.setColor(color);
    glowContainer.addHolders(getTarget().getEntity());
    glowContainer.display(player);
    Disguise disguise = DisguiseAPI.getDisguise(getTarget().getEntity());
    if (disguise != null) {
      FlagWatcher watcher = disguise.getWatcher();
      watcher.setGlowColor(color);
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
      barrierBar.setVisible(
          !hidden && StatUtil.getMaximumBarrier(Objects.requireNonNull(target.get())) > 1);
    }
    Bukkit.getScheduler()
        .runTaskLater(StrifePlugin.getInstance(), () -> healthBar.setVisible(!hidden), 0L);
  }
}
