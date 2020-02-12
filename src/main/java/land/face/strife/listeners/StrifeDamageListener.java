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
package land.face.strife.listeners;

import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import java.util.Map;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.ability.EntityAbilitySet.TriggerAbilityType;
import land.face.strife.data.champion.LifeSkillType;
import land.face.strife.events.SneakAttackEvent;
import land.face.strife.events.StrifeDamageEvent;
import land.face.strife.stats.StrifeStat;
import land.face.strife.stats.StrifeTrait;
import land.face.strife.util.DamageUtil;
import land.face.strife.util.DamageUtil.AbilityMod;
import land.face.strife.util.DamageUtil.DamageType;
import land.face.strife.util.StatUtil;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.metadata.FixedMetadataValue;

public class StrifeDamageListener implements Listener {

  private StrifePlugin plugin;
  private static float PVP_MULT;

  public StrifeDamageListener(StrifePlugin plugin) {
    this.plugin = plugin;
    PVP_MULT = (float) StrifePlugin.getInstance().getSettings()
        .getDouble("config.mechanics.pvp-multiplier", 0.5);
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
      plugin.getSneakManager().tempDisableSneak(defender.getEntity());
      plugin.getChampionManager().updateEquipmentStats(
          plugin.getChampionManager().getChampion((Player) defender.getEntity()));
    }

    if (plugin.getCounterManager().executeCounters(event.getAttacker().getEntity(),
        event.getDefender().getEntity())) {
      event.setCancelled(true);
      return;
    }

    float evasionMultiplier = 1;
    if (event.isCanBeEvaded()) {
      evasionMultiplier = DamageUtil.getFullEvasionMult(attacker, defender, event.getAbilityMods());
      if (evasionMultiplier < DamageUtil.EVASION_THRESHOLD) {
        if (defender.getEntity() instanceof Player) {
          plugin.getCombatStatusManager().addPlayer((Player) defender.getEntity());
        }
        DamageUtil.doEvasion(attacker.getEntity(), defender.getEntity());
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
        DamageUtil.doReflectedDamage(defender, attacker, event.getAttackType());
        return;
      }
    }

    if (attacker.getStat(StrifeStat.RAGE_ON_HIT) > 0.1) {
      plugin.getRageManager()
          .addRage(attacker, attacker.getStat(StrifeStat.RAGE_ON_HIT) * attackMult);
    }
    if (defender.getStat(StrifeStat.RAGE_WHEN_HIT) > 0.1) {
      plugin.getRageManager().addRage(defender, defender.getStat(StrifeStat.RAGE_WHEN_HIT));
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

    DamageUtil.applyElementalEffects(attacker, defender, damageMap, event.isConsumeEarthRunes());

    float critMult = 0;
    double bonusOverchargeMultiplier = 0;

    boolean criticalHit = isCriticalHit(attacker, defender, attackMult,
        event.getAbilityMods(AbilityMod.CRITICAL_CHANCE));
    if (criticalHit) {
      critMult = (attacker.getStat(StrifeStat.CRITICAL_DAMAGE) +
          event.getAbilityMods(AbilityMod.CRITICAL_DAMAGE)) / 100;
    }

    boolean overcharge = attackMult > 0.99;
    if (overcharge) {
      bonusOverchargeMultiplier = attacker.getStat(StrifeStat.OVERCHARGE) / 100;
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
    float potionMult = DamageUtil.getPotionMult(attacker.getEntity(), defender.getEntity());

    standardDamage += standardDamage * critMult + standardDamage * bonusOverchargeMultiplier;
    standardDamage *= evasionMultiplier;
    standardDamage *= potionMult;
    standardDamage *= StatUtil.getDamageMult(attacker);
    standardDamage *= pvpMult;

    DamageUtil.applyLifeSteal(attacker, Math.min(standardDamage, defender.getEntity().getHealth()),
        event.getHealMultiplier(), event.getAbilityMods(AbilityMod.LIFE_STEAL));
    DamageUtil.applyHealthOnHit(attacker, attackMult, event.getHealMultiplier(),
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

    rawDamage *= DamageUtil.getRageMult(defender);

    if (event.getProjectile() != null) {
      rawDamage *= DamageUtil.getProjectileMultiplier(attacker, defender);
    }
    rawDamage *= DamageUtil.getTenacityMult(defender);
    rawDamage *= DamageUtil.getMinionMult(attacker);
    rawDamage += damageMap.getOrDefault(DamageType.TRUE_DAMAGE, 0f);

    boolean isSneakAttack =
        event.isSneakAttack() && !defender.getEntity().hasMetadata("IGNORE_SNEAK");
    if (isSneakAttack) {
      Player player = (Player) attacker.getEntity();
      float sneakSkill = plugin.getChampionManager().getChampion(player)
          .getEffectiveLifeSkillLevel(LifeSkillType.SNEAK, false);
      float sneakDamage = sneakSkill;
      sneakDamage += defender.getEntity().getMaxHealth() * (0.1 + 0.002 * sneakSkill);
      sneakDamage *= attackMult;
      sneakDamage *= pvpMult;
      SneakAttackEvent sneakEvent = DamageUtil.callSneakAttackEvent((Player) attacker.getEntity(),
          defender.getEntity(), sneakSkill, sneakDamage);
      if (!sneakEvent.isCancelled()) {
        defender.getEntity().setMetadata("IGNORE_SNEAK", new FixedMetadataValue(plugin, true));
        rawDamage += sneakEvent.getSneakAttackDamage();
      }
    }

    String damageString = String.valueOf((int) Math.ceil(rawDamage));
    if (overcharge) {
      damageString = "&l" + damageString;
    }
    if (criticalHit) {
      damageString = damageString + "!";
    }
    if (attacker.getEntity() instanceof Player) {
      plugin.getIndicatorManager().addIndicator(attacker.getEntity(), defender.getEntity(),
          plugin.getDamageManager().buildHitIndicator((Player) attacker.getEntity()), damageString);
    }
    if (attacker.getMaster() != null && attacker.getMaster() instanceof Player) {
      plugin.getIndicatorManager().addIndicator(attacker.getMaster(), defender.getEntity(),
          plugin.getDamageManager().buildHitIndicator((Player) attacker.getMaster()),
          "&7" + damageString);
    }

    double finalDamage = plugin.getBarrierManager().damageBarrier(defender, rawDamage);
    plugin.getBarrierManager().updateShieldDisplay(defender);

    if (damageMap.containsKey(DamageType.PHYSICAL)) {
      DamageUtil.attemptBleed(attacker, defender, damageMap.get(DamageType.PHYSICAL), attackMult,
          event.getAbilityMods(), false);
    }

    DamageUtil.doReflectedDamage(defender, attacker, event.getAttackType());
    plugin.getAbilityManager().abilityCast(attacker, defender, TriggerAbilityType.ON_HIT);
    plugin.getAbilityManager().abilityCast(defender, attacker, TriggerAbilityType.WHEN_HIT);
    plugin.getSneakManager().tempDisableSneak(attacker.getEntity());

    defender.trackDamage(attacker, (float) finalDamage);
    event.setFinalDamage(finalDamage);
  }

  private boolean isCriticalHit(StrifeMob attacker, StrifeMob defender, float attackMult,
      float bonusCrit) {
    if (DamageUtil.isCrit(attacker, attackMult, bonusCrit)) {
      DamageUtil.callCritEvent(attacker.getEntity(), attacker.getEntity());
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
