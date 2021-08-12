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

import com.tealcube.minecraft.bukkit.facecore.utilities.TextUtils;
import io.pixeloutlaw.minecraft.spigot.garbage.StringExtensionsKt;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.util.ArrayList;
import java.util.List;
import land.face.strife.StrifePlugin;
import ninja.amp.ampmenus.events.ItemClickEvent;
import ninja.amp.ampmenus.items.MenuItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

public class ReviveConfirmItem extends MenuItem {

  private final String reviverName;
  private final int restoreXP;

  ReviveConfirmItem(String reviverName, int restoreXP) {
    super("", new ItemStack(Material.TOTEM_OF_UNDYING));
    this.reviverName = reviverName;
    this.restoreXP = restoreXP;
  }

  @Override
  public ItemStack getFinalIcon(Player player) {
    Material material = Material.getMaterial(StrifePlugin.getInstance().getSettings()
        .getString("config.revive.confirm-material", "TOTEM_OF_UNDYING"));
    ItemStack stack;
    if (material == null || material == Material.AIR) {
      stack = this.getIcon().clone();
    } else {
      stack = new ItemStack(material);
    }
    List<String> baseLore = StrifePlugin.getInstance().getSettings()
        .getStringList("language.revive.confirm-lore");
    List<String> newLore = new ArrayList<>();
    for (String s : baseLore) {
      newLore.add(s.replace("{name}", reviverName).replace("{xp}", String.valueOf(restoreXP)));
    }
    String name = StrifePlugin.getInstance().getSettings()
        .getString("language.revive.confirm-name", "dew it");
    ItemStackExtensionsKt.setDisplayName(stack, StringExtensionsKt.chatColorize(name));
    TextUtils.setLore(stack, newLore, true);
    stack.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
    return stack;
  }

  @Override
  public void onItemClick(ItemClickEvent event) {
    super.onItemClick(event);
    StrifePlugin.getInstance().getSoulManager().revive(event.getPlayer(), restoreXP);
    event.setWillClose(true);
  }
}
