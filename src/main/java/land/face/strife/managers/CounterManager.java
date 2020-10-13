/**
 * The MIT License Copyright (c) 2015 Teal Cube Games
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package land.face.strife.managers;

import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import land.face.strife.StrifePlugin;
import land.face.strife.data.CounterData;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.TargetResponse;
import land.face.strife.managers.IndicatorManager.IndicatorStyle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class CounterManager {

  private final Map<LivingEntity, Set<CounterData>> counterMap = new WeakHashMap<>();
  private StrifePlugin plugin;
  private Sound counterSound;
  private float pitch;

  public CounterManager(StrifePlugin plugin) {
    this.plugin = plugin;
    counterSound = Sound.valueOf(plugin.getSettings()
        .getString("config.mechanics.counter.sound", "ENCHANT_THORNS_HIT"));
    pitch = (float) plugin.getSettings().getDouble("config.mechanics.counter.pitch", 1);
  }

  public void clearCounters(LivingEntity livingEntity) {
    counterMap.remove(livingEntity);
  }

  public void addCounter(LivingEntity livingEntity, CounterData counterData) {
    if (!counterMap.containsKey(livingEntity)) {
      counterMap.put(livingEntity, new HashSet<>());
    }
    counterMap.get(livingEntity).add(counterData);
  }

  public boolean executeCounters(LivingEntity attacker, LivingEntity defender) {
    if (!counterMap.containsKey(defender)) {
      return false;
    }
    boolean isCountered = false;
    Iterator<CounterData> it = counterMap.get(defender).iterator();
    List<CounterData> removeData = new ArrayList<>();
    while (it.hasNext()) {
      CounterData data = it.next();
      if (System.currentTimeMillis() > data.getEndTime()) {
        removeData.add(data);
        continue;
      }
      isCountered = true;
      defender.getWorld().playSound(defender.getLocation(), counterSound, 1.0f, pitch);
      if (attacker instanceof Player) {
        StrifePlugin.getInstance().getIndicatorManager().addIndicator(attacker, defender,
            IndicatorStyle.BOUNCE, 6, "&3&o&lCounter!");
      }
      if (defender instanceof Player) {
        MessageUtils.sendActionBar((Player) defender, "&e&lCountered!");
      }
      if (!data.isTriggered()) {
        StrifeMob defenderMob = plugin.getStrifeMobManager().getStatMob(defender);
        Set<LivingEntity> entities = new HashSet<>();
        entities.add(attacker);
        TargetResponse response = new TargetResponse(entities);
        plugin.getEffectManager().processEffectList(defenderMob, response, data.getEffects());
        if (data.isRemoveOnTrigger()) {
          counterMap.get(defender).remove(data);
        } else {
          data.setTriggered(true);
        }
        break;
      }
    }
    counterMap.get(defender).removeAll(removeData);
    return isCountered;
  }
}
