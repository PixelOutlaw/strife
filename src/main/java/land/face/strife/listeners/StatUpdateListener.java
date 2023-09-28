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

import com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor;
import com.tealcube.minecraft.bukkit.facecore.utilities.TextUtils;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.util.ArrayList;
import java.util.List;
import land.face.dinvy.events.EquipmentUpdateEvent;
import land.face.dinvy.windows.equipment.EquipmentMenu.DeluxeSlot;
import land.face.strife.StrifePlugin;
import land.face.strife.data.LoadedMount;
import land.face.strife.util.ItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;

public record StatUpdateListener(StrifePlugin plugin) implements Listener {

  @EventHandler(priority = EventPriority.MONITOR)
  public void onPlayerRespawn(final PlayerRespawnEvent event) {
    Bukkit.getScheduler().runTaskLater(plugin, () -> {
      plugin.getChampionManager().getChampion(event.getPlayer()).recombineCache();
      plugin.getStatUpdateManager().updateAllAttributes(event.getPlayer());
    }, 3L);
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onPlayerJoin(final PlayerJoinEvent event) {
    Bukkit.getScheduler().runTaskLater(plugin, () -> {
      plugin.getStrifeMobManager().updateEquipmentStats(
          plugin.getStrifeMobManager().getStatMob(event.getPlayer()));
      plugin.getStatUpdateManager().updateAllAttributes(event.getPlayer());
    }, 10L);
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onEquipmentUpdate(EquipmentUpdateEvent event) {
    plugin.getStrifeMobManager().updateEquipmentStats(
        plugin.getStrifeMobManager().getStatMob(event.getPlayer()));
    plugin.getStatUpdateManager().updateAllAttributes(event.getPlayer());
    if (event.isHard()) {
      plugin.getPlayerMountManager().updateSelectedMount(event.getPlayer());
      ItemStack mountStack = event.getData().getEquipmentItem(DeluxeSlot.MOUNT);
      if (mountStack != null && mountStack.getType() == Material.SADDLE) {
        int data = ItemUtil.getCustomData(mountStack);
        LoadedMount loadedMount = plugin.getPlayerMountManager().getLoadedMountFromData(data);
        if (loadedMount != null) {
          ItemStackExtensionsKt.setDisplayName(mountStack, loadedMount.getName());
          List<String> lore = new ArrayList<>(loadedMount.getLore());
          if (!loadedMount.isCanBeTraded()) {
            lore.add("");
            lore.add(FaceColor.TRUE_WHITE + "傝");
          }
          TextUtils.setLore(mountStack, lore);
        }
      }
    }
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onWorldChange(PlayerTeleportEvent event) {
    if (event.getTo().getWorld() != event.getFrom().getWorld()) {
      plugin.getStrifeMobManager()
          .updateEquipmentStats(plugin.getStrifeMobManager().getStatMob(event.getPlayer()));
      plugin.getStatUpdateManager().updateAllAttributes(event.getPlayer());
    }
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onGamemodeChange(PlayerGameModeChangeEvent event) {
    plugin.getStrifeMobManager().updateEquipmentStats(
        plugin.getStrifeMobManager().getStatMob(event.getPlayer()));
    plugin.getStatUpdateManager().updateAllAttributes(event.getPlayer());
    plugin.getAttackSpeedManager().resetAttack(
        plugin.getStrifeMobManager().getStatMob(event.getPlayer()), 1);
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
