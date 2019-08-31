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
package info.faceland.strife.events;

import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.util.DamageUtil.AbilityMod;
import info.faceland.strife.util.DamageUtil.AttackType;
import info.faceland.strife.util.DamageUtil.DamageType;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class StrifeDamageEvent extends Event implements Cancellable {

  private static final HandlerList HANDLER_LIST = new HandlerList();

  public static HandlerList getHandlerList() {
    return HANDLER_LIST;
  }

  private double finalDamage;

  private final StrifeMob attacker;
  private final StrifeMob defender;
  private final AttackType attackType;

  private float attackMultiplier = 1f;
  private float healMultiplier = 1f;
  private final Map<DamageType, Float> damageModifiers = new HashMap<>();
  private final Map<DamageType, Float> flatDamageBonuses = new HashMap<>();
  private final Map<AbilityMod, Float> abilityMods = new HashMap<>();
  private boolean isBlocking = false;
  private boolean canBeBlocked = true;
  private boolean canBeEvaded = true;
  private Projectile projectile;
  private String[] extraEffects;
  private boolean cancel;

  public StrifeDamageEvent(StrifeMob attacker, StrifeMob defender, AttackType attackType) {
    this.attacker = attacker;
    this.defender = defender;
    this.attackType = attackType;
  }

  public StrifeDamageEvent(StrifeMob attacker, StrifeMob defender, AttackType attackType, float attackMultiplier) {
    this.attacker = attacker;
    this.defender = defender;
    this.attackType = attackType;
    this.attackMultiplier = attackMultiplier;
  }

  @Override
  public HandlerList getHandlers() {
    return HANDLER_LIST;
  }

  public StrifeMob getAttacker() {
    return attacker;
  }

  public StrifeMob getDefender() {
    return defender;
  }

  public AttackType getAttackType() {
    return attackType;
  }

  public float getAttackMultiplier() {
    return attackMultiplier;
  }

  public void setAttackMultiplier(float attackMultiplier) {
    this.attackMultiplier = attackMultiplier;
  }

  public float getHealMultiplier() {
    return healMultiplier;
  }

  public void setHealMultiplier(float healMultiplier) {
    this.healMultiplier = healMultiplier;
  }

  public boolean isBlocking() {
    return isBlocking;
  }

  public void setBlocking(boolean blocking) {
    isBlocking = blocking;
  }

  public boolean isCanBeBlocked() {
    return canBeBlocked;
  }

  public void setCanBeBlocked(boolean canBeBlocked) {
    this.canBeBlocked = canBeBlocked;
  }

  public boolean isCanBeEvaded() {
    return canBeEvaded;
  }

  public void setCanBeEvaded(boolean canBeEvaded) {
    this.canBeEvaded = canBeEvaded;
  }

  public Projectile getProjectile() {
    return projectile;
  }

  public void setProjectile(Projectile projectile) {
    this.projectile = projectile;
  }

  public String[] getExtraEffects() {
    return extraEffects;
  }

  public void setExtraEffects(String[] extraEffects) {
    this.extraEffects = extraEffects;
  }

  public Map<DamageType, Float> getDamageModifiers() {
    return damageModifiers;
  }

  public Map<DamageType, Float> getFlatDamageBonuses() {
    return flatDamageBonuses;
  }

  public float getDamageMod(DamageType damageType) {
    return damageModifiers.getOrDefault(damageType, 1f);
  }

  public float getFlatDamageBonus(DamageType damageType) {
    return flatDamageBonuses.getOrDefault(damageType, 0f);
  }

  public Map<AbilityMod, Float> getAbilityMods() {
    return abilityMods;
  }

  public float getAbilityMods(AbilityMod mod) {
    return abilityMods.getOrDefault(mod, 0f);
  }

  public double getFinalDamage() {
    return finalDamage;
  }

  public void setFinalDamage(double finalDamage) {
    this.finalDamage = finalDamage;
  }

  public void setCancelled(boolean cancel) {
    this.cancel = cancel;
  }

  public boolean isCancelled() {
    return this.cancel;
  }
}