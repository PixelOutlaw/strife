package land.face.strife.listeners;

import static land.face.strife.managers.LoreAbilityManager.TriggerType.ON_AIR_JUMP;
import static land.face.strife.managers.LoreAbilityManager.TriggerType.ON_BLOCK;
import static land.face.strife.managers.LoreAbilityManager.TriggerType.ON_CAST;
import static land.face.strife.managers.LoreAbilityManager.TriggerType.ON_COMBAT_END;
import static land.face.strife.managers.LoreAbilityManager.TriggerType.ON_COMBAT_START;
import static land.face.strife.managers.LoreAbilityManager.TriggerType.ON_CRIT;
import static land.face.strife.managers.LoreAbilityManager.TriggerType.ON_EVADE;
import static land.face.strife.managers.LoreAbilityManager.TriggerType.ON_FALL;
import static land.face.strife.managers.LoreAbilityManager.TriggerType.ON_HIT;
import static land.face.strife.managers.LoreAbilityManager.TriggerType.ON_KILL;
import static land.face.strife.managers.LoreAbilityManager.TriggerType.ON_SNEAK_ATTACK;
import static land.face.strife.managers.LoreAbilityManager.TriggerType.WHEN_HIT;

import java.util.Iterator;
import java.util.Set;
import land.face.strife.StrifePlugin;
import land.face.strife.data.LoreAbility;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.champion.Champion;
import land.face.strife.data.effects.FiniteUsesEffect;
import land.face.strife.events.AbilityCastEvent;
import land.face.strife.events.AirJumpEvent;
import land.face.strife.events.BlockEvent;
import land.face.strife.events.CombatChangeEvent;
import land.face.strife.events.CombatChangeEvent.NewCombatState;
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

  public LoreAbilityListener(StrifeMobManager strifeMobManager, LoreAbilityManager loreAbilityManager) {
    this.strifeMobManager = strifeMobManager;
    this.loreAbilityManager = loreAbilityManager;
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onAbilityCast(AbilityCastEvent event) {
    Champion champion = event.getCaster().getChampion();
    if (champion != null) {
      executeBoundEffects(event.getCaster(), event.getCaster().getEntity(), champion.getLoreAbilities().get(ON_CAST));
    }
    executeFiniteEffects(event.getCaster(), event.getCaster(), ON_CAST);
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onAirJump(AirJumpEvent event) {
    Champion champion = event.getJumper().getChampion();
    if (champion != null) {
      executeBoundEffects(event.getJumper(), event.getJumper().getEntity(), champion.getLoreAbilities().get(ON_AIR_JUMP));
    }
    executeFiniteEffects(event.getJumper(), event.getJumper(), ON_AIR_JUMP);
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onCombatChange(CombatChangeEvent event) {
    Champion champion = event.getTarget().getChampion();
    TriggerType type = event.getNewState() == NewCombatState.ENTER ? ON_COMBAT_START : ON_COMBAT_END;
    if (champion != null) {
      executeBoundEffects(event.getTarget(), event.getTarget().getEntity(), champion.getLoreAbilities().get(type));
    }
    executeFiniteEffects(event.getTarget(), event.getTarget(), type);
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
    executeBoundEffects(mob, (LivingEntity) event.getEntity(), mob.getChampion().getLoreAbilities().get(ON_FALL));
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
          event.getBlocker().getChampion().getLoreAbilities().get(ON_BLOCK));
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
      killer = victim.getTopDamager();
      if (killer == null) {
        return;
      }
    }
    StrifeMob killerMob = strifeMobManager.getStatMob(killer);
    executeBoundEffects(killerMob, event.getEntity(), killerMob.getChampion().getLoreAbilities().get(ON_KILL));
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

    if (attacker.getEntity() instanceof Player && event.getDamageModifiers().getAttackMultiplier() > Math.random()) {
      if (attacker.isMasterOf(defender)) {
        return;
      }
      executeBoundEffects(attacker, defender.getEntity(), attacker.getChampion().getLoreAbilities().get(ON_HIT));
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

  public static void executeBoundEffects(StrifeMob caster, LivingEntity target, Set<LoreAbility> effects) {
    if (effects == null || effects.isEmpty()) {
      return;
    }
    for (LoreAbility la : effects) {
      StrifePlugin.getInstance().getLoreAbilityManager().applyLoreAbility(la, caster, target);
    }
  }

  public static void executeFiniteEffects(StrifeMob caster, StrifeMob target, TriggerType type) {
    Iterator<FiniteUsesEffect> it = caster.getTempEffects().iterator();
    while (it.hasNext()) {
      FiniteUsesEffect tempEffect = it.next();
      if (tempEffect.getLoreAbility().getTriggerType() != type) {
        continue;
      }
      StrifePlugin.getInstance().getLoreAbilityManager().applyLoreAbility(tempEffect.getLoreAbility(), caster, target.getEntity());
      if (tempEffect.getUses() > 1) {
        tempEffect.setUses(tempEffect.getUses() - 1);
      } else {
        it.remove();
      }
    }
  }

  private StrifeMob getAttrEntity(LivingEntity livingEntity) {
    return strifeMobManager.getStatMob(livingEntity);
  }
}
