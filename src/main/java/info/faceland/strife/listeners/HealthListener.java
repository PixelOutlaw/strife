/******************************************************************************
 * Copyright (c) 2014, Richard Harrah                                         *
 *                                                                            *
 * Permission to use, copy, modify, and/or distribute this software for any purpose with or without fee is hereby granted, provided that the above copyright notice and this permission notice appear in all copies.
 *                                                                            *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 ******************************************************************************/

package info.faceland.strife.listeners;

import info.faceland.strife.StrifePlugin;
import info.faceland.strife.attributes.AttributeHandler;
import info.faceland.strife.attributes.StrifeAttribute;
import info.faceland.strife.data.Champion;
import info.faceland.strife.stats.StrifeStat;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class HealthListener implements Listener {

    private final StrifePlugin plugin;

    public HealthListener(StrifePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {
        for (HumanEntity entity : event.getViewers()) {
            if (entity instanceof Player) {
                Player player = (Player) event.getPlayer();
                Champion champion = plugin.getChampionManager().getChampion(player.getUniqueId());
                AttributeHandler.updateHealth(player, champion.getAttributeValues());
            }
        }
        if (event.getPlayer() instanceof Player) {
            Player player = (Player) event.getPlayer();
            Champion champion = plugin.getChampionManager().getChampion(player.getUniqueId());
            AttributeHandler.updateHealth(player, champion.getAttributeValues());
        }
        if (event.getInventory().getHolder() instanceof Player) {
            Player player = (Player) event.getInventory().getHolder();
            Champion champion = plugin.getChampionManager().getChampion(player.getUniqueId());
            AttributeHandler.updateHealth(player, champion.getAttributeValues());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryOpen(InventoryOpenEvent event) {
        for (HumanEntity entity : event.getViewers()) {
            if (entity instanceof Player) {
                Player player = (Player) event.getPlayer();
                Champion champion = plugin.getChampionManager().getChampion(player.getUniqueId());
                AttributeHandler.updateHealth(player, champion.getAttributeValues());
            }
        }
        if (event.getPlayer() instanceof Player) {
            Player player = (Player) event.getPlayer();
            Champion champion = plugin.getChampionManager().getChampion(player.getUniqueId());
            AttributeHandler.updateHealth(player, champion.getAttributeValues());
        }
        if (event.getInventory().getHolder() instanceof Player) {
            Player player = (Player) event.getInventory().getHolder();
            Champion champion = plugin.getChampionManager().getChampion(player.getUniqueId());
            AttributeHandler.updateHealth(player, champion.getAttributeValues());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        Champion champion = plugin.getChampionManager().getChampion(player.getUniqueId());
        AttributeHandler.updateHealth(player, champion.getAttributeValues());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        Champion champion = plugin.getChampionManager().getChampion(player.getUniqueId());
        AttributeHandler.updateHealth(player, champion.getAttributeValues());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        Champion champion = plugin.getChampionManager().getChampion(player.getUniqueId());
        AttributeHandler.updateHealth(player, champion.getAttributeValues());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerItemBreak(PlayerItemBreakEvent event) {
        Player player = event.getPlayer();
        Champion champion = plugin.getChampionManager().getChampion(player.getUniqueId());
        AttributeHandler.updateHealth(player, champion.getAttributeValues());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        Champion champion = plugin.getChampionManager().getChampion(player.getUniqueId());
        Map<StrifeAttribute, Double> attributeDoubleMap = new HashMap<>();
        for (StrifeAttribute attr : StrifeAttribute.values()) {
            attributeDoubleMap.put(attr, attr.getBaseValue());
        }
        for (Map.Entry<StrifeStat, Integer> entry : champion.getLevelMap().entrySet()) {
            for (StrifeAttribute attr : StrifeAttribute.values()) {
                double val = attributeDoubleMap.containsKey(attr) ? attributeDoubleMap.get(attr) : 0;
                attributeDoubleMap.put(attr, val + entry.getKey().getAttribute(attr) * entry.getValue());
            }
        }
        for (ItemStack itemStack : champion.getPlayer().getEquipment().getArmorContents()) {
            if (itemStack == null || itemStack.getType() == Material.AIR) {
                continue;
            }
            for (StrifeAttribute attr : StrifeAttribute.values()) {
                double val = attributeDoubleMap.containsKey(attr) ? attributeDoubleMap.get(attr) : 0;
                attributeDoubleMap.put(attr, val + AttributeHandler.getValue(itemStack, attr));
            }
        }
        if (champion.getPlayer().getInventory().getItem(event.getNewSlot()) != null
            && champion.getPlayer().getInventory().getItem(event.getNewSlot()).getType() != Material.AIR) {
            ItemStack itemStack = champion.getPlayer().getInventory().getItem(event.getNewSlot());
            for (StrifeAttribute attr : StrifeAttribute.values()) {
                if (attr == StrifeAttribute.ARMOR || attr == StrifeAttribute.DAMAGE_REFLECT || attr == StrifeAttribute.EVASION
                    || attr == StrifeAttribute.HEALTH || attr == StrifeAttribute.REGENERATION) {
                    continue;
                }
                double val = attributeDoubleMap.containsKey(attr) ? attributeDoubleMap.get(attr) : 0;
                attributeDoubleMap.put(attr, val + AttributeHandler.getValue(itemStack, attr));
            }
        }
        AttributeHandler.updateHealth(player, attributeDoubleMap);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityRegainHealth(EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof Player) || !(event.getRegainReason() == EntityRegainHealthEvent.RegainReason.REGEN || event
                                                                                                                                         .getRegainReason() ==
                                                                                                                                 EntityRegainHealthEvent.RegainReason.SATIATED) ||
            event.isCancelled()) {
            return;
        }
        Player player = (Player) event.getEntity();
        Champion champion = plugin.getChampionManager().getChampion(player.getUniqueId());
        double amount = champion.getAttributeValues().get(StrifeAttribute.REGENERATION);
        event.setAmount(amount);
    }

}
