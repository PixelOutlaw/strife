/******************************************************************************
 * Copyright (c) 2014, Richard Harrah                                         *
 *                                                                            *
 * Permission to use, copy, modify, and/or distribute this software for any purpose with or without fee is hereby granted, provided that the above copyright notice and this permission notice appear in all copies.
 *                                                                            *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 ******************************************************************************/

package info.faceland.strife.storage;

import info.faceland.facecore.shade.nun.ivory.config.IvoryJsonConfiguration;
import info.faceland.strife.StrifePlugin;
import info.faceland.strife.data.Champion;
import info.faceland.strife.stats.StrifeStat;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

public class JsonDataStorage implements DataStorage {

    private final StrifePlugin plugin;
    private IvoryJsonConfiguration configuration;

    public JsonDataStorage(StrifePlugin plugin) {
        this.plugin = plugin;
        this.configuration = new IvoryJsonConfiguration(new File(plugin.getDataFolder(), "data.json"));
    }

    @Override
    public void save(Collection<Champion> champions) {
        for (Champion champ : champions) {
            for (Map.Entry<StrifeStat, Integer> entry : champ.getLevelMap().entrySet()) {
                configuration.set(champ.getUniqueId().toString() + "." + entry.getKey().getKey(), entry.getValue());
            }
            configuration.set(champ.getUniqueId().toString() + ".unused-stat-points", champ.getUnusedStatPoints());
            configuration.set(champ.getUniqueId().toString() + ".highest-reached-level", champ.getHighestReachedLevel());
        }
        configuration.save();
    }

    @Override
    public Collection<Champion> load() {
        configuration.load();
        Collection<Champion> collection = new HashSet<>();
        for (String key : configuration.getKeys(false)) {
            if (!configuration.isConfigurationSection(key)) {
                continue;
            }
            UUID uuid = UUID.fromString(key);
            Champion champion = new Champion(uuid);
            for (String k : configuration.getConfigurationSection(key).getKeys(false)) {
                StrifeStat stat = plugin.getStatManager().getStat(k);
                if (stat == null) {
                    continue;
                }
                champion.setLevel(stat, configuration.getConfigurationSection(key).getInt(k));
            }
            champion.setUnusedStatPoints(configuration.getConfigurationSection(key).getInt("unused-stat-points"));
            champion.setHighestReachedLevel(configuration.getConfigurationSection(key).getInt("highest-reached-level"));
            collection.add(champion);
        }
        return collection;
    }

}
