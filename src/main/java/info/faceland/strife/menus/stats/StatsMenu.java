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
package info.faceland.strife.menus.stats;

import com.tealcube.minecraft.bukkit.TextUtils;

import info.faceland.strife.StrifePlugin;
import java.text.DecimalFormat;
import ninja.amp.ampmenus.menus.ItemMenu;
import org.bukkit.entity.Player;

public class StatsMenu extends ItemMenu {

  private Player target;
  static final DecimalFormat INT_FORMAT = new DecimalFormat("#");
  static final DecimalFormat ONE_DECIMAL = new DecimalFormat("#.#");
  static final DecimalFormat TWO_DECIMAL = new DecimalFormat("#.##");
  static final String breakLine = TextUtils.color("&7&m-----------------------");

  public StatsMenu(StrifePlugin plugin) {
    super(TextUtils.color("&0&lStats!"), Size.fit(36), plugin);
    if (target == null) {
      setItem(0, new StatsHelmetItem(plugin));
      setItem(9, new StatsChestItem(plugin));
      setItem(18, new StatsLegsItem(plugin));
      setItem(27, new StatsBootsItem(plugin));
      setItem(1, new StatsMainHandItem(plugin));
      setItem(10, new StatsOffHandItem(plugin));

      setItem(12, new StatsOffenseMenuItem(plugin));
      setItem(14, new StatsDefenseMenuItem(plugin));
      setItem(16, new StatsMiscMenuItem(plugin));
      setItem(22, new StatsBonusMenuItem(plugin));
      setItem(24, new StatsEffectMenuItem(plugin));

      setItem(35, new StatsChangeHealthDisplay(plugin, 35));
    } else {
      setItem(0, new StatsHelmetItem(plugin, target));
      setItem(9, new StatsChestItem(plugin, target));
      setItem(18, new StatsLegsItem(plugin, target));
      setItem(27, new StatsBootsItem(plugin, target));
      setItem(1, new StatsMainHandItem(plugin, target));
      setItem(10, new StatsOffHandItem(plugin, target));

      setItem(13, new StatsOffenseMenuItem(plugin, target));
      setItem(15, new StatsDefenseMenuItem(plugin, target));
      setItem(22, new StatsMiscMenuItem(plugin, target));
      setItem(24, new StatsBonusMenuItem(plugin, target));

      setItem(35, new StatsBonusMenuItem(plugin, target));
    }
    target = null;
  }

  public StatsMenu(StrifePlugin plugin, Player player) {
    super(TextUtils.color("&0&lStats!"), Size.fit(36), plugin);

    setItem(0, new StatsHelmetItem(plugin, player));
    setItem(9, new StatsChestItem(plugin, player));
    setItem(18, new StatsLegsItem(plugin, player));
    setItem(27, new StatsBootsItem(plugin, player));
    setItem(1, new StatsMainHandItem(plugin, player));
    setItem(10, new StatsOffHandItem(plugin, player));

    setItem(13, new StatsOffenseMenuItem(plugin, player));
    setItem(15, new StatsDefenseMenuItem(plugin, player));
    setItem(22, new StatsMiscMenuItem(plugin, player));

    setItem(24, new StatsBonusMenuItem(plugin, player));
  }

  public void setTarget(Player target) {
    this.target = target;
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
