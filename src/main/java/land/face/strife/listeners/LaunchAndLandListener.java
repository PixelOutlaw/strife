package land.face.strife.listeners;

import static land.face.strife.data.champion.LifeSkillType.AGILITY;

import com.tealcube.minecraft.bukkit.facecore.event.LaunchEvent;
import com.tealcube.minecraft.bukkit.facecore.utilities.MoveUtil;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.champion.SkillRank;
import land.face.strife.managers.GuiManager;
import land.face.strife.stats.StrifeStat;
import land.face.strife.util.JumpUtil;
import land.face.strife.util.StatUtil;
import org.bukkit.GameMode;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;
import soujah.dev.honeyImHome.API.HoneyImHomeAPI;

public class LaunchAndLandListener implements Listener {

  private final StrifePlugin plugin;

  public LaunchAndLandListener(StrifePlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler
  public void onLaunch(LaunchEvent event) {
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
      float cost = AgilityListener.LONG_JUMP_COST_FLAT + AgilityListener.LONG_JUMP_COST_PERCENT * mob.getMaxEnergy();
      cost *= (100 + mob.getStat(StrifeStat.WEIGHT)) / 200;
      if (mob.getEnergy() < cost) {
        plugin.getGuiManager().postNotice(event.getPlayer(), GuiManager.NOTICE_ENERGY, 10);
        event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 0.5f, 2.0f);
        return;
      }
      StatUtil.changeEnergy(mob, -cost);
    }
    Vector bonusVelocity = event.getPlayer().getLocation().getDirection();
    bonusVelocity.setY(Math.max(0.06, bonusVelocity.getY()));
    double moveMult = 1 + 0.25 * mob.getStat(StrifeStat.MOVEMENT_SPEED) / 100;
    bonusVelocity.normalize().multiply(0.28);
    bonusVelocity.setX(bonusVelocity.getX() * moveMult);
    bonusVelocity.setZ(bonusVelocity.getZ() * moveMult);
    bonusVelocity.multiply(1 / ((100 + mob.getStat(StrifeStat.WEIGHT)) / 200));

    Vector oldVelocity = event.getPlayer().getVelocity().clone()
        .setY(Math.max(0, event.getPlayer().getVelocity().getY()));
    event.getPlayer().setVelocity(oldVelocity.add(bonusVelocity));

    if (HoneyImHomeAPI.isPlayerInHouse(event.getPlayer())) {
      event.getPlayer().spawnParticle(Particle.CRIT, event.getPlayer().getLocation(), 20, 0, 0, 0, 0.4);
      event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.BLOCK_WOOL_BREAK, 1, 2.0F);
    } else {
      event.getPlayer().getWorld().spawnParticle(Particle.CRIT, event.getPlayer().getLocation(), 20, 0, 0, 0, 0.4);
      event.getPlayer().getWorld().playSound(event.getPlayer().getLocation(), Sound.BLOCK_WOOL_BREAK, 1, 2.0F);
    }
  }
}
