/**
 * The MIT License Copyright (c) 2015 Teal Cube Games
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package info.faceland.strife.tasks;

import info.faceland.strife.data.ContinuousParticle;
import info.faceland.strife.data.UniqueEntity;
import info.faceland.strife.effects.SpawnParticle;
import info.faceland.strife.managers.UniqueEntityManager;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;

public class ParticleTask extends BukkitRunnable {

  private UniqueEntityManager uniqueEntityManager;
  private static Map<LivingEntity, Queue<ContinuousParticle>> continuousParticles = new ConcurrentHashMap<>();

  public ParticleTask(UniqueEntityManager uniqueEntityManager) {
    this.uniqueEntityManager = uniqueEntityManager;
  }

  @Override
  public void run() {
    for (LivingEntity le : continuousParticles.keySet()) {
      if (!le.isValid()) {
        continuousParticles.remove(le);
        continue;
      }
      for (ContinuousParticle particle : continuousParticles.get(le)) {
        if (particle.getTicksRemaining() < 1) {
          continuousParticles.get(le).remove();
          continue;
        }
        particle.getParticle().playAtLocation(le);
        particle.tickDown();
      }
    }
    for (LivingEntity livingEntity : uniqueEntityManager.getLiveUniquesMap().keySet()) {
      UniqueEntity unique = uniqueEntityManager.getLivingUnique(livingEntity);
      if (!livingEntity.isValid() || unique == null || unique.getParticle() == null) {
        continue;
      }
      Location location = livingEntity.getLocation();
      location.getWorld().spawnParticle(
          unique.getParticle(),
          location,
          unique.getParticleCount(),
          unique.getParticleRadius(), unique.getParticleRadius(), unique.getParticleRadius(),
          0.02f
      );
    }
  }

  public static void addContinuousParticle(LivingEntity livingEntity, SpawnParticle particle, int ticks) {
    if (!continuousParticles.containsKey(livingEntity)) {
      continuousParticles.put(livingEntity, new ConcurrentLinkedQueue<>());
      return;
    }
    continuousParticles.get(livingEntity).add(new ContinuousParticle(particle, ticks));
  }
}
