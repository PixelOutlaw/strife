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

import info.faceland.strife.stats.StrifeStat;

import java.util.*;

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
