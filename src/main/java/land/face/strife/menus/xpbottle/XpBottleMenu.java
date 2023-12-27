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
package land.face.strife.menus.xpbottle;

import io.pixeloutlaw.minecraft.spigot.garbage.StringExtensionsKt;
import land.face.strife.StrifePlugin;
import land.face.strife.data.champion.LifeSkillType;
import ninja.amp.ampmenus.menus.ItemMenu;

public class XpBottleMenu extends ItemMenu {

  public static String BASE_NAME = StringExtensionsKt.chatColorize("&f拹");

  public XpBottleMenu(StrifePlugin plugin, String title, int bowlId, int amount) {
    super(BASE_NAME + title, Size.FOUR_LINE, plugin);

    setItem(1, new SkillButton(plugin, LifeSkillType.CRAFTING, bowlId, 415, amount));
    setItem(2, new SkillButton(plugin, LifeSkillType.FISHING, bowlId, 436, amount));
    setItem(3, new SkillButton(plugin, LifeSkillType.ENCHANTING, bowlId, 410, amount));
    setItem(10, new SkillButton(plugin, LifeSkillType.MINING, bowlId, 421, amount));
    setItem(11, new SkillButton(plugin, LifeSkillType.FARMING, bowlId, 413, amount));
    setItem(12, new SkillButton(plugin, LifeSkillType.COOKING, bowlId, 419, amount));
    setItem(19, new SkillButton(plugin, LifeSkillType.ALCHEMY, bowlId, 461, amount));
    setItem(20, new SkillButton(plugin, LifeSkillType.TRADING, bowlId, 437, amount));
    setItem(21, new SkillButton(plugin, LifeSkillType.AGILITY, bowlId, 438, amount));
    setItem(28, new SkillButton(plugin, LifeSkillType.SNEAK, bowlId, 400, amount));

    setItem(5, new SkillButton(plugin, LifeSkillType.SWORDSMANSHIP, bowlId, 406, amount));
    setItem(6, new SkillButton(plugin, LifeSkillType.AXE_MASTERY, bowlId, 426, amount));
    setItem(7, new SkillButton(plugin, LifeSkillType.BLUNT_WEAPONS, bowlId, 441, amount));
    setItem(14, new SkillButton(plugin, LifeSkillType.ARCHERY, bowlId, 431, amount));
    setItem(15, new SkillButton(plugin, LifeSkillType.MARKSMANSHIP, bowlId, 435, amount));
    setItem(16, new SkillButton(plugin, LifeSkillType.SHIELD_MASTERY, bowlId, 416, amount));
    setItem(23, new SkillButton(plugin, LifeSkillType.DAGGER_MASTERY, bowlId, 471, amount));
    setItem(24, new SkillButton(plugin, LifeSkillType.DUAL_WIELDING, bowlId, 408, amount));
    setItem(25, new SkillButton(plugin, LifeSkillType.ARCANE_MAGICS, bowlId, 465, amount));
    setItem(32, new SkillButton(plugin, LifeSkillType.NATURAL_MAGICS, bowlId, 470, amount));
    setItem(33, new SkillButton(plugin, LifeSkillType.CELESTIAL_MAGICS, bowlId, 420, amount));
    setItem(34, new SkillButton(plugin, LifeSkillType.BLACK_MAGICS, bowlId, 456, amount));
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
