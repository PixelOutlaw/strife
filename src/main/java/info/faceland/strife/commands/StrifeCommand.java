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
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StrifeCommand {

    private final StrifePlugin plugin;

    public StrifeCommand(StrifePlugin plugin) {
        this.plugin = plugin;
    }

    @Command(identifier = "strife profile", permissions = "strife.command.strife.profile", onlyPlayers = false)
    public void profileCommand(CommandSender sender, @Arg(name = "target") Player target) {
        Champion champion = plugin.getChampionManager().getChampion(target.getUniqueId());
        Chatty.sendMessage(sender, "<gold>----------------------------------");
        Chatty.sendMessage(sender, "<gray>Unused Stat Points: <white>%amount%", new String[][]{{"%amount%", "" + champion.getUnusedStatPoints()}});
        Chatty.sendMessage(sender, "<gold>----------------------------------");
        for (StrifeStat stat : plugin.getStatManager().getStats()) {
            Chatty.sendMessage(sender,
                               ChatColor.GRAY + " [ " + champion.getLevel(stat) + " / " + champion.getMaximumStatLevel() + " ] " +
                               stat.getName());
        }
        Chatty.sendMessage(sender, "<gold>----------------------------------");
    }

    @Command(identifier = "strife reset", permissions = "strife.command.strife.reset", onlyPlayers = false)
    public void resetCommand(CommandSender sender, @Arg(name = "target") Player target) {
        Champion champion = plugin.getChampionManager().getChampion(target.getUniqueId());
        for (StrifeStat stat : plugin.getStatManager().getStats()) {
            int lev = champion.getLevel(stat);
            champion.setUnusedStatPoints(champion.getUnusedStatPoints() + lev);
            champion.setLevel(stat, 0);
        }
        plugin.getChampionManager().removeChampion(champion.getUniqueId());
        plugin.getChampionManager().addChampion(champion);
        Chatty.sendMessage(sender, "<green>You reset <white>%player%<green>.", new String[][]{{"%player%", target.getDisplayName()}});
        Chatty.sendMessage(target, "<green>Your stats have been reset.");
        IPrettyMessage message = PrettyMessageFactory.buildPrettyMessage();
        message.then("You have unspent stat points. ").color(ChatColor.GOLD).then("Click here").command("/levelup")
               .color(ChatColor.WHITE).then(" or use ").color(ChatColor.GOLD).then("/levelup")
               .color(ChatColor.WHITE).then(" to spend them.").send(target);
        AttributeHandler.updateHealth(target, champion.getAttributeValues());
    }

    @Command(identifier = "strife clear", permissions = "strife.command.strife.clear", onlyPlayers = false)
    public void clearCommand(CommandSender sender, @Arg(name = "target") Player target) {
        target.setExp(0f);
        target.setLevel(0);
        Champion champion = plugin.getChampionManager().getChampion(target.getUniqueId());
        for (StrifeStat stat : plugin.getStatManager().getStats()) {
            champion.setLevel(stat, 0);
        }
        champion.setUnusedStatPoints(0);
        champion.setHighestReachedLevel(0);
        plugin.getChampionManager().removeChampion(champion.getUniqueId());
        plugin.getChampionManager().addChampion(champion);
        Chatty.sendMessage(sender, "<green>You cleared <white>%player%<green>.", new String[][]{{"%player%", target.getDisplayName()}});
        Chatty.sendMessage(target, "<green>Your stats have been cleared.");
        AttributeHandler.updateHealth(target, champion.getAttributeValues());
    }

    @Command(identifier = "strife raise", permissions = "strife.command.strife.raise", onlyPlayers = false)
    public void raiseCommand(CommandSender sender, @Arg(name = "target") Player target, @Arg(name = "level") int newLevel) {
        int oldLevel = target.getLevel();
        if (newLevel <= oldLevel) {
            Chatty.sendMessage(sender, "<red>New level must be higher than old level.");
        }
        target.setExp(0f);
        target.setLevel(newLevel);
        Champion champion = plugin.getChampionManager().getChampion(target.getUniqueId());
        int diff = newLevel - oldLevel;
        champion.setUnusedStatPoints(champion.getUnusedStatPoints() + diff - 1);
        plugin.getChampionManager().removeChampion(champion.getUniqueId());
        plugin.getChampionManager().addChampion(champion);
        Chatty.sendMessage(sender, "<green>You raised <white>%player%<green> to level <white>%level%<green>.",
                           new String[][]{{"%player%", target.getDisplayName()}, {"%level%", "" + newLevel}});
        Chatty.sendMessage(target, "<green>Your level has been raised.");
        AttributeHandler.updateHealth(target, champion.getAttributeValues());
    }

}
