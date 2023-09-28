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
import com.tealcube.minecraft.bukkit.facecore.utilities.TextUtils;
import io.pixeloutlaw.minecraft.spigot.garbage.StringExtensionsKt;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import land.face.strife.StrifePlugin;
import ninja.amp.ampmenus.events.ItemClickEvent;
import ninja.amp.ampmenus.items.MenuItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

public class ReviveConfirmItem extends MenuItem {

  private final ReviveMenu reviveMenu;

  private final Map<Player, ItemStack> cachedIcon = new WeakHashMap<>();

  ReviveConfirmItem(ReviveMenu reviveMenu) {
    super("", new ItemStack(Material.BARRIER));
    ItemStackExtensionsKt.setCustomModelData(getIcon(), 50);
    this.reviveMenu = reviveMenu;
  }

  @Override
  public ItemStack getFinalIcon(Player player) {
    if (cachedIcon.containsKey(player)) {
      return cachedIcon.get(player);
    }

    String reviverName = reviveMenu.getDataMap().get(player.getUniqueId()).getLeft();
    int restoreXP = reviveMenu.getDataMap().get(player.getUniqueId()).getRight();

    ItemStack stack = getIcon().clone();
    List<String> baseLore = StrifePlugin.getInstance().getSettings()
        .getStringList("language.revive.confirm-lore");
    List<String> newLore = new ArrayList<>();
    for (String s : baseLore) {
      newLore.add(s.replace("{name}", reviverName).replace("{xp}", String.valueOf(restoreXP)));
    }
    String name = StrifePlugin.getInstance().getSettings()
        .getString("language.revive.confirm-name", "dew it");
    ItemStackExtensionsKt.setDisplayName(stack, PaletteUtil.color(name));
    TextUtils.setLore(stack, PaletteUtil.color(newLore), false);
    stack.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

    cachedIcon.put(player, stack);
    Bukkit.getScheduler().runTaskLater(StrifePlugin.getInstance(),
        () -> cachedIcon.remove(player), 2);
    return stack;
  }

  @Override
  public void onItemClick(ItemClickEvent event) {
    super.onItemClick(event);
    int restoreXP = reviveMenu.getDataMap().get(event.getPlayer().getUniqueId()).getRight();
    StrifePlugin.getInstance().getSoulManager().revive(event.getPlayer(), restoreXP);
    reviveMenu.getDataMap().remove(event.getPlayer().getUniqueId());
    event.setWillClose(true);
  }
}
