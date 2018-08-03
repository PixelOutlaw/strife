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

import java.util.HashMap;
import java.util.Map;
import org.bukkit.entity.LivingEntity;

public class DarknessManager {

    private Map<LivingEntity, Double> darkMap = new HashMap<>();

    public boolean isValidEntity(LivingEntity entity) {
        return entity.isValid() && !entity.isDead();
    }

    public boolean isCorrupted(LivingEntity entity) {
        if (darkMap.get(entity) == null) {
            return false;
        }
        return darkMap.get(entity) >= 0;
    }

    public void applyCorruptionStacks(LivingEntity entity, double amount) {
        if (darkMap.get(entity) != null) {
            darkMap.put(entity, darkMap.get(entity) + amount);
            return;
        }
        darkMap.put(entity, amount);
    }

    public double getCorruptionStacks(LivingEntity entity) {
        if (darkMap.get(entity) != null) {
            return darkMap.get(entity);
        }
        return 0D;
    }

    public void removeEntity(LivingEntity entity) {
        if (darkMap.get(entity) != null) {
            darkMap.remove(entity);
        }
    }

    public Map<LivingEntity, Double> getDarkMap() {
        return darkMap;
    }
}
