package land.face.strife.listeners;

import com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor;
import com.tealcube.minecraft.bukkit.facecore.utilities.MoveUtil;
import io.pixeloutlaw.minecraft.spigot.garbage.StringExtensionsKt;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.champion.LifeSkillType;
import land.face.strife.data.pojo.SkillLevelData;
import land.face.strife.stats.StrifeStat;
import land.face.strife.stats.StrifeTrait;
import land.face.strife.util.DamageUtil;
import land.face.strife.util.PlayerDataUtil;
import land.face.strife.util.SpecialStatusUtil;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class FallListener implements Listener {

  private final StrifePlugin plugin;
  private final int fallMs;
  private final static String ROLL_TEXT = StringExtensionsKt.chatColorize("&3&l- Roll -");

  public FallListener(StrifePlugin plugin) {
    this.plugin = plugin;
    fallMs = plugin.getSettings().getInt("config.mechanics.agility.fall-damage-crouch-reduction-ms");
  }

  @EventHandler(priority = EventPriority.LOW)
  public void onFallDamage(EntityDamageEvent event) {
    if (event.getCause() != DamageCause.FALL || event.isCancelled()) {
      return;
    }
    if (SpecialStatusUtil.isFallImmune(event.getEntity())) {
      event.setCancelled(true);
      return;
    }
    if (!(event.getEntity() instanceof Player player)) {
      return;
    }

    int msSinceLastSneak = MoveUtil.getLastSneak(player);
    boolean rollBonus = msSinceLastSneak != -1 && msSinceLastSneak <= fallMs;

    StrifeMob mob = plugin.getStrifeMobManager().getStatMob(player);

    if (player.getEyeLocation().getPitch() > 60 && player.isBlocking() && mob.hasTrait(StrifeTrait.SHIELD_BOUNCE)) {
      Location location = player.getLocation().clone();
      location.setPitch(0);
      Vector newMovement = location.getDirection().setY(0.00001);
      newMovement.normalize();
      double intensity = Math.min(2, event.getDamage() / 10);
      newMovement.multiply(1.5 + intensity * 0.55);
      newMovement.setY(0.625 + intensity * 0.55);
      event.getEntity().setVelocity(newMovement);
      player.getWorld().playSound(player.getLocation(), Sound.ITEM_SHIELD_BLOCK, 1, 1.3f);
      event.setCancelled(true);
      return;
    }
    if (mob.hasTrait(StrifeTrait.SOFT_LANDING)) {
      player.getWorld().playSound(player.getLocation(), Sound.BLOCK_GRASS_STEP, 1.2f, 1f);
      event.setCancelled(true);
      return;
    }
    if (mob.isInvincible()) {
      event.setCancelled(true);
      return;
    }

    double damage = event.getDamage(DamageModifier.BASE) - 1;
    float jumpHeight = mob.getStat(StrifeStat.JUMP_HEIGHT);
    if (jumpHeight > 0) {
      damage -= 0.04 * jumpHeight;
    }
    if (damage <= 0) {
      event.setCancelled(true);
      return;
    }

    float fallDamageStat = mob.getStat(StrifeStat.FALL_DAMAGE);
    if (fallDamageStat >= 100) {
      event.setCancelled(true);
      return;
    }

    double maxHealth = mob.getMaxLife();
    damage += damage * maxHealth * 0.055;
    damage = Math.max(damage, 0);

    SkillLevelData data = PlayerDataUtil.getSkillLevels(mob, LifeSkillType.AGILITY, true);
    if (rollBonus) {
      damage *= 100.0 / (100 + data.getLevelWithBonus());
    } else {
      damage *= 50.0 / (50 + data.getLevelWithBonus());
    }

    if (player.hasPotionEffect(PotionEffectType.RESISTANCE)) {
      //noinspection DataFlowIssue
      int level = player.getPotionEffect(PotionEffectType.RESISTANCE).getAmplifier();
      if (level >= 10) {
        event.setCancelled(true);
        return;
      }
      damage *= 1 - (0.1 * (level + 1));
    }

    float netGravity = mob.getStat(StrifeStat.INCREASED_GRAVITY) - mob.getStat(StrifeStat.REDUCED_GRAVITY);
    if (netGravity < 0) {
      damage *= 100 / (-netGravity + 100);
    }

    damage *= 1 - fallDamageStat / 100;

    DamageUtil.removeDamageModifiers(event);
    if (damage >= player.getHealth()) {
      damage = DamageUtil.doPreDeath(mob, (float) damage);
    }
    if (damage >= player.getHealth()) {
      plugin.getDamageManager().getSourceOfDeath().put(player.getUniqueId(), FaceColor.YELLOW + "falling damage");
    }
    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 0, true, false));
    event.setDamage(DamageModifier.BASE, damage);
  }
}
