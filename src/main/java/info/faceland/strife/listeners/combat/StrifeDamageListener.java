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
import static info.faceland.strife.stats.StrifeStat.OVERCHARGE;
import static info.faceland.strife.stats.StrifeStat.RAGE_ON_HIT;
import static info.faceland.strife.stats.StrifeStat.RAGE_WHEN_HIT;
import static info.faceland.strife.util.DamageUtil.applyElementalEffects;
import static info.faceland.strife.util.DamageUtil.applyHealthOnHit;
import static info.faceland.strife.util.DamageUtil.applyLifeSteal;
import static info.faceland.strife.util.DamageUtil.callCritEvent;
import static info.faceland.strife.util.DamageUtil.callSneakAttackEvent;
import static info.faceland.strife.util.DamageUtil.doEvasion;
import static info.faceland.strife.util.DamageUtil.getPotionMult;
import static info.faceland.strife.util.PlayerDataUtil.sendActionbarDamage;

import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import info.faceland.strife.StrifePlugin;
import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.data.ability.EntityAbilitySet.TriggerAbilityType;
import info.faceland.strife.data.champion.LifeSkillType;
import info.faceland.strife.events.SneakAttackEvent;
import info.faceland.strife.events.StrifeDamageEvent;
import info.faceland.strife.stats.StrifeStat;
import info.faceland.strife.stats.StrifeTrait;
import info.faceland.strife.util.DamageUtil;
import info.faceland.strife.util.DamageUtil.AbilityMod;
import info.faceland.strife.util.DamageUtil.DamageType;
import info.faceland.strife.util.StatUtil;
import java.util.Map;
import java.util.Set;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class StrifeDamageListener implements Listener {

  private final StrifePlugin plugin;
  private static final float PVP_MULT = (float) StrifePlugin.getInstance().getSettings()
      .getDouble("config.mechanics.pvp-multiplier", 0.5);

  public StrifeDamageListener(StrifePlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler(priority = EventPriority.NORMAL)
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

    float evasionMultiplier = 1;
    if (event.isCanBeEvaded()) {
      evasionMultiplier = DamageUtil.getFullEvasionMult(attacker, defender, event.getAbilityMods());
      if (evasionMultiplier < DamageUtil.EVASION_THRESHOLD) {
        if (defender.getEntity() instanceof Player) {
          plugin.getCombatStatusManager().addPlayer((Player) defender.getEntity());
        }
        doEvasion(attacker.getEntity(), defender.getEntity());
        removeIfExisting(event.getProjectile());
        event.setCancelled(true);
        return;
      }
    }

    float attackMult = event.getAttackMultiplier();

    if (event.isCanBeBlocked()) {
      if (plugin.getBlockManager().isAttackBlocked(attacker, defender, attackMult,
          event.getAttackType(), event.isBlocking())) {
        if (defender.getEntity() instanceof Player) {
          plugin.getCombatStatusManager().addPlayer((Player) defender.getEntity());
        }
        removeIfExisting(event.getProjectile());
        event.setCancelled(true);
        return;
      }
    }

    if (attacker.getStat(RAGE_ON_HIT) > 0.1) {
      plugin.getRageManager()
          .addRage(attacker, attacker.getStat(RAGE_ON_HIT) * attackMult);
    }
    if (defender.getStat(RAGE_WHEN_HIT) > 0.1) {
      plugin.getRageManager().addRage(defender, defender.getStat(RAGE_WHEN_HIT));
    }

    // Handle projectiles created by abilities/effects. Has to be done after
    // block and evasion to properly mitigate hits.
    if (event.getExtraEffects() != null) {
      for (String s : event.getExtraEffects()) {
        if (StringUtils.isBlank(s)) {
          continue;
        }
        plugin.getEffectManager()
            .execute(plugin.getEffectManager().getEffect(s), attacker, defender.getEntity());
      }
      if (attackMult <= 0) {
        event.setFinalDamage(0.001);
        return;
      }
    }

    Map<DamageType, Float> damageMap = DamageUtil.buildDamageMap(attacker, event.getAttackType());
    damageMap.replaceAll((t, v) -> damageMap.get(t) * event.getDamageMod(t) * attackMult);
    for (DamageType type : DamageType.values()) {
      if (event.getFlatDamageBonuses().containsKey(type)) {
        damageMap.put(type, damageMap.getOrDefault(type, 0f) + event.getFlatDamageBonus(type));
      }
    }
    DamageUtil.applyDamageReductions(attacker, defender, damageMap, event.getAbilityMods());

    Set<DamageType> triggeredElements = applyElementalEffects(attacker, defender, damageMap,
        event.isConsumeEarthRunes());

    float critMult = 0;
    double bonusOverchargeMultiplier = 0;
    if (isCriticalHit(attacker, defender, attackMult,
        event.getAbilityMods(AbilityMod.CRITICAL_CHANCE))) {
      critMult = (attacker.getStat(CRITICAL_DAMAGE) +
          event.getAbilityMods(AbilityMod.CRITICAL_DAMAGE)) / 100;
    }
    if (attackMult > 0.99) {
      bonusOverchargeMultiplier = attacker.getStat(OVERCHARGE) / 100;
    }

    double standardDamage = damageMap.getOrDefault(DamageType.PHYSICAL, 0f) +
        damageMap.getOrDefault(DamageType.MAGICAL, 0f);
    double elementalDamage = damageMap.getOrDefault(DamageType.FIRE, 0f) +
        damageMap.getOrDefault(DamageType.ICE, 0f) +
        damageMap.getOrDefault(DamageType.LIGHTNING, 0f) +
        damageMap.getOrDefault(DamageType.DARK, 0f) +
        damageMap.getOrDefault(DamageType.EARTH, 0f) +
        damageMap.getOrDefault(DamageType.LIGHT, 0f);

    float pvpMult = 1f;
    if (attacker.getEntity() instanceof Player && defender.getEntity() instanceof Player) {
      pvpMult = PVP_MULT;
    }
    float potionMult = getPotionMult(attacker.getEntity(), defender.getEntity());

    standardDamage += standardDamage * critMult + standardDamage * bonusOverchargeMultiplier;
    standardDamage *= evasionMultiplier;
    standardDamage *= potionMult;
    standardDamage *= StatUtil.getDamageMult(attacker);
    standardDamage *= pvpMult;

    applyLifeSteal(attacker, Math.min(standardDamage, defender.getEntity().getHealth()),
        event.getHealMultiplier(), event.getAbilityMods(AbilityMod.LIFE_STEAL));
    applyHealthOnHit(attacker, attackMult, event.getHealMultiplier(),
        event.getAbilityMods(AbilityMod.HEALTH_ON_HIT));

    if (attacker.hasTrait(StrifeTrait.ELEMENTAL_CRITS)) {
      elementalDamage += elementalDamage * critMult;
    }
    elementalDamage *= evasionMultiplier;
    elementalDamage *= potionMult;
    elementalDamage *= StatUtil.getDamageMult(attacker);
    elementalDamage *= pvpMult;

    float damageReduction = defender.getStat(StrifeStat.DAMAGE_REDUCTION) * pvpMult;
    float rawDamage = (float) Math.max(0D, (standardDamage + elementalDamage) - damageReduction);

    rawDamage *= 200 / (200 + plugin.getRageManager().getRage(defender.getEntity()));

    if (event.getProjectile() != null) {
      rawDamage *= DamageUtil.getProjectileMultiplier(attacker, defender);
    }
    rawDamage *= StatUtil.getTenacityMult(defender);
    rawDamage *= StatUtil.getMinionMult(attacker);
    rawDamage += damageMap.getOrDefault(DamageType.TRUE_DAMAGE, 0f);

    double finalDamage = plugin.getBarrierManager().damageBarrier(defender, rawDamage);
    plugin.getBarrierManager().updateShieldDisplay(defender);

    boolean isBleedApplied = false;
    if (damageMap.containsKey(DamageType.PHYSICAL)) {
      isBleedApplied = DamageUtil.attemptBleed(attacker, defender,
          damageMap.get(DamageType.PHYSICAL), attackMult, event.getAbilityMods());
    }

    boolean isSneakAttack = event.getProjectile() == null ?
        plugin.getSneakManager().isSneakAttack(attacker.getEntity(), defender.getEntity()) :
        plugin.getSneakManager().isSneakAttack(event.getProjectile(), defender.getEntity());

    if (isSneakAttack) {
      Player player = (Player) attacker.getEntity();
      float sneakSkill = plugin.getChampionManager().getChampion(player)
          .getEffectiveLifeSkillLevel(LifeSkillType.SNEAK, false);
      float sneakDamage = sneakSkill;
      sneakDamage += defender.getEntity().getMaxHealth() * (0.1 + 0.002 * sneakSkill);
      sneakDamage *= attackMult;
      sneakDamage *= pvpMult;
      SneakAttackEvent sneakEvent = callSneakAttackEvent((Player) attacker.getEntity(),
          defender.getEntity(), sneakSkill, sneakDamage);
      if (sneakEvent.isCancelled()) {
        isSneakAttack = false;
      } else {
        finalDamage += sneakEvent.getSneakAttackDamage();
      }
    }
    if (!isSneakAttack && attacker.getEntity() instanceof Player) {
      plugin.getSneakManager().tempDisableSneak((Player) attacker.getEntity());
    }

    DamageUtil.doReflectedDamage(defender, attacker, event.getAttackType());

    plugin.getAbilityManager().abilityCast(attacker, TriggerAbilityType.ON_HIT);
    plugin.getAbilityManager().abilityCast(defender, TriggerAbilityType.WHEN_HIT);

    sendActionbarDamage(attacker.getEntity(), rawDamage, bonusOverchargeMultiplier,
        critMult, triggeredElements, isBleedApplied, isSneakAttack);

    event.setFinalDamage(finalDamage);
  }

  private boolean isCriticalHit(StrifeMob attacker, StrifeMob defender, float attackMult,
      float bonusCrit) {
    if (DamageUtil.isCrit(attacker, attackMult, bonusCrit)) {
      callCritEvent(attacker.getEntity(), attacker.getEntity());
      defender.getEntity().getWorld().playSound(defender.getEntity().getEyeLocation(),
          Sound.ENTITY_GENERIC_BIG_FALL, 2f, 0.8f);
      return true;
    }
    return false;
  }

  private void removeIfExisting(Projectile projectile) {
    if (projectile == null) {
      return;
    }
    projectile.remove();
  }
}
