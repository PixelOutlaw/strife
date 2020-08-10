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
package land.face.strife.tasks;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import land.face.strife.data.ContinuousParticle;
import land.face.strife.data.effects.StrifeParticle;
import land.face.strife.util.TargetingUtil;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;

public class ParticleTask extends BukkitRunnable {

  private final Map<LivingEntity, Set<ContinuousParticle>> continuousParticles = new WeakHashMap<>();
  private final Map<LivingEntity, StrifeParticle> boundParticles = new WeakHashMap<>();
  private static int tick = 0;

  @Override
  public void run() {
    tick++;
    if (tick > 360) {
      tick = 1;
    }

    for (LivingEntity entity : continuousParticles.keySet()) {
      if (!entity.isValid() || continuousParticles.get(entity).isEmpty()) {
        continuousParticles.remove(entity);
        continue;
      }
      doContinuousParticles(entity);
    }

    for (LivingEntity le : boundParticles.keySet()) {
      if (!le.isValid()) {
        boundParticles.remove(le);
        continue;
      }
      StrifeParticle particle = boundParticles.get(le);
      particle.applyAtLocation(null, TargetingUtil.getOriginLocation(le, particle.getOrigin()));
    }
  }

  public void addContinuousParticle(LivingEntity livingEntity, StrifeParticle particle, int ticks) {
    if (!continuousParticles.containsKey(livingEntity)) {
      continuousParticles.put(livingEntity, new HashSet<>());
    }
    if (particleUpdate(particle.getId(), ticks, continuousParticles.get(livingEntity))) {
      return;
    }
    continuousParticles.get(livingEntity).add(new ContinuousParticle(particle, ticks));
  }

  private boolean particleUpdate(String id, int ticks, Set<ContinuousParticle> particles) {
    for (ContinuousParticle particle : particles) {
      if (particle.getParticle().getId().equals(id)) {
        particle.setTicksRemaining(Math.max(ticks, particle.getTicksRemaining()));
        return true;
      }
    }
    return false;
  }

  public void addParticle(LivingEntity livingEntity, StrifeParticle particle) {
    if (particle == null) {
      return;
    }
    boundParticles.put(livingEntity, particle);
  }

  public void clearParticles() {
    continuousParticles.clear();
  }

  public static int getCurrentTick() {
    return tick;
  }

  private void doContinuousParticles(LivingEntity le) {
    Iterator<ContinuousParticle> iterator = continuousParticles.get(le).iterator();
    while (iterator.hasNext()) {
      ContinuousParticle cParticle = iterator.next();
      if (cParticle.getTicksRemaining() < 1) {
        iterator.remove();
        continue;
      }
      cParticle.getParticle().applyAtLocation(null, TargetingUtil.getOriginLocation(le,
          cParticle.getParticle().getOrigin()));
      cParticle.setTicksRemaining(cParticle.getTicksRemaining() - 1);
    }
  }
}
