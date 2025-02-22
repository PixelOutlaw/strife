package land.face.strife.listeners;

import static land.face.strife.data.champion.LifeSkillType.AGILITY;

import com.tealcube.minecraft.bukkit.facecore.event.LandEvent;
import com.tealcube.minecraft.bukkit.facecore.event.ToggleJumpEvent;
import com.tealcube.minecraft.bukkit.facecore.utilities.MoveUtil;
import java.util.HashMap;
import java.util.Map;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.champion.SkillRank;
import land.face.strife.data.conditions.GroundedCondition;
import land.face.strife.events.AirJumpEvent;
import land.face.strife.managers.GuiManager;
import land.face.strife.stats.StrifeStat;
import land.face.strife.stats.StrifeTrait;
import land.face.strife.util.DirectionUtil;
import land.face.strife.util.JumpUtil;
import land.face.strife.util.StatUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import soujah.dev.honeyImHome.API.HoneyImHomeAPI;

public class AgilityListener implements Listener {

  private final StrifePlugin plugin;
  private final Map<Integer, Vector> cachedJumpAnimation = new HashMap<>();

  public static float ROLL_COST_FLAT;
  public static float ROLL_COST_PERCENT;
  public static float AIR_JUMP_COST_FLAT;
  public static float AIR_JUMP_COST_PERCENT;
  public static float LONG_JUMP_COST_FLAT;
  public static float LONG_JUMP_COST_PERCENT;

  private final float rollPower;
  private final float rollAscent;

  private final int rollLeviTicks;

  private final float airDashAscent;
  private final float airDashPower;

  private final float particleSpeed;
  private final float longParticleSpeed;
  private final Particle particle;

  public AgilityListener(StrifePlugin plugin) {
    this.plugin = plugin;
    for (int step = 0; step < 21; step++) {
      double radian = Math.toRadians(360 * (step * 0.05));
      Vector vector = new Vector(Math.cos(radian), 0, Math.sin(radian));
      cachedJumpAnimation.put(step, vector);
    }
    ROLL_COST_FLAT = (float) plugin.getSettings()
        .getDouble("config.mechanics.agility.base-roll-cost", 6f);
    ROLL_COST_PERCENT = (float) plugin.getSettings()
        .getDouble("config.mechanics.agility.percent-roll-cost", 0.12f);

    AIR_JUMP_COST_FLAT = (float) plugin.getSettings()
        .getDouble("config.mechanics.agility.air-jump-cost", 6f);
    AIR_JUMP_COST_PERCENT = (float) plugin.getSettings()
        .getDouble("config.mechanics.agility.air-jump-percent-cost", 0.12f);

    LONG_JUMP_COST_FLAT = (float) plugin.getSettings()
        .getDouble("config.mechanics.agility.long-jump-cost", 6f);
    LONG_JUMP_COST_PERCENT = (float) plugin.getSettings()
        .getDouble("config.mechanics.agility.long-jump-percent-cost", 0.12f);

    rollPower = (float) plugin.getSettings()
        .getDouble("config.mechanics.agility.roll-power", 1f);
    rollAscent = (float) plugin.getSettings()
        .getDouble("config.mechanics.agility.roll-ascent", 0.12f);
    rollLeviTicks = plugin.getSettings()
        .getInt("config.mechanics.agility.roll-levitation-ticks", 2);

    airDashAscent = (float) plugin.getSettings()
        .getDouble("config.mechanics.agility.air-dash-ascent", 0.275);
    airDashPower = (float) plugin.getSettings()
        .getDouble("config.mechanics.agility.air-dash-power", 0.825);

    particleSpeed = (float) plugin.getSettings()
        .getDouble("config.mechanics.agility.air-jump-particle-speed", 0.11);
    longParticleSpeed = (float) plugin.getSettings()
        .getDouble("config.mechanics.agility.long-jump-particle-speed", 0.11);
    particle = Particle.valueOf(plugin.getSettings()
        .getString("config.mechanics.agility.air-jump-particle", "SPIT"));
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
    int before = mob.getAirJumps();
    mob.setMaxAirJumps((int) mob.getStat(StrifeStat.AIR_JUMPS));
    mob.setAirJumps(mob.getMaxAirJumps());
    if (before != mob.getAirJumps()) {
      plugin.getGuiManager().updateAirJumpDisplay(mob);
    }
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void airJump(ToggleJumpEvent event) {
    if (!event.isState()) {
      return;
    }
    if (event.getPlayer().isOnGround() || event.getPlayer().isFlying() ||
        event.getPlayer().getGameMode() != GameMode.ADVENTURE) {
      return;
    }
    var velocity = MoveUtil.getVelocity(event.getPlayer());
    if (velocity.getY() >= 0 && GroundedCondition.isGroundedOrCloseToGround(event.getPlayer(), 1.5f)) {
      return;
    }
    StrifeMob mob = plugin.getStrifeMobManager().getStatMob(event.getPlayer());
    int jumps = mob.getAirJumps();
    if (jumps < 1) {
      return;
    }

    float cost = AIR_JUMP_COST_FLAT + mob.getMaxEnergy() * AIR_JUMP_COST_PERCENT;
    cost *= (100 + mob.getStat(StrifeStat.WEIGHT)) / 200;

    if (mob.getEnergy() < cost) {
      plugin.getGuiManager().postNotice(event.getPlayer(), GuiManager.NOTICE_ENERGY, 10);
      event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 0.5f, 2.0f);
      return;
    }

    doAirJump(mob, event.getPlayer(), jumps, cost);

    mob.setAirJumps(mob.getAirJumps() - 1);

    AirJumpEvent airJumpEvent = new AirJumpEvent(mob);
    Bukkit.getPluginManager().callEvent(airJumpEvent);

    plugin.getGuiManager().updateAirJumpDisplay(mob);
  }

