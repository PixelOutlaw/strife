package info.faceland.strife.listeners;

import info.faceland.strife.data.Champion;
import info.faceland.strife.data.EntityStatCache;
import info.faceland.strife.data.LoreAbility;
import info.faceland.strife.events.BlockEvent;
import info.faceland.strife.events.CriticalEvent;
import info.faceland.strife.events.EvadeEvent;
import info.faceland.strife.managers.AbilityManager;
import info.faceland.strife.managers.ChampionManager;
import info.faceland.strife.managers.LoreAbilityManager.TriggerType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

public class LoreAbilityListener implements Listener {

  private final AbilityManager abilityManager;
  private final EntityStatCache entityStatCache;
  private final ChampionManager championManager;

  public LoreAbilityListener(EntityStatCache entityStatCache, AbilityManager abilityManager,
      ChampionManager championManager) {
    this.abilityManager = abilityManager;
    this.entityStatCache = entityStatCache;
    this.championManager = championManager;
  }

  @EventHandler
  public void onCriticalHit(CriticalEvent event) {
    if (!(event.getAttacker() instanceof Player)) {
      return;
    }
    Champion champion = championManager.getChampion((Player) event.getAttacker());
    for (LoreAbility la : champion.getLoreAbilities().get(TriggerType.ON_EVADE)) {
      abilityManager.execute(
          la.getAbility(),
          entityStatCache.getAttributedEntity(event.getAttacker()),
          la.isSingleTarget() ? entityStatCache.getAttributedEntity(event.getVictim()) : null
      );
    }
  }

  @EventHandler
  public void onEvade(EvadeEvent event) {
    if (!(event.getEvader() instanceof Player)) {
      return;
    }
    Champion champion = championManager.getChampion((Player) event.getEvader());
    for (LoreAbility la : champion.getLoreAbilities().get(TriggerType.ON_EVADE)) {
      abilityManager.execute(
          la.getAbility(),
          entityStatCache.getAttributedEntity(event.getEvader()),
          la.isSingleTarget() ? entityStatCache.getAttributedEntity(event.getAttacker()) : null
      );
    }
  }

  @EventHandler
  public void onBlock(BlockEvent event) {
    if (!(event.getBlocker() instanceof Player)) {
      return;
    }
    Champion champion = championManager.getChampion((Player) event.getBlocker());
    for (LoreAbility la : champion.getLoreAbilities().get(TriggerType.ON_BLOCK)) {
      abilityManager.execute(
          la.getAbility(),
          entityStatCache.getAttributedEntity(event.getBlocker()),
          la.isSingleTarget() ? entityStatCache.getAttributedEntity(event.getAttacker()) : null
      );
    }
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onEntityDeath(EntityDeathEvent event) {
    if (event.getEntity().getKiller() == null) {
      return;
    }
    Player killer = event.getEntity().getKiller();
    Champion champion = championManager.getChampion(killer);
    for (LoreAbility la : champion.getLoreAbilities().get(TriggerType.ON_KILL)) {
      abilityManager.execute(
          la.getAbility(),
          entityStatCache.getAttributedEntity(killer)
      );
    }
  }

  @EventHandler
  public void onPlayerSneak(PlayerToggleSneakEvent event) {
    if (!event.isSneaking()) {
      return;
    }
    Champion champion = championManager.getChampion(event.getPlayer());
    for (LoreAbility la : champion.getLoreAbilities().get(TriggerType.ON_SNEAK)) {
      abilityManager.execute(
          la.getAbility(),
          entityStatCache.getAttributedEntity(event.getPlayer())
      );
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
    if (event.isCancelled()) {
      return;
    }
    if (!(event.getEntity() instanceof LivingEntity)) {
      return;
    }
    LivingEntity attacker = getAttacker(event.getDamager());
    LivingEntity defender = (LivingEntity) event.getEntity();

    if (attacker instanceof Player) {
      Champion champion = championManager.getChampion((Player)attacker);
      for (LoreAbility la : champion.getLoreAbilities().get(TriggerType.ON_HIT)) {
        abilityManager.execute(
            la.getAbility(),
            entityStatCache.getAttributedEntity(attacker),
            la.isSingleTarget() ? entityStatCache.getAttributedEntity(defender) : null
        );
      }
    }
    if (attacker != null && defender instanceof Player) {
      Champion champion = championManager.getChampion((Player) defender);
      for (LoreAbility la : champion.getLoreAbilities().get(TriggerType.WHEN_HIT)) {
        abilityManager.execute(
            la.getAbility(),
            entityStatCache.getAttributedEntity(defender),
            la.isSingleTarget() ? entityStatCache.getAttributedEntity(attacker) : null
        );
      }
    }
  }

  private LivingEntity getAttacker(Entity entity) {
    if (entity instanceof LivingEntity) {
      return (LivingEntity) entity;
    } else if (entity instanceof Projectile) {
      if (((Projectile) entity).getShooter() instanceof LivingEntity) {
        return (LivingEntity) ((Projectile) entity).getShooter();
      }
    }
    return null;
  }
}
