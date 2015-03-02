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

import com.tealcube.minecraft.bukkit.kern.fanciful.FancyMessage;
import com.tealcube.minecraft.bukkit.kern.methodcommand.Command;
import info.faceland.strife.StrifePlugin;
import info.faceland.strife.attributes.StrifeAttribute;
import info.faceland.strife.data.Champion;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.util.Map;

public class AttributesCommand {

    private final StrifePlugin plugin;
    private static final DecimalFormat FORMAT = new DecimalFormat("#.##");

    public AttributesCommand(StrifePlugin plugin) {
        this.plugin = plugin;
    }

    @Command(identifier = "stats", permissions = "strife.command.stats")
    public void baseCommand(Player sender) {
        Champion champion = plugin.getChampionManager().getChampion(sender.getUniqueId());
        Map<StrifeAttribute, Double> valueMap = champion.getAttributeValues();
        for (StrifeAttribute attribute : StrifeAttribute.values()) {
            double val = valueMap.get(attribute);
            FancyMessage message = new FancyMessage("");
            if (attribute == StrifeAttribute.ATTACK_SPEED) {
                message.then(attribute.getName()).color(attribute.getDisplayColor()).tooltip(attribute.getDescription()).then(":")
                       .color(ChatColor.DARK_GRAY).then(" ").then(FORMAT.format(100D * (attribute.getBaseValue() / attribute.getBaseValue() + val)));
            } else if (attribute == StrifeAttribute.XP_GAIN || attribute == StrifeAttribute.ITEM_DISCOVERY || attribute == StrifeAttribute.GOLD_FIND) {
                message.then(attribute.getName()).color(attribute.getDisplayColor()).tooltip(attribute.getDescription()).then(":")
                        .color(ChatColor.DARK_GRAY).then(" ").then(FORMAT.format(attribute.isPercentage() ? val * 100
                        + 100 : val + 100));
            } else {
                message.then(attribute.getName()).color(attribute.getDisplayColor()).tooltip(attribute.getDescription()).then(":")
                       .color(ChatColor.DARK_GRAY).then(" ").then(FORMAT.format(attribute.isPercentage() ? val * 100 : val));
            }
            message.color(ChatColor.WHITE);
            if (attribute.isPercentage()) {
                message.then("%").color(ChatColor.WHITE);
            }
            message.send(sender);
        }
    }

}
