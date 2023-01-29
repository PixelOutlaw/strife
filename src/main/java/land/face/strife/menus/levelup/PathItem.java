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
package land.face.strife.menus.levelup;

import com.tealcube.minecraft.bukkit.facecore.utilities.TextUtils;
import io.pixeloutlaw.minecraft.spigot.garbage.StringExtensionsKt;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.util.ArrayList;
import java.util.List;
import land.face.strife.StrifePlugin;
import land.face.strife.data.LevelPath.Choice;
import land.face.strife.data.LevelPath.Path;
import land.face.strife.data.champion.Champion;
import ninja.amp.ampmenus.events.ItemClickEvent;
import ninja.amp.ampmenus.items.MenuItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PathItem extends MenuItem {

  private final StrifePlugin plugin;
  private final int levelRequirement;
  private final Path path;
  private ItemStack lockedIcon;
  private ItemStack unlockedIcon;

  PathItem(StrifePlugin plugin, int levelRequirement, Path path) {
    super("", new ItemStack(Material.MAGMA_CREAM));
    this.plugin = plugin;
    this.levelRequirement = levelRequirement;
    this.path = path;
    buildPathIcon(path);
  }

  @Override
  public ItemStack getFinalIcon(Player player) {
    if (player.getLevel() < levelRequirement) {
      return lockedIcon;
    }
    Champion champion = plugin.getChampionManager().getChampion(player);
    if (!champion.getSaveData().getPathMap().containsKey(path)) {
      return unlockedIcon;
    }
    ItemStack stack = plugin.getPathManager().getIcon(path,
        champion.getSaveData().getPathMap().get(path)).clone();
    stack.setType(Material.PAPER);
    ItemStackExtensionsKt.setCustomModelData(stack, 202);
    return stack;
  }

  @Override
  public void onItemClick(ItemClickEvent event) {
    super.onItemClick(event);
    Player player = event.getPlayer();
    if (player.getLevel() < levelRequirement) {
      event.setWillClose(false);
      event.setWillGoBack(false);
      event.setWillUpdate(false);
      return;
    }
    Champion champion = plugin.getChampionManager().getChampion(player);
    if (champion.getSaveData().getPathMap().containsKey(path)) {
      event.setWillClose(false);
      event.setWillGoBack(false);
      event.setWillUpdate(false);
      return;
    }
    event.setWillClose(false);
    plugin.getPathMenu(path).open(event.getPlayer());
  }

  private void buildPathIcon(Path path) {
    List<String> lore = new ArrayList<>();
    lore.add("&f&oThis choice may be made at level " + levelRequirement);
    lore.addAll(TextUtils.color(
        plugin.getPathManager().getLevelPath(path, Choice.OPTION_1).getLore()));
    lore.addAll(TextUtils.color(
        plugin.getPathManager().getLevelPath(path, Choice.OPTION_2).getLore()));
    lore.addAll(TextUtils.color(
        plugin.getPathManager().getLevelPath(path, Choice.OPTION_3).getLore()));

    lockedIcon = new ItemStack(Material.PAPER);
    ItemStackExtensionsKt.setDisplayName(lockedIcon, StringExtensionsKt.chatColorize("&8&l[ Locked ]"));
    ItemStackExtensionsKt.setCustomModelData(lockedIcon, 200);
    TextUtils.setLore(lockedIcon, TextUtils.color(lore));

    unlockedIcon = new ItemStack(Material.PAPER);
    ItemStackExtensionsKt.setDisplayName(unlockedIcon, TextUtils.color("&f&l&nClick to choose your path!"));
    ItemStackExtensionsKt.setCustomModelData(unlockedIcon, 201);
    TextUtils.setLore(unlockedIcon, TextUtils.color(lore));
  }
}
