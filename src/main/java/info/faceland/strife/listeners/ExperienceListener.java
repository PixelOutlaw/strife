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
import com.tealcube.minecraft.bukkit.shade.objecthunter.exp4j.Expression;
import com.tealcube.minecraft.bukkit.shade.objecthunter.exp4j.ExpressionBuilder;
import info.faceland.strife.StrifePlugin;
import info.faceland.strife.attributes.StrifeAttribute;
import info.faceland.strife.data.Champion;
import me.desht.dhutils.ExperienceManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerLevelChangeEvent;

import java.util.Map;

public class ExperienceListener implements Listener {

    private final StrifePlugin plugin;
    private final Expression expCapExpr;

    public ExperienceListener(StrifePlugin plugin) {
        this.plugin = plugin;
        this.expCapExpr = new ExpressionBuilder(plugin.getSettings().getString("config.leveling.formula")).variable("LEVEL").build();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        event.setKeepLevel(true);
        event.setDroppedExp(0);
        event.getEntity().setExp(Math.max(event.getEntity().getExp() - 0.1f, 0.001f));
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
                MessageUtils
                        .sendMessage(p, "<green>[Levelup!] <white>%player%<green> has reached level <white>%level%<green>!",
                                new String[][]{{"%player%", player.getDisplayName()},
                                        {"%level%", "" + event.getNewLevel()}});
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

        double mult = 1D;
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

        double factor = (double) defaultLevelUp / (double) desiredLevelUp;
        double exact = Math.min(amount * (1 + attributeDoubleMap.get(StrifeAttribute.XP_GAIN) + mult),
                expCapExpr.setVariable("LEVEL", player.getLevel() + 1).evaluate() * desiredLevelUp) * factor;

        int newXp = (int) exact;

        if (player.hasPermission("strife.xp")) {
            MessageUtils.sendMessage(player, "XP Orb value: " + event.getAmount() + " | Adjusted amount: " + newXp);
        }

        event.setAmount(newXp);

        if (exact > newXp) {
            experienceManager.changeExp(exact - newXp);
        }

        double perc = 0.2;
        for (Entity e : player.getNearbyEntities(8, 8, 8)) {
            if (!(e instanceof Player)) {
                continue;
            }
            ExperienceManager manager = new ExperienceManager((Player) e);
            manager.changeExp(perc * exact);
        }
    }

}
