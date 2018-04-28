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
package info.faceland.strife.managers;

import com.tealcube.minecraft.bukkit.TextUtils;
import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;
import com.tealcube.minecraft.bukkit.shade.fanciful.FancyMessage;
import gyurix.api.TitleAPI;
import gyurix.spigotlib.ChatAPI;
import info.faceland.strife.StrifePlugin;
import info.faceland.strife.api.StrifeExperienceManager;
import info.faceland.strife.attributes.StrifeAttribute;
import info.faceland.strife.data.AttributedEntity;
import info.faceland.strife.data.Champion;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ExperienceManager implements StrifeExperienceManager {

    private final StrifePlugin plugin;

    public ExperienceManager(StrifePlugin plugin) {
        this.plugin = plugin;
    }

    public void addExperience(Player player, double amount, boolean exact) {
        // Get all the values!
        double maxFaceExp = (double) getMaxFaceExp(player.getLevel());
        double currentExpPercent = player.getExp();

        AttributedEntity pStats = plugin.getEntityStatCache().getAttributedEntity(player);

        if (!exact) {
            double statsMult = pStats.getAttribute(StrifeAttribute.XP_GAIN) / 100;
            double globalMult = plugin.getSettings().getDouble("config.xp-bonus", 0.0);
            double eventMult = plugin.getMultiplierManager().getExpMult();
            double bonusMult = 1 + statsMult + globalMult + eventMult;
            amount = Math.min(amount, (maxFaceExp / Math.pow(player.getLevel(), 1.5)));
            amount *= bonusMult;
        }

        double faceExpToLevel = maxFaceExp * (1 - currentExpPercent);

        while (amount > faceExpToLevel) {
            player.setExp(0);
            amount -= faceExpToLevel;
            currentExpPercent = 0;
            Champion champion = plugin.getChampionManager().getChampion(player.getUniqueId());
            if (player.getLevel() < 100) {
                player.setLevel(player.getLevel() + 1);
                pushLevelUpSpam(player, player.getLevel() % 5 == 0);
            } else {
                champion.setBonusLevels(champion.getBonusLevels() + 1);
                pushBonusLevelUpSpam(player, champion.getBonusLevels(), champion.getBonusLevels() % 10 == 0);
            }
            maxFaceExp = (double) getMaxFaceExp(player.getLevel());
            faceExpToLevel = maxFaceExp;
        }

        double newExpPercent = currentExpPercent + amount / maxFaceExp;
        String xpMsg = "&a&l( &f&l" + (int) (newExpPercent * maxFaceExp) + " &a&l/ &f&l" + (int) maxFaceExp + " XP &a&l)";
        ChatAPI.sendJsonMsg(ChatAPI.ChatMessageType.ACTION_BAR, TextUtils.color(xpMsg), player);

        player.setExp((float) newExpPercent);
    }

    public Integer getMaxFaceExp(int level) {
        if (level == 100) {
            return 10000000;
        }
        return plugin.getLevelingRate().get(level);
    }

    private void pushLevelUpSpam(Player player, boolean announce) {
        MessageUtils.sendMessage(player, "<green>You have leveled up!");
        FancyMessage message = new FancyMessage("");
        message.then("You gained a Levelpoint! ").color(ChatColor.GOLD).then("CLICK HERE").command("/levelup")
            .color(ChatColor.WHITE).then(" or use ").color(ChatColor.GOLD).then("/levelup")
            .color(ChatColor.WHITE).then(" to spend them and raise your stats!").color(ChatColor.GOLD).send(player);
        TitleAPI.set("§aLEVEL UP!", "§aOh dang, you got stronger!", 15 , 20, 10, player);
        if (announce) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                MessageUtils.sendMessage(p, "&a&lLevelup! &f" + player.getDisplayName() + " &ahas reached level &f" +
                    + player.getLevel() + "&a!");
            }
        }
    }

    private void pushBonusLevelUpSpam(Player player, int bonusLevel, boolean announce) {
        MessageUtils.sendMessage(player, "&eYou got a &fBONUS LEVEL&e!");
        MessageUtils.sendMessage(player, "&eYour Health, Regeneration, Movement Speed, and Damage have increased!");
        FancyMessage message = new FancyMessage("");
        TitleAPI.set("§eBONUS LEVEL UP!", "§eOh dang, you got stronger!", 15 , 20, 10, player);
        if (announce) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                MessageUtils.sendMessage(p, "&e&lLevelup! &f" + player.getDisplayName() + " &ehas reached bonus level &f" +
                    + bonusLevel + "&e!");
            }
        }
    }
}
