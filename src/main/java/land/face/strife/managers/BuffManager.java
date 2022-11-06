/**
 * The MIT License Copyright (c) 2015 Teal Cube Games
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package land.face.strife.managers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import land.face.strife.StrifePlugin;
import land.face.strife.data.LoreAbility;
import land.face.strife.data.buff.LoadedBuff;
import land.face.strife.data.effects.BuffEffect;
import land.face.strife.stats.StrifeStat;
import land.face.strife.stats.StrifeTrait;
import land.face.strife.util.StatUtil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

public class BuffManager {

  private final Map<String, LoadedBuff> buffList = new HashMap<>();

  public LoadedBuff getBuffFromId(String buffId) {
    return buffList.get(buffId);
  }

  public Set<String> getLoadedBuffIds() {
    return buffList.keySet();
  }

  public void loadBuff(String key, ConfigurationSection cs) {
    Map<StrifeStat, Float> statsMap = StatUtil.getStatMapFromSection(cs.getConfigurationSection("stats"));
    int maxStacks = cs.getInt("max-stacks", 1);
    int durationSeconds = cs.getInt("duration-seconds", 10);
    String tag = cs.getString("action-bar-tag", "");
    LoadedBuff loadedBuff = new LoadedBuff(key, statsMap, tag, maxStacks, durationSeconds);
    List<String> traits = cs.getStringList("traits");
    List<String> loreAbilities = cs.getStringList("lore-abilities");
    Bukkit.getScheduler().runTaskLater(StrifePlugin.getInstance(), () -> {
      for (String s : traits) {
        try {
          loadedBuff.getTraits().add(StrifeTrait.valueOf(s));
        } catch (Exception e) {
          Bukkit.getLogger().warning("[Strife] (Buff) Unknown trait " + s);
        }
      }
      for (String s : loreAbilities) {
        LoreAbility la = StrifePlugin.getInstance().getLoreAbilityManager().getLoreAbilityFromId(s);
        if (la == null) {
          Bukkit.getLogger().warning("[Strife] (Buff) Unknown lore ability " + s);
        } else {
          loadedBuff.getLoreAbilities().add(la);
        }
      }
    }, 10L);
    buffList.put(key, loadedBuff);
  }
}
