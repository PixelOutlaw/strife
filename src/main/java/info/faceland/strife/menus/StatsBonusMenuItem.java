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
import info.faceland.strife.data.Champion;
import ninja.amp.ampmenus.events.ItemClickEvent;
import ninja.amp.ampmenus.items.MenuItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class StatsBonusMenuItem extends MenuItem {

    private final StrifePlugin plugin;
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#");
    private static final String breakLine = TextUtils.color("&7&m--------------------");

    public StatsBonusMenuItem(StrifePlugin plugin) {
        super(TextUtils.color("&a&lDrop Modifiers"), new ItemStack(Material.EMERALD));
        this.plugin = plugin;
    }

    @Override
    public ItemStack getFinalIcon(Player player) {
        Champion champion = plugin.getChampionManager().getChampion(player.getUniqueId());
        ItemStack itemStack = new ItemStack(Material.EMERALD);
        ItemMeta itemMeta = Bukkit.getItemFactory().getItemMeta(itemStack.getType());
        itemMeta.setDisplayName(getDisplayName());
        List<String> lore = new ArrayList<>();

        lore.add(breakLine);

        double xpMult = plugin.getSettings().getDouble("config.xp-bonus", 0.0);
        double dropMult = plugin.getSettings().getDouble("config.drop-bonus", 0.0);
        lore.add(ChatColor.GREEN + "Bonus Experience: " + ChatColor.WHITE + "+" + DECIMAL_FORMAT
                .format((xpMult + champion.getCache().getAttribute(StrifeAttribute.XP_GAIN)) * 100) + "%");
        lore.add(ChatColor.GREEN + "Bonus Item Drop Rate: " + ChatColor.WHITE + "+" + DECIMAL_FORMAT
                .format((dropMult + champion.getCache().getAttribute(StrifeAttribute.ITEM_DISCOVERY)) * 100) + "%");
        lore.add(ChatColor.GREEN + "Bonus Cash Dropped: " + ChatColor.WHITE + "+" + DECIMAL_FORMAT
                .format(champion.getCache().getAttribute(StrifeAttribute.GOLD_FIND) * 100) + "%");
        lore.add(ChatColor.GREEN + "Head Drop Chance: " + ChatColor.WHITE + DECIMAL_FORMAT
                .format(champion.getCache().getAttribute(StrifeAttribute.HEAD_DROP) * 100) + "%");

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
