package land.face.strife.data;

import java.lang.ref.WeakReference;
import land.face.strife.data.champion.Champion;
import land.face.strife.data.champion.LifeSkillType;
import org.bukkit.boss.BossBar;

public class SkillBar {

  private WeakReference<Champion> owner;
  private final BossBar skillBar;
  private LifeSkillType lifeSkillType;
  private int lifeTicks;

  public SkillBar(Champion owner, BossBar skillBar) {
    this.owner = new WeakReference<>(owner);
    this.lifeTicks = 200;
    this.skillBar = skillBar;
  }

  public Champion getOwner() {
    return owner.get();
  }

  public BossBar getSkillBar() {
    return skillBar;
  }

  public LifeSkillType getLifeSkillType() {
    return lifeSkillType;
  }

  public void setLifeSkillType(LifeSkillType lifeSkillType) {
    this.lifeSkillType = lifeSkillType;
  }

  public int getLifeTicks() {
    return lifeTicks;
  }

  public void setLifeTicks(int lifeTicks) {
    this.lifeTicks = lifeTicks;
  }

}
