package land.face.strife.listeners;

import static land.face.strife.managers.LoreAbilityManager.TriggerType.EARLY_ON_HIT;
import static land.face.strife.managers.LoreAbilityManager.TriggerType.EARLY_WHEN_HIT;
import static land.face.strife.managers.LoreAbilityManager.TriggerType.ON_AIR_JUMP;
import static land.face.strife.managers.LoreAbilityManager.TriggerType.ON_BASIC_HIT;
import static land.face.strife.managers.LoreAbilityManager.TriggerType.ON_BASIC_MELEE_HIT;
import static land.face.strife.managers.LoreAbilityManager.TriggerType.ON_BASIC_RANGED_HIT;
import static land.face.strife.managers.LoreAbilityManager.TriggerType.ON_BLOCK;
import static land.face.strife.managers.LoreAbilityManager.TriggerType.ON_CAST;
import static land.face.strife.managers.LoreAbilityManager.TriggerType.ON_COMBAT_END;
import static land.face.strife.managers.LoreAbilityManager.TriggerType.ON_COMBAT_START;
import static land.face.strife.managers.LoreAbilityManager.TriggerType.ON_CRIT;
import static land.face.strife.managers.LoreAbilityManager.TriggerType.ON_EVADE;
import static land.face.strife.managers.LoreAbilityManager.TriggerType.ON_FALL;
import static land.face.strife.managers.LoreAbilityManager.TriggerType.ON_HIT;
import static land.face.strife.managers.LoreAbilityManager.TriggerType.ON_KILL;
import static land.face.strife.managers.LoreAbilityManager.TriggerType.ON_MELEE_HIT;
import static land.face.strife.managers.LoreAbilityManager.TriggerType.ON_RANGED_HIT;
import static land.face.strife.managers.LoreAbilityManager.TriggerType.ON_SNEAK_ATTACK;
import static land.face.strife.managers.LoreAbilityManager.TriggerType.WHEN_HIT;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import land.face.strife.StrifePlugin;
import land.face.strife.data.LoreAbility;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.buff.Buff;
import land.face.strife.data.champion.Champion;
import land.face.strife.events.AbilityCastEvent;
import land.face.strife.events.AirJumpEvent;
import land.face.strife.events.BlockEvent;
import land.face.strife.events.CombatChangeEvent;
import land.face.strife.events.CombatChangeEvent.NewCombatState;
import land.face.strife.events.CriticalEvent;
import land.face.strife.events.EvadeEvent;
import land.face.strife.events.SneakAttackEvent;
import land.face.strife.events.StrifeDamageEvent;
import land.face.strife.events.StrifeEarlyDamageEvent;
import land.face.strife.managers.LoreAbilityManager.TriggerType;
import land.face.strife.managers.StrifeMobManager;
import land.face.strife.util.DamageUtil.AttackType;
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

  public LoreAbilityListener(StrifeMobManager strifeMobManager) {
    this.strifeMobManager = strifeMobManager;
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onAbilityCast(AbilityCastEvent event) {
    executeBoundEffects(event.getCaster(), event.getCaster(), event.getCaster().getLoreAbilities(ON_CAST));
    executeFiniteEffects(event.getCaster(), event.getCaster(), new HashSet<>(List.of(ON_CAST)));
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onAirJump(AirJumpEvent event) {
    executeBoundEffects(event.getJumper(), event.getJumper(), event.getJumper().getLoreAbilities(ON_AIR_JUMP));
    executeFiniteEffects(event.getJumper(), event.getJumper(), new HashSet<>(List.of(ON_AIR_JUMP)));
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onCombatChange(CombatChangeEvent event) {
    TriggerType type = event.getNewState() == NewCombatState.ENTER ? ON_COMBAT_START : ON_COMBAT_END;
    executeBoundEffects(event.getTarget(), event.getTarget(), event.getTarget().getLoreAbilities(type));
    executeFiniteEffects(event.getTarget(), event.getTarget(), new HashSet<>(List.of(type)));
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onCriticalHit(CriticalEvent event) {
    Set<LoreAbility> abilitySet = new HashSet<>(event.getAttacker().getLoreAbilities(ON_CRIT));
    executeBoundEffects(event.getAttacker(), event.getVictim(), abilitySet);
    executeFiniteEffects(event.getAttacker(), event.getVictim(), new HashSet<>(List.of(ON_CRIT)));
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
    executeBoundEffects(mob, mob, mob.getLoreAbilities(ON_FALL));
    executeFiniteEffects(mob, mob, new HashSet<>(List.of(ON_FALL)));
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onEvade(EvadeEvent event) {
    Champion champion = event.getEvader().getChampion();
    if (champion != null) {
      executeBoundEffects(event.getEvader(), event.getAttacker(), event.getEvader().getLoreAbilities(ON_EVADE));
    }
    executeFiniteEffects(event.getEvader(), event.getAttacker(), new HashSet<>(List.of(ON_EVADE)));
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onBlock(BlockEvent event) {
    Champion champion = event.getBlocker().getChampion();
    if (champion != null) {
      executeBoundEffects(event.getBlocker(), event.getAttacker(), event.getBlocker().getLoreAbilities(ON_BLOCK));
    }
    executeFiniteEffects(event.getBlocker(), event.getAttacker(), new HashSet<>(List.of(ON_BLOCK)));
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onSneakAttack(SneakAttackEvent event) {
    Champion champion = event.getAttacker().getChampion();
    if (champion != null) {
      executeBoundEffects(event.getAttacker(), event.getVictim(),
          event.getAttacker().getLoreAbilities(ON_SNEAK_ATTACK));
    }
    executeFiniteEffects(event.getAttacker(), event.getVictim(), new HashSet<>(List.of(ON_SNEAK_ATTACK)));
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onEntityDeath(EntityDeathEvent event) {
    StrifeMob victim = strifeMobManager.getStatMob(event.getEntity());
    LivingEntity killer = victim.getTopDamager();
    if (killer == null) {
      killer = event.getEntity().getKiller();
    }
    StrifeMob killerMob = strifeMobManager.getStatMob(killer);
    if (killerMob == null || killerMob.getLoreAbilities() == null) {
      return;
    }
    HashSet<LoreAbility> abilitySet = new HashSet<>(killerMob.getLoreAbilities(ON_KILL));
    executeBoundEffects(killerMob, strifeMobManager.getStatMob(event.getEntity()), abilitySet);
    executeFiniteEffects(killerMob, victim, new HashSet<>(List.of(ON_KILL)));
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onStrifeDamage(StrifeDamageEvent event) {
    if (event.isCancelled() || !event.getDamageModifiers().isApplyOnHitEffects()) {
      return;
    }
    StrifeMob attacker = event.getAttacker();
    if (attacker == null) {
      return;
    }
    StrifeMob defender = event.getDefender();

    if (attacker.isMasterOf(defender)) {
      return;
    }

    boolean trigger = StrifePlugin.RNG.nextFloat() < Math.max(event.getDamageModifiers().getAttackMultiplier(),
        event.getDamageModifiers().getDamageReductionRatio());

    Set<TriggerType> applicableTriggers = new HashSet<>();
    if (trigger) {
      applicableTriggers.add(ON_HIT);
      if (event.getDamageModifiers().isBasicAttack()) {
        applicableTriggers.add(ON_BASIC_HIT);
        if (event.getDamageModifiers().getAttackType() == AttackType.MELEE) {
          applicableTriggers.add(ON_MELEE_HIT);
          applicableTriggers.add(ON_BASIC_MELEE_HIT);
        } else if (event.getDamageModifiers().getAttackType() == AttackType.PROJECTILE) {
          applicableTriggers.add(ON_RANGED_HIT);
          applicableTriggers.add(ON_BASIC_RANGED_HIT);
        }
      } else {
        if (event.getDamageModifiers().getAttackType() == AttackType.MELEE) {
          applicableTriggers.add(ON_MELEE_HIT);
        } else if (event.getDamageModifiers().getAttackType() == AttackType.PROJECTILE) {
          applicableTriggers.add(ON_RANGED_HIT);
        }
      }
    }


    Set<LoreAbility> abilitySet = new HashSet<>();
    for (TriggerType t : applicableTriggers) {
      abilitySet.addAll(attacker.getLoreAbilities(t));
    }

    executeBoundEffects(attacker, defender, abilitySet);
    executeFiniteEffects(attacker, defender, applicableTriggers);

    if (defender.getEntity() instanceof Player) {
      executeBoundEffects(defender, attacker, event.getDefender().getLoreAbilities(WHEN_HIT));
    }
    executeFiniteEffects(defender, attacker, new HashSet<>(List.of(WHEN_HIT)));
  }

  @EventHandler
  public void earlyDamage(StrifeEarlyDamageEvent event) {
    if (event.isCancelled() || !event.getDamageModifiers().isApplyOnHitEffects()) {
      return;
    }
    StrifeMob attacker = event.getAttacker();
    if (attacker == null) {
      return;
    }
    StrifeMob defender = event.getDefender();
    if (attacker.isMasterOf(defender)) {
      return;
    }
    boolean trigger = StrifePlugin.RNG.nextFloat() < Math.max(event.getDamageModifiers().getAttackMultiplier(),
        event.getDamageModifiers().getDamageReductionRatio());
    if (trigger) {
      HashSet<LoreAbility> abilitySet = new HashSet<>(attacker.getLoreAbilities(EARLY_ON_HIT));
      executeBoundEffects(attacker, defender, abilitySet);
    }

    executeFiniteEffects(attacker, defender, new HashSet<>(List.of(EARLY_ON_HIT)));

    if (defender.getEntity() instanceof Player) {
      executeBoundEffects(defender, attacker, event.getDefender().getLoreAbilities(EARLY_WHEN_HIT));
    }
    executeFiniteEffects(defender, attacker, new HashSet<>(List.of(EARLY_WHEN_HIT)));
  }

  public static void executeBoundEffects(StrifeMob caster, StrifeMob target, Set<LoreAbility> loreAbilities) {
    if (loreAbilities == null || loreAbilities.isEmpty()) {
      return;
    }
    for (LoreAbility la : loreAbilities) {
      StrifePlugin.getInstance().getLoreAbilityManager().applyLoreAbility(la, caster, target);
    }
  }

  public static void executeFiniteEffects(StrifeMob caster, StrifeMob target, Set<TriggerType> triggerTypes) {
    if (caster.getBuffs().isEmpty()) {
      return;
    }
    for (Buff buff : new HashSet<>(caster.getBuffs())) {
      if (!triggerTypes.contains(buff.getUseType()) || buff.getUsesRemaining() == -1) {
        continue;
      }
      for (LoreAbility la : buff.getAbilities()) {
        StrifePlugin.getInstance().getLoreAbilityManager().applyLoreAbility(la, caster, target);
      }
      buff.setUsesRemaining(buff.getUsesRemaining() - 1);
      if (buff.getUsesRemaining() < 1) {
        caster.removeBuff(buff);
      }
    }
  }

  private StrifeMob getAttrEntity(LivingEntity livingEntity) {
    return strifeMobManager.getStatMob(livingEntity);
  }
}
