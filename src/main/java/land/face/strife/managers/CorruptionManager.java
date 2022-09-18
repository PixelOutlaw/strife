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

import com.sentropic.guiapi.gui.Alignment;
import com.sentropic.guiapi.gui.GUIComponent;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.timers.CorruptionTimer;
import land.face.strife.util.DamageUtil.OriginLocation;
import land.face.strife.util.LogUtil;
import land.face.strife.util.TargetingUtil;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class CorruptionManager {

  private final StrifePlugin plugin;
  private final float flatCorruptPerTick;
  private final float percentCorruptPerTick;

  private final Map<UUID, CorruptionTimer> corruptMap = new HashMap<>();

  public CorruptionManager(StrifePlugin plugin) {
    this.plugin = plugin;
    flatCorruptPerTick = (float) StrifePlugin.getInstance().getSettings()
        .getDouble("config.mechanics.base-corrupt-loss", 0.15);
    percentCorruptPerTick = 1f - (float) StrifePlugin.getInstance().getSettings()
        .getDouble("config.mechanics.config.mechanics.percent-corrupt-loss", 0.05);
  }

  public boolean addCorruption(StrifeMob mob, float amount, boolean silent) {
    if (!mob.getEntity().isValid()) {
      return false;
    }
    mob.addCorruption(amount);
    if (!silent) {
      mob.getEntity().getWorld().playSound(mob.getEntity().getLocation(),
          Sound.ENTITY_WITHER_SHOOT, 0.7f, 2f);
    }
    mob.getEntity().getWorld().spawnParticle(Particle.SMOKE_NORMAL,
        mob.getEntity().getLocation(), 10, 0.4, 1, 0.5, 0.1);
    if (!corruptMap.containsKey(mob.getEntity().getUniqueId())) {
      corruptMap.put(mob.getEntity().getUniqueId(), new CorruptionTimer(this, mob));
    }
    pushRuneGui(mob, (int) mob.getCorruption());
    return true;
  }

  public void pushRuneGui(StrifeMob mob, int corruption) {
    if (mob.getEntity().getType() != EntityType.PLAYER) {
      return;
    }
    if (corruption < 1) {
      StrifePlugin.getInstance().getGuiManager().updateComponent((Player) mob.getEntity(),
          new GUIComponent("corrupt-display", GuiManager.EMPTY, 0, 0, Alignment.RIGHT));
      StrifePlugin.getInstance().getGuiManager().updateComponent((Player) mob.getEntity(),
          new GUIComponent("corrupt-amount", GuiManager.EMPTY, 0, 0, Alignment.RIGHT));
      return;
    }
    StrifePlugin.getInstance().getGuiManager().updateComponent((Player) mob.getEntity(),
        new GUIComponent("corrupt-display", GuiManager.CORRUPT_ICON, 14, 112, Alignment.CENTER));
    String string = StrifePlugin.getInstance().getGuiManager().convertToEnergyDisplayFont(corruption);
    TextComponent aaa = GuiManager.noShadow(new TextComponent(string));
    StrifePlugin.getInstance().getGuiManager().updateComponent((Player) mob.getEntity(),
        new GUIComponent("corrupt-amount", aaa, string.length() * 8, 113, Alignment.CENTER));
  }

  public float getCorruptionMultiplier(StrifeMob mob) {
    return mob.getCorruption() > 0 ? 1 + 0.01f * mob.getCorruption() : 1;
  }

  public void tickCorruption(StrifeMob mob) {
    float newCorruption = mob.getCorruption() * percentCorruptPerTick;
    newCorruption -= flatCorruptPerTick;
    mob.setCorruption(newCorruption);
    spawnCorruptionParticles(mob.getEntity(), mob.getCorruption());
    pushRuneGui(mob, (int) newCorruption);
  }

  public void spawnCorruptionParticles(LivingEntity target, float corruption) {
    double particleAmount = Math.min(5 + corruption / 3, 30);
    target.getWorld().spawnParticle(Particle.SMOKE_NORMAL,
        TargetingUtil.getOriginLocation(target, OriginLocation.CENTER),
        (int) particleAmount,
        0.4, 0.4, 0.5,
        0.03
    );
  }

  public void endTasks() {
    for (CorruptionTimer timer : corruptMap.values()) {
      timer.cancel();
    }
    corruptMap.clear();
  }

  public void clearCorrupt(LivingEntity entity) {
    clearCorrupt(entity.getUniqueId());
  }

  public void clearCorrupt(UUID uuid) {
    if (corruptMap.containsKey(uuid)) {
      LogUtil.printDebug("Cancelled Corruption - Cleared");
      corruptMap.get(uuid).cancel();
      corruptMap.remove(uuid);
    }
  }
}
