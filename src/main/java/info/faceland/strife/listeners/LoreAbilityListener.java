package info.faceland.strife.listeners;

import info.faceland.strife.data.AttributedEntity;
import info.faceland.strife.data.LoreAbility;
import info.faceland.strife.data.champion.Champion;
import info.faceland.strife.events.BlockEvent;
import info.faceland.strife.events.CriticalEvent;
import info.faceland.strife.events.EvadeEvent;
import info.faceland.strife.events.SneakAttackEvent;
import info.faceland.strife.managers.AttributedEntityManager;
import info.faceland.strife.managers.ChampionManager;
import info.faceland.strife.managers.LoreAbilityManager;
import info.faceland.strife.managers.LoreAbilityManager.TriggerType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

public class LoreAbilityListener implements Listener {

  private final AttributedEntityManager attributedEntityManager;
  private final ChampionManager championManager;
  private final LoreAbilityManager loreAbilityManager;

  public LoreAbilityListener(AttributedEntityManager attributedEntityManager,
      ChampionManager championManager,
      LoreAbilityManager loreAbilityManager) {
    this.attributedEntityManager = attributedEntityManager;
    this.championManager = championManager;
    this.loreAbilityManager = loreAbilityManager;
  }

  @EventHandler
  public void onCriticalHit(CriticalEvent event) {
    if (!(event.getAttacker() instanceof Player)) {
      return;
    }
    Champion champion = championManager.getChampion((Player) event.getAttacker());
    for (LoreAbility la : champion.getLoreAbilities().get(TriggerType.ON_CRIT)) {
      loreAbilityManager.applyLoreAbility(la, getAttrEntity(event.getAttacker()),
          getAttrEntity(event.getVictim()));
    }
  }

  @EventHandler
  public void onEvade(EvadeEvent event) {
    if (!(event.getEvader() instanceof Player)) {
      return;
    }
    Champion champion = championManager.getChampion((Player) event.getEvader());
    for (LoreAbility la : champion.getLoreAbilities().get(TriggerType.ON_EVADE)) {
      loreAbilityManager.applyLoreAbility(la, getAttrEntity(event.getEvader()),
          getAttrEntity(event.getAttacker()));
    }
  }

  @EventHandler
  public void onBlock(BlockEvent event) {
    if (!(event.getBlocker() instanceof Player)) {
      return;
    }
    Champion champion = championManager.getChampion((Player) event.getBlocker());
    for (LoreAbility la : champion.getLoreAbilities().get(TriggerType.ON_BLOCK)) {
      loreAbilityManager.applyLoreAbility(la, getAttrEntity(event.getBlocker()),
          getAttrEntity(event.getAttacker()));
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onSneakAttack(SneakAttackEvent event) {
    Champion champion = championManager.getChampion(event.getAttacker());
    for (LoreAbility la : champion.getLoreAbilities().get(TriggerType.ON_SNEAK_ATTACK)) {
      loreAbilityManager.applyLoreAbility(la, getAttrEntity(event.getAttacker()),
          getAttrEntity(event.getVictim()));
    }
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onEntityDeath(EntityDeathEvent event) {
    if (event.getEntity().getKiller() == null) {
      return;
    }
    Champion champion = championManager.getChampion(event.getEntity().getKiller());
    for (LoreAbility la : champion.getLoreAbilities().get(TriggerType.ON_KILL)) {
      loreAbilityManager.applyLoreAbility(la, getAttrEntity(event.getEntity().getKiller()),
          attributedEntityManager.getAttributedEntity(event.getEntity()));
    }
  }

  @EventHandler
  public void onPlayerSneak(PlayerToggleSneakEvent event) {
    if (!event.isSneaking()) {
      return;
    }
    Champion champion = championManager.getChampion(event.getPlayer());
    for (LoreAbility la : champion.getLoreAbilities().get(TriggerType.ON_SNEAK)) {
      loreAbilityManager
          .applyLoreAbility(la, getAttrEntity(event.getPlayer()), getAttrEntity(event.getPlayer()));
    }
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
    if (event.isCancelled() || event.getCause() == DamageCause.CUSTOM) {
      return;
    }
    if (!(event.getEntity() instanceof LivingEntity)) {
      return;
    }
    LivingEntity attacker = getAttacker(event.getDamager());
    LivingEntity defender = (LivingEntity) event.getEntity();

    if (attacker instanceof Player) {
      Champion champion = championManager.getChampion((Player) attacker);
      for (LoreAbility la : champion.getLoreAbilities().get(TriggerType.ON_HIT)) {
        loreAbilityManager.applyLoreAbility(la, getAttrEntity(attacker), getAttrEntity(defender));
      }
    }
    if (attacker != null && defender instanceof Player) {
      Champion champion = championManager.getChampion((Player) defender);
      for (LoreAbility la : champion.getLoreAbilities().get(TriggerType.WHEN_HIT)) {
        loreAbilityManager.applyLoreAbility(la, getAttrEntity(defender), getAttrEntity(attacker));
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

  // Just to make things prettier, so sue me
  private AttributedEntity getAttrEntity(LivingEntity livingEntity) {
    return attributedEntityManager.getAttributedEntity(livingEntity);
  }
}
