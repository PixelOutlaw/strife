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

import static info.faceland.strife.stats.StrifeStat.BARRIER;

import info.faceland.strife.StrifePlugin;
import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.util.StatUtil;
import java.util.Map.Entry;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;

public class BarrierTask extends BukkitRunnable {

  private StrifePlugin plugin;

  public BarrierTask(StrifePlugin plugin) {
    this.plugin = plugin;
  }

  @Override
  public void run() {
    for (Entry<UUID, Double> entry : plugin.getBarrierManager().getBarrierMap().entrySet()) {
      if (plugin.getBarrierManager().getTickMap().containsKey(entry.getKey())) {
        plugin.getBarrierManager().tickEntity(entry.getKey());
        continue;
      }
      LivingEntity entity = (LivingEntity) Bukkit.getEntity(entry.getKey());
      if (entity == null || !entity.isValid()) {
        plugin.getBarrierManager().removeEntity(entry.getKey());
        continue;
      }
      StrifeMob player = plugin.getStrifeMobManager().getAttributedEntity(entity);
      if (player.getStat(BARRIER) < 0.1) {
        plugin.getBarrierManager().removeEntity(entry.getKey());
        continue;
      }
      if (entry.getValue() >= player.getStat(BARRIER)) {
        continue;
      }
      // Restore this amount per barrier tick (4 MC ticks, 0.2s)
      double barrierGain = StatUtil.getBarrierPerSecond(player) / 5;
      plugin.getBarrierManager().restoreBarrier(player, barrierGain);
    }
  }
}
