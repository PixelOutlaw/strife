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
package land.face.strife.listeners;

import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;
import land.face.strife.StrifePlugin;
import land.face.strife.data.champion.Champion;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class ExperienceListener implements Listener {

  private final StrifePlugin plugin;

  private static final String LEVEL_UP = "&a&l( &f&lDANG &a&l/ &f&lSON! &a&l)";
  private static final String LEVEL_DOWN = "&c&l( &f&lDANG &c&l/ &f&lSON! &c&l)";

  public ExperienceListener(StrifePlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onPlayerDeath(PlayerDeathEvent event) {
    event.setKeepLevel(true);
    event.setDroppedExp(1 + event.getEntity().getLevel() / 2);
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onPlayerRespawn(PlayerRespawnEvent event) {
    Player p = event.getPlayer();
    if (plugin.getSettings().getStringList("config.penalty-free-worlds")
        .contains(p.getWorld().getName())) {
      return;
    }
    if (p.getLevel() >= 100) {
      return;
    }
    PlayerInventory inv = p.getInventory();
    for (int i = 0; i < inv.getContents().length; i++) {
      ItemStack is = inv.getItem(i);
      if (is == null) {
        continue;
      }
      if (isSoulShard(is)) {
        if (is.getAmount() > 1) {
          is.setAmount(is.getAmount() - 1);
          inv.setItem(i, is);
        } else {
          inv.setItem(i, null);
        }
        MessageUtils
            .sendMessage(p, "&a&oYou consumed a &f&oSoul Shard&a&o! You lost &f&o0 XP&a&o!");
        return;
      }
    }
    double lostXP = Math
        .min(plugin.getLevelingRate().get(p.getLevel()) * 0.025, plugin.getLevelingRate()
            .get(p.getLevel()) * p.getExp());
    MessageUtils.sendMessage(p, "<red>You lost <gold>" + (int) lostXP + " XP<red>!");
    p.setExp(Math.max(p.getExp() - 0.025f, 0.001f));
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onPlayerLevelChange(PlayerLevelChangeEvent event) {
    Player player = event.getPlayer();
    Champion champion = plugin.getChampionManager().getChampion(player);
    if (event.getOldLevel() < event.getNewLevel()) {
      MessageUtils.sendActionBar(player, LEVEL_UP);
    } else {
      MessageUtils.sendActionBar(player, LEVEL_DOWN);
    }
    if (event.getNewLevel() <= champion.getHighestReachedLevel()) {
      return;
    }
    int points = event.getNewLevel() - event.getOldLevel();
    champion.setHighestReachedLevel(event.getNewLevel());
    champion.setUnusedStatPoints(champion.getUnusedStatPoints() + points);
    plugin.getChampionManager().updateAll(champion);
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onPlayerExpChange(PlayerExpChangeEvent event) {
    plugin.getExperienceManager().addExperience(event.getPlayer(), event.getAmount(), false);
    event.setAmount(0);
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onKillMinion(EntityDeathEvent event) {
    if (plugin.getMinionManager().isMinion(event.getEntity())) {
      event.setDroppedExp(0);
      event.getDrops().clear();
    }
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onEntityDeathAutoOrb(EntityDeathEvent event) {
    if (event.getEntity().getKiller() == null) {
      event.setDroppedExp(0);
      return;
    }
    plugin.getExperienceManager()
        .addExperience(event.getEntity().getKiller(), event.getDroppedExp(), false);
    event.setDroppedExp(0);
  }

  private boolean isSoulShard(ItemStack itemStack) {
    if (itemStack.getType() != Material.QUARTZ) {
      return false;
    }
    if (!itemStack.hasItemMeta() || !itemStack.getItemMeta().hasDisplayName()) {
      return false;
    }
    return itemStack.getItemMeta().getDisplayName().equals(ChatColor.WHITE + "Soul Shard");
  }
}
