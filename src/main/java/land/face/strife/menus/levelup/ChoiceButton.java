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

import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;
import land.face.strife.StrifePlugin;
import land.face.strife.data.LevelPath.Choice;
import land.face.strife.data.LevelPath.Path;
import land.face.strife.data.champion.Champion;
import ninja.amp.ampmenus.events.ItemClickEvent;
import ninja.amp.ampmenus.items.MenuItem;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ChoiceButton extends MenuItem {

  private final StrifePlugin plugin;
  private final Path path;
  private final Choice choice;
  private final ItemStack icon;

  ChoiceButton(StrifePlugin plugin, Path path, Choice choice) {
    super("", new ItemStack(Material.PLAYER_HEAD));
    this.plugin = plugin;
    this.path = path;
    this.choice = choice;
    icon = plugin.getPathManager().getIcon(path, choice);
  }

  @Override
  public ItemStack getFinalIcon(Player player) {
    return icon;
  }

  @Override
  public void onItemClick(ItemClickEvent event) {
    super.onItemClick(event);
    Player p = event.getPlayer();
    Champion champion = plugin.getChampionManager().getChampion(p);
    champion.getSaveData().getPathMap().put(path, choice);
    plugin.getPathManager().buildPathBonus(champion);
    champion.recombineCache(plugin);
    p.playSound(p.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1f, 0.5f);
    MessageUtils.sendMessage(p, "&f&l&oYou have chosen your path!");
    event.setWillClose(true);
  }
}
