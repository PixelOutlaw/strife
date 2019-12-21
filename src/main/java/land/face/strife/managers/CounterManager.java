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

import static land.face.strife.listeners.StrifeDamageListener.buildMissIndicator;

import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;
import io.netty.util.internal.ConcurrentSet;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import land.face.strife.StrifePlugin;
import land.face.strife.data.CounterData;
import land.face.strife.data.StrifeMob;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class CounterManager {

  private final Map<UUID, ConcurrentSet<CounterData>> counterMap = new HashMap<>();
  private StrifePlugin plugin;
  private Sound counterSound;
  private float pitch;

  public CounterManager(StrifePlugin plugin) {
    this.plugin = plugin;
    counterSound = Sound.valueOf(plugin.getSettings()
        .getString("config.mechanics.counter.sound", "ENCHANT_THORNS_HIT"));
    pitch = (float) plugin.getSettings().getDouble("config.mechanics.counter.pitch", 1);
  }

  public void clearCounters(UUID uuid) {
    counterMap.remove(uuid);
  }

  public void addCounter(UUID uuid, CounterData counterData) {
    if (!counterMap.containsKey(uuid)) {
      counterMap.put(uuid, new ConcurrentSet<>());
    }
    counterMap.get(uuid).add(counterData);
  }

  public boolean executeCounters(LivingEntity attacker, LivingEntity defender) {
    if (!counterMap.containsKey(defender.getUniqueId())) {
      return false;
    }
    boolean isCountered = false;
    for (CounterData data : counterMap.get(defender.getUniqueId())) {
      if (System.currentTimeMillis() > data.getEndTime()) {
        counterMap.get(defender.getUniqueId()).remove(data);
        continue;
      }
      isCountered = true;
      defender.getWorld().playSound(defender.getLocation(), counterSound, 1.0f, pitch);
      if (attacker instanceof Player) {
        StrifePlugin.getInstance().getIndicatorManager().addIndicator(attacker,
            defender, buildMissIndicator((Player) attacker), "&f&lCounter!");
      }
      if (defender instanceof Player) {
        MessageUtils.sendActionBar((Player) defender, "&e&lCountered!");
      }
      if (!data.isTriggered()) {
        StrifeMob defenderMob = plugin.getStrifeMobManager().getStatMob(defender);
        plugin.getEffectManager().execute(defenderMob, attacker, data.getEffects());
        data.setTriggered(true);
        break;
      }
    }
    return isCountered;
  }
}
