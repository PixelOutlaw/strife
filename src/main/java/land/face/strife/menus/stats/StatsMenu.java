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
import land.face.strife.StrifePlugin;
import ninja.amp.ampmenus.menus.ItemMenu;
import org.bukkit.entity.Player;

public class StatsMenu extends ItemMenu {

  static final DecimalFormat INT_FORMAT = new DecimalFormat("#");
  static final DecimalFormat ONE_DECIMAL = new DecimalFormat("#.#");
  static final DecimalFormat TWO_DECIMAL = new DecimalFormat("#.##");
  static final String breakLine = StringExtensionsKt.chatColorize("&8==========================");

  private final Map<Player, Player> inspectionTargetMap = new WeakHashMap<>();

/*
00 01 02 03 04 05 06 07 08
09 10 11 12 13 14 15 16 17
18 19 20 21 22 23 24 25 26
27 28 29 30 31 32 33 34 35
36 37 38 39 40 41 42 43 44
45 46 47 48 49 50 51 52 53
*/

  public StatsMenu(StrifePlugin plugin) {
    super(StringExtensionsKt.chatColorize("&f\uF808砙\uF80C\uF80A\uF808\uF804&0Stats"), Size.fit(45), plugin);

    setItem(10, new StatsOffenseMenuItem(this));
    setItem(11, new StatsOffenseMenuItem(this));
    setItem(12, new StatsOffenseMenuItem(this));

    setItem(14, new StatsDefenseMenuItem(this));
    setItem(15, new StatsDefenseMenuItem(this));
    setItem(16, new StatsDefenseMenuItem(this));

    setItem(19, new StatsMiscMenuItem(this));
    setItem(20, new StatsMiscMenuItem(this));
    setItem(21, new StatsMiscMenuItem(this));

    setItem(23, new StatsEffectMenuItem(this));
    setItem(24, new StatsEffectMenuItem(this));
    setItem(25, new StatsEffectMenuItem(this));

    setItem(30, new StatsBonusMenuItem(this));
    setItem(31, new StatsBonusMenuItem(this));
    setItem(32, new StatsBonusMenuItem(this));
  }

  public Map<Player, Player> getInspectionTargetMap() {
    return inspectionTargetMap;
  }
}
