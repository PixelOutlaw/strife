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

import com.tealcube.minecraft.bukkit.facecore.utilities.ItemUtils;
import com.tealcube.minecraft.bukkit.facecore.utilities.PaletteUtil;
import com.tealcube.minecraft.bukkit.facecore.utilities.TextUtils;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.util.List;
import lombok.Getter;
import ninja.amp.ampmenus.events.ItemClickEvent;
import ninja.amp.ampmenus.items.MenuItem;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

public class SubmenuSelectButton extends MenuItem {

  private final AbilitySubmenu menu;

  @Getter
  private final int slot;
  private final ItemStack icon;

  public SubmenuSelectButton(AbilitySubmenu menu, String model, String name, List<String> lore, int slot) {
    super("", ItemUtils.BLANK.clone());
    icon = ItemUtils.BLANK.clone();
    ItemStackExtensionsKt.setDisplayName(icon, PaletteUtil.color(name));
    TextUtils.setLore(icon, PaletteUtil.color(lore));
    icon.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
    //ItemStackExtensionsKt.setDisplayName(getIcon(), StringExtensionsKt.chatColorize(name));
    //TextUtils.setLore(getIcon(), lore, true);
    //ItemUtils.setItemModel(getIcon(), model);
    this.menu = menu;
    this.slot = slot;
  }

  @Override
  public ItemStack getFinalIcon(Player player) {
    return icon;
  }

  @Override
  public void onItemClick(ItemClickEvent event) {
    super.onItemClick(event);
    event.setWillClose(false);
    menu.open(event.getPlayer());
  }
}
