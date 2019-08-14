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

  private double attackMultiplier = 1;
  private double healMultiplier = 1;
  private final Map<DamageType, Double> damageModifiers = new HashMap<>();
  private final Map<DamageType, Double> flatDamageBonuses = new HashMap<>();
  private final Map<AbilityMod, Double> abilityMods = new HashMap<>();
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

  public StrifeDamageEvent(StrifeMob attacker, StrifeMob defender, AttackType attackType, double attackMultiplier) {
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

  public double getAttackMultiplier() {
    return attackMultiplier;
  }

  public void setAttackMultiplier(double attackMultiplier) {
    this.attackMultiplier = attackMultiplier;
  }

  public double getHealMultiplier() {
    return healMultiplier;
  }

  public void setHealMultiplier(double healMultiplier) {
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

  public Map<DamageType, Double> getDamageModifiers() {
    return damageModifiers;
  }

  public Map<DamageType, Double> getFlatDamageBonuses() {
    return flatDamageBonuses;
  }

  public double getDamageMod(DamageType damageType) {
    return damageModifiers.getOrDefault(damageType, 1D);
  }

  public double getFlatDamageBonus(DamageType damageType) {
    return flatDamageBonuses.getOrDefault(damageType, 0D);
  }

  public Map<AbilityMod, Double> getAbilityMods() {
    return abilityMods;
  }

  public double getAbilityMods(AbilityMod mod) {
    return abilityMods.getOrDefault(mod, 0D);
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