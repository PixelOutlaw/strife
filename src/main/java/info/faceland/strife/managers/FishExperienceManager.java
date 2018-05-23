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
import gyurix.api.TitleAPI;
import gyurix.spigotlib.ChatAPI;
import info.faceland.strife.StrifePlugin;
import info.faceland.strife.api.StrifeFishExperienceManager;
import info.faceland.strife.data.Champion;
import info.faceland.strife.data.ChampionSaveData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class FishExperienceManager implements StrifeFishExperienceManager {

    private final StrifePlugin plugin;

    public FishExperienceManager(StrifePlugin plugin) {
        this.plugin = plugin;
    }

    public void addExperience(Player player, double amount) {
        addExperience(plugin.getChampionManager().getChampion(player.getUniqueId()), amount);
    }

    public void addExperience(Champion champion, double amount) {
        ChampionSaveData saveData = champion.getSaveData();
        double currentExp = saveData.getFishingExp() + amount;
        double maxExp = (double) getMaxExp(saveData.getFishingLevel());

        while (currentExp > maxExp) {
            currentExp -= maxExp;
            saveData.setFishingLevel(saveData.getFishingLevel() + 1);
            pushLevelUpSpam(champion.getPlayer(), saveData.getFishingLevel(), saveData.getFishingLevel() % 5 == 0);
            maxExp = (double) getMaxExp(saveData.getFishingLevel());
        }

        saveData.setFishingExp((float)currentExp);
        String xpMsg = "&b&l( &f&l" + (int) currentExp + " &b&l/ &f&l" + (int) maxExp + " XP &b&l)";
        ChatAPI.sendJsonMsg(ChatAPI.ChatMessageType.ACTION_BAR, TextUtils.color(xpMsg), champion.getPlayer());
        champion.setSaveData(saveData);
    }

    public Integer getMaxExp(int level) {
        return plugin.getFishRate().get(level);
    }

    private void pushLevelUpSpam(Player player, int level, boolean announce) {
        MessageUtils.sendMessage(player, "&bSkill Up! Your &fFishing &blevel has increased to &f" + level + "&b!");
        TitleAPI.set("§bSKILL UP!", "§bFishing Level §f" + level, 10 , 40, 20, player);
        if (announce) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                MessageUtils.sendMessage(p, "&b&lSkillUp! &f" + player.getDisplayName() +
                    " &bhas reached skill level &f" + level + " &bin fishing!");
            }
        }
    }
}
