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
package info.faceland.strife.tasks;

import info.faceland.strife.StrifePlugin;
import info.faceland.strife.data.Champion;
import info.faceland.strife.data.ChampionSaveData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class SaveTask extends BukkitRunnable {

    private final StrifePlugin plugin;

    public SaveTask(StrifePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        plugin.debug(Level.INFO, "Running save task");
        Map<UUID, ChampionSaveData> championDataMap = new HashMap<>();
        for (Champion champion : plugin.getChampionManager().getChampions()) {
            championDataMap.put(champion.getUniqueId(), champion.getSaveData());
        }
        plugin.getStorage().save(plugin.getChampionManager().getChampionSaveData());
        plugin.getChampionManager().clear();
        for (ChampionSaveData saveData : plugin.getStorage().load()) {
            if (championDataMap.containsKey(saveData.getUniqueId())) {
                saveData = championDataMap.get(saveData.getUniqueId());
            }
            plugin.getChampionManager().addChampion(new Champion(saveData));
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            ChampionSaveData saveData = plugin.getStorage().load(player.getUniqueId());
            if (championDataMap.containsKey(saveData.getUniqueId())) {
                saveData = championDataMap.get(saveData.getUniqueId());
            }
            plugin.getChampionManager().addChampion(new Champion(saveData));
        }
    }

}
