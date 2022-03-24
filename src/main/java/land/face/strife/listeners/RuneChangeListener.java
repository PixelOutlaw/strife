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
package land.face.strife.listeners;

import land.face.strife.StrifePlugin;
import land.face.strife.events.RuneChangeEvent;
import land.face.strife.timers.RuneTimer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class RuneChangeListener implements Listener {

  private final StrifePlugin plugin;

  public RuneChangeListener(StrifePlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler
  public void onRuneChange(RuneChangeEvent event) {
    if (event.getHolder().getEntity() instanceof Player) {
      plugin.getRuneManager().pushRuneGui(event.getHolder(), event.getNewValue());
    }
    if (event.getNewValue() == 0) {
      plugin.getRuneManager().clearRunes(event.getHolder().getEntity().getUniqueId());
      return;
    }
    RuneTimer runeTimer;
    if (!plugin.getRuneManager().getRuneTimers()
        .containsKey(event.getHolder().getEntity().getUniqueId())) {
      runeTimer = new RuneTimer(plugin.getRuneManager(), event.getHolder());
      plugin.getRuneManager().getRuneTimers()
          .put(event.getHolder().getEntity().getUniqueId(), runeTimer);
    } else {
      runeTimer = plugin.getRuneManager().getRuneTimers()
          .get(event.getHolder().getEntity().getUniqueId());
      runeTimer.bumpTime();
    }
    runeTimer.updateHolos();
  }
}
