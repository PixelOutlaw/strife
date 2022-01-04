package land.face.strife.tasks;

import com.tealcube.minecraft.bukkit.facecore.utilities.ToastUtils;
import com.tealcube.minecraft.bukkit.facecore.utilities.ToastUtils.ToastStyle;
import java.lang.ref.WeakReference;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.champion.Champion;
import land.face.strife.data.champion.LifeSkillType;
import land.face.strife.events.CombatChangeEvent;
import land.face.strife.events.CombatChangeEvent.NewCombatState;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class CombatCountdownTask extends BukkitRunnable {

  private static final int BUMP_TIME_HALF_SECONDS = 24;
  private final WeakReference<StrifeMob> parentMob;
  private boolean pvp = false;
  private static final ItemStack combatStack = new ItemStack(Material.IRON_SWORD);
  private static final ItemStack exitStack = new ItemStack(Material.OXEYE_DAISY);
  private int halfSecondsRemaining;

  public CombatCountdownTask(StrifeMob parentMob) {
    this.parentMob = new WeakReference<>(parentMob);
    halfSecondsRemaining = BUMP_TIME_HALF_SECONDS;
    CombatChangeEvent cce = new CombatChangeEvent(parentMob, NewCombatState.ENTER);
    if (parentMob.getEntity() instanceof Player) {
      ToastUtils.sendToast(((Player) parentMob.getEntity()).getPlayer(), "Entered Combat!", combatStack, ToastStyle.INFO);
    }
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
      if (mob.getEntity() instanceof Player) {
        ToastUtils.sendToast(((Player) mob.getEntity()).getPlayer(), "Exited Combat...", exitStack, ToastStyle.INFO);
      }
      Bukkit.getPluginManager().callEvent(cce);
      cancel();
      awardSkillExp(mob);
      mob.endCombat();
    }
  }

  public void bump() {
    halfSecondsRemaining = BUMP_TIME_HALF_SECONDS;
  }

  public void setPvp() {
    pvp = true;
  }

  public boolean isPvp() {
    return pvp;
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
