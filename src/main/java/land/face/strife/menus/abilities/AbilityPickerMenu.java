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

import java.util.List;
import java.util.stream.Collectors;
import land.face.strife.StrifePlugin;
import land.face.strife.data.ability.Ability;
import ninja.amp.ampmenus.menus.ItemMenu;
import org.bukkit.ChatColor;

public class AbilityPickerMenu extends ItemMenu {

  public AbilityPickerMenu(StrifePlugin plugin, String name, List<String> abilities) {
    super(ChatColor.BLACK + name, Size.fit(36), plugin);

    List<Ability> abilityList = abilities.stream()
        .map(a -> plugin.getAbilityManager().getAbility(a))
        .collect(Collectors.toList());

    int index = 0;
    for (Ability ability : abilityList) {
      setItem(index, new AbilityPickerItem(plugin, ability));
      index++;
    }
  }

  public enum AbilityMenuType {
    BASIC_LEVEL_MENU,
    MELEE_ABILITY,
    ARCHERY_ABILITY,
    MAGIC_ABILITY
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
