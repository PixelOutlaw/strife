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

import land.face.strife.StrifePlugin;
import land.face.strife.util.JumpUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class EveryTickTask extends BukkitRunnable {

  private final StrifePlugin plugin;

  public EveryTickTask(StrifePlugin plugin) {
    this.plugin = plugin;
  }

  @Override
  public void run() {
    plugin.getBlockManager().tickHolograms();
    plugin.getBlockManager().tickBlock();
    plugin.getStrifeMobManager().tickFrost();
    for (Player p : Bukkit.getOnlinePlayers()) {
      double hoverPower = JumpUtil.determineHoverPower(p);
      if (hoverPower > 0) {
        Vector bonusVelocity = p.getLocation().getDirection().clone().multiply(0.01);
        bonusVelocity.setY(bonusVelocity.getY() + hoverPower * 0.0005);
        p.setVelocity(p.getVelocity().clone().add(bonusVelocity));
      }
    }
  }
}
