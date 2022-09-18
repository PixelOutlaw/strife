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
package land.face.strife.menus.abilities;

import io.pixeloutlaw.minecraft.spigot.garbage.StringExtensionsKt;
import java.util.List;
import land.face.strife.StrifePlugin;
import land.face.strife.data.ability.Ability;
import land.face.strife.data.effects.TargetingComparators.AbilityComparator;
import land.face.strife.menus.BlankIcon;
import ninja.amp.ampmenus.menus.ItemMenu;

public class AbilitySubmenu extends ItemMenu {

  private String id;

  public AbilitySubmenu(StrifePlugin plugin, String name, List<Ability> abilities, ReturnButton returnButton) {
    super(StringExtensionsKt.chatColorize(name), Size.fit(abilities.size() + 1), plugin);
    int index = 0;
    if (returnButton != null) {
      setItem(0, returnButton);
      index++;
    }
    abilities.sort(new AbilityComparator());
    for (Ability ability : abilities) {
      setItem(index, new AbilityButton(plugin, ability));
      index++;
    }
    fillEmptySlots(new BlankIcon());
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
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
