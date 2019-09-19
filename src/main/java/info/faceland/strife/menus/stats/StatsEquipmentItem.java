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
import info.faceland.strife.util.ItemUtil;
import ninja.amp.ampmenus.events.ItemClickEvent;
import ninja.amp.ampmenus.items.MenuItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class StatsEquipmentItem extends MenuItem {

  private EquipmentSlot slot;

  public StatsEquipmentItem(EquipmentSlot slot, String invalidText) {
    super(TextUtils.color(invalidText), new ItemStack(Material.BARRIER));
    this.slot = slot;
  }

  @Override
  public ItemStack getFinalIcon(Player player) {
    ItemStack item = ItemUtil.getItem(player.getEquipment(), slot);
    if (item == null || item.getType() == Material.AIR) {
      item = new ItemStack(this.getIcon());
      ItemMeta im = item.getItemMeta();
      im.setDisplayName(this.getDisplayName());
      item.setItemMeta(im);
    }
    return item;
  }

  @Override
  public void onItemClick(ItemClickEvent event) {
    super.onItemClick(event);
  }

}
