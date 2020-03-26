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
package land.face.strife.events;

import land.face.strife.data.DamageModifiers;
import land.face.strife.data.StrifeMob;
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
  private final DamageModifiers damageModifiers;
  private boolean cancel;

  public StrifeDamageEvent(StrifeMob attacker, StrifeMob defender, DamageModifiers damageModifiers) {
    this.attacker = attacker;
    this.defender = defender;
    this.damageModifiers = damageModifiers;
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

  public DamageModifiers getDamageModifiers() {
    return damageModifiers;
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