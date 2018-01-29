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

import static info.faceland.strife.attributes.StrifeAttribute.BARRIER;

import info.faceland.strife.attributes.AttributeHandler;
import info.faceland.strife.data.AttributedEntity;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class BarrierManager {

    private Map<LivingEntity, Double> barrierMap = new HashMap<>();
    private Map<LivingEntity, Integer> tickMap = new HashMap<>();

    public Map<LivingEntity, Integer> getTickMap() {
        return tickMap;
    }

    public Map<LivingEntity, Double> getBarrierMap() {
        return barrierMap;
    }

    public boolean checkBarrier(AttributedEntity attributedEntity) {
        if (barrierMap.containsKey(attributedEntity.getEntity())) {
            return true;
        }
        if (attributedEntity.getAttribute(BARRIER) > 0) {
            setEntityBarrier(attributedEntity.getEntity(), attributedEntity.getAttribute(BARRIER));
            return true;
        }
        return false;
    }

    public boolean hasBarrierUp(AttributedEntity attributedEntity) {
        checkBarrier(attributedEntity);
        if (barrierMap.containsKey(attributedEntity.getEntity()) && barrierMap.get(attributedEntity.getEntity()) > 0) {
            return true;
        }
        return false;
    }

    public void setEntityBarrier(LivingEntity entity, double amount) {
        barrierMap.put(entity, amount);
    }

    public void updateShieldDisplay(AttributedEntity attributedEntity) {
        if (!(attributedEntity.getEntity() instanceof Player)) {
            return;
        }
        if (attributedEntity.getAttribute(BARRIER) == 0) {
            AttributeHandler.setPlayerArmor((Player) attributedEntity.getEntity(), 0);
            return;
        }
        if (!barrierMap.containsKey(attributedEntity.getEntity())) {
            AttributeHandler.setPlayerArmor((Player) attributedEntity.getEntity(), 0);
            return;
        }
        double percent = barrierMap.get(attributedEntity.getEntity()) / attributedEntity.getAttribute(BARRIER);
        AttributeHandler.setPlayerArmor((Player) attributedEntity.getEntity(), percent);
    }

    public void removeEntity(LivingEntity entity) {
        if (barrierMap.containsKey(entity)) {
            barrierMap.remove(entity);
        }
    }

    public void setDamaged(LivingEntity entity, int ticks) {
        tickMap.put(entity, ticks);
    }

    public void tickEntity(LivingEntity entity) {
        if (!tickMap.containsKey(entity)) {
            return;
        }
        if (tickMap.get(entity) == 0) {
            tickMap.remove(entity);
            return;
        }
        tickMap.put(entity, tickMap.get(entity) - 1);
    }

    public double damageBarrier(AttributedEntity attributedEntity, double amount) {
        LivingEntity entity = attributedEntity.getEntity();
        if (!(entity instanceof Player) || !attributedEntity.getEntity().isValid()) {
            return amount;
        }
        if (!checkBarrier(attributedEntity)) {
            return amount;
        }

        double remainingBarrier = barrierMap.get(entity) - amount;
        // Stat for lowering the delay to be added.
        // Task ticks once every 4 server ticks, so, 3s * 5 TPS = 15
        setDamaged(entity, 15);

        if (remainingBarrier > 0) {
            barrierMap.put(entity, remainingBarrier);
            return 0;
        }
        barrierMap.put(entity, 0D);
        return Math.abs(remainingBarrier);
    }
}
