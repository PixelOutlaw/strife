package land.face.strife.tasks;

import java.lang.ref.WeakReference;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.champion.Champion;
import land.face.strife.data.champion.LifeSkillType;
import land.face.strife.events.CombatChangeEvent;
import land.face.strife.events.CombatChangeEvent.NewCombatState;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class CombatCountdownTask extends BukkitRunnable {

  private static final int BUMP_TIME_HALF_SECONDS = 20;
  private final WeakReference<StrifeMob> parentMob;

  private int halfSecondsRemaining;

  public CombatCountdownTask(StrifeMob parentMob) {
    this.parentMob = new WeakReference<>(parentMob);
    halfSecondsRemaining = BUMP_TIME_HALF_SECONDS;
    CombatChangeEvent cce = new CombatChangeEvent(parentMob, NewCombatState.ENTER);
    Bukkit.getPluginManager().callEvent(cce);
  }

  @Override
  public void run() {
    StrifeMob mob = parentMob.get();
    if (mob == null || mob.getEntity() == null) {
      cancel();
      return;
    }
    halfSecondsRemaining--;
    if (halfSecondsRemaining == 0) {
      CombatChangeEvent cce = new CombatChangeEvent(mob, NewCombatState.EXIT);
      Bukkit.getPluginManager().callEvent(cce);
      cancel();
      awardSkillExp(mob);
      mob.endCombat();
    }
  }

  public void bump() {
    halfSecondsRemaining = BUMP_TIME_HALF_SECONDS;
  }

  public static void awardSkillExp(StrifeMob mob) {
    Champion champion = mob.getChampion();
    if (champion == null) {
      return;
    }
    if (champion.getDetailsContainer().getExpValues() == null) {
      return;
    }
    for (LifeSkillType type : champion.getDetailsContainer().getExpValues().keySet()) {
      StrifePlugin.getInstance().getSkillExperienceManager().addExperience((Player) mob.getEntity(), type,
          champion.getDetailsContainer().getExpValues().get(type), false, false);
    }
    champion.getDetailsContainer().clearAll();
  }
}
