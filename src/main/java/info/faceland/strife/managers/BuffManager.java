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

import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.data.buff.Buff;
import info.faceland.strife.data.buff.LoadedBuff;
import info.faceland.strife.stats.StrifeStat;
import info.faceland.strife.util.LogUtil;
import info.faceland.strife.util.StatUtil;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.configuration.ConfigurationSection;

public class BuffManager {

  private final Map<String, LoadedBuff> buffList = new HashMap<>();

  public void applyBuff(String buffId, StrifeMob strifeMob) {
    LoadedBuff loadedBuff = getBuffFromId(buffId);
    if (loadedBuff == null) {
      LogUtil.printWarning("Attempted to apply unknown buff '" + buffId + "'");
      return;
    }
    applyBuff(loadedBuff, strifeMob);
  }

  public void applyBuff(LoadedBuff loadedBuff, StrifeMob strifeMob) {
    Buff buff = LoadedBuff.createBuffFromLoadedBuff(loadedBuff);
    strifeMob.addBuff(loadedBuff.getName(), buff, loadedBuff.getSeconds());
  }

  public LoadedBuff getBuffFromId(String buffId) {
    return buffList.get(buffId);
  }

  public void loadBuff(String key, ConfigurationSection cs) {
    ConfigurationSection statsSection = cs.getConfigurationSection("stats");
    Map<StrifeStat, Double> statsMap = StatUtil.getStatMapFromSection(statsSection);
    int maxStacks = cs.getInt("max-stacks", 1);
    int durationSeconds = cs.getInt("duration-seconds", 10);
    LoadedBuff loadedBuff = new LoadedBuff(key, statsMap, maxStacks, durationSeconds);
    buffList.put(key, loadedBuff);
  }
}
