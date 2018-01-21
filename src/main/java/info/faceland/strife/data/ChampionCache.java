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
package info.faceland.strife.data;

import com.google.common.base.Objects;
import info.faceland.strife.attributes.AttributeHandler;
import info.faceland.strife.attributes.StrifeAttribute;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChampionCache {

    private final UUID owner;
    private final Map<StrifeAttribute, Double> attributeBase;
    private final Map<StrifeAttribute, Double> attributeLevelPoint;
    private final Map<StrifeAttribute, Double> attributeArmorCache;
    private final Map<StrifeAttribute, Double> attributeWeaponCache;
    private final Map<StrifeAttribute, Double> combinedAttributeCache;

    public ChampionCache(UUID owner) {
        this.owner = owner;
        this.attributeBase = new HashMap<>();
        this.attributeLevelPoint = new HashMap<>();
        this.attributeArmorCache = new HashMap<>();
        this.attributeWeaponCache = new HashMap<>();
        this.combinedAttributeCache = new HashMap<>();
    }

    public UUID getOwner() {
        return owner;
    }

    public Map<StrifeAttribute, Double> getBaseCache() {
        return new HashMap<>(attributeBase);
    }

    public Map<StrifeAttribute, Double> getLevelpointCache() {
        return new HashMap<>(attributeLevelPoint);
    }

    public Map<StrifeAttribute, Double> getArmorCache() {
        return new HashMap<>(attributeArmorCache);
    }

    public Map<StrifeAttribute, Double> getWeaponCache() {
        return new HashMap<>(attributeWeaponCache);
    }

    public Map<StrifeAttribute, Double> getCombinedCache() {
        return new HashMap<>(combinedAttributeCache);
    }

    void clearBaseStatCache() {
        attributeBase.clear();
    }

    void clearLevelPointCache() {
        attributeLevelPoint.clear();
    }

    void clearArmorCache() {
        attributeArmorCache.clear();
    }

    void clearWeaponCache() {
        attributeWeaponCache.clear();
    }

    void clearCombinedCache() {
        combinedAttributeCache.clear();
    }

    public void recombine() {
        clearCombinedCache();
        combinedAttributeCache.putAll(AttributeHandler.combineMaps(
            getBaseCache(),
            getLevelpointCache(),
            getArmorCache(),
            getWeaponCache()
        ));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChampionCache)) return false;
        ChampionCache that = (ChampionCache) o;
        return Objects.equal(getOwner(), that.getOwner());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getOwner());
    }

    void setAttributeBaseCache(Map<StrifeAttribute, Double> map) {
        attributeBase.clear();
        if (map == null) {
            return;
        }
        attributeBase.putAll(map);
    }

    void setAttributeLevelPointCache(Map<StrifeAttribute, Double> map) {
        attributeLevelPoint.clear();
        if (map == null) {
            return;
        }
        attributeLevelPoint.putAll(map);
    }

    void setAttributeArmorCache(Map<StrifeAttribute, Double> map) {
        attributeArmorCache.clear();
        if (map == null) {
            return;
        }
        attributeArmorCache.putAll(map);
    }

    void setAttributeWeaponCache(Map<StrifeAttribute, Double> map) {
        attributeWeaponCache.clear();
        if (map == null) {
            return;
        }
        attributeWeaponCache.putAll(map);
    }
}