  @EventHandler(priority = EventPriority.NORMAL)
  public void dodgeRoll(PlayerToggleSneakEvent event) {
    if (event.getPlayer().getGameMode() != GameMode.ADVENTURE) {
      return;
    }
    if (event.isSneaking() || event.isCancelled() || event.getPlayer().isSprinting()) {
      return;
    }
    if (JumpUtil.isRooted(event.getPlayer())) {
      return;
    }
    if (event.getPlayer().getCurrentInput().isJump()) {
      return;
    }
    if (event.getPlayer().hasPotionEffect(PotionEffectType.SLOWNESS)) {
      return;
    }
    int lastSneak = MoveUtil.getLastSneak(event.getPlayer());
    if (lastSneak == -1 || lastSneak > 250) {
      return;
    }
    if (MoveUtil.getVelocity(event.getPlayer()).getY() > 0) {
      return;
    }
    if (!event.getPlayer().getLocation().clone().add(0, -0.02, 0)
        .getBlock().isCollidable()) {
      return;
    }
    doDodgeRoll(plugin.getStrifeMobManager().getStatMob(event.getPlayer()), event.getPlayer());
  }

  private void doDodgeRoll(StrifeMob mob, Player player) {
    if (!mob.checkSkill(AGILITY, SkillRank.APPRENTICE)) {
      return;
    }
    if (!player.getCurrentInput().isForward() && !player.getCurrentInput().isBackward() &&
        !player.getCurrentInput().isLeft() && !player.getCurrentInput().isRight()) {
      return;
    }

    float currentCost = ROLL_COST_FLAT + mob.getMaxEnergy() * ROLL_COST_PERCENT;
    currentCost *= (100 + mob.getStat(StrifeStat.WEIGHT)) / 200;

    if (player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE) {
      if (mob.getEnergy() < currentCost) {
        plugin.getGuiManager().postNotice(player, GuiManager.NOTICE_ENERGY, 10);
        player.playSound(player.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 0.5f, 2.0f);
        return;
      }
    }

    Vector dodgeVector = DirectionUtil.getDodgeRollVelocity(player);

    StatUtil.changeEnergy(mob, -currentCost);

    plugin.getAbilityManager().setGlobalCooldown(player, 16);
    plugin.getAttackSpeedManager().resetAttack(mob, 1f, false);

    PotionListener.ignoreFx = true;
    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 18, 1, true, false));
    player.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, rollLeviTicks, 1, true, false));
    player.addPotionEffect(new PotionEffect(PotionEffectType.WIND_CHARGED, rollLeviTicks, 2, true, false));
    PotionListener.ignoreFx = false;

    player.setVelocity(dodgeVector.multiply(rollPower).setY(rollAscent));

    if (plugin.isInHome(player)) {
      player.spawnParticle(particle, player.getLocation(), 10, 0, 0, 0, longParticleSpeed);
      player.playSound(player.getLocation(), Sound.BLOCK_WOOL_BREAK, 1, 2.0F);
    } else {
      player.getWorld().spawnParticle(particle, player.getLocation(), 10, 0, 0, 0, longParticleSpeed);
      player.getWorld().playSound(player.getLocation(), Sound.BLOCK_WOOL_BREAK, 1, 2.0F);
    }
  }

  private void doAirJump(StrifeMob mob, Player player, int jumps, float cost) {
    if (player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE) {
      StatUtil.changeEnergy(mob, -cost);
    }

    player.setFallDistance(0);

    Vector velocity = player.getVelocity().clone();
    Vector bonusVelocity = player.getLocation().getDirection();

    if (mob.getTraits().contains(StrifeTrait.AIR_DASH)) {
      bonusVelocity.setY(0.0001);
      bonusVelocity.normalize().multiply(airDashPower);
      double bonusY = Math.max(airDashAscent, velocity.getY() + airDashAscent / 3);
      bonusVelocity.setY(bonusY);
    } else {
      bonusVelocity.setY(Math.max(2, bonusVelocity.getY()));
      bonusVelocity.normalize().multiply(0.55);
      double bonusY = Math.max(bonusVelocity.getY(), velocity.getY());
      bonusVelocity.setY(bonusY);
    }

    bonusVelocity.multiply(1 / ((100 + mob.getStat(StrifeStat.WEIGHT)) / 200));
    float netGravity = mob.getStat(StrifeStat.INCREASED_GRAVITY) - mob.getStat(StrifeStat.REDUCED_GRAVITY);
    if (netGravity > 0) {
      bonusVelocity.setY(bonusVelocity.getY() * (1 + netGravity / 185f));
    } else if (netGravity < 0) {
      bonusVelocity.multiply(140f / (-netGravity + 140f));
    }

    velocity.setY(0);
    player.setVelocity(velocity.add(bonusVelocity));
    MoveUtil.setVelocity(player);

    if (plugin.isInHome(player)) {
      flingParticle2(player);
      player.getWorld().playSound(player.getLocation(), Sound.BLOCK_WOOL_BREAK, 1, 2.0F);
    } else {
      flingParticle(player);
      player.getWorld().playSound(player.getLocation(), Sound.BLOCK_WOOL_BREAK, 1, 2.0F);
    }
  }

  private void flingParticle(Player player) {
    for (Integer step : cachedJumpAnimation.keySet()) {
      player.getWorld().spawnParticle(particle, player.getLocation(), 0,
          cachedJumpAnimation.get(step).getX(), 0.1, cachedJumpAnimation.get(step).getZ(), particleSpeed);
    }
  }

  private void flingParticle2(Player player) {
    for (Integer step : cachedJumpAnimation.keySet()) {
      player.spawnParticle(particle, player.getLocation(), 0,
          cachedJumpAnimation.get(step).getX(), 0.1, cachedJumpAnimation.get(step).getZ(), particleSpeed);
    }
  }
}
