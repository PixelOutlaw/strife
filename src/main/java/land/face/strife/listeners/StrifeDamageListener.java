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
import java.util.Set;
import land.face.strife.StrifePlugin;
import land.face.strife.data.IndicatorData;
import land.face.strife.data.IndicatorData.IndicatorStyle;
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
import land.face.strife.util.PlayerDataUtil;
import land.face.strife.util.StatUtil;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

public class StrifeDamageListener implements Listener {

  private StrifePlugin plugin;
  private float PVP_MULT = (float) StrifePlugin.getInstance().getSettings()
      .getDouble("config.mechanics.pvp-multiplier", 0.5);
  private float IND_FLOAT_SPEED = (float) StrifePlugin.getInstance().getSettings()
      .getDouble("config.indicators.float-speed", 70);
  private float IND_MISS_SPEED = (float) StrifePlugin.getInstance().getSettings()
      .getDouble("config.indicators.miss-speed", 80);
  private float IND_GRAVITY_HSPEED = (float) StrifePlugin.getInstance().getSettings()
      .getDouble("config.indicators.gravity-horizontal-speed", 30);
  private float IND_GRAVITY_VSPEED = (float) StrifePlugin.getInstance().getSettings()
      .getDouble("config.indicators.gravity-vertical-speed", 80);
  private Vector IND_FLOAT_VECTOR = new Vector(0, IND_FLOAT_SPEED, 0);
  private Vector IND_MISS_VECTOR = new Vector(0, IND_MISS_SPEED, 0);

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
      plugin.getSneakManager().tempDisableSneak(defender.getEntity());
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
        DamageUtil.doEvasion(attacker.getEntity(), defender.getEntity());
        removeIfExisting(event.getProjectile());
        event.setCancelled(true);
        if (attacker.getEntity() instanceof Player) {
          plugin.getIndicatorManager()
              .addIndicator(attacker.getEntity(), defender.getEntity(),
                  buildMissIndicator((Player) attacker.getEntity()), "&fMiss");
        }
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
        if (attacker.getEntity() instanceof Player) {
          plugin.getIndicatorManager()
              .addIndicator(attacker.getEntity(), defender.getEntity(),
                  buildMissIndicator((Player) attacker.getEntity()), "Blocked");
        }
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

    Set<DamageType> triggeredElements = DamageUtil
        .applyElementalEffects(attacker, defender, damageMap, event.isConsumeEarthRunes());

    float critMult = 0;
    double bonusOverchargeMultiplier = 0;
    if (isCriticalHit(attacker, defender, attackMult,
        event.getAbilityMods(AbilityMod.CRITICAL_CHANCE))) {
      critMult = (attacker.getStat(StrifeStat.CRITICAL_DAMAGE) +
          event.getAbilityMods(AbilityMod.CRITICAL_DAMAGE)) / 100;
    }
    if (attackMult > 0.99) {
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

    rawDamage *= 200 / (200 + plugin.getRageManager().getRage(defender.getEntity()));

    if (event.getProjectile() != null) {
      rawDamage *= DamageUtil.getProjectileMultiplier(attacker, defender);
    }
    rawDamage *= StatUtil.getTenacityMult(defender);
    rawDamage *= StatUtil.getMinionMult(attacker);
    rawDamage += damageMap.getOrDefault(DamageType.TRUE_DAMAGE, 0f);

    boolean isSneakAttack = event.isSneakAttack();
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
      if (sneakEvent.isCancelled()) {
        isSneakAttack = false;
      } else {
        rawDamage += sneakEvent.getSneakAttackDamage();
      }
    }

    double finalDamage = plugin.getBarrierManager().damageBarrier(defender, rawDamage);
    plugin.getBarrierManager().updateShieldDisplay(defender);

    boolean isBleedApplied = false;
    if (damageMap.containsKey(DamageType.PHYSICAL)) {
      isBleedApplied = DamageUtil.attemptBleed(attacker, defender,
          damageMap.get(DamageType.PHYSICAL), attackMult, event.getAbilityMods());
    }

    DamageUtil.doReflectedDamage(defender, attacker, event.getAttackType());
    plugin.getAbilityManager().abilityCast(attacker, TriggerAbilityType.ON_HIT);
    plugin.getAbilityManager().abilityCast(defender, TriggerAbilityType.WHEN_HIT);
    plugin.getSneakManager().tempDisableSneak(attacker.getEntity());

    PlayerDataUtil.sendActionbarDamage(attacker.getEntity(), rawDamage, bonusOverchargeMultiplier,
        critMult, triggeredElements, isBleedApplied, isSneakAttack);
    if (attacker.getEntity() instanceof Player) {
      plugin.getIndicatorManager().addIndicator(attacker.getEntity(), defender.getEntity(),
          buildHitIndicator((Player) attacker.getEntity()),
          String.valueOf((int) Math.ceil(rawDamage)));
    }

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

  private IndicatorData buildHitIndicator(Player player) {
    IndicatorData data = new IndicatorData(new Vector(
        IND_GRAVITY_HSPEED - Math.random() * 2 * IND_GRAVITY_HSPEED,
        IND_GRAVITY_VSPEED * (1 + Math.random()),
        IND_GRAVITY_HSPEED - Math.random() * 2 * IND_GRAVITY_HSPEED),
        IndicatorStyle.GRAVITY);
    data.addOwner(player);
    return data;
  }

  private IndicatorData buildMissIndicator(Player player) {
    IndicatorData data = new IndicatorData(IND_MISS_VECTOR.clone(), IndicatorStyle.GRAVITY);
    data.addOwner(player);
    return data;
  }

  private IndicatorData buildFloatIndicator(Player player) {
    IndicatorData data = new IndicatorData(IND_FLOAT_VECTOR.clone(), IndicatorStyle.FLOAT_UP);
    data.addOwner(player);
    return data;
  }
}
