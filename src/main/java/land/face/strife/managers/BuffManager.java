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

import java.util.HashMap;
import java.util.Map;
import land.face.strife.data.buff.Buff;
import land.face.strife.data.buff.LoadedBuff;
import land.face.strife.stats.StrifeStat;
import land.face.strife.util.StatUtil;
import org.bukkit.configuration.ConfigurationSection;

public class BuffManager {

  private final Map<String, LoadedBuff> buffList = new HashMap<>();

  public LoadedBuff getBuffFromId(String buffId) {
    return buffList.get(buffId);
  }

  public Buff buildFromLoadedBuff(String buffId) {
    return buildFromLoadedBuff(getBuffFromId(buffId));
  }

  public Buff buildFromLoadedBuff(LoadedBuff loadedBuff) {
    return new Buff(loadedBuff.getId(), loadedBuff.getStats(), loadedBuff.getMaxStacks());
  }

  public void loadBuff(String key, ConfigurationSection cs) {
    ConfigurationSection statsSection = cs.getConfigurationSection("stats");
    Map<StrifeStat, Float> statsMap = StatUtil.getStatMapFromSection(statsSection);
    int maxStacks = cs.getInt("max-stacks", 1);
    int durationSeconds = cs.getInt("duration-seconds", 10);
    LoadedBuff loadedBuff = new LoadedBuff(key, statsMap, maxStacks, durationSeconds);
    buffList.put(key, loadedBuff);
  }
}
