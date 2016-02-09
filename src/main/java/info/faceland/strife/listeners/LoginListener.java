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

import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;

import info.faceland.strife.StrifePlugin;
import info.faceland.strife.attributes.AttributeHandler;
import info.faceland.strife.attributes.StrifeAttribute;
import info.faceland.strife.data.Champion;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.logging.Level;

public class LoginListener implements Listener {

    private final StrifePlugin plugin;

    public LoginListener(StrifePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Champion champion = plugin.getChampionManager().getChampion(player.getUniqueId());
        champion.getAttributeValues(true);
        AttributeHandler.updateHealth(player, champion.getCache().getAttribute(StrifeAttribute.HEALTH));
        double perc = champion.getCache().getAttribute(StrifeAttribute.MOVEMENT_SPEED) / 100D;
        float speed = 0.2F * (float) perc;
        player.setWalkSpeed(Math.min(Math.max(-1F, speed), 1F));
        player.setFlySpeed(Math.min(Math.max(-1F, speed), 1F));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Champion champion = plugin.getChampionManager().getChampion(player.getUniqueId());
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new QuitRunnable(plugin, champion), 20L * 10);
    }

    class QuitRunnable implements Runnable {
        StrifePlugin plugin;
        Champion champion;

        QuitRunnable(StrifePlugin plugin, Champion champion) {
            this.plugin = plugin;
            this.champion = champion;
        }

        @Override
        public void run() {
            if (champion.getPlayer() != null && champion.getPlayer().isOnline()) {
                return;
            }
            plugin.getStorage().save(champion);
            plugin.getChampionManager().removeChampion(champion.getUniqueId());
        }
    }

}
