package land.face.strife.listeners;

import static land.face.strife.managers.LoreAbilityManager.TriggerType.ON_BLOCK;
import static land.face.strife.managers.LoreAbilityManager.TriggerType.ON_CRIT;
import static land.face.strife.managers.LoreAbilityManager.TriggerType.ON_EVADE;
import static land.face.strife.managers.LoreAbilityManager.TriggerType.ON_FALL;
import static land.face.strife.managers.LoreAbilityManager.TriggerType.ON_HIT;
import static land.face.strife.managers.LoreAbilityManager.TriggerType.ON_KILL;
import static land.face.strife.managers.LoreAbilityManager.TriggerType.ON_SNEAK_ATTACK;
import static land.face.strife.managers.LoreAbilityManager.TriggerType.WHEN_HIT;

import land.face.strife.data.LoreAbility;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.champion.Champion;
import land.face.strife.events.BlockEvent;
import land.face.strife.events.CriticalEvent;
import land.face.strife.events.EvadeEvent;
import land.face.strife.events.SneakAttackEvent;
import land.face.strife.events.StrifeDamageEvent;
import land.face.strife.managers.ChampionManager;
import land.face.strife.managers.LoreAbilityManager;
import land.face.strife.managers.StrifeMobManager;
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
    StrifeMob victim = strifeMobManager.getStatMob(event.getEntity());
    Player killer = event.getEntity().getKiller();
    if (killer == null) {
      killer = victim.getKiller();
      if (killer == null) {
        return;
      }
    }
    StrifeMob killerMob = strifeMobManager.getStatMob(killer);
    for (LoreAbility la : killerMob.getChampion().getLoreAbilities().get(ON_KILL)) {
      loreAbilityManager.applyLoreAbility(la, killerMob, event.getEntity());
    }
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onStrifeDamage(StrifeDamageEvent event) {
    if (event.isCancelled()) {
      return;
    }
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
