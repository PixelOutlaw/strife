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
package info.faceland.strife.commands;

import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;
import com.tealcube.minecraft.bukkit.kern.methodcommand.Arg;
import com.tealcube.minecraft.bukkit.kern.methodcommand.Command;

import info.faceland.strife.StrifePlugin;
import info.faceland.strife.attributes.AttributeHandler;
import info.faceland.strife.data.Champion;
import info.faceland.strife.stats.StrifeStat;

import org.bukkit.entity.Player;

public class LevelUpCommand {

    private final StrifePlugin plugin;

    public LevelUpCommand(StrifePlugin plugin) {
        this.plugin = plugin;
    }

    @Command(identifier = "levelup")
    public void baseCommand(Player sender) {
        plugin.getLevelupMenu().open(sender);
    }

    @Command(identifier = "levelup level", permissions = "strife.command.levelup")
    public void levelSubCommand(Player sender, @Arg(name = "stat") String name) {
        StrifeStat stat = plugin.getStatManager().getStatByName(name);
        if (stat == null) {
            MessageUtils.sendMessage(sender, "<red>That is not a valid stat.");
            return;
        }
        Champion champion = plugin.getChampionManager().getChampion(sender.getUniqueId());
        if (champion.getUnusedStatPoints() <= 0) {
            MessageUtils.sendMessage(sender, "<red>You must have unused stat points in order to level up.");
            return;
        }
        int currentLevel = champion.getLevel(stat);
        if (currentLevel + 1 > champion.getMaximumStatLevel()) {
            MessageUtils.sendMessage(sender, "<red>You cannot level up that stat at the moment.");
            return;
        }
        champion.setLevel(stat, currentLevel + 1);
        champion.setUnusedStatPoints(champion.getUnusedStatPoints() - 1);
        plugin.getChampionManager().removeChampion(champion.getUniqueId());
        plugin.getChampionManager().addChampion(champion);
        MessageUtils
            .sendMessage(sender, "<green>You leveled up <white>%stat%<green>.",
                         new String[][]{{"%stat%", stat.getName()}});
        AttributeHandler.updateHealth(champion.getPlayer(), champion.getAttributeValues());
    }

}
