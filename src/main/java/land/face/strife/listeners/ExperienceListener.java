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

import static com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils.sendMessage;

import com.tealcube.minecraft.bukkit.TextUtils;
import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;
import com.tealcube.minecraft.bukkit.facecore.utilities.TitleUtils;
import java.util.List;
import java.util.Set;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.champion.Champion;
import land.face.strife.events.UniqueKillEvent;
import land.face.strife.util.StatUtil;
import org.bukkit.Bukkit;
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

  private List<String> penaltyFreeWorlds;

  public ExperienceListener(StrifePlugin plugin) {
    this.plugin = plugin;
    penaltyFreeWorlds = plugin.getSettings().getStringList("config.penalty-free-worlds");
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onXpShare(EntityDeathEvent event) {
    if (event.getDroppedExp() == 0 || event.getEntity().hasMetadata("SPAWNED")) {
      return;
    }
    StrifeMob mob = plugin.getStrifeMobManager().getMobUnsafe(event.getEntity().getUniqueId());
    if (mob == null) {
      return;
    }

    Player killer = mob.getKiller();
    if (killer == null) {
      killer = event.getEntity().getKiller();
      if (killer == null) {
        event.setDroppedExp(0);
        return;
      }
    }

    UniqueKillEvent ev = new UniqueKillEvent(mob, killer);
    Bukkit.getPluginManager().callEvent(ev);

    float droppedXp = event.getDroppedExp();
    event.setDroppedExp(0);

    int mobLevel = StatUtil.getMobLevel(event.getEntity());
    int highestPlayerLevel = mobLevel;
    int lowestPlayerLevel = mobLevel;

    Set<Player> killers = plugin.getSnazzyPartiesHook()
        .getNearbyPartyMembers(killer, event.getEntity().getLocation(), 30);

    for (Player player : killers) {
      if (player.getLevel() > highestPlayerLevel) {
        highestPlayerLevel = player.getLevel();
      }
      if (player.getLevel() < lowestPlayerLevel) {
        lowestPlayerLevel = player.getLevel();
      }
    }

    int levelDiff = Math.max(Math.abs(mobLevel - highestPlayerLevel),
        Math.abs(mobLevel - lowestPlayerLevel));
    for (Player player : killers) {
      float expMultiplier = (1f / killers.size()) + ((killers.size() - 1) * 0.2f);
      if (levelDiff > 7) {
        expMultiplier *= Math.pow(0.98, Math.pow(levelDiff - 7, 2));
      }
      plugin.getExperienceManager().addExperience(player, (droppedXp * expMultiplier), false);
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onPlayerDeath(PlayerDeathEvent event) {
    event.setKeepLevel(true);
    event.setDroppedExp(5 + event.getEntity().getLevel() / 2);
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onPlayerRespawn(PlayerRespawnEvent event) {
    Player p = event.getPlayer();
    if (penaltyFreeWorlds.contains(p.getWorld().getName())) {
      return;
    }
    if (p.getLevel() >= 100) {
      return;
    }
    PlayerInventory inv = p.getInventory();
    double lostXP;
    if (hadSoulShard(inv)) {
      lostXP = 0;
      sendMessage(p, "&a&oYou consumed a &f&oSoul Shard&a&o! You lost &f&o0 XP&a&o!");
    } else {
      double xpToLevel = plugin.getLevelingRate().get(p.getLevel());
      lostXP = Math.min(xpToLevel * 0.025, p.getExp() * xpToLevel);
      sendMessage(p, "<red>You lost <gold>" + (int) lostXP + " XP<red>!");
      p.setExp(Math.max(p.getExp() - 0.025f, 0.00001f));
    }
    plugin.getSoulManager().setLostExp(p, lostXP);
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
    String upperTitle = TextUtils.color("&aLEVEL UP!");
    String lowerTitle = TextUtils.color("&aYou've reached &fLevel " + event.getNewLevel());
    TitleUtils.sendTitle(event.getPlayer(), upperTitle, lowerTitle, 20, 5, 5);
    plugin.getChampionManager().updateAll(champion);

    player.setHealth(player.getMaxHealth());
    plugin.getEnergyManager().changeEnergy(player, Integer.MAX_VALUE, false);
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onPlayerExpChange(PlayerExpChangeEvent event) {
    plugin.getExperienceManager().addExperience(event.getPlayer(), event.getAmount(), false);
    event.setAmount(0);
  }

  private boolean hadSoulShard(PlayerInventory inv) {
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
        return true;
      }
    }
    return false;
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
