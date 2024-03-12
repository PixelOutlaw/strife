package land.face.strife.listeners;

import static land.face.strife.data.champion.LifeSkillType.AGILITY;

import com.tealcube.minecraft.bukkit.facecore.event.LandEvent;
import com.tealcube.minecraft.bukkit.facecore.event.LaunchEvent;
import com.tealcube.minecraft.bukkit.facecore.utilities.MoveUtil;
import java.util.Map;
import java.util.UUID;
import land.face.strife.StrifePlugin;
import land.face.strife.data.AgilityLocationContainer;
import land.face.strife.data.NoticeData;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.champion.Champion;
import land.face.strife.data.champion.SkillRank;
import land.face.strife.data.effects.Riptide;
import land.face.strife.managers.GuiManager;
import land.face.strife.stats.StrifeStat;
import land.face.strife.util.JumpUtil;
import land.face.strife.util.PlayerDataUtil;
import land.face.strife.util.StatUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
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
    if (JumpUtil.isRooted(event.getPlayer())) {
      return;
    }
    int lastSneak = MoveUtil.getLastSneak(event.getPlayer());
    if (lastSneak == -1 || lastSneak > 200) {
      return;
    }
    StrifeMob mob = plugin.getStrifeMobManager().getStatMob(event.getPlayer());
    if (!mob.checkSkill(AGILITY, SkillRank.APPRENTICE)) {
      return;
    }
    if (event.getPlayer().getGameMode() == GameMode.SURVIVAL || event.getPlayer().getGameMode() == GameMode.ADVENTURE) {
      if (mob.getEnergy() < 12) {
        plugin.getGuiManager().postNotice(event.getPlayer(), new NoticeData(GuiManager.NOTICE_ENERGY, 10));
        event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 0.5f, 2.0f);
        return;
      }
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
    event.getPlayer().getWorld()
        .spawnParticle(Particle.CRIT_MAGIC, event.getPlayer().getLocation(), 20, 0, 0, 0, 0.4);
    event.getPlayer().getWorld()
        .playSound(event.getPlayer().getLocation(), Sound.BLOCK_WOOL_BREAK, 1, 2.0F);

  }

  @EventHandler
  public void onLand(LandEvent event) {
    Riptide.sendCancelPacket(event.getPlayer());
    for (AgilityLocationContainer cont : plugin.getAgilityManager().getInWorld(event.getLocation().getWorld().getName())) {
      boolean success = AgilityLocationContainer.setProgress(cont, event.getPlayer(), event.getLocation());
      if (success) {
        float xp = cont.getExp();
        if (cont.getRecentPlayers().contains(event.getPlayer().getUniqueId())) {
          // Nothing
        } else {
          cont.getRecentPlayers().add(event.getPlayer().getUniqueId());
          UUID uuid = event.getPlayer().getUniqueId();
          Bukkit.getScheduler().runTaskLater(plugin, () -> cont.getRecentPlayers().remove(uuid), 20L * 60 * 5);
          xp *= 10;
        }
        plugin.getSkillExperienceManager().addExperience(event.getPlayer(), AGILITY, xp, false, false);
      }
    }
  }
}
