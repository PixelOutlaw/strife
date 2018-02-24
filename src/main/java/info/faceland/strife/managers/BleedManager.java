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

import info.faceland.strife.data.BleedData;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.entity.LivingEntity;

public class BleedManager {

    private Map<LivingEntity, BleedData> bleedMap = new HashMap<>();

    public Map<LivingEntity, BleedData> getBleedMap() {
        return bleedMap;
    }

    public BleedData getEntity(LivingEntity entity) {
        return bleedMap.get(entity);
    }

    public void removeEntity(LivingEntity entity) {
        if (bleedMap.containsKey(entity)) {
            bleedMap.remove(entity);
        }
    }

    public void applyBleed(LivingEntity livingEntity, double amount, int ticks) {
        if (!livingEntity.isValid()) {
            return;
        }
        if (!bleedMap.containsKey(livingEntity)) {
            bleedMap.put(livingEntity, new BleedData(amount, ticks));
            return;
        }
        if (amount > bleedMap.get(livingEntity).getBleedAmount()) {
            bleedMap.get(livingEntity).setBleedAmount(amount);
        }
        bleedMap.get(livingEntity).bumpTicks(ticks);
    }

    public void applyBleed(LivingEntity livingEntity, BleedData bleedData) {
        if (!livingEntity.isValid()) {
            return;
        }
        bleedMap.put(livingEntity, bleedData);
    }

    public int removeTick(LivingEntity livingEntity) {
        if (!bleedMap.containsKey(livingEntity)) {
            return 0;
        }
        bleedMap.get(livingEntity).setTicksRemaining(bleedMap.get(livingEntity).getTicksRemaining() - 1);
        return bleedMap.get(livingEntity).getTicksRemaining();
    }
}
