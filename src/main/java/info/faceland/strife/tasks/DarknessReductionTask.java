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

import info.faceland.strife.managers.DarknessManager;
import java.util.ArrayList;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;

public class DarknessReductionTask extends BukkitRunnable {

    private ArrayList<LivingEntity> toBeRemoved = new ArrayList<>();

    @Override
    public void run() {
        for (LivingEntity le : DarknessManager.getDarkMap().keySet()) {
            double particleAmount = Math.min(5 + DarknessManager.getCorruptionStacks(le) / 3, 30);
            le.getWorld().spawnParticle(Particle.SMOKE_NORMAL, le.getEyeLocation(), (int) particleAmount, 0.4, 0.4, 0.5, 0.03);
            DarknessManager.applyCorruptionStacks(le, -0.2f - (DarknessManager.getCorruptionStacks(le) * 0.05));
            if (!DarknessManager.isValidEntity(le) || !DarknessManager.isCorrupted(le)) {
                toBeRemoved.add(le);
            }
        }
        for (LivingEntity le : toBeRemoved) {
            DarknessManager.removeEntity(le);
        }
        toBeRemoved.clear();
    }
}
