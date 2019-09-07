package info.faceland.strife.listeners;

import static info.faceland.strife.managers.LoreAbilityManager.TriggerType.ON_BLOCK;
import static info.faceland.strife.managers.LoreAbilityManager.TriggerType.ON_CRIT;
import static info.faceland.strife.managers.LoreAbilityManager.TriggerType.ON_EVADE;
import static info.faceland.strife.managers.LoreAbilityManager.TriggerType.ON_HIT;
import static info.faceland.strife.managers.LoreAbilityManager.TriggerType.ON_KILL;
import static info.faceland.strife.managers.LoreAbilityManager.TriggerType.ON_SNEAK;
import static info.faceland.strife.managers.LoreAbilityManager.TriggerType.ON_SNEAK_ATTACK;
import static info.faceland.strife.managers.LoreAbilityManager.TriggerType.WHEN_HIT;

import info.faceland.strife.data.LoreAbility;
import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.data.champion.Champion;
import info.faceland.strife.events.BlockEvent;
import info.faceland.strife.events.CriticalEvent;
import info.faceland.strife.events.EvadeEvent;
import info.faceland.strife.events.SneakAttackEvent;
import info.faceland.strife.managers.ChampionManager;
import info.faceland.strife.managers.LoreAbilityManager;
import info.faceland.strife.managers.StrifeMobManager;
import info.faceland.strife.util.DamageUtil;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

public class LoreAbilityListener implements Listener {

  private final StrifeMobManager strifeMobManager;
  private final ChampionManager championManager;
  private final LoreAbilityManager loreAbilityManager;

  public LoreAbilityListener(StrifeMobManager strifeMobManager,
      ChampionManager championManager,
      LoreAbilityManager loreAbilityManager) {
    this.strifeMobManager = strifeMobManager;
    this.championManager = championManager;
    this.loreAbilityManager = loreAbilityManager;
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onCriticalHit(CriticalEvent event) {
    if (!(event.getAttacker() instanceof Player)) {
      return;
    }
    Champion champion = championManager.getChampion((Player) event.getAttacker());
    for (LoreAbility la : champion.getLoreAbilities().get(ON_CRIT)) {
      loreAbilityManager
          .applyLoreAbility(la, getAttrEntity(event.getAttacker()), event.getVictim());
    }
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onEvade(EvadeEvent event) {
    if (!(event.getEvader() instanceof Player)) {
      return;
    }
    Champion champion = championManager.getChampion((Player) event.getEvader());
    for (LoreAbility la : champion.getLoreAbilities().get(ON_EVADE)) {
      loreAbilityManager
          .applyLoreAbility(la, getAttrEntity(event.getEvader()), event.getAttacker());
    }
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onBlock(BlockEvent event) {
    if (!(event.getBlocker() instanceof Player)) {
      return;
    }
    Champion champion = championManager.getChampion((Player) event.getBlocker());
    for (LoreAbility la : champion.getLoreAbilities().get(ON_BLOCK)) {
      loreAbilityManager
          .applyLoreAbility(la, getAttrEntity(event.getBlocker()), event.getAttacker());
    }
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onSneakAttack(SneakAttackEvent event) {
    Champion champion = championManager.getChampion(event.getAttacker());
    for (LoreAbility la : champion.getLoreAbilities().get(ON_SNEAK_ATTACK)) {
      loreAbilityManager
          .applyLoreAbility(la, getAttrEntity(event.getAttacker()), event.getVictim());
    }
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onEntityDeath(EntityDeathEvent event) {
    if (event.getEntity().getKiller() == null) {
      return;
    }
    Champion champion = championManager.getChampion(event.getEntity().getKiller());
    StrifeMob strifeMob = getAttrEntity(event.getEntity().getKiller());
    if (strifeMob.isMasterOf(event.getEntity())) {
      return;
    }
    for (LoreAbility la : champion.getLoreAbilities().get(ON_KILL)) {
      loreAbilityManager.applyLoreAbility(la, strifeMob, event.getEntity());
    }
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onPlayerSneak(PlayerToggleSneakEvent event) {
    if (!event.isSneaking()) {
      return;
    }
    Champion champion = championManager.getChampion(event.getPlayer());
    for (LoreAbility la : champion.getLoreAbilities().get(ON_SNEAK)) {
      loreAbilityManager.applyLoreAbility(la, getAttrEntity(event.getPlayer()), event.getPlayer());
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
    LivingEntity attacker = DamageUtil.getAttacker(event.getDamager());
    if (attacker == null) {
      return;
    }
    LivingEntity defender = (LivingEntity) event.getEntity();

    StrifeMob attackEntity = getAttrEntity(attacker);
    StrifeMob defendEntity = getAttrEntity(defender);
    if (attacker instanceof Player) {
      if (attackEntity.isMasterOf(defendEntity)) {
        return;
      }
      for (LoreAbility la : attackEntity.getChampion().getLoreAbilities().get(ON_HIT)) {
        loreAbilityManager.applyLoreAbility(la, attackEntity, defender);
      }
    }
    if (defender instanceof Player) {
      if (attackEntity.isMasterOf(defendEntity)) {
        return;
      }
      for (LoreAbility la : defendEntity.getChampion().getLoreAbilities().get(WHEN_HIT)) {
        loreAbilityManager.applyLoreAbility(la, defendEntity, attacker);
      }
    }
  }

  private StrifeMob getAttrEntity(LivingEntity livingEntity) {
    return strifeMobManager.getStatMob(livingEntity);
  }
}
