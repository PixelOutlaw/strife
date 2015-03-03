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

import com.tealcube.minecraft.bukkit.facecore.shade.amp.ampmenus.items.CloseItem;
import com.tealcube.minecraft.bukkit.facecore.shade.amp.ampmenus.menus.ItemMenu;
import info.faceland.strife.StrifePlugin;
import info.faceland.strife.stats.StrifeStat;
import org.bukkit.ChatColor;

import java.util.List;

public class StatsMenu extends ItemMenu {

    public StatsMenu(StrifePlugin plugin, List<StrifeStat> stats) {
        super(ChatColor.BLACK + "Levelup Menu", Size.fit(plugin.getSettings().getInt("config.menu.num-of-rows") * 9), plugin);

        for (StrifeStat stat : stats) {
            int counter = stat.getMenuY() * 9 + stat.getMenuX();
            setItem(counter, new StatMenuItem(plugin, stat));
        }

        int counter = plugin.getSettings().getInt("config.menu.unused-marker-y") * 9;
        counter += plugin.getSettings().getInt("config.menu.unused-marker-x");
        setItem(counter, new StatPointsMenuItem(plugin));

        counter = plugin.getSettings().getInt("config.menu.close-marker-y") * 9;
        counter += plugin.getSettings().getInt("config.menu.close-marker-x");
        setItem(counter, new CloseItem());
    }

}

/*
00 01 02 03 04 05 06 07 08
09 10 11 12 13 14 15 16 17
18 19 20 21 22 23 24 25 26
27 28 29 30 31 32 33 34 35
36 37 38 39 40 41 42 43 44
45 46 47 48 49 50 51 52 53
*/