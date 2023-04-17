package land.face.strife.listeners;

import java.util.ArrayList;
import java.util.List;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.stats.StrifeTrait;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent.Action;
import org.bukkit.event.entity.EntityPotionEffectEvent.Cause;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PotionListener implements Listener {

  private final StrifePlugin plugin;
  private final List<PotionEffect> ignoreEffects = new ArrayList<>();

  public PotionListener(StrifePlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onPotionMonitor(EntityPotionEffectEvent event) {
    ignoreEffects.remove(event.getNewEffect());
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onPotion(EntityPotionEffectEvent event) {
    if (event.getEntity().hasMetadata("NPC") || event.getEntity().isInvulnerable()) {
      return;
    }
    if (!(event.getAction() == Action.ADDED || event.getAction() == Action.CHANGED)) {
      return;
    }
    if (event.isCancelled() || !(event.getEntity() instanceof LivingEntity)) {
      return;
    }
    if (ignoreEffects.contains(event.getNewEffect())) {
      return;
    }
    switch (event.getNewEffect().getType().getName()) {
      case "SLOW" -> {
        StrifeMob mob = plugin.getStrifeMobManager().getStatMob((LivingEntity) event.getEntity());
        if (mob.hasTrait(StrifeTrait.UNSTOPPABLE)) {
          event.setCancelled(true);
          return;
        }
        if (mob.hasTrait(StrifeTrait.STRIDE)) {
          if (event.getNewEffect().getAmplifier() == 0) {
            event.setCancelled(true);
            return;
          }
          event.setCancelled(true);
          PotionEffect newEffect = event.getNewEffect()
              .withAmplifier(event.getNewEffect().getAmplifier() - 1);
          ignoreEffects.add(newEffect);
          newEffect.apply((LivingEntity) event.getEntity());
        }
      }
      case "UNLUCK" -> {
        StrifeMob mob = plugin.getStrifeMobManager().getStatMob((LivingEntity) event.getEntity());
        if (mob.hasTrait(StrifeTrait.TOXIC_MASCULINITY)) {
          event.setCancelled(true);
        }
      }
      case "WEAKNESS" -> {
        StrifeMob mob = plugin.getStrifeMobManager().getStatMob((LivingEntity) event.getEntity());
        if (mob.hasTrait(StrifeTrait.HIGHLY_MOTIVATED)) {
          event.setCancelled(true);
        }
      }
      case "POISON" -> {
        StrifeMob mob = plugin.getStrifeMobManager().getStatMob((LivingEntity) event.getEntity());
        if (mob.hasTrait(StrifeTrait.ANTI_POISON)) {
          event.setCancelled(true);
        }
      }
    }
  }

  @EventHandler
  public void onEffectExpire(EntityPotionEffectEvent event) {
    if (!(event.getCause() == Cause.EXPIRATION && event.getAction() == Action.REMOVED)) {
      return;
    }
    if ("POISON".equals(event.getOldEffect().getType().getName())) {
      if (event.getOldEffect().getAmplifier() > 0) {
        int newAmp = event.getOldEffect().getAmplifier() - 1;
        Bukkit.getScheduler().runTaskLater(plugin, () ->
            ((LivingEntity) event.getEntity()).addPotionEffect(
                new PotionEffect(PotionEffectType.POISON, 100, newAmp, false, true)), 0L);
      }
    }
  }
}
