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

import info.faceland.strife.data.Champion;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

public class ChampionManager {

    private Map<UUID, Champion> championMap;

    public ChampionManager() {
        championMap = new HashMap<>();
    }

    public Champion getChampion(UUID uuid) {
        if (uuid == null) {
            return null;
        }
        if (!hasChampion(uuid)) {
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

    public Collection<Champion> getChampions() {
        return new HashSet<>(championMap.values());
    }

    public void clear() {
        championMap.clear();
    }

}
