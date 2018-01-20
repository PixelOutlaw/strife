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
package info.faceland.strife.menus;

import com.tealcube.minecraft.bukkit.TextUtils;
import info.faceland.strife.StrifePlugin;
import info.faceland.strife.attributes.AttributeHandler;
import info.faceland.strife.attributes.StrifeAttribute;
import info.faceland.strife.data.Champion;
import info.faceland.strife.stats.StrifeStat;
import ninja.amp.ampmenus.events.ItemClickEvent;
import ninja.amp.ampmenus.items.MenuItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Wool;

import java.util.ArrayList;
import java.util.List;

public class LevelupMenuItem extends MenuItem {

    private final StrifePlugin plugin;
    private final StrifeStat stat;

    public LevelupMenuItem(StrifePlugin plugin, StrifeStat strifeStat) {
        super(strifeStat.getChatColor() + strifeStat.getName(), new Wool().toItemStack(),
              TextUtils.color(strifeStat.getDescription()).split("/n"));
        this.plugin = plugin;
        this.stat = strifeStat;
    }

    @Override
    public ItemStack getFinalIcon(Player player) {
        Champion champion = plugin.getChampionManager().getChampion(player.getUniqueId());
        int level = champion.getLevel(stat);
        ItemStack itemStack;
        if (level != 0) {
            itemStack = new Wool(stat.getDyeColor()).toItemStack(Math.min(level, 64));
        } else {
            itemStack = new Wool(DyeColor.GRAY).toItemStack(1);
        }
        ItemMeta itemMeta = Bukkit.getItemFactory().getItemMeta(itemStack.getType());
        itemMeta.setDisplayName(getDisplayName() + " [" + level + "/" + champion.getMaximumStatLevel() + "]");
        List<String> lore = new ArrayList<>(getLore());
        if (champion.getUnusedStatPoints() == 0) {
            lore.add(ChatColor.RED + "No unused points.");
        } else if (level >= champion.getMaximumStatLevel()) {
            lore.add(ChatColor.RED + "Point cap reached.");
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
        champion.getLevelpointAttributeValues();
        champion.getCache().recombine();
        AttributeHandler.updateHealth(champion.getPlayer(), champion.getCache().getAttribute(StrifeAttribute.HEALTH));
        event.setWillUpdate(true);
    }

}
