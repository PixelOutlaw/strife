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
package land.face.strife.menus.revive;

import com.tealcube.minecraft.bukkit.facecore.utilities.PaletteUtil;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.tuple.Pair;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import land.face.strife.StrifePlugin;
import lombok.Getter;
import ninja.amp.ampmenus.menus.ItemMenu;

public class ReviveMenu extends ItemMenu {

  private final StrifePlugin plugin;
  @Getter
  private Map<UUID, Pair<String, Integer>> dataMap = new HashMap<>();

  public ReviveMenu(StrifePlugin plugin) {
    super(PaletteUtil.color(StrifePlugin.getInstance().getSettings()
        .getString("language.revive.menu-name", "NAME MENU LOL")), Size.FOUR_LINE, plugin);
    this.plugin = plugin;
    for (int i=0; i<=26; i++) {
      setItem(i, new ReviveConfirmItem(this));
    }
    for (int i=27; i<=35; i++) {
      setItem(i, new ReviveRejectItem(this));
    }
  }

  public void postNewReviveData(UUID deadGuy, String name, int amount) {
    dataMap.put(deadGuy, Pair.of(name, amount));
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
