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
import info.faceland.strife.data.BleedData;
import java.util.Map.Entry;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.material.MaterialData;
import org.bukkit.scheduler.BukkitRunnable;

public class BleedTask extends BukkitRunnable {

    private StrifePlugin plugin;

    public BleedTask(StrifePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        for (Entry<LivingEntity, BleedData> entry : plugin.getBleedManager().getBleedMap().entrySet()) {
            LivingEntity bleedingEntity = entry.getKey();
            BleedData bleedData = entry.getValue();
            if (!bleedingEntity.isValid()) {
                plugin.getBleedManager().removeEntity(bleedingEntity);
                continue;
            }

            double bleedDamage = bleedData.getBleedAmount() / 20;
            if (bleedingEntity.getHealth() > bleedDamage) {
                bleedingEntity.setHealth(bleedingEntity.getHealth() - bleedDamage);
            } else {
                bleedingEntity.damage(bleedDamage);
            }

            int particleAmount = 10 + (int)(bleedDamage * 10);

            bleedingEntity.getWorld().spawnParticle(
                Particle.BLOCK_CRACK,
                bleedingEntity.getEyeLocation().clone().add(0, -0.7, 0),
                particleAmount,
                0.0, 0.0, 0.0,
                new MaterialData(Material.REDSTONE_WIRE)
            );

            plugin.getBleedManager().removeTick(bleedingEntity);
        }
    }


}
