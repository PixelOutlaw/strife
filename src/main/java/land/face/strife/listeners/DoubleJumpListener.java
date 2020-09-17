package land.face.strife.listeners;

import static land.face.strife.data.champion.LifeSkillType.AGILITY;

import com.tealcube.minecraft.bukkit.facecore.event.LandEvent;
import com.tealcube.minecraft.bukkit.facecore.utilities.AdvancedActionBarUtil;
import com.tealcube.minecraft.bukkit.facecore.utilities.MoveUtil;
import io.pixeloutlaw.minecraft.spigot.garbage.StringExtensionsKt;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.champion.LifeSkillType;
import land.face.strife.util.JumpUtil;
import land.face.strife.util.PlayerDataUtil;
import org.bukkit.GameMode;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.util.Vector;

public class DoubleJumpListener implements Listener {

  private final StrifePlugin plugin;
  private final Map<Integer, Vector> cachedJumpAnimation = new HashMap<>();
  private final String JUMP_AB_KEY = "air-jump";

  public DoubleJumpListener(StrifePlugin plugin) {
    this.plugin = plugin;
    for (int step = 0; step < 21; step++) {
      double radian = Math.toRadians(360 * (step * 0.05));
      Vector vector = new Vector(Math.cos(radian), 0, Math.sin(radian));
      cachedJumpAnimation.put(step, vector);
    }
  }

  @EventHandler
  public void join(PlayerJoinEvent event) {
    if (event.getPlayer().isOnGround()) {
      resetJumps(event.getPlayer());
    }
  }

  @EventHandler
  public void onLand(LandEvent event) {
    resetJumps(event.getPlayer());
  }

  private void resetJumps(Player player) {
    StrifeMob mob = plugin.getStrifeMobManager().getStatMob(player);
    int agilityLevel = mob.getChampion().getLifeSkillLevel(AGILITY);
    if (agilityLevel > 39) {
      int jumps = JumpUtil.getJumps(player);
      int maxJumps = JumpUtil.getMaxJumps(mob);
      if (jumps != maxJumps) {
        String message = StringExtensionsKt.chatColorize("&bJumps Recharged!");
        AdvancedActionBarUtil.addMessage(player, JUMP_AB_KEY, message, 60, 0);
      }
      JumpUtil.setJumps(player, maxJumps);
    }
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void airJump(PlayerToggleSneakEvent event) {
    if (event.isSneaking() || event.getPlayer().isOnGround() || event.getPlayer().isFlying() ||
        MoveUtil.getLastSneak(event.getPlayer().getUniqueId()) > 200) {
      return;
    }
    if (MoveUtil.timeOffGround(event.getPlayer()) < 200) {
      return;
    }

    int jumps = JumpUtil.getJumps(event.getPlayer());
    if (event.getPlayer().getFoodLevel() < 6 || jumps < 1) {
      return;
    }

    StrifeMob mob = plugin.getStrifeMobManager().getStatMob(event.getPlayer());
    int agilityLevel = PlayerDataUtil.getLifeSkillLevel(mob.getChampion(), AGILITY);

    if (agilityLevel < 40) {
      return;
    }

    if (event.getPlayer().getGameMode() == GameMode.SURVIVAL
        || event.getPlayer().getGameMode() == GameMode.ADVENTURE) {
      plugin.getEnergyManager().changeEnergy(mob, -20);
    }
    event.getPlayer().setFallDistance(0);

    Vector velocity = event.getPlayer().getVelocity().clone();

    Vector bonusVelocity = event.getPlayer().getLocation().getDirection();
    bonusVelocity.setY(Math.max(2, bonusVelocity.getY()));
    bonusVelocity.normalize().multiply(0.55);

    double bonusY = Math.max(bonusVelocity.getY(), velocity.getY());
    bonusVelocity.setY(bonusY);

    velocity.setY(0);
    event.getPlayer().setVelocity(velocity.add(bonusVelocity));

    jumps--;
    JumpUtil.setJumps(event.getPlayer(), jumps);

    int maxJumps = JumpUtil.getMaxJumps(mob);

    String bars = IntStream.range(0, maxJumps).mapToObj(i -> "▌").collect(Collectors.joining(""));
    bars = insert(bars, "&0", Math.min(jumps, maxJumps));
    String message = StringExtensionsKt.chatColorize("&3&lAir Jumps: &b" + bars);
    AdvancedActionBarUtil.addMessage(event.getPlayer(), JUMP_AB_KEY, message, 70, 0);

    plugin.getSkillExperienceManager().addExperience(mob, LifeSkillType.AGILITY, 3, false, false);
    flingParticle(event.getPlayer());
    event.getPlayer().getWorld()
        .playSound(event.getPlayer().getLocation(), Sound.BLOCK_WOOL_BREAK, 1, 2.0F);
  }

  public static String insert(String str, String insert, int position) {
    return str.substring(0, position) + insert + str.substring(position);
  }

  private void flingParticle(Player player) {
    for (Integer step : cachedJumpAnimation.keySet()) {
      player.getWorld().spawnParticle(Particle.CRIT_MAGIC, player.getLocation(), 0,
          cachedJumpAnimation.get(step).getX(), 0, cachedJumpAnimation.get(step).getZ(), 0.35);
    }
  }
}
