/**
 * The MIT License Copyright (c) 2015 Teal Cube Games
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
import land.face.strife.util.PlayerDataUtil;
import land.face.strife.util.StatUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

@Getter
@Setter
public class DamageManager {

  private final StrifePlugin plugin;



  private final Map<UUID, Double> handledDamages = new HashMap<>();
  private final Map<UUID, String> sourceOfDeath = new HashMap<>();

  public DamageManager(StrifePlugin plugin) {
    this.plugin = plugin;
  }

  public boolean isHandledDamage(Entity entity) {
    return handledDamages.containsKey(entity.getUniqueId());
  }

  public double getHandledDamage(Entity entity) {
    return handledDamages.getOrDefault(entity.getUniqueId(), 0D);
  }

  public double dealDamage(StrifeMob atkMob, StrifeMob defMob, float damage, DamageModifiers modifiers) {
    if (defMob.isInvincible()) {
      return 0;
    }
    if (!modifiers.isBypassBarrier() && defMob.getBarrier() > 0) {
      float dmgVsBarrier = 1 + atkMob.getStat(StrifeStat.DAMAGE_TO_BARRIERS) / 100;
      damage *= dmgVsBarrier;
      damage = defMob.damageBarrier(atkMob, damage);
      damage /= dmgVsBarrier;
    } else {
      defMob.damageBarrier(atkMob, 0);
    }

    damage = doEnergyAbsorb(defMob, damage);
    if (atkMob == defMob) {
      DamageUtil.dealRawDamage(defMob, damage);
      return damage;
    }

    if (defMob.hasTrait(StrifeTrait.BLEEDING_EDGE)) {
      damage *= 0.5F;
      float bleed = damage;
      DamageUtil.applyBleed(atkMob, defMob, bleed, true, true, false);
    }

    handledDamages.put(atkMob.getEntity().getUniqueId(), (double) damage);
    if (damage >= defMob.getEntity().getHealth()) {
      damage = DamageUtil.doPreDeath(defMob, damage);
    }
    if (atkMob.getEntity() instanceof Player) {
      atkMob.bumpCombat(defMob);
      defMob.bumpCombat(null);
      defMob.getEntity().setKiller((Player) atkMob.getEntity());
    }
    if (atkMob.getEntity() instanceof Player && defMob.getEntity() instanceof Player &&
        atkMob.getEntity() != defMob.getEntity()) {
      atkMob.flagPvp();
      defMob.flagPvp();
    }
    if (defMob.getEntity() instanceof Player && atkMob.getMaster() != null &&
        atkMob.getMaster().getEntity() instanceof Player) {
      defMob.flagPvp();
      atkMob.getMaster().flagPvp();
    }
    if (damage >= defMob.getEntity().getHealth() && defMob.isPlayer()) {
      sourceOfDeath.put(defMob.getEntity().getUniqueId(), PlayerDataUtil.getName(atkMob.getEntity()));
    }
    defMob.getEntity().damage(damage);
    handledDamages.remove(atkMob.getEntity().getUniqueId());
    /*
    if (defMob.isPlayer()) {
      float hpPercent = (float) defMob.getLife() / defMob.getMaxLife();
      float amount = 2 * (0.5f - hpPercent);
      BorderEffectUtil.sendBorder((Player) defMob.getEntity(), amount, 100);
    }
    */
    return damage;
  }

  public float doEnergyAbsorb(StrifeMob defender, float damage) {
    if (defender.hasTrait(StrifeTrait.MASOCHISM)) {
      defender.setEnergy(defender.getEnergy() + damage * 0.2f);
    }
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
