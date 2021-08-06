package land.face.strife.listeners;

import static land.face.strife.data.champion.LifeSkillType.AGILITY;

import com.tealcube.minecraft.bukkit.facecore.event.LandEvent;
import com.tealcube.minecraft.bukkit.facecore.event.LaunchEvent;
import com.tealcube.minecraft.bukkit.facecore.utilities.MoveUtil;
import land.face.strife.StrifePlugin;
import land.face.strife.data.AgilityLocationContainer;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.champion.Champion;
import land.face.strife.data.champion.LifeSkillType;
import land.face.strife.data.effects.Riptide;
import land.face.strife.stats.StrifeStat;
import land.face.strife.util.PlayerDataUtil;
import land.face.strife.util.StatUtil;
import org.bukkit.GameMode;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class LaunchAndLandListener implements Listener {

  private final StrifePlugin plugin;

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
    if (mob.getChampion().getLifeSkillLevel(AGILITY) < 20) {
      return;
    }
    if (event.getPlayer().getGameMode() == GameMode.SURVIVAL || event.getPlayer().getGameMode() == GameMode.ADVENTURE) {
      StatUtil.changeEnergy(mob, -12);
    }
    Vector bonusVelocity = event.getPlayer().getLocation().getDirection();
    bonusVelocity.setY(Math.max(0.06, bonusVelocity.getY()));
    double moveMult = 1 + 0.25 * mob.getStat(StrifeStat.MOVEMENT_SPEED) / 100;
    bonusVelocity.normalize().multiply(0.28);
    bonusVelocity.setX(bonusVelocity.getX() * moveMult);
    bonusVelocity.setZ(bonusVelocity.getZ() * moveMult);
    bonusVelocity.multiply((120 - mob.getStat(StrifeStat.WEIGHT)) / 100);

    Vector oldVelocity = event.getPlayer().getVelocity().clone()
        .setY(Math.max(0, event.getPlayer().getVelocity().getY()));
    event.getPlayer().setVelocity(oldVelocity.add(bonusVelocity));
    plugin.getSkillExperienceManager().addExperience(mob, LifeSkillType.AGILITY,
        2, false, false);
    event.getPlayer().getWorld()
        .spawnParticle(Particle.SPIT, event.getPlayer().getLocation(), 20, 0, 0, 0, 0.08);
    event.getPlayer().getWorld()
        .playSound(event.getPlayer().getLocation(), Sound.BLOCK_WOOL_BREAK, 1, 2.0F);

  }

  @EventHandler
  public void onLand(LandEvent event) {
    Riptide.sendCancelPacket(event.getPlayer());
    for (AgilityLocationContainer cont : plugin.getAgilityManager()
        .getInWorld(event.getLocation().getWorld().getName())) {
      boolean success = AgilityLocationContainer.setProgress(cont, event.getPlayer(), event.getLocation());
      if (success) {
        Champion champion = plugin.getChampionManager().getChampion(event.getPlayer());
        float xp = cont.getExp();
        xp *= PlayerDataUtil.getLifeSkillLevel(champion, AGILITY) / cont.getDifficulty();
        xp = Math.min(cont.getExp(), xp);
        plugin.getSkillExperienceManager().addExperience(event.getPlayer(), AGILITY, xp, false, false);
      }
    }
  }
}
