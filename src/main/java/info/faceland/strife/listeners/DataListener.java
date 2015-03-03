/*
 * This file is part of Strife, licensed under the ISC License.
 *
 * Copyright (c) 2014 Richard Harrah
 *
 * Permission to use, copy, modify, and/or distribute this software for any purpose with or without fee is hereby granted,
 * provided that the above copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING ALL
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT,
 * INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF
 * THIS SOFTWARE.
 */
package info.faceland.strife.listeners;

import com.tealcube.minecraft.bukkit.kern.fanciful.FancyMessage;
import info.faceland.strife.StrifePlugin;
import info.faceland.strife.data.Champion;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class DataListener implements Listener {

    private final StrifePlugin plugin;

    public DataListener(StrifePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(final PlayerJoinEvent event) {
        if (!plugin.getChampionManager().hasChampion(event.getPlayer().getUniqueId())) {
            Champion champion = plugin.getChampionManager().getChampion(event.getPlayer().getUniqueId());
            champion.setHighestReachedLevel(event.getPlayer().getLevel());
            champion.setUnusedStatPoints(event.getPlayer().getLevel());
            plugin.getChampionManager().removeChampion(event.getPlayer().getUniqueId());
            plugin.getChampionManager().addChampion(champion);
            if (event.getPlayer().getLevel() > 0) {
                Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                    @Override
                    public void run() {
                        FancyMessage message = new FancyMessage("");
                        message.then("Your stats have been reset! ").color(ChatColor.GOLD).then("Click here").command("/levelup")
                                .color(ChatColor.WHITE).then(" or use ").color(ChatColor.GOLD).then("/levelup")
                                .color(ChatColor.WHITE).then(" to spend them.").color(ChatColor.GOLD).send(event.getPlayer());
                    }
                }, 20L * 2);
            }
        } else {
            Champion champion = plugin.getChampionManager().getChampion(event.getPlayer().getUniqueId());
            if (champion.getUnusedStatPoints() > 0) {
                Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                    @Override
                    public void run() {
                        FancyMessage message = new FancyMessage("");
                        message.then("You have unspent levelup points. ").color(ChatColor.GOLD).then("Click here").command("/levelup")
                                .color(ChatColor.WHITE).then(" or use ").color(ChatColor.GOLD).then("/levelup")
                                .color(ChatColor.WHITE).then(" to spend them.").color(ChatColor.GOLD).send(event.getPlayer());
                    }
                }, 20L * 2);
            }
        }
    }

}
