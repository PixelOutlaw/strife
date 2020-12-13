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
package land.face.strife.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.stats.StrifeTrait;
import land.face.strife.util.StatUtil;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

public class DamageManager {

  private final StrifePlugin plugin;
  private final Map<UUID, Double> handledDamages = new HashMap<>();

  public DamageManager(StrifePlugin plugin) {
    this.plugin = plugin;
  }

  public boolean isHandledDamage(Entity entity) {
    return handledDamages.containsKey(entity.getUniqueId());
  }

  public double getHandledDamage(Entity entity) {
    return handledDamages.getOrDefault(entity.getUniqueId(), 0D);
  }

  public double dealDamage(StrifeMob attacker, StrifeMob defender, float damage) {
    damage = Math.max(0.002f, defender.damageBarrier(damage));
    damage = doEnergyAbsorb(defender, damage);
    if (attacker == defender) {
      if (damage > defender.getEntity().getHealth()) {
        defender.getEntity().damage(damage);
        return damage;
      }
      defender.getEntity().setHealth(defender.getEntity().getHealth() - damage);
      return damage;
    }
    int noDamageTicks = defender.getEntity().getNoDamageTicks();
    Vector velocity = defender.getEntity().getVelocity();
    defender.getEntity().setNoDamageTicks(0);

    handledDamages.put(attacker.getEntity().getUniqueId(), (double) damage);
    defender.getEntity().damage(damage, attacker.getEntity());
    handledDamages.remove(attacker.getEntity().getUniqueId());

    defender.getEntity().setNoDamageTicks(noDamageTicks);
    defender.getEntity().setVelocity(velocity);

    return damage;
  }

  public float doEnergyAbsorb(StrifeMob defender, float damage) {
    if (!defender.hasTrait(StrifeTrait.ENERGY_ABSORB)) {
      return damage;
    }
    float maxAbsorb = damage * 0.2f;
    float energy = defender.getEnergy();
    StatUtil.changeEnergy(defender, -maxAbsorb);
    if (energy > maxAbsorb) {
      return damage - maxAbsorb;
    }
    return damage - energy;
  }
}
