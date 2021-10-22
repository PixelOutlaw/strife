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
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.PlayerInventory;

public record StatUpdateListener(StrifePlugin plugin) implements Listener {

  @EventHandler(priority = EventPriority.MONITOR)
  public void onPlayerRespawn(final PlayerRespawnEvent event) {
    Bukkit.getScheduler().runTaskLater(plugin, () -> {
      plugin.getChampionManager().update(event.getPlayer());
      plugin.getStatUpdateManager().updateVanillaAttributes(event.getPlayer());
    }, 3L);
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onInventoryClose(InventoryCloseEvent event) {
    if (!((OfflinePlayer) event.getPlayer()).isOnline()) {
      return;
    }
    InventoryView inventoryView = event.getView();
    if (!(inventoryView.getTopInventory() instanceof PlayerInventory) &&
        !(inventoryView.getBottomInventory() instanceof PlayerInventory)) {
      return;
    }
    PlayerInventory playerInventory;
    if (inventoryView.getTopInventory() instanceof PlayerInventory) {
      playerInventory = (PlayerInventory) inventoryView.getTopInventory();
    } else {
      playerInventory = (PlayerInventory) inventoryView.getBottomInventory();
    }
    HumanEntity humanEntity = playerInventory.getHolder();
    if (!(humanEntity instanceof Player player)) {
      return;
    }
    if (player.isDead() || player.getHealth() <= 0D) {
      return;
    }
    plugin.getStrifeMobManager().updateEquipmentStats(plugin.getStrifeMobManager()
        .getStatMob(event.getPlayer()));
    plugin.getStatUpdateManager().updateVanillaAttributes(player);
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onPlayerQuit(PlayerQuitEvent event) {
    Bukkit.getScheduler().runTaskLater(plugin,
        () -> plugin.getStrifeMobManager().removeStrifeMob(event.getPlayer()), 1L);
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onPlayerKick(PlayerKickEvent event) {
    Bukkit.getScheduler().runTaskLater(plugin,
        () -> plugin.getStrifeMobManager().removeStrifeMob(event.getPlayer()), 1L);
  }
}
