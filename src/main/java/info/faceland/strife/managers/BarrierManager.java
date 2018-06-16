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
import java.util.UUID;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class BarrierManager {
    
    private static final int BASE_BARRIER_TICKS = 15;

    private Map<UUID, Double> barrierMap = new HashMap<>();
    private Map<UUID, Integer> tickMap = new HashMap<>();

    public Map<UUID, Integer> getTickMap() {
        return tickMap;
    }

    public Map<UUID, Double> getBarrierMap() {
        return barrierMap;
    }

    public boolean createBarrierEntry(AttributedEntity attributedEntity) {
        if (!(attributedEntity.getEntity() instanceof Player)) {
            return false;
        }
        if (attributedEntity.getAttribute(BARRIER) <= 0 || !attributedEntity.getEntity().isValid()) {
            AttributeHandler.setPlayerArmor((Player) attributedEntity.getEntity(), 0);
            return false;
        }
        if (barrierMap.containsKey(attributedEntity.getEntity().getUniqueId())) {
            return true;
        }
        setEntityBarrier(attributedEntity.getEntity().getUniqueId(), attributedEntity.getAttribute(BARRIER));
        return true;
    }

    public boolean hasBarrierUp(AttributedEntity attributedEntity) {
        if (!createBarrierEntry(attributedEntity)) {
            return false;
        }
        UUID uuid = attributedEntity.getEntity().getUniqueId();
        return barrierMap.containsKey(uuid) && barrierMap.get(uuid) > 0;
    }

    public void setEntityBarrier(UUID uuid, double amount) {
        barrierMap.put(uuid, amount);
    }

    public void updateShieldDisplay(AttributedEntity attributedEntity) {
        if (!createBarrierEntry(attributedEntity)) {
            return;
        }
        AttributeHandler.setPlayerArmor((Player) attributedEntity.getEntity(), 0);
        if (attributedEntity.getAttribute(BARRIER) <= 0.1) {
            AttributeHandler.setPlayerArmor((Player) attributedEntity.getEntity(), 0);
            return;
        }
        double percent = barrierMap.get(attributedEntity.getEntity().getUniqueId()) / attributedEntity.getAttribute(BARRIER);
        AttributeHandler.setPlayerArmor((Player) attributedEntity.getEntity(), percent);
    }

    public void removeEntity(LivingEntity entity) {
        if (barrierMap.containsKey(entity.getUniqueId())) {
            barrierMap.remove(entity.getUniqueId());
        }
    }

    public void removeEntity(UUID uuid) {
        if (barrierMap.containsKey(uuid)) {
            barrierMap.remove(uuid);
        }
    }

    public void interruptBarrier(LivingEntity entity, int ticks) {
        if (!barrierMap.containsKey(entity.getUniqueId())) {
            return;
        }
        tickMap.put(entity.getUniqueId(), ticks);
    }

    public void interruptBarrier(LivingEntity entity) {
        interruptBarrier(entity, BASE_BARRIER_TICKS);
    }

    public void tickEntity(UUID uuid) {
        if (!tickMap.containsKey(uuid)) {
            return;
        }
        if (tickMap.get(uuid) == 0) {
            tickMap.remove(uuid);
            return;
        }
        tickMap.put(uuid, tickMap.get(uuid) - 1);
    }

    public double damageBarrier(AttributedEntity attributedEntity, double amount) {
        interruptBarrier(attributedEntity.getEntity());
        if (!hasBarrierUp(attributedEntity)) {
            return amount;
        }
        LivingEntity entity = attributedEntity.getEntity();
        double remainingBarrier = barrierMap.get(entity.getUniqueId()) - amount;
        if (remainingBarrier > 0) {
            setEntityBarrier(entity.getUniqueId(), remainingBarrier);
            return 0;
        }
        setEntityBarrier(entity.getUniqueId(), 0);
        return Math.abs(remainingBarrier);
    }
}
