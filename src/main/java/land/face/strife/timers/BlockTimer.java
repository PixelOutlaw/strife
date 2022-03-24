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

import java.lang.ref.WeakReference;
import java.util.UUID;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.managers.BlockManager;
import land.face.strife.managers.CorruptionManager;
import org.bukkit.scheduler.BukkitRunnable;

public class BlockTimer extends BukkitRunnable {

  private final WeakReference<StrifeMob> target;
  private final UUID savedUUID;
  private final BlockManager blockManager;

  public BlockTimer(BlockManager blockManager, StrifeMob mob) {
    this.target = new WeakReference<>(mob);
    this.blockManager = blockManager;
    savedUUID = mob.getEntity().getUniqueId();
    runTaskTimer(StrifePlugin.getInstance(), 0L, BlockManager.BLOCK_TICK);
  }

  @Override
  public void run() {
    StrifeMob mob = target.get();
    if (mob == null || mob.getEntity() == null || !mob.getEntity().isValid()) {
      blockManager.clearBlock(savedUUID);
      return;
    }
    blockManager.tickBlock(mob);
    if (mob.getBlock() >= mob.getMaxBlock()) {
      mob.setBlock(mob.getMaxBlock());
      blockManager.clearBlock(savedUUID);
    }
  }
}
