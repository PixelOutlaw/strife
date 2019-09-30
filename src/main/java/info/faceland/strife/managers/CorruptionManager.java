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

import info.faceland.strife.StrifePlugin;
import info.faceland.strife.timers.CorruptionTimer;
import info.faceland.strife.util.DamageUtil.OriginLocation;
import info.faceland.strife.util.LogUtil;
import info.faceland.strife.util.TargetingUtil;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;

public class CorruptionManager {

  private StrifePlugin plugin;

  private Map<UUID, CorruptionTimer> corruptionMap = new ConcurrentHashMap<>();

  public CorruptionManager(StrifePlugin plugin) {
    this.plugin = plugin;
  }

  public boolean isCorrupted(LivingEntity entity) {
    return getCorruption(entity) != 0;
  }

  public float getCorruption(LivingEntity entity) {
    if (corruptionMap.containsKey(entity.getUniqueId())) {
      return corruptionMap.get(entity.getUniqueId()).getStacks();
    }
    return 0;
  }

  public void applyCorruption(LivingEntity entity, float amount) {
    if (!entity.isValid()) {
      return;
    }
    if (corruptionMap.containsKey(entity.getUniqueId())) {
      corruptionMap.get(entity.getUniqueId()).bumpCorrupt(amount);
      return;
    }
    corruptionMap.put(entity.getUniqueId(), new CorruptionTimer(plugin, entity, amount));
  }

  public float getCorruptionMult(LivingEntity entity) {
    if (corruptionMap.containsKey(entity.getUniqueId())) {
      return 1 + 0.02f * corruptionMap.get(entity.getUniqueId()).getStacks();
    }
    return 1;
  }

  public void endCorruptTasks() {
    for (CorruptionTimer timer : corruptionMap.values()) {
      timer.cancel();
    }
    corruptionMap.clear();
  }

  public void clearCorrupt(LivingEntity entity) {
    clearCorrupt(entity.getUniqueId());
  }

  public void clearCorrupt(UUID uuid) {
    if (corruptionMap.containsKey(uuid)) {
      LogUtil.printDebug("Cancelled CorruptionTimer - Cleared");
      corruptionMap.get(uuid).cancel();
      corruptionMap.remove(uuid);
    }
  }

  public void spawnCorruptionParticles(LivingEntity target, float corruption) {
    double particleAmount = Math.min(5 + corruption / 3, 30);
    target.getWorld().spawnParticle(Particle.SMOKE_NORMAL,
        TargetingUtil.getOriginLocation(target, OriginLocation.CENTER),
        (int) particleAmount,
        0.4, 0.4, 0.5,
        0.03
    );
  }
}
