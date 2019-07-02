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
package info.faceland.strife.listeners;

import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;
import info.faceland.strife.StrifePlugin;
import info.faceland.strife.data.champion.Champion;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

public class DataListener implements Listener {

  private final StrifePlugin plugin;
  private final static String UNUSED_MESSAGE_1 =
      "&6&lLevelup! You have &f&l{0} &6&lunused Levelpoints!";
  private final static String UNUSED_MESSAGE_2 =
      "&6&lOpen your inventory or use &f&l/levelup &6&lto spend them!";

  public DataListener(StrifePlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onPlayerJoinChampionStuff(final PlayerJoinEvent event) {
    Champion champion = plugin.getChampionManager().getChampion(event.getPlayer());
    plugin.getChampionManager().verifyStatValues(champion);
    if (champion.getUnusedStatPoints() > 0) {
      notifyUnusedPoints(event.getPlayer(), champion.getUnusedStatPoints());
    }
    plugin.getBossBarManager().createSkillBar(champion);
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onPlayerJoinUpdateAttributes(final PlayerJoinEvent event) {
    event.getPlayer().setHealthScaled(false);
    plugin.getAttributeUpdateManager().updateAttributes(event.getPlayer());
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onEntityDeath(final EntityDeathEvent event) {
    plugin.getBossBarManager().doBarDeath(event.getEntity());
    plugin.getUniqueEntityManager().removeEntity(event.getEntity(), false, true);
    plugin.getBarrierManager().removeEntity(event.getEntity());
    if (!(event.getEntity() instanceof Player)) {
      UUID uuid = event.getEntity().getUniqueId();
      Bukkit.getScheduler().runTaskLater(plugin,
          () -> plugin.getAttributedEntityManager().removeEntity(uuid), 20L * 30);
    }
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onPlayerQuit(final PlayerQuitEvent event) {
    plugin.getBossBarManager().removeBar(event.getPlayer().getUniqueId());
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onPlayerKick(final PlayerKickEvent event) {
    plugin.getBossBarManager().removeBar(event.getPlayer().getUniqueId());
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onPlayerRespawn(final PlayerRespawnEvent event) {
    plugin.getBossBarManager().removeBar(event.getPlayer().getUniqueId());
    plugin.getBarrierManager()
        .createBarrierEntry(
            plugin.getAttributedEntityManager().getAttributedEntity(event.getPlayer()));
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onInteract(final PlayerInteractEntityEvent event) {
    if (!(event.getRightClicked() instanceof LivingEntity) || event
        .getRightClicked() instanceof ArmorStand) {
      return;
    }
    if (!event.getRightClicked().isValid() || event.getRightClicked().hasMetadata("NPC")) {
      return;
    }
    final Player player = event.getPlayer();
    final LivingEntity entity = (LivingEntity) event.getRightClicked();
    plugin.getAttributedEntityManager().getAttributedEntity(entity);
    plugin.getBossBarManager()
        .pushBar(player, plugin.getAttributedEntityManager().getAttributedEntity(entity));
  }

  @EventHandler(priority = EventPriority.NORMAL)
  public void onChunkUnload(ChunkUnloadEvent e) {
    for (Entity ent : e.getChunk().getEntities()) {
      if (!(ent instanceof LivingEntity)) {
        continue;
      }
      plugin.getAttributedEntityManager().doChunkDespawn((LivingEntity) ent);
      plugin.getUniqueEntityManager().removeEntity((LivingEntity) ent, true, false);
    }
  }

  @EventHandler(priority = EventPriority.NORMAL)
  public void onInvyClose(InventoryCloseEvent event) {
    if (!plugin.getLevelupMenu().getName().equals(event.getView().getTitle())) {
      return;
    }
    if (!plugin.getChampionManager().hasPendingChanges((Player) event.getPlayer())) {
      return;
    }
    Bukkit.getScheduler().runTaskLater(plugin, () ->
      plugin.getConfirmationMenu().open((Player) event.getPlayer()), 1L);
  }

  private void notifyUnusedPoints(final Player player, final int unused) {
    Bukkit.getScheduler().runTaskLater(plugin, () -> {
      MessageUtils.sendMessage(player, UNUSED_MESSAGE_1.replace("{0}", String.valueOf(unused)));
      MessageUtils.sendMessage(player, UNUSED_MESSAGE_2);
    }, 20L * 5);
  }
}
