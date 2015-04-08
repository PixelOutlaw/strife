/*
 * This file is part of Strife, licensed under the ISC License.
 *
 * Copyright (c) 2014 Richard Harrah
 *
 * Permission to use, copy, modify, and/or distribute this software for any purpose with or without fee is hereby granted,
 * provided that the above copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING ALL
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT,
 * INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF
 * THIS SOFTWARE.
 */
package info.faceland.strife.menus;

import com.tealcube.minecraft.bukkit.facecore.shade.amp.ampmenus.events.ItemClickEvent;
import com.tealcube.minecraft.bukkit.facecore.shade.amp.ampmenus.items.MenuItem;

import info.faceland.strife.StrifePlugin;
import info.faceland.strife.data.Champion;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class StatPointsMenuItem extends MenuItem {

  private static final String DISPLAY_NAME = ChatColor.WHITE + "Unused Levelup Points";
  private static final ItemStack DISPLAY_ICON = new ItemStack(Material.WOOL);
  private static final String[] DISPLAY_LORE = {ChatColor.GRAY + "Click a stat to spend your points!"};
  private final StrifePlugin plugin;

  public StatPointsMenuItem(StrifePlugin plugin) {
    super(DISPLAY_NAME, DISPLAY_ICON, DISPLAY_LORE);
    this.plugin = plugin;
  }

  @Override
  public ItemStack getFinalIcon(Player player) {
    ItemStack itemStack = super.getFinalIcon(player);
    Champion champion = plugin.getChampionManager().getChampion(player.getUniqueId());
    int stacks = champion.getUnusedStatPoints();
    itemStack.setAmount(stacks);
    return itemStack;
  }

  @Override
  public void onItemClick(ItemClickEvent event) {
    super.onItemClick(event);
  }

}
