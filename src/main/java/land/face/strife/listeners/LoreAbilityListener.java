package land.face.strife.listeners;

import static land.face.strife.managers.LoreAbilityManager.TriggerType.ON_BLOCK;
import static land.face.strife.managers.LoreAbilityManager.TriggerType.ON_CRIT;
import static land.face.strife.managers.LoreAbilityManager.TriggerType.ON_EVADE;
import static land.face.strife.managers.LoreAbilityManager.TriggerType.ON_FALL;
import static land.face.strife.managers.LoreAbilityManager.TriggerType.ON_HIT;
import static land.face.strife.managers.LoreAbilityManager.TriggerType.ON_KILL;
import static land.face.strife.managers.LoreAbilityManager.TriggerType.ON_SNEAK_ATTACK;
import static land.face.strife.managers.LoreAbilityManager.TriggerType.WHEN_HIT;

import java.util.Iterator;
import java.util.Set;
import land.face.strife.data.LoreAbility;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.champion.Champion;
import land.face.strife.data.effects.FiniteUsesEffect;
import land.face.strife.events.BlockEvent;
import land.face.strife.events.CriticalEvent;
import land.face.strife.events.EvadeEvent;
import land.face.strife.events.SneakAttackEvent;
import land.face.strife.events.StrifeDamageEvent;
import land.face.strife.managers.LoreAbilityManager;
import land.face.strife.managers.LoreAbilityManager.TriggerType;
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
  private final LoreAbilityManager loreAbilityManager;

  public LoreAbilityListener(StrifeMobManager strifeMobManager,
      LoreAbilityManager loreAbilityManager) {
    this.strifeMobManager = strifeMobManager;
    this.loreAbilityManager = loreAbilityManager;
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onCriticalHit(CriticalEvent event) {
    Champion champion = event.getAttacker().getChampion();
    if (champion != null) {
      executeBoundEffects(event.getAttacker(), event.getVictim().getEntity(),
          event.getAttacker().getChampion().getLoreAbilities().get(ON_CRIT));
    }
    executeFiniteEffects(event.getAttacker(), event.getVictim(), ON_CRIT);
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onFall(EntityDamageEvent event) {
    if (event.getCause() != DamageCause.FALL || event.isCancelled()) {
      return;
    }
    if (!(event.getEntity() instanceof Player)) {
      return;
    }
    StrifeMob mob = getAttrEntity((Player) event.getEntity());
    executeBoundEffects(mob, (LivingEntity) event.getEntity(),
        mob.getChampion().getLoreAbilities().get(ON_FALL));
    executeFiniteEffects(mob, mob, ON_FALL);
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onEvade(EvadeEvent event) {
    Champion champion = event.getEvader().getChampion();
    if (champion != null) {
      executeBoundEffects(event.getEvader(), event.getAttacker().getEntity(),
          event.getEvader().getChampion().getLoreAbilities().get(ON_EVADE));
    }
    executeFiniteEffects(event.getEvader(), event.getAttacker(), ON_EVADE);
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onBlock(BlockEvent event) {
    Champion champion = event.getBlocker().getChampion();
    if (champion != null) {
      executeBoundEffects(event.getBlocker(), event.getAttacker().getEntity(),
          event.getAttacker().getChampion().getLoreAbilities().get(ON_BLOCK));
    }
    executeFiniteEffects(event.getBlocker(), event.getAttacker(), ON_BLOCK);
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onSneakAttack(SneakAttackEvent event) {
    Champion champion = event.getAttacker().getChampion();
    if (champion != null) {
      executeBoundEffects(event.getAttacker(), event.getVictim().getEntity(),
          event.getAttacker().getChampion().getLoreAbilities().get(ON_SNEAK_ATTACK));
    }
    executeFiniteEffects(event.getAttacker(), event.getVictim(), ON_SNEAK_ATTACK);
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
    executeBoundEffects(killerMob, event.getEntity(),
        killerMob.getChampion().getLoreAbilities().get(ON_KILL));
    executeFiniteEffects(killerMob, victim, ON_KILL);
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
      executeBoundEffects(defender, attacker.getEntity(),
          attacker.getChampion().getLoreAbilities().get(ON_HIT));
    }
    executeFiniteEffects(attacker, defender, ON_HIT);

    if (defender.getEntity() instanceof Player) {
      if (attacker.isMasterOf(defender)) {
        return;
      }
      executeBoundEffects(defender, attacker.getEntity(),
          event.getDefender().getChampion().getLoreAbilities().get(WHEN_HIT));
    }
    executeFiniteEffects(defender, attacker, WHEN_HIT);
  }

  private void executeBoundEffects(StrifeMob caster, LivingEntity target,
      Set<LoreAbility> effects) {
    if (effects == null || effects.isEmpty()) {
      return;
    }
    Iterator<LoreAbility> it = effects.iterator();
    while (it.hasNext()) {
      LoreAbility la = it.next();
      loreAbilityManager.applyLoreAbility(la, caster, target);
    }
  }

  private void executeFiniteEffects(StrifeMob attacker, StrifeMob target, TriggerType type) {
    Iterator<FiniteUsesEffect> it = attacker.getTempEffects().iterator();
    while (it.hasNext()) {
      FiniteUsesEffect tempEffect = it.next();
      if (tempEffect.getLoreAbility().getTriggerType() != type) {
        continue;
      }
      loreAbilityManager.applyLoreAbility(tempEffect.getLoreAbility(), attacker,
          target.getEntity());
      if (tempEffect.getUses() > 1) {
        tempEffect.setUses(tempEffect.getUses() - 1);
      } else {
        attacker.getTempEffects().remove(tempEffect);
      }
    }
  }

  private StrifeMob getAttrEntity(LivingEntity livingEntity) {
    return strifeMobManager.getStatMob(livingEntity);
  }
}
