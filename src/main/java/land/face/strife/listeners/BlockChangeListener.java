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

import com.sentropic.guiapi.gui.Alignment;
import com.sentropic.guiapi.gui.GUIComponent;
import java.util.List;
import land.face.strife.StrifePlugin;
import land.face.strife.events.BlockChangeEvent;
import land.face.strife.events.RuneChangeEvent;
import land.face.strife.managers.GuiManager;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class BlockChangeListener implements Listener {

  private final StrifePlugin plugin;

  public BlockChangeListener(StrifePlugin plugin) {
    this.plugin = plugin;
  }

  private final List<TextComponent> blockLevels = List.of(
      new TextComponent("௦"),
      new TextComponent("௧"),
      new TextComponent("௨"),
      new TextComponent("௩"),
      new TextComponent("௪"),
      new TextComponent("௫"),
      new TextComponent("௬"),
      new TextComponent("௭"),
      new TextComponent("௮"),
      new TextComponent("௯")
  );

  @EventHandler
  public void onBlockChange(BlockChangeEvent event) {
    if (!(event.getMob().getEntity() instanceof Player player)) {
      return;
    }
    double maxBlock = event.getMob().getMaxBlock();
    if (maxBlock > 20) {
      int blockStage = 0;
      if (event.getMob().getBlock() > 0) {
        blockStage = 1 + (int) (9 * event.getMob().getBlock() / event.getMob().getMaxBlock());
      }
      if (blockStage < 10) {
        plugin.getGuiManager().updateComponent(player,
            new GUIComponent("block-ind", blockLevels.get(blockStage), 20, -120, Alignment.CENTER));
      } else {
        plugin.getGuiManager().updateComponent(player,
            new GUIComponent("block-ind", GuiManager.EMPTY, 0, 0, Alignment.CENTER));
      }
    } else {
      plugin.getGuiManager().updateComponent(player,
          new GUIComponent("block-ind", GuiManager.EMPTY, 0, 0, Alignment.CENTER));
    }
  }
}
