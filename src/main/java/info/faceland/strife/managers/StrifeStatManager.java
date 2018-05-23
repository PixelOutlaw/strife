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
import info.faceland.strife.data.Champion;
import info.faceland.strife.stats.StrifeStat;

import java.util.*;
import java.util.Map.Entry;

public class StrifeStatManager {

    private static Map<String, StrifeStat> statMap;
    private static final String upgradeAvailable = TextUtils.color("&a&lCLICK TO UPGRADE!");
    private static final String pointCapReached = TextUtils.color("&f&lMaxed Out!");
    private static final String noUnspentPoints = TextUtils.color("&f&lNo Unspent Points");

    public StrifeStatManager() {
        statMap = new LinkedHashMap<>();
    }

    public StrifeStat getStat(String name) {
        if (statMap.containsKey(name)) {
            return statMap.get(name);
        }
        return null;
    }

    public void addStat(StrifeStat stat) {
        if (!statMap.containsKey(stat.getKey())) {
            statMap.put(stat.getKey(), stat);
        }
    }

    public void removeStat(String key) {
        if (statMap.containsKey(key)) {
            statMap.remove(key);
        }
    }

    public List<StrifeStat> getStats() {
        return new ArrayList<>(statMap.values());
    }

    public StrifeStat getStatByName(String name) {
        for (StrifeStat stat : statMap.values()) {
            if (stat.getKey().equalsIgnoreCase(name) || stat.getName().equalsIgnoreCase(name)) {
                return stat;
            }
        }
        return null;
    }

    public int getStatCap(StrifeStat stat, Champion champion) {

        int statCap = stat.getMaxCap();
        if (stat.getLevelsToRaiseCap() > 0) {
            double levelCap = stat.getStartCap() + (double) champion.getPlayer().getLevel() / stat.getLevelsToRaiseCap();
            statCap = Math.max(stat.getStartCap(), (int) Math.floor(levelCap));
        }

        for (Entry<String, Integer> baseEntry : stat.getBaseStatRequirements().entrySet()) {
            StrifeStat requirementStat = getStat(baseEntry.getKey());
            int requirementStatValue = champion.getLevel(requirementStat);
            int unlockRequirement = baseEntry.getValue();
            int statIncrement = stat.getStatIncreaseIncrements().get(baseEntry.getKey());

            if (requirementStatValue < unlockRequirement) {
                return 0;
            }

            int newStatCap = stat.getStartCap();
            newStatCap += (requirementStatValue - unlockRequirement) / statIncrement;
            statCap = Math.min(statCap, newStatCap);
        }
        return Math.min(statCap, stat.getMaxCap());
    }

    public List<String> generateRequirementString(StrifeStat stat, Champion champion, int cap) {
        List<String> requirementList = new ArrayList<>();

        int levelRequirement = getLevelRequirement(stat, champion);
        int allocatedPoints = champion.getLevel(stat);
        if (levelRequirement > champion.getPlayer().getLevel() && allocatedPoints < stat.getMaxCap() &&
            allocatedPoints == cap) {
            requirementList.add(increaseString("Level", levelRequirement));
        }
        for (Entry<String, Integer> entry : stat.getBaseStatRequirements().entrySet()) {
            int statRequirement = getStatRequirement(stat, entry.getKey(), champion);
            StrifeStat requirementStat = getStat(entry.getKey());
            if (allocatedPoints < stat.getMaxCap() && allocatedPoints == cap &&
                champion.getLevel(requirementStat) < statRequirement) {
                requirementList.add(increaseString(requirementStat.getName(), statRequirement));
            }
        }
        if (requirementList.isEmpty()) {
            if (allocatedPoints == stat.getMaxCap()) {
                requirementList.add(pointCapReached);
            } else if (champion.getUnusedStatPoints() == 0) {
                requirementList.add(noUnspentPoints);
            } else {
                requirementList.add(upgradeAvailable);
            }
        }
        return requirementList;
    }

    private String increaseString(String name, int value) {
        return TextUtils.color("&f&lCap Increase: " + name + " " + value);
    }

    private int getLevelRequirement(StrifeStat stat, Champion champion) {
        if (stat.getLevelsToRaiseCap() <= 0) {
            return 0;
        }
        int requirementIncrease = champion.getPlayer().getLevel() / stat.getLevelsToRaiseCap();
        return stat.getLevelsToRaiseCap() + requirementIncrease * stat.getLevelsToRaiseCap();
    }

    private int getStatRequirement(StrifeStat stat, String requiredStatName, Champion champion) {
        StrifeStat requirementStat = getStat(requiredStatName);
        int baseRequirement = stat.getBaseStatRequirements().get(requiredStatName);
        int statIncrement = stat.getStatIncreaseIncrements().get(requiredStatName);
        if (baseRequirement > champion.getLevel(requirementStat)) {
            return stat.getBaseStatRequirements().get(requiredStatName);
        }
        if (baseRequirement + statIncrement * champion.getLevel(stat) <= champion.getLevel(requirementStat)) {
            return -1;
        }
        int nextRank = (champion.getLevel(requirementStat) - baseRequirement) / statIncrement;
        return stat.getBaseStatRequirements().get(requiredStatName) + (nextRank + 1) * statIncrement;
    }

}
