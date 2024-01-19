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

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.WeakHashMap;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.Union;
import org.bukkit.Bukkit;

public class UnionManager {

  private final StrifePlugin plugin;
  private final Map<StrifeMob, Union> unions = new WeakHashMap<>();

  public UnionManager(StrifePlugin plugin) {
    this.plugin = plugin;
    Bukkit.getScheduler().runTaskTimer(plugin, this::tickUnions, 23L, 1L);
  }

  //TODO: break this into the union object on creation
  public void tickUnions() {
    Iterator<Entry<StrifeMob, Union>> unionIterator = unions.entrySet().iterator();
    while (unionIterator.hasNext()) {
      Entry<StrifeMob, Union> e = unionIterator.next();
      e.getValue().tick();
      if (e.getValue().canCancel()) {
        unionIterator.remove();
        if (e.getValue().getLoadedBuff() != null) {
          e.getKey().removeBuff(e.getValue().getLoadedBuff().getId(), e.getKey().getEntity().getUniqueId());
        }
        continue;
      }
      if (e.getValue().getLoadedBuff() != null) {
        e.getKey().addBuff(e.getValue().getLoadedBuff(), e.getKey().getEntity().getUniqueId(), 5);
      }
    }
  }

  public void activateUnion(StrifeMob mob, String modelId, int ticks) {
    if (hasActiveUnion(mob)) {
      return;
    }
    ActiveModel model = ModelEngineAPI.createActiveModel(modelId);
    if (model == null) {
      Bukkit.getLogger().warning("[Strife] Failed to create union animation! No model!" + modelId);
      return;
    }
    Union union = new Union();
    union.setId(modelId);
    union.setUnionManager(this);
    union.setTicksRemaining(ticks);
    union.setActiveModel(model);
    union.build(mob.getEntity(), mob.getEntity().getLocation());
    unions.put(mob, union);
  }

  public void endUnion(StrifeMob mob, String modelId) {
    if (hasActiveUnion(mob, modelId)) {
      return;
    }
    unions.get(mob).setTicksRemaining(-1);
  }

  public void playUnionAnimation(StrifeMob mob, String animation, float speed, int lockTicks) {
    if (!hasActiveUnion(mob)) {
      return;
    }
    if (lockTicks > 0) {
      ModeledEntity me = unions.get(mob).getModeledEntity();
      me.setModelRotationLocked(true);
      Bukkit.getScheduler().runTaskLater(plugin, () -> me.setModelRotationLocked(false), lockTicks);
    }
    unions.get(mob).getActiveModel().getAnimationHandler().playAnimation(animation, 0.05, 0.05, speed, false);
  }

  public boolean hasActiveUnion(StrifeMob mob) {
    return unions.containsKey(mob);
  }

  public boolean hasActiveUnion(StrifeMob mob, String id) {
    return hasActiveUnion(mob) && id.equals(unions.get(mob).getId());
  }
}
