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

import info.faceland.strife.StrifePlugin;
import info.faceland.strife.attributes.AttributeHandler;
import info.faceland.strife.attributes.StrifeAttribute;
import info.faceland.strife.data.Champion;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.PlayerInventory;

public class AttributeUpdateListener implements Listener {

    private final StrifePlugin plugin;

    public AttributeUpdateListener(StrifePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerRespawn(final PlayerRespawnEvent event) {
        Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
            @Override
            public void run() {
                updateAttributes(event.getPlayer());
            }
        }, 20L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!((OfflinePlayer) event.getPlayer()).isOnline()) {
            return;
        }
        InventoryView inventoryView = event.getView();
        if (!(inventoryView.getTopInventory() instanceof PlayerInventory) && !(inventoryView.getBottomInventory()
                instanceof PlayerInventory)) {
            return;
        }
        PlayerInventory playerInventory;
        if (inventoryView.getTopInventory() instanceof PlayerInventory) {
            playerInventory = (PlayerInventory) inventoryView.getTopInventory();
        } else {
            playerInventory = (PlayerInventory) inventoryView.getBottomInventory();
        }
        HumanEntity humanEntity = playerInventory.getHolder();
        if (!(humanEntity instanceof Player)) {
            return;
        }
        Player player = (Player) humanEntity;
        if (player.isDead() || player.getHealth() <= 0D) {
            return;
        }
        updateAttributes(player);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        updateAttributes(event.getPlayer());
    }

    public void updateAttributes(Player p) {
        Champion champion = plugin.getChampionManager().getChampion(p.getUniqueId());
        champion.getAttributeValues(true);
        double maxHealth = champion.getCache().getAttribute(StrifeAttribute.HEALTH);
        AttributeHandler.updateHealth(p, maxHealth, champion.trueHealthDisplay());
        double perc = champion.getCache().getAttribute(StrifeAttribute.MOVEMENT_SPEED) / 100D;
        float speed = 0.2F * (float) perc;
        p.setWalkSpeed(Math.min(Math.max(-1F, speed), 1F));
        p.setFlySpeed(Math.min(Math.max(-1F, speed / 1.5f), 1F));
        p.getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(1000);
        p.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(200);
    }



}
