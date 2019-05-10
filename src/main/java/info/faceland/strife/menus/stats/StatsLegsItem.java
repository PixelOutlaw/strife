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
package info.faceland.strife.menus.stats;

import com.tealcube.minecraft.bukkit.TextUtils;

import info.faceland.strife.StrifePlugin;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import ninja.amp.ampmenus.events.ItemClickEvent;
import ninja.amp.ampmenus.items.MenuItem;

public class StatsLegsItem extends MenuItem {

    private final StrifePlugin plugin;
    private Player player;

    public StatsLegsItem(StrifePlugin plugin, Player player) {
        super(TextUtils.color("&eNo Leggings"), new ItemStack(Material.BARRIER));
        this.plugin = plugin;
        this.player = player;
    }

    public StatsLegsItem(StrifePlugin plugin) {
        super(TextUtils.color("&eNo Leggings"), new ItemStack(Material.BARRIER));
        this.plugin = plugin;
    }

    @Override
    public ItemStack getFinalIcon(Player player) {
        if (this.player != null) {
            player = this.player;
        }
        ItemStack chest = player.getEquipment().getLeggings();
        if (chest == null || chest.getType() == Material.AIR) {
            chest = new ItemStack(this.getIcon());
            ItemMeta im = chest.getItemMeta();
            im.setDisplayName(this.getDisplayName());
            chest.setItemMeta(im);
        }
        return chest;
    }

    @Override
    public void onItemClick(ItemClickEvent event) {
        super.onItemClick(event);
    }

}
