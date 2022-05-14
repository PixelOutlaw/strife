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
import land.face.strife.data.DamageModifiers;
import land.face.strife.data.StrifeMob;
import land.face.strife.stats.StrifeStat;
import land.face.strife.stats.StrifeTrait;
import land.face.strife.util.DamageUtil;
import land.face.strife.util.StatUtil;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

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

  public double dealDamage(StrifeMob attacker, StrifeMob defender, float damage, DamageModifiers modifiers) {
    if (defender.isInvincible()) {
      return 0;
    }
    if (!modifiers.isBypassBarrier() && defender.getBarrier() > 0) {
      float dmgVsBarrier = 1 + attacker.getStat(StrifeStat.DAMAGE_TO_BARRIERS) / 100;
      damage *= dmgVsBarrier;
      damage = defender.damageBarrier(damage);
      damage /= dmgVsBarrier;
    } else {
      defender.damageBarrier(0);
    }
    damage = doEnergyAbsorb(defender, damage);
    if (attacker == defender) {
      DamageUtil.dealRawDamage(defender, damage);
      return damage;
    }

    if (defender.hasTrait(StrifeTrait.BLEEDING_EDGE)) {
      damage *= 0.5;
      float bleed = damage;
      DamageUtil.applyBleed(attacker, defender, bleed, true);
    }

    handledDamages.put(attacker.getEntity().getUniqueId(), (double) damage);
    if (damage >= defender.getEntity().getHealth()) {
      damage = DamageUtil.doPreDeath(defender, damage);
    }
    boolean death = damage >= defender.getEntity().getHealth();
    if (attacker.getEntity() instanceof Player) {
      plugin.getBossBarManager().pushBar((Player) attacker.getEntity(), defender, death);
      defender.getEntity().setKiller((Player) attacker.getEntity());
    }
    defender.getEntity().damage(damage);
    handledDamages.remove(attacker.getEntity().getUniqueId());

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
