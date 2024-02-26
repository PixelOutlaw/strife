/**
 * The MIT License Copyright (c) 2015 Teal Cube Games
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package land.face.strife.menus.prayer;

import com.tealcube.minecraft.bukkit.facecore.utilities.PaletteUtil;
import land.face.strife.StrifePlugin;
import land.face.strife.managers.PrayerManager.Prayer;
import land.face.strife.menus.revive.ReviveConfirmItem;
import land.face.strife.menus.revive.ReviveRejectItem;
import ninja.amp.ampmenus.menus.ItemMenu;

public class PrayerMenu extends ItemMenu {

  public PrayerMenu(StrifePlugin plugin) {
    super(
        PaletteUtil.color(plugin.getSettings().getString("language.prayer.menu-name", "NAME MENU LOL")),
        Size.SIX_LINE,
        plugin
    );

    setItem(1, new PrayerIcon(plugin, Prayer.ONE));
    setItem(4, new PrayerIcon(plugin, Prayer.TWO));
    setItem(7, new PrayerIcon(plugin, Prayer.THREE));

    setItem(11, new PrayerIcon(plugin, Prayer.FOUR));
    setItem(15, new PrayerIcon(plugin, Prayer.FIVE));

    setItem(19, new PrayerIcon(plugin, Prayer.SIX));
    setItem(22, new PrayerIcon(plugin, Prayer.SEVEN));
    setItem(25, new PrayerIcon(plugin, Prayer.EIGHT));

    setItem(29, new PrayerIcon(plugin, Prayer.NINE));
    setItem(33, new PrayerIcon(plugin, Prayer.TEN));

    setItem(37, new PrayerIcon(plugin, Prayer.ELEVEN));
    setItem(43, new PrayerIcon(plugin, Prayer.TWELVE));

    setItem(47, new GodPrayerIcon(plugin, Prayer.THIRTEEN, 2));
    setItem(49, new GodPrayerIcon(plugin, Prayer.FOURTEEN, 3));
    setItem(51, new GodPrayerIcon(plugin, Prayer.FIFTEEN, 4));
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
