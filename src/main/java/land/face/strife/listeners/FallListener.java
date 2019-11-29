package land.face.strife.listeners;

import static org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH;
import static org.bukkit.potion.PotionEffectType.DAMAGE_RESISTANCE;

import land.face.strife.util.DamageUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier;

public class FallListener implements Listener {

  @EventHandler(priority = EventPriority.LOWEST)
  public void onFallDamage(EntityDamageEvent event) {
    if (event.getCause() != DamageCause.FALL || event.isCancelled()) {
      return;
    }
    if (!(event.getEntity() instanceof Player)) {
      return;
    }
    if ((event.getEntity()).getFallDistance() < 2) {
      event.setCancelled(true);
      return;
    }

    double damage = event.getDamage(DamageModifier.BASE);
    double maxHealth = ((Player) event.getEntity()).getAttribute(GENERIC_MAX_HEALTH).getValue();
    damage += maxHealth * (damage / 100);

    if (damage < 1) {
      event.setCancelled(true);
      return;
    }
    if (((Player) event.getEntity()).hasPotionEffect(DAMAGE_RESISTANCE)) {
      double level = ((Player) event.getEntity()).getPotionEffect(DAMAGE_RESISTANCE).getAmplifier();
      damage *= 1 - (0.1 * (level+1));
    }

    DamageUtil.removeDamageModifiers(event);
    event.setDamage(Math.max(damage, 0));
  }
}
