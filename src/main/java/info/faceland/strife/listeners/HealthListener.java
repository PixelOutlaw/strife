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
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
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
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getPlayer();
        Champion champion = plugin.getChampionManager().getChampion(player.getUniqueId());
        updateHealth(player, champion.getAttributeValues());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        Champion champion = plugin.getChampionManager().getChampion(player.getUniqueId());
        updateHealth(player, champion.getAttributeValues());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        Champion champion = plugin.getChampionManager().getChampion(player.getUniqueId());
        updateHealth(player, champion.getAttributeValues());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerItemBreak(PlayerItemBreakEvent event) {
        Player player = event.getPlayer();
        Champion champion = plugin.getChampionManager().getChampion(player.getUniqueId());
        updateHealth(player, champion.getAttributeValues());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        Champion champion = plugin.getChampionManager().getChampion(player.getUniqueId());
        Map<StrifeAttribute, Double> attributeDoubleMap = new HashMap<>();
        for (Map.Entry<StrifeStat, Integer> entry : champion.getLevelMap().entrySet()) {
            for (StrifeAttribute attr : StrifeAttribute.values()) {
                attributeDoubleMap.put(attr, entry.getKey().getAttribute(attr) * entry.getValue());
            }
        }
        for (ItemStack itemStack : player.getEquipment().getArmorContents()) {
            if (itemStack == null || itemStack.getType() == Material.AIR) {
                continue;
            }
            for (StrifeAttribute attr : StrifeAttribute.values()) {
                double val = attributeDoubleMap.containsKey(attr) ? attributeDoubleMap.get(attr) : 0;
                attributeDoubleMap.put(attr, attr.getBaseValue() + val + AttributeHandler.getValue(itemStack, attr));
            }
        }
        if (player.getInventory().getItem(event.getNewSlot()) != null && player.getInventory().getItem(event.getNewSlot()).getType() != Material.AIR) {
            for (StrifeAttribute attr : StrifeAttribute.values()) {
                double val = attributeDoubleMap.containsKey(attr) ? attributeDoubleMap.get(attr) : 0;
                attributeDoubleMap.put(attr, attr.getBaseValue() + val + AttributeHandler.getValue(player.getInventory().getItem(event.getNewSlot())
                        , attr));
            }
        } else {
            for (StrifeAttribute attr : StrifeAttribute.values()) {
                double val = attributeDoubleMap.containsKey(attr) ? attributeDoubleMap.get(attr) : 0;
                attributeDoubleMap.put(attr, attr.getBaseValue() + val);
            }
        }
        updateHealth(player, attributeDoubleMap);
    }

    private void updateHealth(Player player, Map<StrifeAttribute, Double> attributeDoubleMap) {
        if (!attributeDoubleMap.containsKey(StrifeAttribute.HEALTH)) {
            return;
        }
        double newMaxHealth = attributeDoubleMap.get(StrifeAttribute.HEALTH);
        if (player.getHealth() > newMaxHealth) {
            double tempHealth = Math.min(newMaxHealth, player.getMaxHealth()) / 2;
            player.setHealth(tempHealth);
        }
        player.setMaxHealth(newMaxHealth);
        player.setHealthScaled(true);
        player.setHealthScale(player.getMaxHealth());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityRegainHealth(EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof Player) || !(event.getRegainReason() == EntityRegainHealthEvent.RegainReason.REGEN || event
                .getRegainReason() == EntityRegainHealthEvent.RegainReason.SATIATED) || event.isCancelled()) {
            return;
        }
        Player player = (Player) event.getEntity();
        Champion champion = plugin.getChampionManager().getChampion(player.getUniqueId());
        double amount = event.getAmount() + champion.getAttributeValues().get(StrifeAttribute.REGENERATION);
        event.setAmount(amount);
    }

}
