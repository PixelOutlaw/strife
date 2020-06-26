package land.face.strife.listeners;

import land.face.strife.data.champion.LifeSkillType;
import land.face.strife.util.FishingUtil;
import land.face.strife.util.PlayerDataUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerFishEvent.State;

public class FishingListener implements Listener {

  @EventHandler(priority = EventPriority.MONITOR)
  public void onDogeProc(PlayerFishEvent event) {
    if (event.getState() != State.FISHING) {
      return;
    }
    double fishSkill = PlayerDataUtil.getEffectiveLifeSkill(event.getPlayer(), LifeSkillType.FISHING, true);
    int fishTime = FishingUtil.getBiteTime(event.getHook());
    fishTime = (int) ((double) fishTime * (100 / (100 + fishSkill)));
    FishingUtil.setBiteTime(event.getHook(), Math.max(fishTime, 1));
  }
}
