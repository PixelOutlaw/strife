/**
 * The MIT License Copyright (c) 2015 Teal Cube Games
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package info.faceland.strife.listeners.combat;

import static info.faceland.strife.attributes.StrifeAttribute.CRITICAL_DAMAGE;
import static info.faceland.strife.attributes.StrifeAttribute.DAMAGE_REFLECT;
import static info.faceland.strife.attributes.StrifeAttribute.HP_ON_KILL;
import static info.faceland.strife.attributes.StrifeAttribute.OVERCHARGE;
import static info.faceland.strife.attributes.StrifeAttribute.RAGE_ON_HIT;
import static info.faceland.strife.attributes.StrifeAttribute.RAGE_ON_KILL;
import static info.faceland.strife.attributes.StrifeAttribute.RAGE_WHEN_HIT;
import static info.faceland.strife.attributes.StrifeAttribute.TRUE_DAMAGE;
import static info.faceland.strife.util.DamageUtil.AttackType;
import static info.faceland.strife.util.DamageUtil.applyHealthOnHit;
import static info.faceland.strife.util.DamageUtil.applyLifeSteal;
import static info.faceland.strife.util.DamageUtil.attemptBleed;
import static info.faceland.strife.util.DamageUtil.attemptCorrupt;
import static info.faceland.strife.util.DamageUtil.attemptFreeze;
import static info.faceland.strife.util.DamageUtil.attemptIgnite;
import static info.faceland.strife.util.DamageUtil.attemptShock;
import static info.faceland.strife.util.DamageUtil.callCritEvent;
import static info.faceland.strife.util.DamageUtil.consumeEarthRunes;
import static info.faceland.strife.util.DamageUtil.doBlock;
import static info.faceland.strife.util.DamageUtil.doEvasion;
import static info.faceland.strife.util.DamageUtil.getLightBonus;
import static info.faceland.strife.util.DamageUtil.getPotionMult;
import static info.faceland.strife.util.DamageUtil.hasLuck;
import static info.faceland.strife.util.DamageUtil.restoreHealth;
import static info.faceland.strife.util.DamageUtil.rollDouble;
import static info.faceland.strife.util.PlayerDataUtil.sendActionbarDamage;
import static info.faceland.strife.util.ProjectileUtil.ATTACK_SPEED_META;

import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import info.faceland.strife.StrifePlugin;
import info.faceland.strife.attributes.StrifeAttribute;
import info.faceland.strife.attributes.StrifeTrait;
import info.faceland.strife.data.AttributedEntity;
import info.faceland.strife.data.ability.EntityAbilitySet.AbilityType;
import info.faceland.strife.util.DamageUtil;
import info.faceland.strife.util.ItemUtil;
import info.faceland.strife.util.StatUtil;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EvokerFangs;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.projectiles.ProjectileSource;

public class CombatListener implements Listener {

  private final StrifePlugin plugin;
  private static final double EVASION_DODGE_THRESHOLD = 0.5D;

  public CombatListener(StrifePlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void strifeDamageHandler(EntityDamageByEntityEvent event) {
    if (event.isCancelled()) {
      return;
    }
    // Catch the spoofed damage from abilities
    if (event.getCause() == DamageCause.CUSTOM) {
      return;
    }
    if (!(event.getEntity() instanceof LivingEntity) || event.getEntity() instanceof ArmorStand) {
      return;
    }

    LivingEntity defendEntity = (LivingEntity) event.getEntity();
    LivingEntity attackEntity;

    Projectile projectile = null;
    String[] projectileEffect = null;
    if (event.getDamager() instanceof Projectile) {
      projectile = (Projectile) event.getDamager();
      ProjectileSource shooter = projectile.getShooter();
      if (defendEntity.hasMetadata("NPC")) {
        projectile.remove();
        return;
      }
      if (!(shooter instanceof LivingEntity)) {
        return;
      }
      attackEntity = (LivingEntity) shooter;
      if (projectile.hasMetadata("EFFECT_PROJECTILE")) {
        projectileEffect = projectile.getMetadata("EFFECT_PROJECTILE").get(0).asString().split("~");
      }
    } else if (event.getDamager() instanceof EvokerFangs) {
      attackEntity = ((EvokerFangs) event.getDamager()).getOwner();
    } else if (event.getDamager() instanceof TNTPrimed) {
      double distance = event.getDamager().getLocation().distance(event.getEntity().getLocation());
      double explosionMult = Math.max(0.3, 4 / (distance + 3));
      event.setDamage(explosionMult * (10 + defendEntity.getMaxHealth() * 0.4));
      return;
    } else if (event.getDamager() instanceof LivingEntity) {
      attackEntity = (LivingEntity) event.getDamager();
    } else {
      event.setDamage(1);
      return;
    }

    if (attackEntity instanceof Player) {
      plugin.getChampionManager().updateEquipmentAttributes(
          plugin.getChampionManager().getChampion((Player) attackEntity));
    }
    if (defendEntity instanceof Player) {
      plugin.getSneakManager().tempDisableSneak((Player) defendEntity);
      plugin.getChampionManager().updateEquipmentAttributes(
          plugin.getChampionManager().getChampion((Player) defendEntity));
    }

    double attackMultiplier = 1D;
    double healMultiplier = 1D;

    AttackType damageType = DamageUtil.getAttackType(event);

    AttributedEntity attacker = plugin.getAttributedEntityManager()
        .getAttributedEntity(attackEntity);
    AttributedEntity defender = plugin.getAttributedEntityManager()
        .getAttributedEntity(defendEntity);

    if (damageType == AttackType.MELEE) {
      if (ItemUtil.isWand(attackEntity.getEquipment().getItemInMainHand())) {
        event.setCancelled(true);
        return;
      }
      attackMultiplier = plugin.getAttackSpeedManager().getAttackMultiplier(attacker);
    } else if (damageType == AttackType.EXPLOSION) {
      double distance = event.getDamager().getLocation().distance(event.getEntity().getLocation());
      attackMultiplier *= Math.max(0.3, 4 / (distance + 3));
      healMultiplier = 0.3D;
    } else if (projectile != null && projectile.hasMetadata(ATTACK_SPEED_META)) {
      attackMultiplier = projectile.getMetadata(ATTACK_SPEED_META).get(0).asDouble();
    }

    if (attackMultiplier < 0.05) {
      event.setDamage(0);
      event.setCancelled(true);
      return;
    }

    boolean blocked = false;
    if (event.isApplicable(DamageModifier.BLOCKING)) {
      if (event.getDamage(EntityDamageEvent.DamageModifier.BLOCKING) != 0) {
        blocked = true;
      }
    }

    DamageUtil.removeDamageModifiers(event);

    double evasionMultiplier = StatUtil.getMinimumEvasionMult(attacker, defender);
    evasionMultiplier = evasionMultiplier + (rollDouble() * (1 - evasionMultiplier));

    if (evasionMultiplier < EVASION_DODGE_THRESHOLD) {
      doEvasion(attackEntity, defendEntity);
      removeIfExisting(projectile);
      event.setCancelled(true);
      return;
    }

    if (plugin.getBlockManager().rollBlock(defender, blocked)) {
      plugin.getBlockManager().blockFatigue(defendEntity.getUniqueId(), attackMultiplier, blocked);
      plugin.getBlockManager().bumpRunes(defender);
      doReflectedDamage(defender, attackEntity, damageType);
      doBlock(attackEntity, defendEntity);
      removeIfExisting(projectile);
      event.setCancelled(true);
      return;
    }

    if (attacker.getAttribute(RAGE_ON_HIT) > 0.1) {
      plugin.getRageManager()
          .addRage(attacker, attacker.getAttribute(RAGE_ON_HIT) * attackMultiplier);
    }
    if (defender.getAttribute(RAGE_WHEN_HIT) > 0.1) {
      plugin.getRageManager()
          .addRage(defender, defender.getAttribute(RAGE_WHEN_HIT));
    }

    double pvpMult = 1D;
    if (attackEntity instanceof Player && defendEntity instanceof Player) {
      pvpMult = plugin.getSettings().getDouble("config.pvp-multiplier", 0.5);
    }

    // Handle projectiles created by abilities/effects. Has to be done after
    // block and evasion to properly mitigate hits.
    if (projectileEffect != null) {
      for (String s : projectileEffect) {
        if (StringUtils.isBlank(s)) {
          continue;
        }
        plugin.getEffectManager().execute(s, attacker, defender);
      }
      event.setDamage(0);
      return;
    }

    double potionMult = getPotionMult(attackEntity, defendEntity);

    double physicalBaseDamage = 0;
    double magicBaseDamage = 0;

    switch (damageType) {
      case MELEE:
        physicalBaseDamage = StatUtil.getBaseMeleeDamage(attacker, defender);
        break;
      case RANGED:
        physicalBaseDamage = StatUtil.getBaseRangedDamage(attacker, defender);
        break;
      case MAGIC:
        magicBaseDamage = StatUtil.getBaseMagicDamage(attacker, defender);
        break;
      case EXPLOSION:
        physicalBaseDamage = StatUtil.getBaseExplosionDamage(attacker, defender);
    }
    double fireBaseDamage = StatUtil.getBaseFireDamage(attacker, defender);
    double iceBaseDamage = StatUtil.getBaseIceDamage(attacker, defender);
    double lightningBaseDamage = StatUtil.getBaseLightningDamage(attacker, defender);
    double earthBaseDamage = StatUtil.getBaseEarthDamage(attacker, defender);
    double lightBaseDamage = StatUtil.getBaseLightDamage(attacker, defender);
    double shadowBaseDamage = StatUtil.getBaseShadowDamage(attacker, defender);

    double bonusFireDamage = attemptIgnite(fireBaseDamage, attacker, defendEntity);
    double bonusIceDamage = attemptFreeze(iceBaseDamage, attacker, defendEntity);
    double bonusLightningDamage = attemptShock(lightningBaseDamage, attacker, defendEntity);
    double bonusEarthDamage = consumeEarthRunes(earthBaseDamage, attacker, defendEntity);
    double bonusLightDamage = getLightBonus(lightBaseDamage, attacker, defendEntity);
    boolean corruptEffect = attemptCorrupt(shadowBaseDamage, attacker, defendEntity);

    fireBaseDamage += bonusFireDamage;
    iceBaseDamage += bonusIceDamage;
    lightningBaseDamage += bonusLightningDamage;
    earthBaseDamage += bonusEarthDamage;
    lightBaseDamage += bonusLightDamage;
    shadowBaseDamage += shadowBaseDamage *
        (plugin.getDarknessManager().getCorruptionStacks(defender.getEntity()) * 0.02);

    double bonusCriticalMultiplier = 0;
    double bonusOverchargeMultiplier = 0;

    if (doCriticalHit(attacker, defender)) {
      bonusCriticalMultiplier = attacker.getAttribute(CRITICAL_DAMAGE) / 100;
    }
    if (doOvercharge(attackMultiplier)) {
      bonusOverchargeMultiplier = attacker.getAttribute(OVERCHARGE) / 100;
    }

    double standardDamage = physicalBaseDamage + magicBaseDamage;
    double elementalDamage = fireBaseDamage + iceBaseDamage + lightningBaseDamage +
        earthBaseDamage + lightBaseDamage + shadowBaseDamage;

    standardDamage +=
        standardDamage * bonusCriticalMultiplier + standardDamage * bonusOverchargeMultiplier;
    standardDamage *= evasionMultiplier;
    standardDamage *= attackMultiplier;
    standardDamage *= potionMult;
    standardDamage *= StatUtil.getDamageMult(attacker);
    standardDamage *= pvpMult;

    applyLifeSteal(attacker, standardDamage, healMultiplier);
    applyHealthOnHit(attacker, attackMultiplier, healMultiplier);

    if (attacker.hasTrait(StrifeTrait.ELEMENTAL_CRITS)) {
      elementalDamage += elementalDamage * bonusCriticalMultiplier;
    }
    elementalDamage *= evasionMultiplier;
    elementalDamage *= attackMultiplier;
    elementalDamage *= potionMult;
    elementalDamage *= StatUtil.getDamageMult(attacker);
    elementalDamage *= pvpMult;

    double damageReduction = defender.getAttribute(StrifeAttribute.DAMAGE_REDUCTION) * pvpMult;
    double rawDamage = (standardDamage + elementalDamage) * (blocked ? 0.6 : 1.0);
    rawDamage = Math.max(0D, rawDamage - damageReduction);
    rawDamage *= 200 / (200 + plugin.getRageManager().getRage(defendEntity));
    if (projectile != null) {
      rawDamage *= DamageUtil.getProjectileMultiplier(attacker, defender);
    }
    rawDamage *= DamageUtil.getTenacityMult(defender);
    rawDamage += attacker.getAttribute(TRUE_DAMAGE) * attackMultiplier;

    double finalDamage = plugin.getBarrierManager().damageBarrier(defender, rawDamage);
    plugin.getBarrierManager().updateShieldDisplay(defender);

    boolean isBleedApplied = false;
    if (physicalBaseDamage > 0) {
      isBleedApplied = attemptBleed(attacker, defender, physicalBaseDamage * pvpMult,
          bonusCriticalMultiplier, attackMultiplier);
    }

    boolean isSneakAttack = projectile == null ?
        plugin.getSneakManager().isMeleeSneakAttack(attackEntity, defendEntity) :
        plugin.getSneakManager().isProjectileSneakAttack(projectile, defendEntity);

    if (isSneakAttack) {
      Player player = (Player) attackEntity;
      int sneakSkill = plugin.getChampionManager().getChampion(player).getSneakSkill(false);
      finalDamage += (sneakSkill + defendEntity.getMaxHealth() * sneakSkill * 0.01) * pvpMult;

      defender.getEntity().getWorld().playSound(defender.getEntity().getEyeLocation(),
          Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 1f, 1.5f);

      if (!(defendEntity instanceof Player)) {
        boolean finishingBlow = finalDamage > defendEntity.getHealth();
        if (!finishingBlow) {
          plugin.getSneakManager().tempDisableSneak((Player) attackEntity);
        }
        float gainedXp = plugin.getSneakManager()
            .getSneakAttackExp(defendEntity, sneakSkill, finishingBlow);
        plugin.getExperienceManager().addExperience(player, gainedXp, false);
      }
    } else if (attackEntity instanceof Player) {
      plugin.getSneakManager().tempDisableSneak((Player) attackEntity);
    }

    doReflectedDamage(defender, attackEntity, damageType);

    event.setDamage(EntityDamageEvent.DamageModifier.BASE, finalDamage);

    if (plugin.getUniqueEntityManager().isUnique(attackEntity)) {
      plugin.getAbilityManager().uniqueAbilityCast(attacker, AbilityType.ON_HIT);
      plugin.getAbilityManager().checkPhaseChange(attacker);
    }
    if (plugin.getUniqueEntityManager().isUnique(defendEntity)) {
      plugin.getAbilityManager().uniqueAbilityCast(defender, AbilityType.WHEN_HIT);
      plugin.getAbilityManager().checkPhaseChange(defender);
    }

    sendActionbarDamage(attackEntity, rawDamage, bonusOverchargeMultiplier, bonusCriticalMultiplier,
        bonusFireDamage, bonusIceDamage, bonusLightningDamage, bonusEarthDamage, bonusLightDamage,
        corruptEffect, isBleedApplied, isSneakAttack);

    if (attackEntity instanceof Player) {
      plugin.getBossBarManager().pushBar((Player) attackEntity, defender);
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void strifeEntityDeath(EntityDeathEvent event) {
    if (event.getEntity().getKiller() == null) {
      return;
    }
    AttributedEntity killer = plugin.getAttributedEntityManager()
        .getAttributedEntity(event.getEntity().getKiller());
    if (killer.getAttribute(HP_ON_KILL) > 0.1) {
      restoreHealth(event.getEntity().getKiller(), killer.getAttribute(HP_ON_KILL));
    }
    if (killer.getAttribute(RAGE_ON_KILL) > 0.1) {
      plugin.getRageManager()
          .addRage(killer, killer.getAttribute(RAGE_ON_KILL));
    }
  }

  private void doReflectedDamage(AttributedEntity defender, LivingEntity attacker,
      AttackType damageType) {
    if (defender.getAttribute(DAMAGE_REFLECT) < 0.1) {
      return;
    }
    double reflectDamage = defender.getAttribute(DAMAGE_REFLECT);
    reflectDamage = damageType == AttackType.MELEE ? reflectDamage : reflectDamage * 0.6D;
    attacker.getWorld().playSound(attacker.getLocation(), Sound.ENCHANT_THORNS_HIT, 0.2f, 1f);
    if (attacker.getHealth() > reflectDamage) {
      attacker.setHealth(attacker.getHealth() - reflectDamage);
    } else {
      attacker.damage(reflectDamage);
    }
  }

  private boolean doCriticalHit(AttributedEntity attacker, AttributedEntity defender) {
    if (attacker.getAttribute(StrifeAttribute.CRITICAL_RATE) / 100 >= rollDouble(
        hasLuck(attacker.getEntity()))) {
      callCritEvent(attacker.getEntity(), attacker.getEntity());
      defender.getEntity().getWorld().playSound(
          defender.getEntity().getEyeLocation(),
          Sound.ENTITY_GENERIC_BIG_FALL,
          2f,
          0.8f
      );
      return true;
    }
    return false;
  }

  private boolean doOvercharge(double attackSpeedMult) {
    return attackSpeedMult >= 0.99;
  }

  private void removeIfExisting(Projectile projectile) {
    if (projectile == null) {
      return;
    }
    projectile.remove();
  }
}
