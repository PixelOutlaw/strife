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
package land.face.strife.managers;

import java.util.HashSet;
import java.util.Set;
import land.face.strife.StrifePlugin;
import land.face.strife.data.HerdLocation;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public class HerdManager {

  @Getter
  private final Set<HerdLocation> herdLocationSet = new HashSet<>();

  public HerdManager(StrifePlugin plugin) {
    for (String key : plugin.getHerdYML().getKeys(false)) {
      try {
        String world = plugin.getHerdYML().getString(key + ".location.world");
        double x = plugin.getHerdYML().getDouble(key + ".location.x");
        double y = plugin.getHerdYML().getDouble(key + ".location.y");
        double z = plugin.getHerdYML().getDouble(key + ".location.z");
        Location location = new Location(Bukkit.getServer().getWorld(world), x, y, z);
        Set<String> uniques = new HashSet<>(plugin.getHerdYML().getStringList(key + ".uniques"));
        double range = plugin.getHerdYML().getDouble(key + ".range");

        herdLocationSet.add(new HerdLocation(key, location, range, uniques));
      } catch (Exception e) {
        Bukkit.getLogger().info("[Strife] Error loading herd loc " + key);
        e.printStackTrace();
      }
    }
    int index = 0;
    for (HerdLocation hl : herdLocationSet) {
      index++;
      Bukkit.getScheduler().runTaskTimer(plugin, hl::runCheck, 100L, 2 * 20L + index);
    }
  }
}
