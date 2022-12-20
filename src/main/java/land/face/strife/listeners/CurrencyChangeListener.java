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
package land.face.strife.listeners;

import com.sentropic.guiapi.gui.Alignment;
import com.sentropic.guiapi.gui.GUIComponent;
import com.tealcube.minecraft.bukkit.bullion.MoneyChangeEvent;
import land.face.strife.StrifePlugin;
import land.face.strife.managers.GuiManager;
import org.black_ixx.playerpoints.event.PlayerPointsChangeEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.nunnerycode.mint.MintPlugin;

public record CurrencyChangeListener(StrifePlugin plugin) implements Listener {

  @EventHandler
  public void OnJoin(PlayerJoinEvent event) {
    int money = (int) MintPlugin.getInstance().getManager().getPlayerBalance(event.getPlayer().getUniqueId());
    plugin.getGuiManager().updateComponent(event.getPlayer(), new GUIComponent("money-display",
        plugin.getGuiManager().convertToMoneyFont(money), divideAndConquerLength(money) * 7, 189, Alignment.RIGHT));

    int gems = plugin.getPlayerPointsPlugin().getAPI().look(event.getPlayer().getUniqueId());
    plugin.getGuiManager().updateComponent(event.getPlayer(), new GUIComponent("gem-display",
        plugin.getGuiManager().convertToGemFont(gems), divideAndConquerLength(gems) * 7, 189, Alignment.RIGHT));
  }

  @EventHandler
  public void onMoneyChange(MoneyChangeEvent event) {
    Player player = Bukkit.getPlayer(event.getPlayer());
    if (player == null || !player.isOnline()) {
      return;
    }
    int money = (int) MintPlugin.getInstance().getManager().getPlayerBalance(player.getUniqueId());
    plugin.getGuiManager().updateComponent(player, new GUIComponent("money-display",
        plugin.getGuiManager().convertToMoneyFont(money), divideAndConquerLength(money) * 7, 189, Alignment.RIGHT));
  }

  @EventHandler
  public void onGemChange(PlayerPointsChangeEvent event) {
    Player player = Bukkit.getPlayer(event.getPlayerId());
    if (player == null || !player.isOnline()) {
      return;
    }
    int gems = plugin.getPlayerPointsPlugin().getAPI().look(player.getUniqueId());
    gems += event.getChange();
    plugin.getGuiManager().updateComponent(player, new GUIComponent("gem-display",
        plugin.getGuiManager().convertToGemFont(gems), divideAndConquerLength(gems) * 7, 189, Alignment.RIGHT));
  }

  // This ugly function is surprisingly the most efficient way
  // to determine the length of an integer...
  // https://www.baeldung.com/java-number-of-digits-in-int
  public static int divideAndConquerLength(int number) {
    if (number < 100000) {
      if (number < 100) {
        if (number < 10) {
          return 1;
        } else {
          return 2;
        }
      } else {
        if (number < 1000) {
          return 3;
        } else {
          if (number < 10000) {
            return 4;
          } else {
            return 5;
          }
        }
      }
    } else {
      if (number < 10000000) {
        if (number < 1000000) {
          return 6;
        } else {
          return 7;
        }
      } else {
        if (number < 100000000) {
          return 8;
        } else {
          if (number < 1000000000) {
            return 9;
          } else {
            return 10;
          }
        }
      }
    }
  }
}
