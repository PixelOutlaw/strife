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
package info.faceland.strife.menus;

import static info.faceland.strife.menus.StatsMenu.breakLine;

import com.tealcube.minecraft.bukkit.TextUtils;
import info.faceland.strife.StrifePlugin;
import info.faceland.strife.data.AttributedEntity;
import info.faceland.strife.data.LoreAbility;
import info.faceland.strife.managers.LoreAbilityManager.TriggerType;
import java.util.ArrayList;
import java.util.List;
import ninja.amp.ampmenus.events.ItemClickEvent;
import ninja.amp.ampmenus.items.MenuItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class StatsEffectMenuItem extends MenuItem {

  private final StrifePlugin plugin;
  private Player player;

  StatsEffectMenuItem(StrifePlugin plugin, Player player) {
    super(TextUtils.color("&6&Item Effects"), new ItemStack(Material.EMERALD));
    this.plugin = plugin;
    this.player = player;
  }

  StatsEffectMenuItem(StrifePlugin plugin) {
    super(TextUtils.color("&6&lItem Effects"), new ItemStack(Material.EMERALD));
    this.plugin = plugin;
  }

  @Override
  public ItemStack getFinalIcon(Player player) {
    if (this.player != null) {
      player = this.player;
    }
    AttributedEntity pStats = plugin.getAttributedEntityManager().getAttributedEntity(player);
    ItemStack itemStack = new ItemStack(Material.EMERALD);
    ItemMeta itemMeta = Bukkit.getItemFactory().getItemMeta(itemStack.getType());
    itemMeta.setDisplayName(getDisplayName());
    List<String> lore = new ArrayList<>();

    lore.add(breakLine);

    List<String> abilityLores = new ArrayList<>();
    for (TriggerType triggerType : pStats.getChampion().getLoreAbilities().keySet()) {
      for (LoreAbility la : pStats.getChampion().getLoreAbilities().get(triggerType)) {
        abilityLores.add(la.getTriggerText());
        abilityLores.addAll(la.getDescription());
      }
    }
    if (abilityLores.isEmpty()) {
      lore.add(TextUtils.color("&6You don't have an item effects! They"));
      lore.add(TextUtils.color("&6usually come from socket gems or"));
      lore.add(TextUtils.color("&6unique items."));
    } else {
      lore.addAll(abilityLores);
    }

    lore.add(breakLine);

    lore.add(TextUtils.color("&8&oUse &7&o/help stats &8&ofor info!"));

    itemMeta.setLore(lore);
    itemStack.setItemMeta(itemMeta);
    return itemStack;
  }

  @Override
  public void onItemClick(ItemClickEvent event) {
    super.onItemClick(event);
  }

}
