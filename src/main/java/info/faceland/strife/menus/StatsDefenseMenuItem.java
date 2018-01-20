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
import info.faceland.strife.attributes.StrifeAttribute;
import info.faceland.strife.data.AttributedEntity;
import info.faceland.strife.data.Champion;
import ninja.amp.ampmenus.events.ItemClickEvent;
import ninja.amp.ampmenus.items.MenuItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class StatsDefenseMenuItem extends MenuItem {

    private final StrifePlugin plugin;
    private Player player;
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#");
    private static final DecimalFormat REDUCER_FORMAT = new DecimalFormat("#.#");
    private static final DecimalFormat TWO_DECIMALS = new DecimalFormat("#.##");
    private static final String breakLine = TextUtils.color("&7&m--------------------");

    public StatsDefenseMenuItem(StrifePlugin plugin, Player player) {
        super(TextUtils.color("&e&lDefensive Stats"), new ItemStack(Material.IRON_CHESTPLATE));
        this.plugin = plugin;
        this.player = player;
    }

    public StatsDefenseMenuItem(StrifePlugin plugin) {
        super(TextUtils.color("&e&lDefensive Stats"), new ItemStack(Material.IRON_CHESTPLATE));
        this.plugin = plugin;
    }

    @Override
    public ItemStack getFinalIcon(Player player) {
        if (this.player != null) {
            player = this.player;
        }
        AttributedEntity pStats = plugin.getEntityStatCache().getEntity(player, false);
        ItemStack itemStack = new ItemStack(Material.IRON_CHESTPLATE);
        ItemMeta itemMeta = Bukkit.getItemFactory().getItemMeta(itemStack.getType());
        itemMeta.setDisplayName(getDisplayName());
        itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        List<String> lore = new ArrayList<>();
        lore.add(breakLine);

        lore.add(ChatColor.YELLOW + "Maximum Health: " + ChatColor.WHITE + DECIMAL_FORMAT.format(pStats.getAttribute(StrifeAttribute.HEALTH)));
        double regenAmount = pStats.getAttribute(StrifeAttribute.REGENERATION) * (1 + pStats.getAttribute(StrifeAttribute.REGEN_MULT) / 100);
        lore.add(ChatColor.YELLOW + "Regeneration: " + ChatColor.WHITE + TWO_DECIMALS.format(regenAmount) + ChatColor.GRAY + " (HP/5s)");

        lore.add(breakLine);

        double statCap;
        lore.add(ChatColor.YELLOW + "Armor Rating: " + ChatColor.WHITE + DECIMAL_FORMAT.format(pStats.getAttribute(StrifeAttribute.ARMOR)));
        lore.add(ChatColor.YELLOW + "Evasion Rating: " + ChatColor.WHITE + DECIMAL_FORMAT.format(pStats.getAttribute(StrifeAttribute.EVASION)));

        lore.add(breakLine);

        lore.add(ChatColor.YELLOW + "Block: " + ChatColor.WHITE + DECIMAL_FORMAT.format(pStats.getAttribute(StrifeAttribute.BLOCK)));

        lore.add(breakLine);

        statCap = Math.min(pStats.getAttribute(StrifeAttribute.RESISTANCE),
                StrifeAttribute.RESISTANCE.getPlayerCap());
        lore.add(ChatColor.YELLOW + "Elemental Resist: " + ChatColor.WHITE + DECIMAL_FORMAT.format(statCap * 100) + "%");

        lore.add(breakLine);

        lore.add(TextUtils.color("&8&oUse &7&o/help stats &8&ofor info!"));

        itemMeta.setLore(lore);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    @Override
    public void onItemClick(ItemClickEvent event) {
        super.onItemClick(event);
    }

}
