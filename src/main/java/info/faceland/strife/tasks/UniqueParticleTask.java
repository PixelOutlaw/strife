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

import info.faceland.strife.StrifePlugin;
import info.faceland.strife.data.UniqueEntity;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map.Entry;

public class UniqueParticleTask extends BukkitRunnable {

    private StrifePlugin plugin;

    public UniqueParticleTask(StrifePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        for (Entry<LivingEntity, UniqueEntity> entry : plugin.getUniqueEntityManager().getLiveUniquesMap().entrySet()) {
            UniqueEntity ue = entry.getValue();
            if (ue.getParticle() == null) {
                continue;
            }
            Location location = entry.getKey().getLocation();
            location.getWorld().spawnParticle(
                    ue.getParticle(),
                    location,
                    ue.getParticleCount(),
                    ue.getParticleRadius(), ue.getParticleRadius(), ue.getParticleRadius(),
                    0.05f
            );
        }
    }

}
