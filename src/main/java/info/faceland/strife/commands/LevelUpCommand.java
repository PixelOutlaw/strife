/******************************************************************************
 * Copyright (c) 2014, Richard Harrah                                         *
 *                                                                            *
 * Permission to use, copy, modify, and/or distribute this software for any purpose with or without fee is hereby granted, provided that the above copyright notice and this permission notice appear in all copies.
 *                                                                            *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 ******************************************************************************/

package info.faceland.strife.commands;

import info.faceland.facecore.shade.command.Arg;
import info.faceland.facecore.shade.command.Command;
import info.faceland.facecore.shade.voorhees.PrettyMessageFactory;
import info.faceland.facecore.shade.voorhees.api.IPrettyMessage;
import info.faceland.messaging.Chatty;
import info.faceland.strife.StrifePlugin;
import info.faceland.strife.attributes.AttributeHandler;
import info.faceland.strife.data.Champion;
import info.faceland.strife.stats.StrifeStat;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class LevelUpCommand {

    private final StrifePlugin plugin;

    public LevelUpCommand(StrifePlugin plugin) {
        this.plugin = plugin;
    }

    @Command(identifier = "levelup")
    public void baseCommand(Player sender) {
        Champion champion = plugin.getChampionManager().getChampion(sender.getUniqueId());
        Chatty.sendMessage(sender, "<gold>----------------------------------");
        Chatty.sendMessage(sender, "<gray>Unused Stat Points: <white>%amount%", new String[][]{{"%amount%", "" + champion.getUnusedStatPoints()}});
        Chatty.sendMessage(sender, "<gold>----------------------------------");
        for (StrifeStat stat : plugin.getStatManager().getStats()) {
            IPrettyMessage message = PrettyMessageFactory.buildPrettyMessage();
            message.then(ChatColor.GRAY + " [ " + champion.getLevel(stat) + " / " + champion.getMaximumStatLevel() + " ] " + stat.getName() + " ");
            if (champion.getLevel(stat) < champion.getMaximumStatLevel() && champion.getUnusedStatPoints() > 0) {
                message.then("[+]").color(ChatColor.GOLD).command("/levelup level " + stat.getKey()).tooltip(ChatColor.WHITE + "Level up.").then(" ");
            }
            message.then("[?]").color(ChatColor.WHITE).tooltip(ChatColor.WHITE + stat.getDescription());
            message.send(sender);
        }
        Chatty.sendMessage(sender, "<gold>----------------------------------");
    }

    @Command(identifier = "levelup level", permissions = "strife.command.levelup")
    public void levelSubCommand(Player sender, @Arg(name = "stat") String name) {
        StrifeStat stat = plugin.getStatManager().getStatByName(name);
        if (stat == null) {
            Chatty.sendMessage(sender, "<red>That is not a valid stat.");
            return;
        }
        Champion champion = plugin.getChampionManager().getChampion(sender.getUniqueId());
        if (champion.getUnusedStatPoints() <= 0) {
            Chatty.sendMessage(sender, "<red>You must have unused stat points in order to level up.");
            return;
        }
        int currentLevel = champion.getLevel(stat);
        if (currentLevel + 1 > champion.getMaximumStatLevel()) {
            Chatty.sendMessage(sender, "<red>You cannot level up that stat at the moment.");
            return;
        }
        champion.setLevel(stat, currentLevel + 1);
        champion.setUnusedStatPoints(champion.getUnusedStatPoints() - 1);
        plugin.getChampionManager().removeChampion(champion.getUniqueId());
        plugin.getChampionManager().addChampion(champion);
        Chatty.sendMessage(sender, "<green>You leveled up <white>%stat%<green>.", new String[][]{{"%stat%", stat.getName()}});
        AttributeHandler.updateHealth(champion.getPlayer(), champion.getAttributeValues());
    }

}
