package land.face.strife.listeners;

import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.champion.LifeSkillType;
import land.face.strife.stats.StrifeStat;
import land.face.strife.util.MoveUtil;
import land.face.strife.util.PlayerDataUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class MoveListener implements Listener {

  private StrifePlugin plugin;
  private float walkCostFlat;
  private float walkCostPercent;
  private float runCostFlat;
  private float runCostPercent;
  private float agilityExp;

  public MoveListener(StrifePlugin plugin) {
    this.plugin = plugin;
    walkCostFlat = (float) plugin.getSettings()
        .getDouble("config.mechanics.energy.walk-cost-flat", 3) / 20;
    walkCostPercent = (float) plugin.getSettings()
        .getDouble("config.mechanics.energy.walk-cost-percent", 0.2) / 20;
    runCostFlat = (float) plugin.getSettings()
        .getDouble("config.mechanics.energy.run-cost-flat", 10) / 20;
    runCostPercent = (float) plugin.getSettings()
        .getDouble("config.mechanics.energy.run-cost-percent", 0.04) / 20;
    agilityExp = (float) plugin.getSettings()
        .getDouble("config.mechanics.energy.agility-xp", 10) / 20;
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onPlayerMoveHorizontally(PlayerMoveEvent event) {
    if (event.getFrom().getBlockX() != event.getTo().getBlockX()
        || event.getFrom().getBlockZ() != event.getTo().getBlockZ()) {
      MoveUtil.setHasMoved(event.getPlayer());
      StrifeMob mob = plugin.getStrifeMobManager().getStatMob(event.getPlayer());
      double agility = PlayerDataUtil.getLifeSkillLevel(mob.getChampion(), LifeSkillType.AGILITY);
      double agilityMult = 50.0 / (50 + agility);
      if (event.getPlayer().isSprinting()) {
        float amount = runCostFlat + mob.getStat(StrifeStat.ENERGY) * runCostPercent;
        amount *= agilityMult;
        plugin.getEnergyManager().changeEnergy(event.getPlayer(), -amount, true);
        plugin.getSkillExperienceManager().addExperience(mob.getChampion(), LifeSkillType.AGILITY,
            agilityExp, false, false);
      } else {
        float amount = walkCostFlat + mob.getStat(StrifeStat.ENERGY) * walkCostPercent;
        amount *= agilityMult;
        plugin.getEnergyManager().changeEnergy(event.getPlayer(), -amount, false);
      }
      if (event.getPlayer().isOnGround()) {
        MoveUtil.setLastGrounded(event.getPlayer());
      }
    }
  }
}
