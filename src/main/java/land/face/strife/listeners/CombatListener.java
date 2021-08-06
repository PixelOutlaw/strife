/**
 * The MIT License Copyright (c) 2015 Teal Cube Games
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package land.face.strife.listeners;

import static org.bukkit.event.entity.EntityDamageEvent.DamageModifier.ARMOR;
import static org.bukkit.event.entity.EntityDamageEvent.DamageModifier.BASE;
import static org.bukkit.event.entity.EntityDamageEvent.DamageModifier.BLOCKING;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;
import land.face.strife.StrifePlugin;
import land.face.strife.data.DamageModifiers;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.ability.EntityAbilitySet.TriggerAbilityType;
import land.face.strife.data.effects.Effect;
import land.face.strife.events.StrifeDamageEvent;
import land.face.strife.stats.StrifeStat;
import land.face.strife.stats.StrifeTrait;
import land.face.strife.util.DamageUtil;
import land.face.strife.util.DamageUtil.AbilityMod;
import land.face.strife.util.DamageUtil.AttackType;
import land.face.strife.util.DamageUtil.DamageType;
import land.face.strife.util.FangUtil;
import land.face.strife.util.ItemUtil;
import land.face.strife.util.ProjectileUtil;
import land.face.strife.util.StatUtil;
import land.face.strife.util.TargetingUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Bee;
import org.bukkit.entity.EvokerFangs;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Shulker;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.metadata.FixedMetadataValue;

public class CombatListener implements Listener {

  private final StrifePlugin plugin;
  private static final Set<Player> FRIENDLY_PLAYER_CHECKER = new HashSet<>();
  private static final Map<LivingEntity, Long> MONSTER_HIT_COOLDOWN = new WeakHashMap<>();

  public CombatListener(StrifePlugin plugin) {
    this.plugin = plugin;
  }

  public static void addPlayer(Player player) {
    FRIENDLY_PLAYER_CHECKER.add(player);
  }

  public static void removePlayer(Player player) {
    FRIENDLY_PLAYER_CHECKER.remove(player);
  }

  public static boolean hasFriendlyPlayer(Player player) {
    return FRIENDLY_PLAYER_CHECKER.contains(player);
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void handleTNT(EntityDamageByEntityEvent event) {
    if (event.isCancelled()) {
      return;
    }
    if (event.getEntity() instanceof LivingEntity && event.getDamager() instanceof TNTPrimed) {
      double distance = event.getDamager().getLocation().distance(event.getEntity().getLocation());
      double multiplier = Math.max(0.3, 4 / (distance + 3));
      DamageUtil.removeDamageModifiers(event);
      event.setDamage(multiplier * (10 + ((LivingEntity) event.getEntity()).getMaxHealth() * 0.4));
    }
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void handleNpcHits(EntityDamageByEntityEvent event) {
    if (event.isCancelled() || event.getEntity().isInvulnerable() ||
        event.getEntity().hasMetadata("NPC") ||
        event.getEntity().hasMetadata("MiniaturePet")) {
      if (event.getDamager() instanceof Projectile) {
        event.getDamager().remove();
      }
      event.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void strifeDamageHandler(EntityDamageByEntityEvent event) {
    if (event.isCancelled()) {
      return;
    }
    if (event.getCause() == DamageCause.THORNS) {
      event.setCancelled(true);
      return;
    }
    if (plugin.getDamageManager().isHandledDamage(event.getDamager())) {
      DamageUtil.removeDamageModifiers(event);
      event.setDamage(BASE, plugin.getDamageManager().getHandledDamage(event.getDamager()));
      return;
    }
    if (!(event.getEntity() instanceof LivingEntity) || event.getEntity() instanceof ArmorStand) {
      return;
    }
    if (event.getDamager() instanceof EvokerFangs && FangUtil
        .isNoDamageFang((EvokerFangs) event.getDamager())) {
      event.setCancelled(true);
      return;
    }

    LivingEntity defendEntity = (LivingEntity) event.getEntity();
    LivingEntity attackEntity = DamageUtil.getAttacker(event.getDamager());

    if (attackEntity == null) {
      return;
    }

    boolean blocked = (event.isApplicable(BLOCKING) && event.getDamage(BLOCKING) != 0) ||
        (defendEntity instanceof Shulker && event.isApplicable(ARMOR) && event.getDamage(ARMOR) != 0);

    DamageUtil.removeDamageModifiers(event);

    if (attackEntity instanceof Player && FRIENDLY_PLAYER_CHECKER.contains(attackEntity)) {
      FRIENDLY_PLAYER_CHECKER.remove(attackEntity);
      event.setCancelled(true);
      return;
    }

    if (event.getCause() == DamageCause.MAGIC) {
      event.setDamage(BASE, event.getDamage(BASE));
      return;
    }

    Projectile projectile = null;
    boolean isMultishot = false;
    List<Effect> extraEffects = null;
    int shotId = -1;

    if (event.getDamager() instanceof Projectile) {
      projectile = (Projectile) event.getDamager();
      shotId = ProjectileUtil.getShotId(projectile);
      if (shotId != -1) {
        String idKey = "SHOT_HIT_" + shotId;
        if (defendEntity.hasMetadata(idKey)) {
          isMultishot = true;
        }
      }
    }

    StrifeMob attacker = plugin.getStrifeMobManager().getStatMob(attackEntity);
    StrifeMob defender = plugin.getStrifeMobManager().getStatMob(defendEntity);

    if (TargetingUtil.isFriendly(attacker, defender)) {
      event.setCancelled(true);
      return;
    }

    float attackMultiplier = 1f;
    float healMultiplier = 1f;
    boolean backAttack = false;

    AttackType attackType = DamageUtil.getAttackType(event);
    if (attackType == AttackType.MELEE) {
      if (ItemUtil.isWandOrStaff(Objects.requireNonNull(attackEntity.getEquipment()).getItemInMainHand())) {
        double attackMult = plugin.getAttackSpeedManager().getAttackMultiplier(attacker);
        ProjectileUtil.shootWand(attacker, Math.pow(attackMult, 1.2D));
        event.setCancelled(true);
        return;
      }
      attackMultiplier = plugin.getAttackSpeedManager().getAttackMultiplier(attacker);
      attackMultiplier = (float) Math.pow(attackMultiplier, 1.1);
      //double angle = attackEntity.getEyeLocation().getDirection()
      //    .angle(defendEntity.getEyeLocation().getDirection());
      //backAttack = angle < 1;
    } else if (attackType == AttackType.PROJECTILE) {
      attackMultiplier = ProjectileUtil.getAttackMult(projectile);
      assert projectile != null;
      //double angle = projectile.getVelocity().angle(defendEntity.getEyeLocation().getDirection());
      //backAttack = angle < 1;
    } else if (attackType == AttackType.AREA) {
      double distance = event.getDamager().getLocation().distance(event.getEntity().getLocation());
      attackMultiplier *= Math.max(0.3, 4 / (distance + 3));
      healMultiplier = 0.3f;
    }

    if (attackType == AttackType.MELEE && !canMonsterHit(attackEntity)) {
      event.setCancelled(true);
      return;
    }
    putMonsterHit(attackEntity);

    if (isMultishot) {
      attackMultiplier *= 0.25;
    }

    if (attackMultiplier < 0.05) {
      event.setCancelled(true);
      removeIfExisting(projectile);
      return;
    }

    boolean isSneakAttack = attackEntity instanceof Player && plugin.getStealthManager()
        .canSneakAttack((Player) attackEntity);

    boolean mobAbility = plugin.getAbilityManager().abilityCast(attacker, defender, TriggerAbilityType.ON_HIT);

    if (mobAbility) {
      event.setCancelled(true);
      return;
    }

    if (attackEntity instanceof Player) {
      plugin.getStealthManager().unstealthPlayer((Player) attackEntity);
    }

    DamageModifiers damageModifiers = new DamageModifiers();
    damageModifiers.setAttackType(attackType);
    damageModifiers.setAttackMultiplier(attackMultiplier);
    damageModifiers.setHealMultiplier(healMultiplier);
    damageModifiers.setDamageReductionRatio(Math.min(attackMultiplier, 1.0f));
    damageModifiers.setScaleChancesWithAttack(true);
    damageModifiers.setConsumeEarthRunes(!isMultishot);
    damageModifiers.setApplyOnHitEffects(!isMultishot && attackMultiplier > Math.random());
    damageModifiers.setSneakAttack(isSneakAttack);
    damageModifiers.setBlocking(blocked);

    if (backAttack) {
      damageModifiers.getAbilityMods().put(AbilityMod.BACK_ATTACK, 1f);
    }

    boolean attackSuccess = DamageUtil.preDamage(attacker, defender, damageModifiers);

    if (!attackSuccess) {
      removeIfExisting(projectile);
      event.setCancelled(true);
      return;
    }

    if (!isMultishot && shotId != -1) {
      String idKey = "SHOT_HIT_" + shotId;
      defendEntity.setMetadata(idKey, new FixedMetadataValue(StrifePlugin.getInstance(), true));
      Bukkit.getScheduler().runTaskLater(plugin, () ->
          defendEntity.removeMetadata(idKey, StrifePlugin.getInstance()), 2500L);
    }

    DamageUtil.applyExtraEffects(attacker, defender, extraEffects);

    if (attackMultiplier < 0.05) {
      event.setDamage(BASE, 0);
      return;
    }

    Map<DamageType, Float> damage = DamageUtil.buildDamage(attacker, defender, damageModifiers);
    DamageUtil.applyElementalEffects(attacker, defender, damage, damageModifiers);
    DamageUtil.reduceDamage(attacker, defender, damage, damageModifiers);

    float finalDamage = DamageUtil.calculateFinalDamage(attacker, defender, damage, damageModifiers);

    StrifeDamageEvent strifeDamageEvent = new StrifeDamageEvent(attacker, defender, damageModifiers);
    strifeDamageEvent.setFinalDamage(finalDamage);
    Bukkit.getPluginManager().callEvent(strifeDamageEvent);

    if (strifeDamageEvent.isCancelled()) {
      event.setCancelled(true);
      return;
    }

    float eventDamage = defender.damageBarrier((float) strifeDamageEvent.getFinalDamage());

    if (finalDamage > 0) {
      eventDamage = plugin.getDamageManager().doEnergyAbsorb(defender, eventDamage);

      if (damage.containsKey(DamageType.PHYSICAL)) {
        DamageUtil.attemptBleed(attacker, defender, damage.get(DamageType.PHYSICAL),
            damageModifiers, false);
      }
    }

    if (defender.hasTrait(StrifeTrait.BLEEDING_EDGE)) {
      finalDamage *= 0.5;
      float bleed = finalDamage;
      if (defender.getStat(StrifeStat.BLEED_RESIST) > 0) {
        bleed *= 1 - defender.getStat(StrifeStat.BLEED_RESIST) / 100;
      }
      DamageUtil.applyBleed(defender, bleed, true);
    }

    Bukkit.getScheduler().runTaskLater(plugin,
        () -> DamageUtil.postDamage(attacker, defender, damageModifiers), 0L);

    if (attackEntity instanceof Bee) {
      plugin.getDamageManager().dealDamage(attacker, defender,
          (float) strifeDamageEvent.getFinalDamage(), damageModifiers);
      event.setCancelled(true);
      return;
    }

    event.setDamage(BASE, eventDamage);
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void strifeEntityDeath(EntityDeathEvent event) {
    if (event.getEntity().getKiller() == null) {
      return;
    }
    StrifeMob killer = plugin.getStrifeMobManager().getStatMob(event.getEntity().getKiller());
    if (killer.getStat(StrifeStat.HP_ON_KILL) > 0.1) {
      DamageUtil.restoreHealthWithPenalties(event.getEntity().getKiller(), killer.getStat(
          StrifeStat.HP_ON_KILL));
    }
    if (killer.getStat(StrifeStat.ENERGY_ON_KILL) > 0.1) {
      StatUtil.changeEnergy(killer, killer.getStat(StrifeStat.ENERGY_ON_KILL));
    }
    if (killer.getStat(StrifeStat.RAGE_ON_KILL) > 0.1) {
      plugin.getRageManager().changeRage(killer, killer.getStat(StrifeStat.RAGE_ON_KILL));
    }
  }

  private boolean canMonsterHit(LivingEntity uuid) {
    return MONSTER_HIT_COOLDOWN.getOrDefault(uuid, 0L) < System.currentTimeMillis();
  }

  public static void putMonsterHit(LivingEntity livingEntity) {
    if (!(livingEntity instanceof Player)) {
      MONSTER_HIT_COOLDOWN.put(livingEntity, System.currentTimeMillis() + 650);
    }
  }

  private void removeIfExisting(Projectile projectile) {
    if (projectile == null) {
      return;
    }
    projectile.remove();
  }
}
