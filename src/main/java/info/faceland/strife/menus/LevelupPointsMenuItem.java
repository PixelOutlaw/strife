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

import info.faceland.strife.StrifePlugin;
import info.faceland.strife.data.Champion;
import ninja.amp.ampmenus.events.ItemClickEvent;
import ninja.amp.ampmenus.items.MenuItem;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Wool;

public class LevelupPointsMenuItem extends MenuItem {

    private static final String DISPLAY_NAME = ChatColor.WHITE + "Unused Levelup Points";
    private static final ItemStack DISPLAY_ICON = new ItemStack(Material.NETHER_STAR);
    private static final String[] DISPLAY_LORE = {ChatColor.GRAY + "Click a stat to spend your points!"};
    private final StrifePlugin plugin;

    public LevelupPointsMenuItem(StrifePlugin plugin) {
        super(DISPLAY_NAME, DISPLAY_ICON, DISPLAY_LORE);
        this.plugin = plugin;
    }

    @Override
    public ItemStack getFinalIcon(Player player) {
        ItemStack itemStack = super.getFinalIcon(player);
        Champion champion = plugin.getChampionManager().getChampion(player);
        int stacks = champion.getUnusedStatPoints();
        String name = ChatColor.WHITE + "Unused Levelpoints (" + stacks + ")";
        itemStack.getItemMeta().setDisplayName(name);
        stacks = Math.min(stacks, 64);
        itemStack.setAmount(stacks);
        return itemStack;
    }

    @Override
    public void onItemClick(ItemClickEvent event) {
        super.onItemClick(event);
    }

}
