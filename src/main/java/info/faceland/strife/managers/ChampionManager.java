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

import info.faceland.strife.StrifePlugin;
import info.faceland.strife.data.Champion;

import java.util.*;

public class ChampionManager {

    private final StrifePlugin plugin;
    private Map<UUID, Champion> championMap;

    public final static String LVL_REQ_MAIN_WEAPON = "<red>You do not meet the level requirement for your weapon! " +
        "It will not give you any stats when used!";
    public final static String LVL_REQ_OFF_WEAPON = "<red>You do not meet the level requirement for your offhand " +
        "item! It will not give you any stats when used!";
    public final static String LVL_REQ_ARMOR = "<red>You do not meet the level requirement for a piece of your " +
        "armor! It will not give you any stats while equipped!";

    public ChampionManager(StrifePlugin plugin) {
        this.plugin = plugin;
        championMap = new HashMap<>();
    }

    public Champion getChampion(UUID uuid) {
        if (uuid == null) {
            return null;
        }
        if (!hasChampion(uuid)) {
            Champion champion = plugin.getStorage().load(uuid);
            if (champion != null) {
                championMap.put(uuid, champion);
                return champion;
            }
            return createChampion(uuid);
        }
        return championMap.get(uuid);
    }

    public boolean hasChampion(UUID uuid) {
        return uuid != null && championMap.containsKey(uuid);
    }

    public Champion createChampion(UUID uuid) {
        Champion champ = new Champion(uuid);
        championMap.put(uuid, champ);
        return champ;
    }

    public void addChampion(Champion champion) {
        if (!hasChampion(champion.getUniqueId())) {
            championMap.put(champion.getUniqueId(), champion);
        }
    }

    public void removeChampion(UUID uuid) {
        if (hasChampion(uuid)) {
            championMap.remove(uuid);
        }
    }

    public static void updateChampionWeapons(StrifePlugin plugin, Champion champion) {
        champion.updateWeapons();
        plugin.getEntityStatCache().setEntityStats(champion.getPlayer(), champion.getCache().getCombinedCache());
    }

    public static void updateChampionArmor(StrifePlugin plugin, Champion champion) {
        champion.updateArmor();
        plugin.getEntityStatCache().setEntityStats(champion.getPlayer(), champion.getCache().getCombinedCache());
    }

    public static void updateChampionLevelPoints(StrifePlugin plugin, Champion champion) {
        champion.updateLevelPoints();
        plugin.getEntityStatCache().setEntityStats(champion.getPlayer(), champion.getCache().getCombinedCache());
    }

    public static void updateChampionBaseStats(StrifePlugin plugin, Champion champion) {
        champion.updateBase();
        plugin.getEntityStatCache().setEntityStats(champion.getPlayer(), champion.getCache().getCombinedCache());
    }

    public static void updateChampionStats(StrifePlugin plugin, Champion champion) {
        champion.updateAll();
        plugin.getEntityStatCache().setEntityStats(champion.getPlayer(), champion.getCache().getCombinedCache());
    }

    public Collection<Champion> getChampions() {
        return new HashSet<>(championMap.values());
    }

    public void clear() {
        championMap.clear();
    }

}
