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

import info.faceland.strife.stats.StrifeStat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StrifeStatManager {

    private Map<String, StrifeStat> statMap;

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
        List<StrifeStat> list = new ArrayList<>(statMap.values());
        Collections.sort(list);
        return list;
    }

    public StrifeStat getStatByName(String name) {
        for (StrifeStat stat : statMap.values()) {
            if (stat.getKey().equalsIgnoreCase(name) || stat.getName().equalsIgnoreCase(name)) {
                return stat;
            }
        }
        return null;
    }

}
