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

import info.faceland.strife.util.LogUtil;
import io.pixeloutlaw.minecraft.spigot.config.MasterConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

public class MonsterLimitTask extends BukkitRunnable {

  private final MasterConfiguration settings;

  public MonsterLimitTask(MasterConfiguration settings) {
    this.settings = settings;
  }

  @Override
  public void run() {
    for (World world : Bukkit.getWorlds()) {
      int players = world.getPlayers().size();
      int limit = getWorldLimit(world, players);
      if (limit == -1) {
        continue;
      }
      world.setMonsterSpawnLimit(limit);
      LogUtil.printDebug("Set spawn limit of world " + world.getName() + " to " + limit);
    }
  }

  private int getWorldLimit(World world, int players) {
    int baseWorldLimit = settings
        .getInt("config.custom-spawn-limits." + world.getName() + ".base-spawn-limit", -1);
    if (baseWorldLimit == -1) {
      return -1;
    }
    int bonusPerPlayer = settings
        .getInt("config.custom-spawn-limits." + world.getName() + ".per-player-spawn-limit", 0);
    return baseWorldLimit + bonusPerPlayer * players;
  }
}
