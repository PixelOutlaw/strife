package info.faceland.strife.listeners;

import static org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH;

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
    double maxHealth = ((Player) event.getEntity()).getAttribute(GENERIC_MAX_HEALTH).getValue();
    double damage = event.getDamage(DamageModifier.BASE);
    damage += maxHealth * (damage / 100);
    event.setDamage(damage);
  }
}
