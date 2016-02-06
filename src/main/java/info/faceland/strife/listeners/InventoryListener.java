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
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class InventoryListener implements Listener {

    private final StrifePlugin plugin;

    public InventoryListener(StrifePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClose(InventoryCloseEvent event) {
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
        final Player player = (Player) humanEntity;
        if (player.isDead() || player.getHealth() <= 0D) {
            return;
        }
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                Champion champion = plugin.getChampionManager().getChampion(player.getUniqueId());
                champion.getAttributeValues(true);
                boolean spam = false;
                for (ItemStack itemStack : champion.getPlayer().getEquipment().getArmorContents()) {
                    if (itemStack == null || itemStack.getType() == Material.AIR) {
                        continue;
                    }
                    if (!AttributeHandler.meetsLevelRequirement(player, itemStack)) {
                        spam = true;
                    }
                }
                if (spam) {
                    MessageUtils.sendMessage(player,
                            "<red>You don't meet the requirement for one of your items! It will not give any stats!");
                }
                AttributeHandler.updateHealth(player, champion.getCacheAttribute(StrifeAttribute.HEALTH,
                        StrifeAttribute.HEALTH.getBaseValue()));
                double perc = champion.getCacheAttribute(StrifeAttribute.MOVEMENT_SPEED,
                        StrifeAttribute.MOVEMENT_SPEED.getBaseValue()) / 100D;
                float speed = 0.2F * (float) perc;
                player.setWalkSpeed(Math.min(Math.max(-1F, speed), 1F));
                player.setFlySpeed(Math.min(Math.max(-1F, speed), 1F));
            }
        });
    }

}
