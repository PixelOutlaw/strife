package land.face.strife.listeners;

import static land.face.strife.data.champion.LifeSkillType.AGILITY;

import land.face.strife.StrifePlugin;
import land.face.strife.data.AgilityLocationContainer;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.champion.Champion;
import land.face.strife.data.champion.LifeSkillType;
import land.face.strife.events.LandEvent;
import land.face.strife.events.LaunchEvent;
import land.face.strife.stats.StrifeStat;
import land.face.strife.util.MoveUtil;
import land.face.strife.util.PlayerDataUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

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
    if (!event.getPlayer().isSprinting() || MoveUtil.timeOffGround(event.getPlayer()) > 65) {
      return;
    }
    if (event.getPlayer().hasPotionEffect(PotionEffectType.JUMP)
        && event.getPlayer().getPotionEffect(PotionEffectType.JUMP).getAmplifier() < 0) {
      return;
    }
    int lastSneak = MoveUtil.getLastSneak(event.getPlayer().getUniqueId());
    if (lastSneak == -1 || lastSneak > 200) {
      return;
    }
    StrifeMob mob = plugin.getStrifeMobManager().getStatMob(event.getPlayer());
    if (plugin.getEnergyManager().getEnergy(mob) > 12) {
      plugin.getEnergyManager().changeEnergy(mob, -12);
      Vector bonusVelocity = event.getPlayer().getLocation().getDirection();
      bonusVelocity.setY(Math.max(0.06, bonusVelocity.getY()));
      double moveMult = 1 + 0.25 * mob.getStat(StrifeStat.MOVEMENT_SPEED) / 100;
      bonusVelocity.normalize().multiply(0.28);
      bonusVelocity.setX(bonusVelocity.getX() * moveMult);
      bonusVelocity.setZ(bonusVelocity.getZ() * moveMult);

      Vector oldVelocity = event.getPlayer().getVelocity().clone()
          .setY(Math.max(0, event.getPlayer().getVelocity().getY()));
      event.getPlayer().setVelocity(oldVelocity.add(bonusVelocity));
      plugin.getSkillExperienceManager().addExperience(mob.getChampion(), LifeSkillType.AGILITY,
          2, false, true);
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
