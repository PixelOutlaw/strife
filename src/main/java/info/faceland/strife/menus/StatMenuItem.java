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
package info.faceland.strife.menus;

import com.tealcube.minecraft.bukkit.facecore.shade.amp.ampmenus.events.ItemClickEvent;
import com.tealcube.minecraft.bukkit.facecore.shade.amp.ampmenus.items.MenuItem;
import com.tealcube.minecraft.bukkit.facecore.utilities.TextUtils;
import info.faceland.strife.StrifePlugin;
import info.faceland.strife.attributes.AttributeHandler;
import info.faceland.strife.data.Champion;
import info.faceland.strife.stats.StrifeStat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Wool;

import java.util.ArrayList;
import java.util.List;

public class StatMenuItem extends MenuItem {

    private final StrifePlugin plugin;
    private final StrifeStat stat;

    public StatMenuItem(StrifePlugin plugin, StrifeStat strifeStat) {
        super(strifeStat.getChatColor() + strifeStat.getName(), new Wool().toItemStack(),
                TextUtils.color(strifeStat.getDescription()).split("/n"));
        this.plugin = plugin;
        this.stat = strifeStat;
    }

    @Override
    public ItemStack getFinalIcon(Player player) {
        Champion champion = plugin.getChampionManager().getChampion(player.getUniqueId());
        int level = champion.getLevel(stat);
        ItemStack itemStack = new Wool(stat.getDyeColor()).toItemStack(level);
        ItemMeta itemMeta = Bukkit.getItemFactory().getItemMeta(itemStack.getType());
        itemMeta.setDisplayName(getDisplayName() + " [" + level + "/" + champion.getMaximumStatLevel() + "]");
        List<String> lore = new ArrayList<>(getLore());
        if (level >= champion.getMaximumStatLevel()) {
            lore.add(ChatColor.RED + "No unused points.");
        } else {
            lore.add(ChatColor.YELLOW + "Click to upgrade!");
        }
        itemMeta.setLore(lore);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    @Override
    public void onItemClick(ItemClickEvent event) {
        super.onItemClick(event);
        Player player = event.getPlayer();
        Champion champion = plugin.getChampionManager().getChampion(player.getUniqueId());
        if (champion.getUnusedStatPoints() < 1) {
            return;
        }
        int currentLevel = champion.getLevel(stat);
        if (currentLevel + 1 > champion.getMaximumStatLevel()) {
            return;
        }
        champion.setLevel(stat, currentLevel + 1);
        champion.setUnusedStatPoints(champion.getUnusedStatPoints() - 1);
        plugin.getChampionManager().removeChampion(champion.getUniqueId());
        plugin.getChampionManager().addChampion(champion);
        AttributeHandler.updateHealth(champion.getPlayer(), champion.getAttributeValues());
        event.setWillUpdate(true);
    }

}
