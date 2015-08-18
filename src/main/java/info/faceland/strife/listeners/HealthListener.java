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
import info.faceland.strife.stats.StrifeStat;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;

public class HealthListener implements Listener {

    private final StrifePlugin plugin;

    public HealthListener(StrifePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        Champion champion = plugin.getChampionManager().getChampion(player.getUniqueId());
        Map<StrifeAttribute, Double> attributeDoubleMap = champion.getAttributeValues();
        AttributeHandler.updateHealth(player, attributeDoubleMap);
        double perc = attributeDoubleMap.get(StrifeAttribute.MOVEMENT_SPEED) / 100D;
        float speed = 0.2F * (float) perc;
        player.setWalkSpeed(Math.min(Math.max(-1F, speed), 1F));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerRespawn(final PlayerRespawnEvent event) {
        Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
            @Override
            public void run() {
                Player player = event.getPlayer();
                Champion champion = plugin.getChampionManager().getChampion(player.getUniqueId());
                Map<StrifeAttribute, Double> attributeDoubleMap = champion.getAttributeValues();
                AttributeHandler.updateHealth(player, attributeDoubleMap);
                double perc = attributeDoubleMap.get(StrifeAttribute.MOVEMENT_SPEED) / 100D;
                float speed = 0.2F * (float) perc;
                player.setWalkSpeed(Math.min(Math.max(-1F, speed), 1F));
            }
        }, 20L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (player.getHealth() > 0) {
            Champion champion = plugin.getChampionManager().getChampion(player.getUniqueId());
            Map<StrifeAttribute, Double> attributeDoubleMap = champion.getAttributeValues();
            AttributeHandler.updateHealth(player, attributeDoubleMap);
            double perc = attributeDoubleMap.get(StrifeAttribute.MOVEMENT_SPEED) / 100D;
            float speed = 0.2F * (float) perc;
            player.setWalkSpeed(Math.min(Math.max(-1F, speed), 1F));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerItemBreak(PlayerItemBreakEvent event) {
        Player player = event.getPlayer();
        Champion champion = plugin.getChampionManager().getChampion(player.getUniqueId());
        Map<StrifeAttribute, Double> attributeDoubleMap = champion.getAttributeValues();
        AttributeHandler.updateHealth(player, attributeDoubleMap);
        double perc = attributeDoubleMap.get(StrifeAttribute.MOVEMENT_SPEED) / 100D;
        float speed = 0.2F * (float) perc;
        player.setWalkSpeed(Math.min(Math.max(-1F, speed), 1F));
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
        boolean spam = false;
        for (ItemStack itemStack : champion.getPlayer().getEquipment().getArmorContents()) {
            if (itemStack == null || itemStack.getType() == Material.AIR) {
                continue;
            }
            if (!AttributeHandler.meetsLevelRequirement(player, itemStack)) {
                spam = true;
                continue;
            }
            for (StrifeAttribute attr : StrifeAttribute.values()) {
                double val = attributeDoubleMap.containsKey(attr) ? attributeDoubleMap.get(attr) : 0;
                attributeDoubleMap.put(attr, val + AttributeHandler.getValue(itemStack, attr));
            }
        }
        if (player.getInventory().getItem(event.getNewSlot()) != null
            && player.getInventory().getItem(event.getNewSlot()).getType() != Material.AIR) {
            ItemStack itemStack = player.getInventory().getItem(event.getNewSlot());
            if (AttributeHandler.meetsLevelRequirement(player, itemStack)) {
                for (StrifeAttribute attr : StrifeAttribute.values()) {
                    if (attr == StrifeAttribute.ARMOR || attr == StrifeAttribute.DAMAGE_REFLECT
                            || attr == StrifeAttribute.EVASION
                            || attr == StrifeAttribute.HEALTH || attr == StrifeAttribute.REGENERATION || attr ==
                            StrifeAttribute.MOVEMENT_SPEED
                            || attr == StrifeAttribute.XP_GAIN) {
                        continue;
                    }
                    double val = attributeDoubleMap.get(attr);
                    attributeDoubleMap.put(attr, attr.getCap() > 0D ? Math
                            .min(val + AttributeHandler.getValue(itemStack, attr), attr.getCap())
                            : val + AttributeHandler.getValue(itemStack, attr));
                }
            } else {
                spam = true;
            }
        }
        if (spam) {
            MessageUtils.sendMessage(player,
                    "<red>You don't meet the level requirement for one or more pieces of equipment! You will not " +
                            "receive stats from those items!");
        }
        AttributeHandler.updateHealth(player, attributeDoubleMap);
        double perc = attributeDoubleMap.get(StrifeAttribute.MOVEMENT_SPEED) / 100D;
        float speed = 0.2F * (float) perc;
        player.setWalkSpeed(Math.min(Math.max(-1F, speed), 1F));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityRegainHealth(EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof Player) ||
            !(event.getRegainReason() == EntityRegainHealthEvent.RegainReason.REGEN ||
              event.getRegainReason() == EntityRegainHealthEvent.RegainReason.SATIATED) ||
            event.isCancelled()) {
            return;
        }
        Player player = (Player) event.getEntity();
        Champion champion = plugin.getChampionManager().getChampion(player.getUniqueId());
        double amount = champion.getAttributeValues().get(StrifeAttribute.REGENERATION);
        if (player.hasPotionEffect(PotionEffectType.POISON)) {
            amount *= 0.25;
        }
        event.setAmount(amount);
    }

}
