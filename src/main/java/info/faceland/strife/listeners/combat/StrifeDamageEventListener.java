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

import static info.faceland.strife.stats.StrifeStat.CRITICAL_DAMAGE;
import static info.faceland.strife.stats.StrifeStat.DAMAGE_REFLECT;
import static info.faceland.strife.stats.StrifeStat.MAGIC_MULT;
import static info.faceland.strife.stats.StrifeStat.MELEE_PHYSICAL_MULT;
import static info.faceland.strife.stats.StrifeStat.OVERCHARGE;
import static info.faceland.strife.stats.StrifeStat.RAGE_ON_HIT;
import static info.faceland.strife.stats.StrifeStat.RAGE_WHEN_HIT;
import static info.faceland.strife.stats.StrifeStat.RANGED_PHYSICAL_MULT;
import static info.faceland.strife.stats.StrifeStat.TRUE_DAMAGE;
import static info.faceland.strife.util.DamageUtil.applyHealthOnHit;
import static info.faceland.strife.util.DamageUtil.applyLifeSteal;
import static info.faceland.strife.util.DamageUtil.attemptBleed;
import static info.faceland.strife.util.DamageUtil.attemptCorrupt;
import static info.faceland.strife.util.DamageUtil.attemptFreeze;
import static info.faceland.strife.util.DamageUtil.attemptIgnite;
import static info.faceland.strife.util.DamageUtil.attemptShock;
import static info.faceland.strife.util.DamageUtil.callCritEvent;
import static info.faceland.strife.util.DamageUtil.callSneakAttackEvent;
import static info.faceland.strife.util.DamageUtil.consumeEarthRunes;
import static info.faceland.strife.util.DamageUtil.doBlock;
import static info.faceland.strife.util.DamageUtil.doEvasion;
import static info.faceland.strife.util.DamageUtil.getLightBonus;
import static info.faceland.strife.util.DamageUtil.getPotionMult;
import static info.faceland.strife.util.DamageUtil.hasLuck;
import static info.faceland.strife.util.DamageUtil.rollDouble;
import static info.faceland.strife.util.PlayerDataUtil.sendActionbarDamage;

