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

import com.tealcube.minecraft.bukkit.facecore.utilities.PaletteUtil;
import land.face.dinvy.events.EquipItemEvent;
import land.face.strife.StrifePlugin;
import land.face.strife.util.ItemUtil;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class DeluxeEquipListener implements Listener {

  private final StrifePlugin plugin;
  private final String msg;

  public DeluxeEquipListener(StrifePlugin plugin) {
    this.plugin = plugin;
    msg = PaletteUtil.color(plugin.getSettings().getString("language.generic.level-req"));
  }

  @EventHandler
  public void onItemEquip(EquipItemEvent event) {
    if (plugin.getAbilityIconManager().isAbilityIcon(event.getStack())) {
      event.setCancelled(true);
      return;
    }
    if (!ItemUtil.meetsLevelRequirement(event.getStack(), event.getPlayer().getLevel())) {
      event.setCancelled(true);
      PaletteUtil.sendMessage(event.getPlayer(), msg);
      event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.8f);
    }
  }
}
