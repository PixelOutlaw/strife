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
package info.faceland.strife.tasks;

import static info.faceland.strife.attributes.StrifeAttribute.BARRIER;
import static info.faceland.strife.attributes.StrifeAttribute.BARRIER_SPEED;

import info.faceland.strife.StrifePlugin;
import info.faceland.strife.data.AttributedEntity;
import java.util.Map.Entry;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;

public class BarrierTask extends BukkitRunnable {

    private StrifePlugin plugin;

    public BarrierTask(StrifePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        for (Entry<LivingEntity, Double> entry : plugin.getBarrierManager().getBarrierMap().entrySet()) {
            if (plugin.getBarrierManager().getTickMap().containsKey(entry.getKey())) {
                plugin.getBarrierManager().tickEntity(entry.getKey());
                continue;
            }
            AttributedEntity player = plugin.getEntityStatCache().getAttributedEntity(entry.getKey());
            if (entry.getValue() >= player.getAttribute(BARRIER)) {
                continue;
            }
            // Restore this amount per barrier tick (4 MC ticks, 0.2s)
            // Restores 2.5% per 0.2s, 10% per 0.8s, 100% per 8.0s
            double barrierGain = player.getAttribute(BARRIER) * 0.025 * (1 + (player.getAttribute(BARRIER_SPEED) / 100));
            double newBarrierValue = Math.min(entry.getValue() + barrierGain, player.getAttribute(BARRIER));
            plugin.getBarrierManager().setEntityBarrier(entry.getKey(), newBarrierValue);
            plugin.getBarrierManager().updateShieldDisplay(player);
        }
    }
}
