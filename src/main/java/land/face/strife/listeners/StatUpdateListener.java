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
package land.face.strife.listeners;

import land.face.dinvy.events.EquipmentUpdateEvent;
import land.face.strife.StrifePlugin;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public record StatUpdateListener(StrifePlugin plugin) implements Listener {

  @EventHandler(priority = EventPriority.MONITOR)
  public void onPlayerRespawn(final PlayerRespawnEvent event) {
    Bukkit.getScheduler().runTaskLater(plugin, () -> {
      plugin.getChampionManager().update(event.getPlayer());
      plugin.getStatUpdateManager().updateVanillaAttributes(event.getPlayer());
    }, 3L);
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onPlayerJoin(final PlayerJoinEvent event) {
    Bukkit.getScheduler().runTaskLater(plugin, () -> {
      plugin.getStrifeMobManager().updateEquipmentStats(
          plugin.getStrifeMobManager().getStatMob(event.getPlayer()));
      plugin.getStatUpdateManager().updateVanillaAttributes(event.getPlayer());
    }, 10L);
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onEquipmentUpdate(EquipmentUpdateEvent event) {
    plugin.getStrifeMobManager().updateEquipmentStats(
        plugin.getStrifeMobManager().getStatMob(event.getPlayer()));
    plugin.getStatUpdateManager().updateVanillaAttributes(event.getPlayer());
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onWorldChange(PlayerTeleportEvent event) {
    if (event.getTo().getWorld() != event.getFrom().getWorld()) {
      plugin.getStrifeMobManager()
          .updateEquipmentStats(plugin.getStrifeMobManager().getStatMob(event.getPlayer()));
      plugin.getStatUpdateManager().updateVanillaAttributes(event.getPlayer());
    }
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onGamemodeChange(PlayerGameModeChangeEvent event) {
    plugin.getStrifeMobManager()
        .updateEquipmentStats(plugin.getStrifeMobManager().getStatMob(event.getPlayer()));
    plugin.getStatUpdateManager().updateVanillaAttributes(event.getPlayer());
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
