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
package info.faceland.strife.commands;

import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;
import info.faceland.strife.StrifePlugin;
import info.faceland.strife.attributes.AttributeHandler;
import info.faceland.strife.data.Champion;
import info.faceland.strife.stats.StrifeStat;
import org.bukkit.entity.Player;
import se.ranzdo.bukkit.methodcommand.Arg;
import se.ranzdo.bukkit.methodcommand.Command;

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
