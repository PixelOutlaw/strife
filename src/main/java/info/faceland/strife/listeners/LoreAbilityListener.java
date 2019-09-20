package info.faceland.strife.listeners;

import static info.faceland.strife.managers.LoreAbilityManager.TriggerType.ON_BLOCK;
import static info.faceland.strife.managers.LoreAbilityManager.TriggerType.ON_CRIT;
import static info.faceland.strife.managers.LoreAbilityManager.TriggerType.ON_EVADE;
import static info.faceland.strife.managers.LoreAbilityManager.TriggerType.ON_FALL;
import static info.faceland.strife.managers.LoreAbilityManager.TriggerType.ON_HIT;
import static info.faceland.strife.managers.LoreAbilityManager.TriggerType.ON_KILL;
import static info.faceland.strife.managers.LoreAbilityManager.TriggerType.ON_SNEAK_ATTACK;
import static info.faceland.strife.managers.LoreAbilityManager.TriggerType.WHEN_HIT;

import info.faceland.strife.data.LoreAbility;
import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.data.champion.Champion;
import info.faceland.strife.events.BlockEvent;
import info.faceland.strife.events.CriticalEvent;
import info.faceland.strife.events.EvadeEvent;
import info.faceland.strife.events.SneakAttackEvent;
import info.faceland.strife.events.StrifeDamageEvent;
import info.faceland.strife.managers.ChampionManager;
import info.faceland.strife.managers.LoreAbilityManager;
import info.faceland.strife.managers.StrifeMobManager;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;

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
  public void onFall(EntityDamageEvent event) {
    if (event.getCause() != DamageCause.FALL || event.isCancelled()) {
      return;
    }
    if (!(event.getEntity() instanceof Player)) {
      return;
    }
    Champion champion = championManager.getChampion((Player) event.getEntity());
    for (LoreAbility la : champion.getLoreAbilities().get(ON_FALL)) {
      loreAbilityManager.applyLoreAbility(la, getAttrEntity((Player) event.getEntity()),
          (Player) event.getEntity());
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
  public void onStrifeDamage(StrifeDamageEvent event) {
    StrifeMob attacker = event.getAttacker();
    if (attacker == null) {
      return;
    }
    StrifeMob defender = event.getDefender();

    if (attacker.getEntity() instanceof Player && event.getAttackMultiplier() > Math.random()) {
      if (attacker.isMasterOf(defender)) {
        return;
      }
      for (LoreAbility la : attacker.getChampion().getLoreAbilities().get(ON_HIT)) {
        loreAbilityManager.applyLoreAbility(la, attacker, defender.getEntity());
      }
    }
    if (defender.getEntity() instanceof Player) {
      if (attacker.isMasterOf(defender)) {
        return;
      }
      for (LoreAbility la : event.getDefender().getChampion().getLoreAbilities().get(WHEN_HIT)) {
        loreAbilityManager.applyLoreAbility(la, defender, attacker.getEntity());
      }
    }
  }

  private StrifeMob getAttrEntity(LivingEntity livingEntity) {
    return strifeMobManager.getStatMob(livingEntity);
  }
}
