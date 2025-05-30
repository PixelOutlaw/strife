package land.face.strife.listeners;

import static land.face.strife.data.champion.LifeSkillType.AGILITY;
import static land.face.strife.util.UberConfig.*;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import com.tealcube.minecraft.bukkit.facecore.utilities.MoveUtil;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
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
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

public class LaunchAndLandListener implements Listener {

  private final StrifePlugin plugin;
  public static Map<UUID, Long> LAST_JUMP = new HashMap<>();

  public LaunchAndLandListener(StrifePlugin plugin) {
    this.plugin = plugin;
  }


  @EventHandler(priority = EventPriority.MONITOR)
  public void onJumpSave(PlayerJumpEvent event) {
    LAST_JUMP.put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
  }

  @EventHandler
  public void onLongJump(PlayerJumpEvent event) {
    if (!event.getPlayer().getCurrentInput().isForward() || !event.getPlayer().getCurrentInput().isSneak()) {
      return;
    }
    int lastSneak = MoveUtil.getLastSneak(event.getPlayer());
    if (lastSneak == -1 || lastSneak > LONG_JUMP_TIME_LEEWAY) {
      return;
    }
    if (JumpUtil.isRooted(event.getPlayer())) {
      return;
    }
    StrifeMob mob = plugin.getStrifeMobManager().getStatMob(event.getPlayer());
    if (!mob.checkSkill(AGILITY, SkillRank.APPRENTICE)) {
      return;
    }
    if (event.getPlayer().getGameMode() == GameMode.SURVIVAL || event.getPlayer().getGameMode() == GameMode.ADVENTURE) {
      float cost = LONG_JUMP_COST_FLAT + LONG_JUMP_COST_PERCENT * mob.getMaxEnergy();
      if (mob.getEnergy() < cost) {
        plugin.getGuiManager().postNotice(event.getPlayer(), GuiManager.NOTICE_ENERGY, 10);
        event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 0.5f, 2.0f);
        return;
      }
      StatUtil.changeEnergy(mob, -cost);
    }
    Vector direction = event.getPlayer().getLocation().getDirection();
    direction.normalize();
    double moveMult = 1 + LONG_JUMP_MOVE_MULT * mob.getStat(StrifeStat.MOVEMENT_SPEED) / 100;
    double weightMult = 1 / ((100 + mob.getStat(StrifeStat.WEIGHT)) / 200);
    direction.multiply(LONG_JUMP_POWER * moveMult * weightMult);
    direction.setY(Math.abs(direction.getY()) * LONG_JUMP_VERT_CLAMP);
    direction.setY(direction.getY() + LONG_JUMP_FLAT_VELOCITY);
    event.getPlayer().setVelocity(direction);

    if (plugin.isInHome(event.getPlayer())) {
      event.getPlayer().spawnParticle(Particle.CRIT, event.getPlayer().getLocation(), 20, 0, 0, 0, 0.6);
      event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.BLOCK_WOOL_BREAK, 1, 2.0F);
    } else {
      event.getPlayer().getWorld().spawnParticle(Particle.CRIT, event.getPlayer().getLocation(), 20, 0, 0, 0, 0.6);
      event.getPlayer().getWorld().playSound(event.getPlayer().getLocation(), Sound.BLOCK_WOOL_BREAK, 1, 2.0F);
    }
  }
}
