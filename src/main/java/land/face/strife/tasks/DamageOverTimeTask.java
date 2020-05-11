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
package land.face.strife.tasks;

import static org.bukkit.potion.PotionEffectType.FIRE_RESISTANCE;
import static org.bukkit.potion.PotionEffectType.POISON;
import static org.bukkit.potion.PotionEffectType.WITHER;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import land.face.strife.StrifePlugin;
import land.face.strife.stats.StrifeStat;
import land.face.strife.util.StatUtil;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;

public class DamageOverTimeTask extends BukkitRunnable {

  private StrifePlugin plugin;

  private float WITHER_FLAT_DAMAGE;
  private float BURN_FLAT_DAMAGE;
  private float POISON_FLAT_DAMAGE;
  private float POISON_PERCENT_MAX_HEALTH_DAMAGE;

  private Set<LivingEntity> poisonedMobs = new HashSet<>();
  private Set<LivingEntity> burningMobs = new HashSet<>();
  private Set<LivingEntity> witheredMobs = new HashSet<>();

  public DamageOverTimeTask(StrifePlugin plugin) {
    this.plugin = plugin;
    BURN_FLAT_DAMAGE = (float) plugin.getSettings()
        .getDouble("config.mechanics.burn-flat-damage", 6) / 4;
    WITHER_FLAT_DAMAGE = (float) plugin.getSettings()
        .getDouble("config.mechanics.wither-flat-damage");
    POISON_FLAT_DAMAGE = (float) plugin.getSettings()
        .getDouble("config.mechanics.poison-flat-damage");
    POISON_PERCENT_MAX_HEALTH_DAMAGE = (float) plugin.getSettings()
        .getDouble("config.mechanics.poison-percent-damage");
  }

  public void trackPoison(LivingEntity livingEntity) {
    poisonedMobs.add(livingEntity);
  }

  public void trackBurning(LivingEntity livingEntity) {
    burningMobs.add(livingEntity);
  }

  public void trackWither(LivingEntity livingEntity) {
    witheredMobs.add(livingEntity);
  }

  @Override
  public void run() {
    Iterator poisonIterator = poisonedMobs.iterator();
    while (poisonIterator.hasNext()) {
      LivingEntity le = (LivingEntity) poisonIterator.next();
      if (le == null || !le.isValid() || !le.hasPotionEffect(POISON)) {
        poisonIterator.remove();
        continue;
      }
      int poisonPower = le.getPotionEffect(POISON).getAmplifier() + 1;
      double damage =
          poisonPower * (POISON_FLAT_DAMAGE + le.getMaxHealth() * POISON_PERCENT_MAX_HEALTH_DAMAGE);
      damage *=
          1 - plugin.getStrifeMobManager().getStatMob(le).getStat(StrifeStat.POISON_RESIST) / 100;
      damage *= 0.25;
      if (le.getHealth() <= damage) {
        le.damage(damage);
      } else {
        le.setHealth(le.getHealth() - damage);
      }
    }
    Iterator witherIterator = witheredMobs.iterator();
    while (witherIterator.hasNext()) {
      LivingEntity le = (LivingEntity) witherIterator.next();
      if (le == null || !le.isValid() || !le.hasPotionEffect(WITHER)) {
        witherIterator.remove();
        continue;
      }
      int witherPower = le.getPotionEffect(WITHER).getAmplifier() + 1;
      double damage = witherPower * WITHER_FLAT_DAMAGE;
      damage *=
          1 - plugin.getStrifeMobManager().getStatMob(le).getStat(StrifeStat.WITHER_RESIST) / 100;
      damage *= 0.25;
      if (le.getHealth() <= damage) {
        le.damage(damage);
      } else {
        le.setHealth(le.getHealth() - damage);
      }
    }
    Iterator fireIterator = burningMobs.iterator();
    while (fireIterator.hasNext()) {
      LivingEntity le = (LivingEntity) fireIterator.next();
      if (le == null || !le.isValid() || le.getFireTicks() < 1) {
        fireIterator.remove();
        continue;
      }
      if (le.hasPotionEffect(FIRE_RESISTANCE)) {
        continue;
      }
      float damage = BURN_FLAT_DAMAGE;
      damage *= 1 - StatUtil.getFireResist(plugin.getStrifeMobManager().getStatMob(le)) / 100;
      damage = plugin.getBarrierManager().damageBarrier(
          plugin.getStrifeMobManager().getStatMob(le), damage);
      if (le.getHealth() <= damage) {
        le.damage(damage);
      } else {
        le.setHealth(le.getHealth() - damage);
      }
    }
  }
}
