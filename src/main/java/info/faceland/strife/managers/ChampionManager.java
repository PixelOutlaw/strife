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

    public Champion createChampion(UUID uuid) {
        Champion champ = new Champion(uuid);
        championMap.put(uuid, champ);
        return champ;
    }

    public boolean hasChampion(UUID uuid) {
        return uuid != null && championMap.containsKey(uuid);
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
