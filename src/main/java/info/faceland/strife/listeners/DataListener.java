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
import info.faceland.strife.data.Champion;
import info.faceland.strife.data.ChampionSaveData;
import info.faceland.strife.stats.StrifeStat;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

public class DataListener implements Listener {

  private final StrifePlugin plugin;
  private final static String RESET_MESSAGE =
      "&a&lYour Levelpoints have been automatically reset due to an update!";
  private final static String UNUSED_MESSAGE_1 =
      "&6&lLevelup! You have &f&l{0} &6&lunused Levelpoints!";
  private final static String UNUSED_MESSAGE_2 =
      "&6&lOpen your inventory or use &f&l/levelup &6&lto spend them!";

  public DataListener(StrifePlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onPlayerJoin(final PlayerJoinEvent event) {
    if (!plugin.getChampionManager().hasChampion(event.getPlayer().getUniqueId())) {
      ChampionSaveData saveData = plugin.getStorage().load(event.getPlayer().getUniqueId());
      if (getChampionLevelpoints(saveData) != event.getPlayer().getLevel()) {
        notifyResetPoints(event.getPlayer());
        for (StrifeStat stat : plugin.getStatManager().getStats()) {
          saveData.setLevel(stat, 0);
        }
        saveData.setHighestReachedLevel(event.getPlayer().getLevel());
        saveData.setUnusedStatPoints(event.getPlayer().getLevel());
      }
      plugin.getChampionManager().addChampion(new Champion(event.getPlayer(), saveData));
    }
    Champion champion = plugin.getChampionManager().getChampion(event.getPlayer().getUniqueId());
    if (champion.getUnusedStatPoints() > 0) {
      notifyUnusedPoints(event.getPlayer(), champion.getUnusedStatPoints());
    }
    plugin.getBarrierManager()
        .createBarrierEntry(plugin.getEntityStatCache().getAttributedEntity(event.getPlayer()));
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onEntityDeath(final EntityDeathEvent event) {
    plugin.getUniqueEntityManager().removeEntity(event.getEntity(), false, true);
    plugin.getBarrierManager().removeEntity(event.getEntity());
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onPlayerRespawn(final PlayerRespawnEvent event) {
    plugin.getBarrierManager()
        .createBarrierEntry(plugin.getEntityStatCache().getAttributedEntity(event.getPlayer()));
  }

  @EventHandler(priority = EventPriority.NORMAL)
  public void onChunkUnload(ChunkUnloadEvent e) {
    for (Entity ent : e.getChunk().getEntities()) {
      if (!(ent instanceof LivingEntity)) {
        continue;
      }
      if (plugin.getUniqueEntityManager().getLiveUniquesMap().containsKey(ent)) {
        plugin.getUniqueEntityManager().removeEntity((LivingEntity) ent, true, false);
        ent.remove();
      }
    }
  }

  private void notifyUnusedPoints(final Player player, final int unused) {
    Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
      @Override
      public void run() {
        MessageUtils.sendMessage(player, UNUSED_MESSAGE_1.replace("{0}", String.valueOf(unused)));
        MessageUtils.sendMessage(player, UNUSED_MESSAGE_2);
      }
    }, 20L * 5);
  }

  private void notifyResetPoints(final Player player) {
    Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
      @Override
      public void run() {
        MessageUtils.sendMessage(player, RESET_MESSAGE);
      }
    }, 20L * 3);
  }

  private int getChampionLevelpoints(ChampionSaveData championSaveData) {
    int total = championSaveData.getUnusedStatPoints();
    for (StrifeStat stat : championSaveData.getLevelMap().keySet()) {
      total += championSaveData.getLevel(stat);
    }
    return total;
  }
}
