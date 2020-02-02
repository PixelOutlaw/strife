package land.face.strife.listeners;

import static org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH;
import static org.bukkit.potion.PotionEffectType.DAMAGE_RESISTANCE;

import land.face.strife.StrifePlugin;
import land.face.strife.data.champion.Champion;
import land.face.strife.data.champion.LifeSkillType;
import land.face.strife.util.DamageUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier;

public class FallListener implements Listener {

  private StrifePlugin plugin;

  public FallListener(StrifePlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onFallDamage(EntityDamageEvent event) {
    if (event.getCause() != DamageCause.FALL || event.isCancelled()) {
      return;
    }
    if (event.getEntity().hasMetadata("NO_FALL")) {
      event.setCancelled(true);
      return;
    }
    if (!(event.getEntity() instanceof Player)) {
      return;
    }

    double damage = event.getDamage(DamageModifier.BASE);
    double maxHealth = ((Player) event.getEntity()).getAttribute(GENERIC_MAX_HEALTH).getValue();
    damage += damage * maxHealth * 0.04;

    Champion champion = plugin.getChampionManager().getChampion((Player) event.getEntity());
    damage *= 100.0 / (100 + champion.getEffectiveLifeSkillLevel(LifeSkillType.AGILITY, true));

    if (((Player) event.getEntity()).hasPotionEffect(DAMAGE_RESISTANCE)) {
      double level = ((Player) event.getEntity()).getPotionEffect(DAMAGE_RESISTANCE).getAmplifier();
      damage *= 1 - (0.1 * (level + 1));
    }

    if (damage < ((Player) event.getEntity()).getHealth()) {
      plugin.getSkillExperienceManager().addExperience(champion, LifeSkillType.AGILITY,
          3 + event.getDamage(DamageModifier.BASE) / 4, false, true);
    }

    if (damage < 2) {
      event.setCancelled(true);
      return;
    }

    DamageUtil.removeDamageModifiers(event);
    event.setDamage(DamageModifier.BASE, Math.max(damage, 0));
  }
}
