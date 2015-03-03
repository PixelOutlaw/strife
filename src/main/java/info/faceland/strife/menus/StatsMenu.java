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

import java.util.List;

public class StatsMenu extends ItemMenu {

    public StatsMenu(StrifePlugin plugin, List<StrifeStat> stats) {
        super("Stats", Size.fit(stats.size() + 9), plugin);

        int counter = 0;
        for (StrifeStat stat : stats) {
            if (counter == getSize().getSize() - 9 || counter == getSize().getSize() - 1) {
                counter++;
            }
            setItem(counter, new StatMenuItem(plugin, stat));
            counter++;
        }
        setItem(getSize().getSize() - 9, new StatPointsMenuItem(plugin));
        setItem(getSize().getSize() - 1, new CloseItem());
    }

}

/*
00 01 02 03 04 05 06 07 08
09 10 11 12 13 14 15 16 17
*/