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
package land.face.strife.timers;

import java.util.UUID;
import land.face.strife.StrifePlugin;
import land.face.strife.util.LogUtil;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;

public class CorruptionTimer extends BukkitRunnable {

  private StrifePlugin plugin;

  private final LivingEntity entity;
  private final UUID mobUuid;
  private float stacksRemaining;
  private int invalidTicks = 0;

  private static float flatCorruptPerTick = (float) StrifePlugin.getInstance().getSettings()
      .getDouble("config.mechanics.base-corrupt-loss", 0.15);
  private static float percentCorruptPerTick = 1f - (float) StrifePlugin.getInstance().getSettings()
      .getDouble("config.mechanics.config.mechanics.percent-corrupt-loss", 0.05);

  public CorruptionTimer(StrifePlugin plugin, LivingEntity entity, float ticksRemaining) {
    this.plugin = plugin;
    this.entity = entity;
    this.mobUuid = entity.getUniqueId();
    this.stacksRemaining = ticksRemaining;
    LogUtil.printDebug("New CorruptionTimer created for " + mobUuid);
    runTaskTimer(StrifePlugin.getInstance(), 0L, 8L);
  }

  @Override
  public void run() {
    if (entity == null || !entity.isValid()) {
      invalidTicks++;
      if (invalidTicks > 2000) {
        LogUtil.printDebug("CorruptionTimer cancelled due to invalid entity");
        plugin.getCorruptionManager().clearCorrupt(mobUuid);
      }
      return;
    }

    stacksRemaining = stacksRemaining * percentCorruptPerTick - flatCorruptPerTick;

    if (stacksRemaining <= 0) {
      LogUtil.printDebug("Corruption complete, removing");
      plugin.getCorruptionManager().clearCorrupt(mobUuid);
      return;
    }

    plugin.getCorruptionManager().spawnCorruptionParticles(entity, stacksRemaining);
  }

  public float getStacks() {
    return stacksRemaining;
  }

  public void bumpCorrupt(float amount) {
    stacksRemaining += amount;
  }
}
