package land.face.strife.listeners;

import static land.face.strife.data.champion.LifeSkillType.AGILITY;

import com.tealcube.minecraft.bukkit.TextUtils;
import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.champion.LifeSkillType;
import land.face.strife.events.LaunchEvent;
import land.face.strife.util.MoveUtil;
import land.face.strife.util.PlayerDataUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.util.Vector;

public class DoubleJumpListener implements Listener {

  private StrifePlugin plugin;


  public DoubleJumpListener(StrifePlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler
  public void join(PlayerJoinEvent event) {
    if (event.getPlayer().getGameMode() == GameMode.CREATIVE || event.getPlayer().getGameMode() == GameMode.SPECTATOR) {
      return;
    }
    int agilityLevel = PlayerDataUtil.getLifeSkillLevel(event.getPlayer(), AGILITY);
    if (agilityLevel > 39 && event.getPlayer().isOnGround()) {
      event.getPlayer().setAllowFlight(true);
      MoveUtil.setJumps(event.getPlayer(), MoveUtil.getMaxJumps(agilityLevel));
      return;
    }
    MoveUtil.setJumps(event.getPlayer(), 0);
  }

  @EventHandler
  public void onLaunch(LaunchEvent event) {
    if (event.getPlayer().getGameMode() == GameMode.CREATIVE || event.getPlayer().getGameMode() == GameMode.SPECTATOR) {
      return;
    }
    int agilityLevel = PlayerDataUtil.getLifeSkillLevel(event.getPlayer(), AGILITY);
    if (agilityLevel > 39) {
      event.getPlayer().setAllowFlight(true);
      MoveUtil.setJumps(event.getPlayer(), MoveUtil.getMaxJumps(agilityLevel));
    }
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void airJump(PlayerToggleFlightEvent event) {
    if (event.getPlayer().getGameMode() == GameMode.CREATIVE || event.getPlayer().getGameMode() == GameMode.SPECTATOR || event.getPlayer().isFlying()) {
      return;
    }

    event.setCancelled(true);
    event.getPlayer().setFlying(false);

    StrifeMob mob = plugin.getStrifeMobManager().getStatMob(event.getPlayer());
    int agilityLevel = PlayerDataUtil.getLifeSkillLevel(mob.getChampion(), AGILITY);

    if (agilityLevel < 40) {
      Bukkit.getLogger().warning("Player below 40 agility tried to toggle fly");
      event.getPlayer().setAllowFlight(false);
      return;
    }

    int jumps = MoveUtil.getJumps(event.getPlayer());

    if (jumps < 1) {
      Bukkit.getLogger().warning("Player with max jumps tried to toggle fly");
      event.getPlayer().setAllowFlight(false);
      return;
    }

    plugin.getEnergyManager().changeEnergy(mob, -20);
    event.getPlayer().setFallDistance(0);

    Vector velocity = event.getPlayer().getVelocity().clone();
    velocity.setY(Math.max(0, velocity.getY()));

    Vector bonusVelocity = event.getPlayer().getLocation().getDirection();
    bonusVelocity.setY(Math.max(2, bonusVelocity.getY()));
    bonusVelocity.normalize().multiply(0.55);

    event.getPlayer().setVelocity(velocity.add(bonusVelocity));

    jumps--;
    MoveUtil.setJumps(event.getPlayer(), jumps);

    if (jumps == 0) {
      event.getPlayer().setAllowFlight(false);
    }

    MessageUtils.sendActionBar(event.getPlayer(),
        TextUtils.color("&3&l" + jumps + " air jumps left!"));

    plugin.getSkillExperienceManager().addExperience(mob, LifeSkillType.AGILITY, 3, false, false);
    event.getPlayer().getWorld()
        .spawnParticle(Particle.CRIT, event.getPlayer().getLocation(), 20, 0, 0, 0, 0.35);
    event.getPlayer().getWorld()
        .playSound(event.getPlayer().getLocation(), Sound.ENTITY_RABBIT_JUMP, 1, 1);
  }
}
