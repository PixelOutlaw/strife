package land.face.strife.listeners;

import static land.face.strife.data.champion.LifeSkillType.AGILITY;

import com.tealcube.minecraft.bukkit.facecore.event.LandEvent;
import com.tealcube.minecraft.bukkit.facecore.utilities.MoveUtil;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import land.face.strife.StrifePlugin;
import land.face.strife.data.NoticeData;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.champion.LifeSkillType;
import land.face.strife.events.AirJumpEvent;
import land.face.strife.managers.GuiManager;
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
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class DoubleJumpListener implements Listener {

  private final StrifePlugin plugin;
  private final Map<Integer, Vector> cachedJumpAnimation = new HashMap<>();

  private final Map<Player, Float> dodgeCost = new WeakHashMap<>();
  private final Map<Player, Long> lastDodge = new WeakHashMap<>();

  private final float rollPower;
  private final float rollAscent;
  private final float baseRollCost;
  private final float rollCostMult;
  private final float rollCostFlat;
  private final float rollCostDecay;
  private final float maxRollCost;

  public DoubleJumpListener(StrifePlugin plugin) {
    this.plugin = plugin;
    for (int step = 0; step < 21; step++) {
      double radian = Math.toRadians(360 * (step * 0.05));
      Vector vector = new Vector(Math.cos(radian), 0, Math.sin(radian));
      cachedJumpAnimation.put(step, vector);
    }
    rollPower = (float) plugin.getSettings()
        .getDouble("config.mechanics.agility.roll-power", 1f);
    rollAscent = (float) plugin.getSettings()
        .getDouble("config.mechanics.agility.roll-ascent", 0.12f);
    baseRollCost = (float) plugin.getSettings()
        .getDouble("config.mechanics.agility.base-roll-cost", 6f);
    rollCostMult = (float) plugin.getSettings()
        .getDouble("config.mechanics.agility.roll-cost-mult", 1.5f);
    rollCostFlat = (float) plugin.getSettings()
        .getDouble("config.mechanics.agility.roll-cost-flat-increase", 1f);
    rollCostDecay = (float) plugin.getSettings()
        .getDouble("config.mechanics.agility.roll-cost-decay", 3f) / 1000;
    maxRollCost = (float) plugin.getSettings()
        .getDouble("config.mechanics.agility.max-roll-cost", 45);
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
      int maxJumps = JumpUtil.getMaxJumps(mob);
      JumpUtil.setJumps(mob, maxJumps);
    }
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void airJump(PlayerToggleSneakEvent event) {
    if (event.isSneaking() || event.getPlayer().isOnGround() || event.getPlayer().isFlying() ||
        MoveUtil.getLastSneak(event.getPlayer()) > 200) {
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
      plugin.getGuiManager()
          .postNotice(event.getPlayer(), new NoticeData(GuiManager.NOTICE_ENERGY, 49, 10));
      event.getPlayer()
          .playSound(event.getPlayer().getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 0.5f, 2.0f);
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

  @EventHandler(priority = EventPriority.NORMAL)
  public void dodgeRoll(PlayerToggleSneakEvent event) {
    if (event.isSneaking() || event.isCancelled() || event.getPlayer().isSprinting()) {
      return;
    }
    if (event.getPlayer().hasPotionEffect(PotionEffectType.JUMP)
        && event.getPlayer().getPotionEffect(PotionEffectType.JUMP).getAmplifier() < 0) {
      return;
    }
    if (event.getPlayer().hasPotionEffect(PotionEffectType.SLOW)) {
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
    if (mob.getChampion().getLifeSkillLevel(AGILITY) < 10) {
      return;
    }

    float currentCost = dodgeCost.getOrDefault(player, baseRollCost);
    if (lastDodge.containsKey(player)) {
      float msSinceDodge = System.currentTimeMillis() - lastDodge.get(player);
      currentCost = Math.max(baseRollCost, currentCost - (msSinceDodge * rollCostDecay));
    }

    if (player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE) {
      if (mob.getEnergy() < currentCost) {
        plugin.getGuiManager().postNotice(player, new NoticeData(GuiManager.NOTICE_ENERGY, 49, 10));
        player.playSound(player.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 0.5f, 2.0f);
        return;
      }
    }
    Vector currentVelocity = MoveUtil.getVelocity(player).clone();
    Vector horizontalMovement = currentVelocity.clone().setY(0.0001);
    if (horizontalMovement.lengthSquared() < 0.001) {
      return;
    }

    float finalCurrentCost = currentCost;
    Bukkit.getScheduler().runTaskLater(StrifePlugin.getInstance(), () -> {
      if (MoveUtil.getVelocity(player).getY() < 0.001) {

        StatUtil.changeEnergy(mob, -finalCurrentCost);

        lastDodge.put(player, System.currentTimeMillis());
        dodgeCost.put(player, Math.min(maxRollCost, finalCurrentCost * rollCostMult + rollCostFlat));

        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20, 1, true));

        plugin.getAbilityManager().setGlobalCooldown(player, 16);
        plugin.getAttackSpeedManager().resetAttack(mob, 1f, false);

        player.setVelocity(currentVelocity.add(horizontalMovement.normalize()
            .multiply(rollPower)).setY(rollAscent));
        player.getWorld().spawnParticle(Particle.SPIT,
            player.getLocation(), 10, 0, 0, 0, 0.11);
        player.getWorld().playSound(player.getLocation(),
            Sound.BLOCK_WOOL_BREAK, 1, 2.0F);
      }
    }, 3L);
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
    JumpUtil.setJumps(mob, jumps);

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
