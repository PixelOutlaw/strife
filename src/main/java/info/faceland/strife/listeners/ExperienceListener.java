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
package info.faceland.strife.listeners;

import be.maximvdw.titlemotd.ui.Title;
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
import org.nunnerycode.facecore.utilities.MessageUtils;
import org.nunnerycode.kern.fanciful.FancyMessage;

import java.util.Map;

public class ExperienceListener implements Listener {

    private final StrifePlugin plugin;

    public ExperienceListener(StrifePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        event.setKeepLevel(true);
        event.setDroppedExp(0);
        event.getEntity().setExp(Math.max(event.getEntity().getExp() - 0.05f, 0f));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerLevelChange(PlayerLevelChangeEvent event) {
        Player player = event.getPlayer();
        Champion champion = plugin.getChampionManager().getChampion(player.getUniqueId());
        if (event.getNewLevel() <= champion.getHighestReachedLevel()) {
            return;
        }
        champion.setHighestReachedLevel(event.getNewLevel());
        champion.setUnusedStatPoints(champion.getUnusedStatPoints() + 1);
        plugin.getChampionManager().removeChampion(champion.getUniqueId());
        plugin.getChampionManager().addChampion(champion);
        MessageUtils.sendMessage(player, "<green>You have leveled up!");
        FancyMessage message = new FancyMessage("");
        message.then("You gained a levelup point! ").color(ChatColor.GOLD).then("Click here").command("/levelup")
               .color(ChatColor.WHITE).then(" or use ").color(ChatColor.GOLD).then("/levelup")
               .color(ChatColor.WHITE).then(" to use it!").color(ChatColor.GOLD).send(event.getPlayer());
        Title title = new Title("<green>LEVEL UP!", "<green>You reached level <white>" + event.getNewLevel() +
                "<green>!", 1, 1, 1);
        title.setTimingsToSeconds();
        title.send(event.getPlayer());
        if (event.getNewLevel() % 5 == 0) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                MessageUtils.sendMessage(p, "<green>[Levelup!] <white>%player%<green> has reached level <white>%level%<green>!",
                        new String[][]{{"%player%", player.getDisplayName()}, {"%level%", "" + event.getNewLevel()}});
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerExpChange(PlayerExpChangeEvent event) {
        Player player = event.getPlayer();
        double amount = event.getAmount();

        ExperienceManager experienceManager = new ExperienceManager(player);
        Champion champion = plugin.getChampionManager().getChampion(player.getUniqueId());
        Map<StrifeAttribute, Double> attributeDoubleMap = champion.getAttributeValues();

        Integer desiredLevelUp = plugin.getLevelingRate().get(player.getLevel());
        Integer defaultLevelUp = player.getExpToLevel();

        if (desiredLevelUp == null || desiredLevelUp == 0) {
            return;
        }

        if (desiredLevelUp.intValue() == defaultLevelUp.intValue()) {
            event.setAmount(0);
            return;
        }

        double factor = (double) defaultLevelUp / (double) desiredLevelUp;
        double exact = Math.min(amount + amount * attributeDoubleMap.get(StrifeAttribute.XP_GAIN),
                plugin.getSettings().getDouble("config.leveling.gain-cap", 0.25) * desiredLevelUp) * factor;

        int newXp = (int) exact;

        if (player.hasPermission("strife.xp")) {
            MessageUtils.sendMessage(player, "XP Orb value: " + event.getAmount() + " | Adjusted amount: " + newXp);
        }

        event.setAmount(newXp);

        if (exact > newXp) {
            experienceManager.changeExp(exact - newXp);
        }
    }

}
