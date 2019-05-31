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
package info.faceland.strife.tasks;

import info.faceland.strife.managers.BarrierManager;
import info.faceland.strife.managers.BleedManager;
import java.util.Map.Entry;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;

public class BleedTask extends BukkitRunnable {

  private final BleedManager bleedManager;
  private final BarrierManager barrierManager;
  private final double percentBleedPerTick;
  private final double baseBleedPerTick;

  public BleedTask(BleedManager bleedManager, BarrierManager barrierManager,
      double baseBleedPerTick, double percentBleedPerTick) {
    this.bleedManager = bleedManager;
    this.barrierManager = barrierManager;
    this.baseBleedPerTick = baseBleedPerTick;
    this.percentBleedPerTick = percentBleedPerTick;
  }

  @Override
  public void run() {
    for (Entry<LivingEntity, Double> entry : bleedManager.getBleedMap().entrySet()) {
      LivingEntity bleedingEntity = entry.getKey();
      double bleedAmount = entry.getValue();
      if (!bleedingEntity.isValid()) {
        bleedManager.removeEntity(bleedingEntity);
        continue;
      }

      barrierManager.interruptBarrier(bleedingEntity);
      double bleedDamage = baseBleedPerTick + bleedAmount * percentBleedPerTick;

      bleedManager.applyDamage(bleedingEntity, bleedDamage);
      bleedManager.spawnBleedParticles(bleedingEntity, bleedDamage);

      if (bleedManager.getBleedOnEntity(bleedingEntity) <= 0) {
        bleedManager.removeEntity(bleedingEntity);
      }
    }
  }
}
