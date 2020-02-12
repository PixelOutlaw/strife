package land.face.strife.listeners;

import static land.face.strife.data.champion.LifeSkillType.AGILITY;

import land.face.strife.StrifePlugin;
import land.face.strife.data.AgilityLocationContainer;
import land.face.strife.data.champion.Champion;
import land.face.strife.events.LandEvent;
import land.face.strife.events.LaunchEvent;
import land.face.strife.util.PlayerDataUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class LaunchAndLandListener implements Listener {

  private StrifePlugin plugin;

  public LaunchAndLandListener(StrifePlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler
  public void onLaunch(LaunchEvent event) {
    for (AgilityLocationContainer cont : plugin.getAgilityManager()
        .getInWorld(event.getLocation().getWorld().getName())) {
      AgilityLocationContainer.checkStart(cont, event.getPlayer(), event.getLocation());
    }
  }

  @EventHandler
  public void onLand(LandEvent event) {
    for (AgilityLocationContainer cont : plugin.getAgilityManager().getInWorld(event.getLocation()
        .getWorld().getName())) {
      boolean success = AgilityLocationContainer
          .setProgress(cont, event.getPlayer(), event.getLocation());
      if (success) {
        Champion champion = plugin.getChampionManager().getChampion(event.getPlayer());
        float xp = cont.getExp();
        xp *= PlayerDataUtil.getLifeSkillLevel(champion, AGILITY) / cont.getDifficulty();
        xp = Math.min(cont.getExp(), xp);
        plugin.getSkillExperienceManager().addExperience(champion, AGILITY, xp, false, true);
      }
    }
  }
}
