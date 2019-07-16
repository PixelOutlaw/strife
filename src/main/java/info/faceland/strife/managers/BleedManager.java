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
package info.faceland.strife.managers;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.material.MaterialData;

public class BleedManager {

  private Map<LivingEntity, Double> bleedMap = new ConcurrentHashMap<>();

  public Map<LivingEntity, Double> getBleedMap() {
    return bleedMap;
  }

  public boolean isBleeding(LivingEntity livingEntity) {
    return getBleedOnEntity(livingEntity) != -1;
  }

  public double getBleedOnEntity(LivingEntity entity) {
    return bleedMap.getOrDefault(entity, -1D);
  }

  public void removeEntity(LivingEntity entity) {
    bleedMap.remove(entity);
  }

  public void applyBleed(LivingEntity livingEntity, double amount) {
    if (!livingEntity.isValid()) {
      return;
    }
    if (!bleedMap.containsKey(livingEntity)) {
      bleedMap.put(livingEntity, amount);
      return;
    }
    bleedMap.put(livingEntity, bleedMap.get(livingEntity) + amount);
  }

  public void spawnBleedParticles(LivingEntity livingEntity, double damage) {
    int particleAmount = 10 + (int) (damage * 20);
    livingEntity.getWorld().spawnParticle(
        Particle.BLOCK_CRACK,
        livingEntity.getEyeLocation().clone().add(0, -0.7, 0),
        particleAmount,
        0.0, 0.0, 0.0,
        new MaterialData(Material.REDSTONE_WIRE)
    );
  }

  public void applyDamage(LivingEntity livingEntity, double damage) {
    if (livingEntity.getHealth() > damage) {
      livingEntity.setHealth(livingEntity.getHealth() - damage);
      bleedMap.replace(livingEntity, bleedMap.get(livingEntity) - damage);
      return;
    }
    bleedMap.replace(livingEntity, 0D);
    livingEntity.damage(damage);
  }
}
