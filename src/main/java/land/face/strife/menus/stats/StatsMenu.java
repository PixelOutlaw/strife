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
package land.face.strife.menus.stats;

import com.tealcube.minecraft.bukkit.TextUtils;
import java.text.DecimalFormat;
import land.face.strife.StrifePlugin;
import land.face.strife.menus.BlankIcon;
import ninja.amp.ampmenus.menus.ItemMenu;
import org.bukkit.inventory.EquipmentSlot;

public class StatsMenu extends ItemMenu {

  static final DecimalFormat INT_FORMAT = new DecimalFormat("#");
  static final DecimalFormat ONE_DECIMAL = new DecimalFormat("#.#");
  static final DecimalFormat TWO_DECIMAL = new DecimalFormat("#.##");
  static final String breakLine = TextUtils.color("&7&m-----------------------");

  public StatsMenu() {
    super(TextUtils.color("&0&lStats!"), Size.fit(36), StrifePlugin.getInstance());
    setItem(0, new StatsEquipmentItem(EquipmentSlot.HEAD, "&eNo Helmet"));
    setItem(9, new StatsEquipmentItem(EquipmentSlot.CHEST, "&eNo Chest Armor"));
    setItem(18, new StatsEquipmentItem(EquipmentSlot.LEGS, "&eNo... pants?"));
    setItem(27, new StatsEquipmentItem(EquipmentSlot.FEET, "&eNo Boots"));
    setItem(1, new StatsEquipmentItem(EquipmentSlot.HAND, "&eNo Weapon"));
    setItem(10, new StatsEquipmentItem(EquipmentSlot.OFF_HAND, "&eNo Offhand Item"));

    setItem(12, new StatsOffenseMenuItem());
    setItem(14, new StatsDefenseMenuItem());
    setItem(16, new StatsMiscMenuItem());
    setItem(22, new StatsBonusMenuItem());
    setItem(24, new StatsEffectMenuItem());

    fillEmptySlots(new BlankIcon());
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
