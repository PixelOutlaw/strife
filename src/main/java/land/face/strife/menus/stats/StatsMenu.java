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

import io.pixeloutlaw.minecraft.spigot.garbage.StringExtensionsKt;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.WeakHashMap;
import land.face.dinvy.windows.equipment.EquipmentMenu.DeluxeSlot;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.menus.BlankIcon;
import land.face.strife.stats.StrifeStat;
import land.face.strife.util.StatUtil;
import ninja.amp.ampmenus.menus.ItemMenu;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;

public class StatsMenu extends ItemMenu {

  static final DecimalFormat INT_FORMAT = new DecimalFormat("#");
  static final DecimalFormat ONE_DECIMAL = new DecimalFormat("#.#");
  static final DecimalFormat TWO_DECIMAL = new DecimalFormat("#.##");
  static final String breakLine = StringExtensionsKt.chatColorize("&8=========================");

  private final Map<Player, Player> inspectionTargetMap = new WeakHashMap<>();

  public StatsMenu(StrifePlugin plugin) {
    super(StringExtensionsKt.chatColorize("&0&lStats!"), Size.fit(45), plugin);
    setItem(0, new StatsDeluxeEquipmentItem(this, DeluxeSlot.HELMET, "&eNo Helmet"));
    setItem(9, new StatsDeluxeEquipmentItem(this, DeluxeSlot.BODY, "&eNo Chest Armor"));
    setItem(18, new StatsDeluxeEquipmentItem(this, DeluxeSlot.LEGS, "&eNo... pants?"));
    setItem(27, new StatsDeluxeEquipmentItem(this, DeluxeSlot.BOOTS, "&eNo Boots"));
    setItem(1, new StatsEquipmentItem(this, EquipmentSlot.HAND, "&eNo Weapon"));
    setItem(10, new StatsDeluxeEquipmentItem(this, DeluxeSlot.OFF_HAND, "&eNo offhand Item"));

    setItem(12, new StatsOffenseMenuItem(this));
    setItem(14, new StatsDefenseMenuItem(this));
    setItem(16, new StatsMiscMenuItem(this));
    setItem(22, new StatsBonusMenuItem(this));
    setItem(24, new StatsEffectMenuItem(this));

    setItem(41, new StatsOpenLevelupMenu(plugin));
    setItem(42, new StatsToggleGlow(plugin));
    setItem(43, new StatsVerboseXP(plugin));
    setItem(44, new StatsChangeHealthDisplay(plugin));

    fillEmptySlots(new BlankIcon());
  }

  public Map<Player, Player> getInspectionTargetMap() {
    return inspectionTargetMap;
  }

  public static String printStatWithoutPlus(String name, ChatColor color,
      StrifeMob mob, StrifeStat stat, DecimalFormat format) {
    float value = StatUtil.getStat(mob, stat);
    if (value < -0.4) {
      return ChatColor.RED + format.format(value);
    }
    return ChatColor.WHITE + format.format(value);
  }

  public static String printStatWithPlus(String name, ChatColor color,
      StrifeMob mob, StrifeStat stat, DecimalFormat format) {
    float value = StatUtil.getStat(mob, stat);
    if (value < -0.4) {
      return ChatColor.RED + format.format(value);
    }
    return ChatColor.WHITE + format.format(value);
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
