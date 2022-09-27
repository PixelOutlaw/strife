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

import com.sentropic.guiapi.gui.Alignment;
import com.sentropic.guiapi.gui.GUIComponent;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.stats.StrifeStat;
import land.face.strife.timers.RuneTimer;
import land.face.strife.util.LogUtil;
import lombok.Getter;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class RuneManager {

  private final StrifePlugin plugin;
  @Getter
  private final Map<UUID, RuneTimer> runeTimers = new HashMap<>();

  public RuneManager(StrifePlugin plugin) {
    this.plugin = plugin;
  }

  public void endTasks() {
    for (RuneTimer timer : runeTimers.values()) {
      timer.updateHolos();
      timer.cancel();
    }
    runeTimers.clear();
  }

  public void clearRunes(LivingEntity entity) {
    clearRunes(entity.getUniqueId());
  }

  public void clearRunes(UUID uuid) {
    if (runeTimers.containsKey(uuid)) {
      LogUtil.printDebug("Cancelled Runez - Cleared");
      runeTimers.get(uuid).clearHolos();
      runeTimers.get(uuid).cancel();
      runeTimers.remove(uuid);
    }
  }

  public void bumpRunes(StrifeMob mob) {
    if (mob.getStat(StrifeStat.EARTH_DAMAGE) < 1) {
      return;
    }
    mob.setEarthRunes(mob.getEarthRunes() + 1);
  }

  public void pushRuneGui(StrifeMob mob, int runes) {
    if (runes < 1) {
      StrifePlugin.getInstance().getGuiManager().updateComponent((Player) mob.getEntity(),
          new GUIComponent("rune-display", GuiManager.EMPTY, 0, 0, Alignment.RIGHT));
      return;
    }
    StrifePlugin.getInstance().getGuiManager().updateComponent((Player) mob.getEntity(),
        new GUIComponent("rune-display", GuiManager.noShadow(new TextComponent(StringUtils.repeat("㆞", runes))), runes * 5, -88, Alignment.LEFT));
  }
}
