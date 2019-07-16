package info.faceland.strife.data;

import info.faceland.strife.data.champion.Champion;
import org.bukkit.boss.BossBar;

public class SkillBossBar {

  private Champion owner;
  private int displayTicks;
  private final BossBar skillBar;

  public SkillBossBar(Champion owner, BossBar skillBar) {
    this.owner = owner;
    this.displayTicks = 0;
    this.skillBar = skillBar;
  }

  public Champion getOwner() {
    return owner;
  }

  public void setOwner(Champion owner) {
    this.owner = owner;
  }

  public int getDisplayTicks() {
    return displayTicks;
  }

  public void setDisplayTicks(int displayTicks) {
    this.displayTicks = displayTicks;
  }

  public BossBar getSkillBar() {
    return skillBar;
  }

}
