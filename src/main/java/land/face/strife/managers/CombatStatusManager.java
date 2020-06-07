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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import land.face.strife.StrifePlugin;
import land.face.strife.data.champion.Champion;
import land.face.strife.data.champion.LifeSkillType;
import org.bukkit.entity.Player;

public class CombatStatusManager {

  private final StrifePlugin plugin;
  private final Map<Player, Integer> tickMap = new ConcurrentHashMap<>();

  private static final int SECONDS_TILL_EXPIRY = 8;

  public CombatStatusManager(StrifePlugin plugin) {
    this.plugin = plugin;
  }

  public boolean isInCombat(Player player) {
    return tickMap.containsKey(player);
  }

  public void addPlayer(Player player) {
    tickMap.put(player, SECONDS_TILL_EXPIRY);
  }

  public void tickCombat() {
    for (Player player : tickMap.keySet()) {
      if (!player.isOnline() || !player.isValid()) {
        tickMap.remove(player);
        continue;
      }
      int ticksLeft = tickMap.get(player);
      if (ticksLeft < 1) {
        doExitCombat(player);
        tickMap.remove(player);
        continue;
      }
      tickMap.put(player, ticksLeft - 1);
    }
  }

  public void doExitCombat(Player player) {
    if (!tickMap.containsKey(player)) {
      return;
    }
    Champion champion = plugin.getChampionManager().getChampion(player);
    if (champion.getDetailsContainer().getExpValues() == null) {
      return;
    }
    for (LifeSkillType type : champion.getDetailsContainer().getExpValues().keySet()) {
      plugin.getSkillExperienceManager().addExperience(player, type,
          champion.getDetailsContainer().getExpValues().get(type), false, false);
    }
    champion.getDetailsContainer().clearAll();
  }
}