import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import info.faceland.strife.StrifePlugin;
import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.data.ability.EntityAbilitySet.AbilityType;
import info.faceland.strife.data.champion.LifeSkillType;
import info.faceland.strife.events.SneakAttackEvent;
import info.faceland.strife.events.StrifeDamageEvent;
import info.faceland.strife.stats.StrifeStat;
import info.faceland.strife.stats.StrifeTrait;
import info.faceland.strife.util.DamageUtil;
import info.faceland.strife.util.DamageUtil.AttackType;
import info.faceland.strife.util.DamageUtil.DamageType;
import info.faceland.strife.util.StatUtil;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class StrifeDamageEventListener implements Listener {

  private final StrifePlugin plugin;
  private static final double EVASION_THRESHOLD = StrifePlugin.getInstance().getSettings()
      .getDouble("config.mechanics.evasion-threshold", 0.5);
  private static final double PVP_MULT = StrifePlugin.getInstance().getSettings()
      .getDouble("config.mechanics.pvp-multiplier", 0.5);

  public StrifeDamageEventListener(StrifePlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void strifeDamageHandler(StrifeDamageEvent event) {
    if (event.isCancelled()) {
      return;
    }

    StrifeMob attacker = event.getAttacker();
    StrifeMob defender = event.getDefender();

    if (attacker.getEntity() instanceof Player) {
      plugin.getChampionManager().updateEquipmentStats(
          plugin.getChampionManager().getChampion((Player) attacker.getEntity()));
    }
    if (defender.getEntity() instanceof Player) {
      plugin.getSneakManager().tempDisableSneak((Player) defender.getEntity());
      plugin.getChampionManager().updateEquipmentStats(
          plugin.getChampionManager().getChampion((Player) defender.getEntity()));
    }

    double evasionMultiplier = StatUtil.getMinimumEvasionMult(attacker, defender);
    evasionMultiplier = evasionMultiplier + (rollDouble() * (1 - evasionMultiplier));

    if (evasionMultiplier < EVASION_THRESHOLD) {
      doEvasion(attacker.getEntity(), defender.getEntity());
      removeIfExisting(event.getProjectile());
      event.setCancelled(true);
      return;
    }

    if (plugin.getBlockManager().rollBlock(defender, event.isBlocking())) {
      plugin.getBlockManager().blockFatigue(defender.getEntity().getUniqueId(),
          event.getAttackMultiplier(), event.isBlocking());
      plugin.getBlockManager().bumpRunes(defender);
      doReflectedDamage(defender, attacker, event.getAttackType());
      doBlock(attacker.getEntity(), defender.getEntity());
      removeIfExisting(event.getProjectile());
      event.setCancelled(true);
      return;
    }

    if (attacker.getStat(RAGE_ON_HIT) > 0.1) {
      plugin.getRageManager()
          .addRage(attacker, attacker.getStat(RAGE_ON_HIT) * event.getAttackMultiplier());
    }
    if (defender.getStat(RAGE_WHEN_HIT) > 0.1) {
      plugin.getRageManager()
          .addRage(defender, defender.getStat(RAGE_WHEN_HIT));
    }

    // Handle projectiles created by abilities/effects. Has to be done after
    // block and evasion to properly mitigate hits.
    if (event.getExtraEffects() != null) {
      for (String s : event.getExtraEffects()) {
        if (StringUtils.isBlank(s)) {
          continue;
        }
        plugin.getEffectManager().execute(plugin.getEffectManager().getEffect(s), attacker, defender);
      }
      defender.getEntity().damage(0, attacker.getEntity());
      event.setCancelled(true);
      return;
    }

    double pvpMult = 1D;
    if (attacker.getEntity() instanceof Player && defender.getEntity() instanceof Player) {
      pvpMult = PVP_MULT;
    }

    double potionMult = getPotionMult(attacker.getEntity(), defender.getEntity());

    double physicalBaseDamage = StatUtil.getBasePhysicalDamage(attacker, defender) *
        event.getDamageMod(DamageType.PHYSICAL);
    double magicBaseDamage = StatUtil.getBaseMagicalDamage(attacker, defender) *
        event.getDamageMod(DamageType.MAGICAL);
    double fireBaseDamage = StatUtil.getBaseFireDamage(attacker, defender) *
        event.getDamageMod(DamageType.FIRE);
    double iceBaseDamage = StatUtil.getBaseIceDamage(attacker, defender) *
        event.getDamageMod(DamageType.ICE);
    double lightningBaseDamage = StatUtil.getBaseLightningDamage(attacker, defender) *
        event.getDamageMod(DamageType.LIGHTNING);
    double earthBaseDamage = StatUtil.getBaseEarthDamage(attacker, defender) *
        event.getDamageMod(DamageType.EARTH);
    double lightBaseDamage = StatUtil.getBaseLightDamage(attacker, defender) *
        event.getDamageMod(DamageType.LIGHT);
    double shadowBaseDamage = StatUtil.getBaseShadowDamage(attacker, defender) *
        event.getDamageMod(DamageType.DARK);

    switch (event.getAttackType()) {
      case MELEE:
        physicalBaseDamage *= 1 + attacker.getStat(MELEE_PHYSICAL_MULT) / 100;
        break;
      case RANGED:
        physicalBaseDamage *= 1 + attacker.getStat(RANGED_PHYSICAL_MULT) / 100;
        break;
    }
    magicBaseDamage *= 1 + attacker.getStat(MAGIC_MULT) / 100;

    double bonusFireDamage = attemptIgnite(fireBaseDamage, attacker, defender.getEntity());
    double bonusIceDamage = attemptFreeze(iceBaseDamage, attacker, defender.getEntity());
    double bonusLightningDamage = attemptShock(lightningBaseDamage, attacker, defender.getEntity());
    double bonusEarthDamage = consumeEarthRunes(earthBaseDamage, attacker, defender.getEntity());
    double bonusLightDamage = getLightBonus(lightBaseDamage, attacker, defender.getEntity());
    boolean corruptEffect = attemptCorrupt(shadowBaseDamage, attacker, defender.getEntity());

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
      bonusCriticalMultiplier = attacker.getStat(CRITICAL_DAMAGE) / 100;
    }
    if (doOvercharge(event.getAttackMultiplier())) {
      bonusOverchargeMultiplier = attacker.getStat(OVERCHARGE) / 100;
    }

    double standardDamage = physicalBaseDamage + magicBaseDamage;
    double elementalDamage = fireBaseDamage + iceBaseDamage + lightningBaseDamage +
        earthBaseDamage + lightBaseDamage + shadowBaseDamage;

    standardDamage +=
        standardDamage * bonusCriticalMultiplier + standardDamage * bonusOverchargeMultiplier;
    standardDamage *= evasionMultiplier;
    standardDamage *= event.getAttackMultiplier();
    standardDamage *= potionMult;
    standardDamage *= StatUtil.getDamageMult(attacker);
    standardDamage *= pvpMult;

    applyLifeSteal(attacker, Math.min(standardDamage, defender.getEntity().getHealth()),
        event.getHealMultiplier());
    applyHealthOnHit(attacker, event.getAttackMultiplier(), event.getHealMultiplier());

    if (attacker.hasTrait(StrifeTrait.ELEMENTAL_CRITS)) {
      elementalDamage += elementalDamage * bonusCriticalMultiplier;
    }
    elementalDamage *= evasionMultiplier;
    elementalDamage *= event.getAttackMultiplier();
    elementalDamage *= potionMult;
    elementalDamage *= StatUtil.getDamageMult(attacker);
    elementalDamage *= pvpMult;

    double damageReduction = defender.getStat(StrifeStat.DAMAGE_REDUCTION) * pvpMult;
    double rawDamage = Math.max(0D, (standardDamage + elementalDamage) - damageReduction);

    rawDamage *= 200 / (200 + plugin.getRageManager().getRage(defender.getEntity()));

    if (event.getProjectile() != null) {
      rawDamage *= DamageUtil.getProjectileMultiplier(attacker, defender);
    }
    rawDamage *= StatUtil.getTenacityMult(defender);
    rawDamage *= StatUtil.getMinionMult(attacker);
    rawDamage += attacker.getStat(TRUE_DAMAGE) * event.getAttackMultiplier();

    double finalDamage = plugin.getBarrierManager().damageBarrier(defender, rawDamage);
    plugin.getBarrierManager().updateShieldDisplay(defender);

    boolean isBleedApplied = false;
    if (physicalBaseDamage > 0) {
      isBleedApplied = attemptBleed(attacker, defender, physicalBaseDamage * pvpMult,
          bonusCriticalMultiplier, event.getAttackMultiplier());
    }

    boolean isSneakAttack = event.getProjectile() == null ?
        plugin.getSneakManager().isSneakAttack(attacker.getEntity(), defender.getEntity()) :
        plugin.getSneakManager().isSneakAttack(event.getProjectile(), defender.getEntity());

    if (isSneakAttack) {
      Player player = (Player) attacker.getEntity();
      float sneakSkill = plugin.getChampionManager().getChampion(player).getEffectiveLifeSkillLevel(
          LifeSkillType.SNEAK, false);
      float sneakDamage = sneakSkill;
      sneakDamage += defender.getEntity().getMaxHealth() * (0.1 + 0.002 * sneakSkill);
      sneakDamage *= event.getAttackMultiplier();
      sneakDamage *= pvpMult;

      SneakAttackEvent sneakEvent = callSneakAttackEvent((Player) attacker.getEntity(),
          defender.getEntity(), sneakSkill, sneakDamage);

      if (sneakEvent == null || sneakEvent.isCancelled()) {
        isSneakAttack = false;
      } else {
        finalDamage += sneakEvent.getSneakAttackDamage();
      }
    }
    if (!isSneakAttack && attacker.getEntity() instanceof Player) {
      plugin.getSneakManager().tempDisableSneak((Player) attacker.getEntity());
    }

    doReflectedDamage(defender, attacker, event.getAttackType());

    if (plugin.getUniqueEntityManager().isUnique(defender.getEntity())) {
      plugin.getAbilityManager().uniqueAbilityCast(attacker, AbilityType.ON_HIT);
      plugin.getAbilityManager().checkPhaseChange(defender.getEntity());
    }
    if (plugin.getUniqueEntityManager().isUnique(defender.getEntity())) {
      plugin.getAbilityManager().uniqueAbilityCast(defender, AbilityType.WHEN_HIT);
      plugin.getAbilityManager().checkPhaseChange(defender.getEntity());
    }

    sendActionbarDamage(attacker.getEntity(), rawDamage, bonusOverchargeMultiplier,
        bonusCriticalMultiplier, bonusFireDamage, bonusIceDamage, bonusLightningDamage,
        bonusEarthDamage, bonusLightDamage, corruptEffect, isBleedApplied, isSneakAttack);

    if (attacker.getEntity() instanceof Player) {
      plugin.getBossBarManager().pushBar((Player) attacker.getEntity(), defender);
    }

    event.setFinalDamage(finalDamage);
  }

  private boolean doCriticalHit(StrifeMob attacker, StrifeMob defender) {
    if (attacker.getStat(StrifeStat.CRITICAL_RATE) / 100 >= rollDouble(
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

  private void doReflectedDamage(StrifeMob defender, StrifeMob attacker, AttackType damageType) {
    if (defender.getStat(DAMAGE_REFLECT) < 0.1) {
      return;
    }
    double reflectDamage = defender.getStat(DAMAGE_REFLECT);
    reflectDamage = damageType == AttackType.MELEE ? reflectDamage : reflectDamage * 0.6D;
    attacker.getEntity().getWorld()
        .playSound(attacker.getEntity().getLocation(), Sound.ENCHANT_THORNS_HIT, 0.2f, 1f);
    attacker.getEntity().setHealth(Math.max(0D, attacker.getEntity().getHealth() - reflectDamage));
  }
}
