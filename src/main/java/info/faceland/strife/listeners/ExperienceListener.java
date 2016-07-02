/**
 * The MIT License
 * Copyright (c) 2015 Teal Cube Games
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package info.faceland.strife.listeners;

import be.maximvdw.titlemotd.ui.Title;
import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;
import com.tealcube.minecraft.bukkit.shade.fanciful.FancyMessage;
import com.tealcubegames.minecraft.spigot.versions.actionbars.ActionBarMessager;
import com.tealcubegames.minecraft.spigot.versions.api.actionbars.ActionBarMessage;

import info.faceland.strife.StrifePlugin;
import info.faceland.strife.attributes.StrifeAttribute;
import info.faceland.strife.data.Champion;

import me.desht.dhutils.ExperienceManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class ExperienceListener implements Listener {

    private final StrifePlugin plugin;

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
        if (plugin.getSettings().getStringList("config.penalty-free-worlds").contains(p.getWorld().getName())) {
            return;
        }
        double lostXP = Math.min(plugin.getLevelingRate().get(p.getLevel()) * 0.025, plugin.getLevelingRate()
                .get(p.getLevel()) * p.getExp());
        MessageUtils.sendMessage(p, "<red>You lost <gold>" + (int) lostXP + " XP<red>!");
        p.setExp(Math.max(p.getExp() - 0.025f, 0.001f));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerLevelChange(PlayerLevelChangeEvent event) {
        Player player = event.getPlayer();
        Champion champion = plugin.getChampionManager().getChampion(player.getUniqueId());
        if (event.getOldLevel() < event.getNewLevel()) {
            ActionBarMessage xpBarMsg = ActionBarMessager.createActionBarMessage("&a&l( &f&lDANG &a&l/ &f&lSON! &a&l)");
            xpBarMsg.send(player);
        } else {
            ActionBarMessage xpBarMsg = ActionBarMessager.createActionBarMessage("&c&l( &f&lDANG &c&l/ &f&lSON! &c&l)");
            xpBarMsg.send(player);
        }
        if (event.getNewLevel() <= champion.getHighestReachedLevel()) {
            return;
        }
        int points = 2 * (event.getNewLevel() - event.getOldLevel());
        champion.setHighestReachedLevel(event.getNewLevel());
        champion.setUnusedStatPoints(champion.getUnusedStatPoints() + points);
        plugin.getChampionManager().removeChampion(champion.getUniqueId());
        plugin.getChampionManager().addChampion(champion);
        MessageUtils.sendMessage(player, "<green>You have leveled up!");
        FancyMessage message = new FancyMessage("");
        message.then("You gained 2 levelpoints! ").color(ChatColor.GOLD).then("CLICK HERE").command("/levelup")
                .color(ChatColor.WHITE).then(" or use ").color(ChatColor.GOLD).then("/levelup")
                .color(ChatColor.WHITE).then(" to spend them and raise your stats!").color(ChatColor.GOLD).send(event
                .getPlayer());
        Title title = new Title("§aLEVEL UP!", "&aYou gained §f" + points + " §aLevelpoints!", 1, 1, 1);
        title.setTimingsToSeconds();
        title.send(event.getPlayer());
        if (event.getNewLevel() % 5 == 0) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                MessageUtils.sendMessage(p, "&a&lLevelup! &f" + player.getDisplayName() + " &ahas reached level &f" +
                        +event.getNewLevel() + "&a!");
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerExpChange(PlayerExpChangeEvent event) {
        Player player = event.getPlayer();
        if (player.getLevel() >= 100) {
            event.setAmount(0);
            return;
        }

        // Get all the values!
        Integer maxFaceExpInt = plugin.getLevelingRate().get(player.getLevel());
        Integer maxVanillaExp = player.getExpToLevel();
        double amount = event.getAmount();
        double currentExpPercent = player.getExp();
        double faceExpToLevel;

        if (maxFaceExpInt == null || maxFaceExpInt == 0) {
            event.setAmount(0);
            return;
        }

        // Apply bonuses and limits to the amount
        double maxFaceExp = maxFaceExpInt;
        ExperienceManager experienceManager = new ExperienceManager(player);
        Champion champion = plugin.getChampionManager().getChampion(player.getUniqueId());
        double xpMult = plugin.getSettings().getDouble("config.xp-bonus", 0.0);
        double bonusMult = 1 + xpMult + champion.getCache().getAttribute(StrifeAttribute.XP_GAIN);

        amount *= bonusMult;
        amount = Math.min(amount, (maxFaceExp / Math.pow(player.getLevel(), 1.5)) * bonusMult);

        faceExpToLevel = maxFaceExp * (1 - currentExpPercent);

        if (amount > faceExpToLevel) {
            player.setExp(0);
            amount -= faceExpToLevel;
            currentExpPercent = 0;
            player.setLevel(player.getLevel() + 1);
            maxFaceExp = plugin.getLevelingRate().get(player.getLevel());
        }

        double remainingExp = amount + (currentExpPercent * maxFaceExp);
        String xpMsg = "&a&l( &f&l" + (int) remainingExp + " &a&l/ &f&l" + (int) maxFaceExp + " XP &a&l)";
        ActionBarMessage xpBarMsg = ActionBarMessager.createActionBarMessage(xpMsg);
        xpBarMsg.send(player);

        double gainedExpPercent = amount * (maxVanillaExp / maxFaceExp);
        event.setAmount(0);
        experienceManager.changeExp(gainedExpPercent);
    }
}
