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
package land.face.strife.menus.settings;

import com.tealcube.minecraft.bukkit.facecore.utilities.PaletteUtil;
import land.face.strife.StrifePlugin;
import ninja.amp.ampmenus.menus.ItemMenu;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public class SettingsMenu extends ItemMenu {

  public SettingsMenu(StrifePlugin plugin) {
    super(
        PaletteUtil.color(plugin.getSettings().getString("language.settings.menu-name", "侻")),
        Size.SIX_LINE,
        plugin
    );

    var baseStack = new ItemStack(Material.PAPER);
    baseStack.editMeta(m -> m.setItemModel(new NamespacedKey("faceland", "icons/checkbox")));

    var excaliburIcon = baseStack.clone();
    excaliburIcon.editMeta(m -> {
      m.setDisplayName(PaletteUtil.color(plugin.getSettings().getString("language.settings.excal-name", "")));
      m.setLore(PaletteUtil.color(plugin.getSettings().getStringList("language.settings.excal-lore")));
    });
    var facelandIcon = baseStack.clone();
    facelandIcon.editMeta(m -> {
      m.setDisplayName(PaletteUtil.color(plugin.getSettings().getString("language.settings.faceland-name", "")));
      m.setLore(PaletteUtil.color(plugin.getSettings().getStringList("language.settings.faceland-lore")));
    });
    var textShaderIcon = baseStack.clone();
    textShaderIcon.editMeta(m -> {
      m.setDisplayName(PaletteUtil.color(plugin.getSettings().getString("language.settings.text-shader-name", "")));
      m.setLore(PaletteUtil.color(plugin.getSettings().getStringList("language.settings.text-shader-lore")));
    });
    var indicatorIcon = baseStack.clone();
    indicatorIcon.editMeta(m -> {
      m.setDisplayName(PaletteUtil.color(plugin.getSettings().getString("language.settings.indicator-name", "")));
      m.setLore(PaletteUtil.color(plugin.getSettings().getStringList("language.settings.indicator-lore")));
    });
    var inspectIcon = baseStack.clone();
    inspectIcon.editMeta(m -> {
      m.setDisplayName(PaletteUtil.color(plugin.getSettings().getString("language.settings.inspect-name", "")));
      m.setLore(PaletteUtil.color(plugin.getSettings().getStringList("language.settings.inspect-lore")));
    });
    var xpIcon = baseStack.clone();
    xpIcon.editMeta(m -> {
      m.setDisplayName(PaletteUtil.color(plugin.getSettings().getString("language.settings.xp-name", "")));
      m.setLore(PaletteUtil.color(plugin.getSettings().getStringList("language.settings.xp-lore")));
    });
    var legsIcon = baseStack.clone();
    legsIcon.editMeta(m -> {
      m.setDisplayName(PaletteUtil.color(plugin.getSettings().getString("language.settings.leg-name", "")));
      m.setLore(PaletteUtil.color(plugin.getSettings().getStringList("language.settings.leg-lore")));
    });
    var potionIcon = baseStack.clone();
    potionIcon.editMeta(m -> {
      m.setDisplayName(PaletteUtil.color(plugin.getSettings().getString("language.settings.potion-name", "")));
      m.setLore(PaletteUtil.color(plugin.getSettings().getStringList("language.settings.potion-lore")));
    });
    var clickCastIcon = baseStack.clone();
    clickCastIcon.editMeta(m -> {
      m.setDisplayName(PaletteUtil.color(plugin.getSettings().getString("language.settings.click-cast-name", "")));
      m.setLore(PaletteUtil.color(plugin.getSettings().getStringList("language.settings.click-cast-lore")));
    });

    setItem(8, new PermissionCheckbox(plugin, this, excaliburIcon, "forceresourcepacks.pack.excaliburLayer", true));
    setItem(17, new PermissionCheckbox(plugin, this, facelandIcon, "forceresourcepacks.pack.facelandResourceLayer", true));
    setItem(26, new PermissionCheckbox(plugin, this, textShaderIcon, "forceresourcepacks.pack.textShaderLayer", true));

    setItem(30, new IndicatorsCheckbox(plugin, indicatorIcon));
    setItem(39, new AllowInspectCheckbox(plugin, inspectIcon));
    setItem(48, new ShowXpCheckbox(plugin, xpIcon));

    // setItem(35, new ShowXpCheckbox(plugin, Prayer.THIRTEEN, 2));
    // setItem(44, new ShowXpCheckbox(plugin, Prayer.FOURTEEN, 3));
    setItem(53, new LongLegsCheckbox(plugin, legsIcon));
    setItem(44, new QuickPotionCheckbox(plugin, potionIcon));
    setItem(35, new ClickCastCheckbox(plugin, clickCastIcon));
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
