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

import static org.bukkit.event.entity.EntityDamageEvent.DamageModifier.BASE;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import land.face.strife.StrifePlugin;
import land.face.strife.data.DamageModifiers;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.ability.EntityAbilitySet.TriggerAbilityType;
import land.face.strife.events.ProjectileDamageEvent;
import land.face.strife.events.StrifeDamageEvent;
import land.face.strife.events.StrifePreDamageEvent;
import land.face.strife.stats.StrifeStat;
import land.face.strife.stats.StrifeTrait;
import land.face.strife.util.DamageUtil;
import land.face.strife.util.DamageUtil.AbilityMod;
import land.face.strife.util.DamageUtil.AttackType;
import land.face.strife.util.DamageUtil.DamageType;
import land.face.strife.util.DirectionUtil;
import land.face.strife.util.DirectionUtil.HitDirection;
import land.face.strife.util.FangUtil;
import land.face.strife.util.PlayerDataUtil;
import land.face.strife.util.ProjectileUtil;
import land.face.strife.util.StatUtil;
import land.face.strife.util.TargetingUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class CombatListener implements Listener {

  private final StrifePlugin plugin;

  private static final Set<Player> FRIENDLY_PLAYER_CHECKER = new HashSet<>();
  private static final Map<LivingEntity, Long> MONSTER_HIT_COOLDOWN = new WeakHashMap<>();

  public CombatListener(StrifePlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler
  public void projectileDamageHandler(ProjectileDamageEvent event) {
    boolean abilityProjectile = ProjectileUtil.isAbilityProjectile(event.getProjectile());

    StrifeMob attacker = event.getAttacker();
    StrifeMob defender = event.getDefender();

    if (attacker == null) {
      DamageUtil.dealRawDamage(defender, 2f + (float) StatUtil.getMobLevel(defender.getEntity()) / 5);
      event.setAlwaysRemove(true);
      return;
    }

    if (!abilityProjectile) {
      boolean mobAbility = plugin.getAbilityManager()
          .abilityCast(event.getAttacker(), event.getDefender(), TriggerAbilityType.ON_HIT);
      if (mobAbility) {
        return;
      }
    }

    Projectile projectile = event.getProjectile();

    float attackMultiplier = ProjectileUtil.getAttackMult(projectile);
    boolean onHitProjectile = ProjectileUtil.getApplyOnHit(projectile);
    float onHitChance = onHitProjectile ? 1.0f : attackMultiplier;
    assert projectile != null;
    if (attackMultiplier < 0.05) {
      event.setAlwaysRemove(true);
      return;
    }

    float multishotRatio = event.getDefender().getMultishotRatio(ProjectileUtil.getShotId(projectile));
    if (multishotRatio < 0.05) {
      event.setAlwaysRemove(true);
      return;
    }
    attackMultiplier *= multishotRatio;
    onHitChance *= multishotRatio;
    //double angle = projectile.getVelocity().angle(defendEntity.getEyeLocation().getDirection());
    //backAttack = angle < 1;

    if (event.getAttacker() != null && event.getAttacker().getEntity() instanceof Player) {
      plugin.getStealthManager().unstealthPlayer((Player) event.getAttacker().getEntity(), false);
    }

    boolean blocked = false;
    HitDirection hitDirection = DirectionUtil.getDirection(defender.getEntity(), projectile.getVelocity().clone().normalize());
    if (DirectionUtil.isBlocking(defender.getEntity())) {
      if (defender.getEntity() instanceof Shulker) {
        blocked = true;
      } else {
        blocked = hitDirection == HitDirection.FRONT;
      }
    }

    boolean isSneakAttack = hitDirection != HitDirection.FRONT && attacker.getEntity() instanceof Player &&
        plugin.getStealthManager().canSneakAttack((Player) attacker.getEntity());

    DamageModifiers mods = new DamageModifiers();
    mods.setAttackType(AttackType.PROJECTILE);
    mods.setAttackMultiplier(attackMultiplier);
    mods.setHealMultiplier(Math.min(1.0f, attackMultiplier));
    mods.setDamageReductionRatio(0.5f + attackMultiplier / 2f);
    mods.setScaleChancesWithAttack(true);
    mods.setApplyOnHitEffects(StrifePlugin.RNG.nextFloat() < onHitChance);
    mods.setSneakAttack(isSneakAttack);
    mods.setBlocking(blocked);

    /*
    if (backAttack) {
      mods.getAbilityMods().put(AbilityMod.BACK_ATTACK, 1f);
    }
    */

    boolean attackSuccess = DamageUtil.preDamage(event.getAttacker(), event.getDefender(), mods);

    if (!attackSuccess) {
      return;
    }

    Map<DamageType, Float> damage = DamageUtil.buildDamageMap(attacker, defender, mods);
    DamageUtil.applyAttackTypeMods(attacker, mods.getAttackType(), damage);
    DamageUtil.applyElementalEffects(attacker, defender, damage, mods);
    DamageUtil.reduceDamage(attacker, defender, damage, mods);
    float finalDamage = DamageUtil.calculateFinalDamage(attacker, defender, AttackType.PROJECTILE, damage, mods);

    StrifeDamageEvent strifeDamageEvent = new StrifeDamageEvent(attacker, defender, mods);
    strifeDamageEvent.setFinalDamage(finalDamage);

    Bukkit.getPluginManager().callEvent(strifeDamageEvent);

    if (strifeDamageEvent.isCancelled()) {
      return;
    }

    event.getDefender().trackDamage(event.getAttacker(), (float) strifeDamageEvent.getFinalDamage());

    StrifePlugin.getInstance().getDamageManager().dealDamage(attacker, defender,
        (float) strifeDamageEvent.getFinalDamage(), mods);

    if (damage.containsKey(DamageType.PHYSICAL) && mods.isApplyOnHitEffects()) {
      DamageUtil.attemptBleed(attacker, defender, damage.get(DamageType.PHYSICAL), mods, true,false);
    }

    if (mods.isApplyOnHitEffects()) {
      DamageUtil.attemptPoison(attacker, defender, mods);
      Bukkit.getScheduler().runTaskLater(StrifePlugin.getInstance(), () ->
          DamageUtil.postDamage(attacker, defender, mods, false), 0L);
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void meleeDamageHandler(EntityDamageByEntityEvent event) {
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
    if (!(event.getEntity() instanceof LivingEntity defendEntity) || event.getEntity() instanceof ArmorStand) {
      return;
    }
    if (event.getDamager() instanceof EvokerFangs && FangUtil.isNoDamageFang((EvokerFangs) event.getDamager())) {
      event.setCancelled(true);
      return;
    }
    LivingEntity attackEntity = DamageUtil.getAttacker(event.getDamager());
    if (attackEntity == null) {
      return;
    }

    StrifePreDamageEvent preDamageEvent = new StrifePreDamageEvent(attackEntity, event.getEntity());
    Bukkit.getPluginManager().callEvent(preDamageEvent);

    if (preDamageEvent.isCancelled()) {
      event.setCancelled(true);
      return;
    }

    boolean blocked = false;
    if (DirectionUtil.isBlocking(defendEntity)) {
      if (defendEntity instanceof Shulker) {
        blocked = true;
      } else {
        Vector locationDiff = defendEntity.getEyeLocation().clone().subtract(attackEntity.getEyeLocation()).toVector();
        HitDirection hitDirection = DirectionUtil.getDirection(defendEntity, locationDiff);
        blocked = hitDirection == HitDirection.FRONT;
      }
    }

    DamageUtil.removeDamageModifiers(event);

    if (attackEntity instanceof Player) {
      plugin.getPlayerMountManager().despawn((Player) attackEntity);
      if (FRIENDLY_PLAYER_CHECKER.contains(attackEntity)) {
        FRIENDLY_PLAYER_CHECKER.remove(attackEntity);
        event.setCancelled(true);
        return;
      }
    }

    if (!canMonsterHit(attackEntity)) {
      event.setCancelled(true);
      return;
    }

    if (event.getCause() == DamageCause.MAGIC) {
      event.setDamage(BASE, event.getDamage(BASE));
      return;
    }

    if (event.getDamager() instanceof Projectile) {
      Bukkit.getLogger().info("[Strife] Normal projectile dmg detected... ignoring");
      event.setCancelled(true);
      return;
    }

    putMonsterHit(attackEntity);

    StrifeMob attacker = plugin.getStrifeMobManager().getStatMob(attackEntity);
    StrifeMob defender = plugin.getStrifeMobManager().getStatMob(defendEntity);

    if (defender.isInvincible()) {
      event.setCancelled(true);
      return;
    }

    if (TargetingUtil.isFriendly(attacker, defender)) {
      event.setCancelled(true);
      return;
    }

    boolean mobAbility = plugin.getAbilityManager().abilityCast(attacker, defender, TriggerAbilityType.ON_HIT);
    if (mobAbility) {
      event.setCancelled(true);
      return;
    }

    float onHitChance = 1.0f;
    float attackMultiplier = 1.0f;
    float healMultiplier = 1f;
    boolean backAttack = false;

    AttackType attackType = DamageUtil.getAttackType(event);
    if (attackType == AttackType.MELEE) {
      ItemStack item = attackEntity.getEquipment() == null ? null : attackEntity.getEquipment().getItemInMainHand();
      attackMultiplier = plugin.getAttackSpeedManager().getAttackMultiplier(attacker, 1);
      onHitChance = attackMultiplier;
      if (item != null && (item.getType() == Material.BOW || item.getType() == Material.WOODEN_SWORD)) {
        attackMultiplier *= 0.35f;
      }
      //double angle = attackEntity.getEyeLocation().getDirection()
      //    .angle(defendEntity.getEyeLocation().getDirection());
      //backAttack = angle < 1;
    } else if (attackType == AttackType.AREA) {
      double distance = event.getDamager().getLocation().distance(event.getEntity().getLocation());
      attackMultiplier *= (float) Math.max(0.3, 4 / (distance + 3));
      onHitChance = attackMultiplier;
      healMultiplier = 0.3f;
    }

    if (attackMultiplier < 0.05) {
      event.setCancelled(true);
      return;
    }

    boolean isSneakAttack = attackEntity instanceof Player && plugin.getStealthManager()
        .canSneakAttack((Player) attackEntity);

    if (attackEntity instanceof Player) {
      plugin.getStealthManager().unstealthPlayer((Player) attackEntity, false);
    }

    DamageModifiers mods = new DamageModifiers();
    mods.setAttackType(attackType);
    mods.setAttackMultiplier(attackMultiplier);
    mods.setHealMultiplier(healMultiplier);
    mods.setDamageReductionRatio(0.5f + attackMultiplier / 2f);
    mods.setScaleChancesWithAttack(true);
    mods.setApplyOnHitEffects(StrifePlugin.RNG.nextFloat() < onHitChance);
    mods.setSneakAttack(isSneakAttack);
    mods.setBlocking(blocked);

    if (backAttack) {
      mods.getAbilityMods().put(AbilityMod.BACK_ATTACK, 1f);
    }

    boolean attackSuccess = DamageUtil.preDamage(attacker, defender, mods);

    if (!attackSuccess) {
      event.setCancelled(true);
      return;
    }

    if (attackMultiplier < 0.05) {
      event.setDamage(BASE, 0);
      return;
    }

    Map<DamageType, Float> damages = DamageUtil.buildDamageMap(attacker, defender, mods);
    DamageUtil.applyAttackTypeMods(attacker, mods.getAttackType(), damages);
    DamageUtil.applyElementalEffects(attacker, defender, damages, mods);
    DamageUtil.reduceDamage(attacker, defender, damages, mods);

    float finalDamage = DamageUtil.calculateFinalDamage(attacker, defender, attackType, damages, mods);

    StrifeDamageEvent strifeDamageEvent = new StrifeDamageEvent(attacker, defender, mods);
    strifeDamageEvent.setFinalDamage(finalDamage);
    Bukkit.getPluginManager().callEvent(strifeDamageEvent);

    if (strifeDamageEvent.isCancelled()) {
      event.setCancelled(true);
      return;
    }

    if (attackEntity instanceof Bee) {
      plugin.getDamageManager().dealDamage(attacker, defender, (float) strifeDamageEvent.getFinalDamage(), mods);
      event.setCancelled(true);
      return;
    }

    if (attacker.getEntity() instanceof Player && defender.getEntity() instanceof Player && (attacker != defender)) {
      attacker.flagPvp();
      defender.flagPvp();
    }

    defender.trackDamage(attacker, (float) strifeDamageEvent.getFinalDamage());
    float eventDamage = (float) strifeDamageEvent.getFinalDamage();
    if (defender.getBarrier() > 0) {
      float dmgVsBarrier = 1 + attacker.getStat(StrifeStat.DAMAGE_TO_BARRIERS) / 100;
      eventDamage *= dmgVsBarrier;
      eventDamage = defender.damageBarrier(attacker, eventDamage);
      eventDamage /= dmgVsBarrier;
    } else {
      defender.damageBarrier(attacker, 0);
    }

    if (eventDamage > 0) {
      eventDamage = plugin.getDamageManager().doEnergyAbsorb(defender, eventDamage);
      if (damages.containsKey(DamageType.PHYSICAL)) {
        DamageUtil.attemptBleed(attacker, defender, damages.get(DamageType.PHYSICAL), mods, true, false);
      }
    }

    if (defender.hasTrait(StrifeTrait.BLEEDING_EDGE)) {
      eventDamage *= 0.5F;
      float bleed = eventDamage;
      DamageUtil.applyBleed(defender, defender, bleed, true, true, false);
    }

    if (mods.isApplyOnHitEffects()) {
      DamageUtil.attemptPoison(attacker, defender, mods);
      Bukkit.getScheduler().runTaskLater(plugin, () ->
          DamageUtil.postDamage(attacker, defender, mods, true), 0L);
    }

    if (eventDamage >= defendEntity.getHealth()) {
      eventDamage = DamageUtil.doPreDeath(defender, eventDamage);
    }
    if (eventDamage >= defender.getEntity().getHealth() && defender.isPlayer()) {
      plugin.getDamageManager().getSourceOfDeath().put(
          defender.getEntity().getUniqueId(), PlayerDataUtil.getName(attacker.getEntity()));
    }
    event.setDamage(BASE, Math.max(eventDamage, 0));
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void strifeEntityDeath(EntityDeathEvent event) {
    if (event.getEntity().getKiller() == null) {
      return;
    }
    StrifeMob killer = plugin.getStrifeMobManager().getStatMob(event.getEntity().getKiller());
    if (killer.getStat(StrifeStat.HP_ON_KILL) > 0.1) {
      if (!killer.getEntity().hasPotionEffect(PotionEffectType.WITHER) || killer.hasTrait(StrifeTrait.CORPSE_EATER)) {
        float healRatio = 1 + killer.getStat(StrifeStat.HEALING_POWER) / 300;
        DamageUtil.restoreHealth(event.getEntity().getKiller(), killer.getStat(StrifeStat.HP_ON_KILL) * healRatio, true,
            true);
      }
    }
    if (killer.getStat(StrifeStat.ENERGY_ON_KILL) > 0.1) {
      StatUtil.changeEnergy(killer, killer.getStat(StrifeStat.ENERGY_ON_KILL));
    }
    if (killer.getStat(StrifeStat.RAGE_ON_KILL) > 0.1) {
      killer.changeRage(killer.getStat(StrifeStat.RAGE_ON_KILL));
    }
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
        event.getEntity().hasMetadata("NPC")) {
      if (event.getDamager() instanceof Projectile) {
        event.getDamager().remove();
      }
      event.setCancelled(true);
    }
  }

  private boolean canMonsterHit(LivingEntity uuid) {
    return MONSTER_HIT_COOLDOWN.getOrDefault(uuid, 0L) < System.currentTimeMillis();
  }

  public static void putMonsterHit(LivingEntity livingEntity) {
    if (!(livingEntity instanceof Player)) {
      MONSTER_HIT_COOLDOWN.put(livingEntity, System.currentTimeMillis() + 500);
    }
  }
}
