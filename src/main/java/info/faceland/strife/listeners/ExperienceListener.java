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

import com.tealcube.minecraft.bukkit.facecore.ui.ActionBarMessage;
import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;

import info.faceland.strife.StrifePlugin;
import info.faceland.strife.attributes.StrifeAttribute;
import info.faceland.strife.data.Champion;

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

import be.maximvdw.titlemotd.ui.Title;
import me.desht.dhutils.ExperienceManager;
import mkremins.fanciful.FancyMessage;

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
            ActionBarMessage.send(event.getPlayer(), "&a&l( &f&lDANG &a&l/ &f&lSON! &a&l)");
        } else {
            ActionBarMessage.send(event.getPlayer(), "&c&l( &f&lDANG &c&l/ &f&lSON! &c&l)");
        }
        if (event.getNewLevel() <= champion.getHighestReachedLevel()) {
            return;
        }
        champion.setHighestReachedLevel(event.getNewLevel());
        champion.setUnusedStatPoints(champion.getUnusedStatPoints() + 2);
        plugin.getChampionManager().removeChampion(champion.getUniqueId());
        plugin.getChampionManager().addChampion(champion);
        MessageUtils.sendMessage(player, "<green>You have leveled up!");
        FancyMessage message = new FancyMessage("");
        message.then("You gained 2 levelpoints! ").color(ChatColor.GOLD).then("CLICK HERE").command("/levelup")
                .color(ChatColor.WHITE).then(" or use ").color(ChatColor.GOLD).then("/levelup")
                .color(ChatColor.WHITE).then(" to spend them and raise your stats!").color(ChatColor.GOLD).send(event
                .getPlayer());
        Title title = new Title("<green>LEVEL UP!", "<green>You reached level <white>" + event.getNewLevel() +
                "<green>!", 1, 1, 1);
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
        double amount = Math.max(event.getAmount(), 1);

        ExperienceManager experienceManager = new ExperienceManager(player);
        Champion champion = plugin.getChampionManager().getChampion(player.getUniqueId());

        Integer desiredLevelUp = plugin.getLevelingRate().get(player.getLevel());
        Integer defaultLevelUp = player.getExpToLevel();

        if (desiredLevelUp == null || desiredLevelUp == 0) {
            return;
        }

        if (desiredLevelUp.intValue() == defaultLevelUp.intValue()) {
            event.setAmount(0);
            return;
        }

        double mult = 0D;
        if (player.hasPermission("strife.mult.75")) {
            mult = -0.25D;
        }
        if (player.hasPermission("strife.mult.125")) {
            mult = 0.25D;
        }
        if (player.hasPermission("strife.mult.150")) {
            mult = 0.5D;
        }
        if (player.hasPermission("strife.mult.175")) {
            mult = 0.75D;
        }
        if (player.hasPermission("strife.mult.200")) {
            mult = 1.0D;
        }

        double bonusMult = 1 + champion.getCache().getAttribute(StrifeAttribute.XP_GAIN) + mult;
        double factor = (double) defaultLevelUp / (double) desiredLevelUp;
        double exact = amount * bonusMult * factor;
        if (plugin.getSettings().getBoolean("config.verbose")) {
            Bukkit.getLogger().info("Incoming Orb Value: " + event.getAmount());
            Bukkit.getLogger().info("Strife XP to level: " + desiredLevelUp);
            Bukkit.getLogger().info("Vanilla XP to level: " + event.getPlayer().getExpToLevel());
            Bukkit.getLogger().info("Pre-Cap Orb Value: " + exact);
        }
        exact = Math.min(exact, (defaultLevelUp / (Math.pow(event.getPlayer().getLevel(), 1.62) + 1)) * bonusMult);
        if (plugin.getSettings().getBoolean("config.verbose")) {
            Bukkit.getLogger().info("Max Rate: " + defaultLevelUp / (Math.pow(event.getPlayer().getLevel(), 1.62) + 1));
            Bukkit.getLogger().info("Minimum Orbs: " + defaultLevelUp / (defaultLevelUp / (Math.pow(event.getPlayer()
                    .getLevel(), 1.62) + 1)));
            Bukkit.getLogger().info("Final Orb Value: " + exact);
            Bukkit.getLogger().info("Final Orb Percentage: " + (exact / defaultLevelUp) * 100 + "%");
        }

        int newXp = (int) exact;
        event.setAmount(newXp);

        double remainingXp = amount + desiredLevelUp * event.getPlayer().getExp();
        ActionBarMessage.send(event.getPlayer(), "&a&l( &f&l" + (int) remainingXp + " &a&l/ &f&l" + desiredLevelUp +
                " XP &a&l)");

        if (exact > newXp) {
            experienceManager.changeExp(exact);
        }
    }
}
