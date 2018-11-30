package info.faceland.strife.data;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.boss.BossBar;

public class StrifeBossBar {

  private AttributedEntity owner;
  private Map<UUID, Integer> playerUuidTickMap;
  private BossBar barrierBar;
  private BossBar healthBar;

  private boolean dead;

  public StrifeBossBar(AttributedEntity owner, BossBar barrierBar, BossBar healthBar) {
    this.owner = owner;
    this.healthBar = healthBar;
    this.barrierBar = barrierBar;
    this.playerUuidTickMap = new ConcurrentHashMap<>();
    this.dead = false;
  }

  public AttributedEntity getOwner() {
    return owner;
  }

  public Map<UUID, Integer> getPlayerUuidTickMap() {
    return playerUuidTickMap;
  }

  public BossBar getBarrierBar() {
    return barrierBar;
  }

  public void setBarrierBar(BossBar barrierBar) {
    this.barrierBar = barrierBar;
  }

  public BossBar getHealthBar() {
    return healthBar;
  }

  public boolean isDead() {
    return dead;
  }

  public void setDead(boolean dead) {
    this.dead = dead;
  }
}
