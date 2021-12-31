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
import land.face.strife.events.AirJumpEvent;
import land.face.strife.stats.StrifeStat;
import land.face.strife.util.JumpUtil;
import land.face.strife.util.PlayerDataUtil;
import land.face.strife.util.StatUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
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

    boolean waterHop = event.getPlayer().getLocation().getBlock().getType() == Material.WATER &&
        event.getPlayer().getVelocity().getY() < -0.4 &&
        event.getPlayer().getLocation().clone().add(0, 1, 0).getBlock().getType().isAir();

    int jumps = JumpUtil.getJumps(event.getPlayer());
    if ((jumps < 1 && !waterHop)) {
      return;
    }
    StrifeMob mob = plugin.getStrifeMobManager().getStatMob(event.getPlayer());
    if (mob.getEnergy() < 20) {
      return;
    }

    int agilityLevel = PlayerDataUtil.getLifeSkillLevel(mob.getChampion(), AGILITY);

    if (agilityLevel < 40) {
      return;
    }

    if (waterHop) {
      doWaterSkip(mob, event.getPlayer());
    } else {
      doAirJump(mob, event.getPlayer(), jumps);
    }
  }

  private void doAirJump(StrifeMob mob, Player player, int jumps) {
    if (player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE) {
      StatUtil.changeEnergy(mob, -20);
    }

    AirJumpEvent airJumpEvent = new AirJumpEvent(mob);
    Bukkit.getPluginManager().callEvent(airJumpEvent);

    player.setFallDistance(0);

    Vector velocity = player.getVelocity().clone();

    Vector bonusVelocity = player.getLocation().getDirection();
    bonusVelocity.setY(Math.max(2, bonusVelocity.getY()));
    bonusVelocity.normalize().multiply(0.55);

    double bonusY = Math.max(bonusVelocity.getY(), velocity.getY());
    bonusVelocity.setY(bonusY);
    bonusVelocity.multiply((120 - mob.getStat(StrifeStat.WEIGHT)) / 100);

    velocity.setY(0);
    player.setVelocity(velocity.add(bonusVelocity));

    jumps--;
    JumpUtil.setJumps(player, jumps);

    int maxJumps = JumpUtil.getMaxJumps(mob);

    String bars = IntStream.range(0, maxJumps).mapToObj(i -> "▌").collect(Collectors.joining(""));
    bars = insert(bars, "&0", Math.min(jumps, maxJumps));
    String message = StringExtensionsKt.chatColorize("&3&lAir Jumps: &b" + bars);
    AdvancedActionBarUtil.addMessage(player, JUMP_AB_KEY, message, 70, 0);

    plugin.getSkillExperienceManager().addExperience(mob, LifeSkillType.AGILITY, 3, false, false);
    flingParticle(player);
    player.getWorld().playSound(player.getLocation(), Sound.BLOCK_WOOL_BREAK, 1, 2.0F);
  }

  private void doWaterSkip(StrifeMob mob, Player player) {
    if (player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE) {
      StatUtil.changeEnergy(mob, -8);
    }

    AirJumpEvent airJumpEvent = new AirJumpEvent(mob);
    Bukkit.getPluginManager().callEvent(airJumpEvent);

    player.setFallDistance(0);

    Vector velocity = player.getVelocity().clone();

    Vector bonusVelocity = player.getLocation().getDirection();
    bonusVelocity.setY(Math.max(1, bonusVelocity.getY()));
    bonusVelocity.normalize().multiply(0.65);

    double bonusY = Math.max(bonusVelocity.getY(), velocity.getY());
    bonusVelocity.setY(bonusY);
    bonusVelocity.multiply((120 - mob.getStat(StrifeStat.WEIGHT)) / 100);

    velocity.setY(0);
    player.setVelocity(velocity.add(bonusVelocity));

    plugin.getSkillExperienceManager().addExperience(mob, LifeSkillType.AGILITY, 2, false, false);
    player.getWorld().playSound(player.getLocation(), Sound.BLOCK_WOOL_BREAK, 1, 2.0F);
  }

  public static String insert(String str, String insert, int position) {
    return str.substring(0, position) + insert + str.substring(position);
  }

  private void flingParticle(Player player) {
    for (Integer step : cachedJumpAnimation.keySet()) {
      player.getWorld().spawnParticle(Particle.SPIT, player.getLocation(), 0,
          cachedJumpAnimation.get(step).getX(), 0.1, cachedJumpAnimation.get(step).getZ(), 0.11);
    }
  }
}
