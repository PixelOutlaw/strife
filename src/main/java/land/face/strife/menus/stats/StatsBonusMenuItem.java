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
package land.face.strife.menus.stats;

import io.pixeloutlaw.minecraft.spigot.garbage.StringExtensionsKt;
import java.util.ArrayList;
import java.util.List;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.stats.StrifeStat;
import ninja.amp.ampmenus.events.ItemClickEvent;
import ninja.amp.ampmenus.items.MenuItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class StatsBonusMenuItem extends MenuItem {

  private final StatsMenu statsMenu;

  StatsBonusMenuItem(StatsMenu statsMenu) {
    super(StringExtensionsKt.chatColorize("&a&lDrop Modifiers"), new ItemStack(Material.GOLD_INGOT));
    this.statsMenu = statsMenu;
  }

  @Override
  public ItemStack getFinalIcon(Player commandSender) {
    Player player = statsMenu.getInspectionTargetMap().get(commandSender);
    if (!player.isValid()) {
      return getIcon();
    }
    StrifeMob pStats = StrifePlugin.getInstance().getStrifeMobManager().getStatMob(player);
    ItemStack itemStack = new ItemStack(Material.GOLD_INGOT);
    ItemMeta itemMeta = Bukkit.getItemFactory().getItemMeta(itemStack.getType());
    itemMeta.setDisplayName(getDisplayName());
    List<String> lore = new ArrayList<>();

    lore.add(StatsMenu.breakLine);

    int totalCombatXp = Math.round(pStats.getStat(StrifeStat.XP_GAIN));
    lore.add(ChatColor.GREEN + "Combat XP: " + ChatColor.WHITE + "+" + totalCombatXp + "%");
    int running = Math.round(StrifePlugin.getInstance().getBoostManager().getStats().getOrDefault(StrifeStat.XP_GAIN, 0f));
    int contributors = StrifePlugin.getInstance().getBoostManager().getContributorSize();
    int buffs = Math.round(pStats.getBuffStats().getOrDefault(StrifeStat.XP_GAIN, 0f));
    if (buffs > 0 || running > 0 || contributors > 0) {
      int fromItems = totalCombatXp - buffs - running;
      if (fromItems > 0) {
        lore.add(ChatColor.GRAY + " +" + fromItems + "% From Items");
      }
      if (buffs > 0) {
        lore.add(ChatColor.GRAY + " +" + buffs + "% From Buffs");
      }
      if (running - contributors > 0) {
        lore.add(ChatColor.GRAY + " +" + (running - contributors) + "% From Global Boosts");
      }
      if (contributors > 0) {
        lore.add(ChatColor.GRAY + " +" + contributors + "% From Contributors");
      }
    }

    int totalSkillXp = Math.round(pStats.getStat(StrifeStat.SKILL_XP_GAIN));
    lore.add(ChatColor.GREEN + "Skill XP: " + ChatColor.WHITE + "+" + totalSkillXp + "%");
    int running2 = Math.round(StrifePlugin.getInstance().getBoostManager().getStats().getOrDefault(StrifeStat.SKILL_XP_GAIN, 0f));
    int boosters = StrifePlugin.getInstance().getBoostManager().getDiscordBoostSize() * 2;
    int buffs2 = Math.round(pStats.getBuffStats().getOrDefault(StrifeStat.SKILL_XP_GAIN, 0f));
    if (buffs2 > 0 || running2 > 0 || boosters > 0) {
      int skillXpFromItems = totalSkillXp - buffs2 - running2;
      if (skillXpFromItems > 0) {
        lore.add(ChatColor.GRAY + " +" + skillXpFromItems + "% From Items");
      }
      if (buffs2 > 0) {
        lore.add(ChatColor.GRAY + " +" + buffs2 + "% From Buffs");
      }
      if (running2 - boosters > 0) {
        lore.add(ChatColor.GRAY + " +" + (running2 - boosters) + "% From Global Boosts");
      }
      if (boosters > 0) {
        lore.add(ChatColor.GRAY + " +" + boosters + "% From Discord Boosters");
      }
    }

    lore.add(ChatColor.GREEN + "Loot Bonus: " + ChatColor.WHITE + "+" + StatsMenu.INT_FORMAT
        .format(pStats.getStat(StrifeStat.ITEM_DISCOVERY)) + "%");
    lore.add(ChatColor.GREEN + "Loot Rarity: " + ChatColor.WHITE + "+" + StatsMenu.INT_FORMAT
        .format(pStats.getStat(StrifeStat.ITEM_RARITY)) + "%");
    lore.add(ChatColor.GREEN + "Gold Bonus: " + ChatColor.WHITE + "+" + StatsMenu.INT_FORMAT
        .format(pStats.getStat(StrifeStat.GOLD_FIND)) + "%");

    if (pStats.getStat(StrifeStat.FISHING_SPEED) > 0F) {
      lore.add(ChatColor.GREEN + "Fishing Speed: " + ChatColor.WHITE + "+" + StatsMenu.INT_FORMAT
          .format(pStats.getStat(StrifeStat.FISHING_SPEED)) + "%");
    }
    if (pStats.getStat(StrifeStat.FISHING_TREASURE) > 0F) {
      lore.add(ChatColor.GREEN + "Fishing Treasures: " + ChatColor.WHITE + "+" + StatsMenu.INT_FORMAT
          .format(pStats.getStat(StrifeStat.FISHING_TREASURE)) + "%");
    }
    if (pStats.getStat(StrifeStat.MINING_GEMS) > 0F) {
      lore.add(ChatColor.GREEN + "Mining Gems: " + ChatColor.WHITE + "+" + StatsMenu.INT_FORMAT
          .format(pStats.getStat(StrifeStat.MINING_GEMS)) + "%");
    }

    lore.add(StatsMenu.breakLine);

    lore.add(StringExtensionsKt.chatColorize("&8&oUse &7&o/help stats &8&ofor info!"));

    itemMeta.setLore(lore);
    itemStack.setItemMeta(itemMeta);
    return itemStack;
  }

  @Override
  public void onItemClick(ItemClickEvent event) {
    super.onItemClick(event);
  }

}
