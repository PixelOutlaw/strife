package land.face.strife.listeners;

import static org.bukkit.potion.PotionEffectType.DAMAGE_RESISTANCE;
import static org.bukkit.potion.PotionEffectType.SLOW;

import com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor;
import com.tealcube.minecraft.bukkit.facecore.utilities.MoveUtil;
import io.pixeloutlaw.minecraft.spigot.garbage.StringExtensionsKt;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.champion.Champion;
import land.face.strife.data.champion.LifeSkillType;
import land.face.strife.stats.StrifeTrait;
import land.face.strife.util.DamageUtil;
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
import org.bukkit.util.Vector;

public class FallListener implements Listener {

  private final StrifePlugin plugin;
  private final int fallMs;
  private final static String ROLL_TEXT = StringExtensionsKt.chatColorize("&3&l- Roll -");

  public FallListener(StrifePlugin plugin) {
    this.plugin = plugin;
    fallMs = plugin.getSettings().getInt("config.mechanics.agility.roll-ms");
  }

  @EventHandler(priority = EventPriority.LOWEST)
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

    if (player.getEyeLocation().getPitch() > 75 && player.isBlocking() && mob.hasTrait(StrifeTrait.SHIELD_BOUNCE)) {
      Location location = player.getLocation().clone();
      location.setPitch(0);
      Vector newMovement = location.getDirection().setY(0.00001);
      newMovement.normalize();
      double intensity = Math.min(2, event.getDamage() / 10);
      newMovement.multiply(1.5 + intensity * 0.65);
      newMovement.setY(0.625 + intensity * 0.3);
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

    double damage = event.getDamage(DamageModifier.BASE) - 1;
    double maxHealth = mob.getMaxLife();
    damage += damage * maxHealth * 0.055;
    damage = Math.max(damage, 0);

    Champion champion = mob.getChampion();
    if (rollBonus) {
      damage *= 100.0 / (100 + champion.getEffectiveLifeSkillLevel(LifeSkillType.AGILITY, true));
    } else {
      damage *= 50.0 / (50 + champion.getEffectiveLifeSkillLevel(LifeSkillType.AGILITY, true));
      player.addPotionEffect(new PotionEffect(SLOW, 100, 0, true, false));
    }

    if (mob.isInvincible()) {
      event.setCancelled(true);
      return;
    }

    if (player.hasPotionEffect(DAMAGE_RESISTANCE)) {
      double level = ((Player) event.getEntity()).getPotionEffect(DAMAGE_RESISTANCE).getAmplifier();
      damage *= 1 - (0.1 * (level + 1));
    }

    if (damage <= 0) {
      event.setCancelled(true);
      return;
    }

    DamageUtil.removeDamageModifiers(event);
    if (damage >= player.getHealth()) {
      damage = DamageUtil.doPreDeath(mob, (float) damage);
    }
    if (damage >= player.getHealth()) {
      plugin.getDamageManager().getSourceOfDeath().put(player.getUniqueId(), FaceColor.YELLOW + "falling damage");
    }
    event.setDamage(DamageModifier.BASE, damage);
  }
}
