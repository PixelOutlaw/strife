/**
 * The MIT License
 * Copyright (c) 2015 Teal Cube Games
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package info.faceland.strife.listeners;

import com.tealcube.minecraft.bukkit.shade.fanciful.FancyMessage;
import info.faceland.strife.StrifePlugin;
import info.faceland.strife.data.Champion;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class DataListener implements Listener {

    private final StrifePlugin plugin;

    public DataListener(StrifePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(final PlayerJoinEvent event) {
        Champion champion;
        if (!plugin.getChampionManager().hasChampion(event.getPlayer().getUniqueId())) {
            champion = plugin.getChampionManager().getChampion(event.getPlayer().getUniqueId());
            champion.setHighestReachedLevel(event.getPlayer().getLevel());
            champion.setUnusedStatPoints(event.getPlayer().getLevel() * 2);
            plugin.getChampionManager().removeChampion(event.getPlayer().getUniqueId());
            plugin.getChampionManager().addChampion(champion);
        } else {
            champion = plugin.getChampionManager().getChampion(event.getPlayer().getUniqueId());
        }

        if (champion.getUnusedStatPoints() > 0) {
            notifyUnusedPoints(event.getPlayer());
        }
    }

    private void notifyUnusedPoints(final Player player) {
        Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
            @Override
            public void run() {
                FancyMessage message = new FancyMessage("");
                message.then("You have unspent levelpoints! ").color(ChatColor.GOLD).then("CLICK HERE")
                    .command("/levelup").color(ChatColor.WHITE).then(" or use ").color(ChatColor.GOLD)
                    .then("/levelup").color(ChatColor.WHITE).then(" to spend them and raise your stats!").color
                    (ChatColor.GOLD).send(player);
            }
        }, 20L * 2);
    }

}
