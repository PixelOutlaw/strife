package land.face.strife.listeners;

import static org.bukkit.potion.PotionEffectType.DAMAGE_RESISTANCE;
import static org.bukkit.potion.PotionEffectType.SLOW;

import com.tealcube.minecraft.bukkit.facecore.utilities.MoveUtil;
import io.pixeloutlaw.minecraft.spigot.garbage.StringExtensionsKt;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.champion.Champion;
import land.face.strife.data.champion.LifeSkillType;
import land.face.strife.util.DamageUtil;
import land.face.strife.util.SpecialStatusUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier;
import org.bukkit.potion.PotionEffect;

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
;
    StrifeMob playerMob = plugin.getStrifeMobManager().getStatMob(player);

    double damage = event.getDamage(DamageModifier.BASE) - 1;
    double maxHealth = playerMob.getMaxLife();
    damage += damage * maxHealth * 0.055;
    damage = Math.max(damage, 0);

    Champion champion = playerMob.getChampion();
    if (rollBonus) {
      damage *= 100.0 / (100 + champion.getEffectiveLifeSkillLevel(LifeSkillType.AGILITY, true));
    } else {
      damage *= 50.0 / (50 + champion.getEffectiveLifeSkillLevel(LifeSkillType.AGILITY, true));
      player.addPotionEffect(new PotionEffect(SLOW, 100, 0, true));
    }

    if (player.hasPotionEffect(DAMAGE_RESISTANCE)) {
      double level = ((Player) event.getEntity()).getPotionEffect(DAMAGE_RESISTANCE).getAmplifier();
      damage *= 1 - (0.1 * (level + 1));
    }

    if (damage < player.getHealth()) {
      float xp = Math.min(2 + (float) event.getDamage(DamageModifier.BASE) / 3, 10);
      if (rollBonus) {
        xp *= 1.8;
      }
      plugin.getSkillExperienceManager().addExperience((Player) event.getEntity(),
          LifeSkillType.AGILITY, xp, false, false);
    }

    if (damage <= 0) {
      event.setCancelled(true);
      return;
    }

    if (damage >= player.getHealth()) {
      DamageUtil.doPreDeath(playerMob);
    }

    DamageUtil.removeDamageModifiers(event);
    event.setDamage(DamageModifier.BASE, damage);
  }
}
