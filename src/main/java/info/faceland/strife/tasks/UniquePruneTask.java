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
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;

public class UniquePruneTask extends BukkitRunnable {

    private final StrifePlugin plugin;

    public UniquePruneTask(StrifePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        ArrayList<LivingEntity> invalidEntities = new ArrayList<>();
        for (LivingEntity entity : plugin.getUniqueEntityManager().getLiveUniquesMap().keySet()) {
            if (entity == null || !entity.isValid()) {
                invalidEntities.add(entity);
            }
        }
        if (invalidEntities.size() > 0) {
            plugin.getLogger().warning("Cleared " + invalidEntities.size() + " no longer valid uniques");
            for (LivingEntity ent : invalidEntities) {
                plugin.getUniqueEntityManager().getLiveUniquesMap().remove(ent);
            }
        }
    }
}