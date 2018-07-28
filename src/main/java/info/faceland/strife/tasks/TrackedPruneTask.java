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
import java.util.ArrayList;
import java.util.UUID;
import org.bukkit.scheduler.BukkitRunnable;

public class TrackedPruneTask extends BukkitRunnable {

    private final StrifePlugin plugin;

    public TrackedPruneTask(StrifePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        ArrayList<UUID> invalidEntities = new ArrayList<>();
        for (UUID uuid : plugin.getEntityStatCache().getTrackedEntities().keySet()) {
            if (plugin.getEntityStatCache().isValid(uuid)) {
                continue;
            }
            invalidEntities.add(uuid);
        }
        plugin.getLogger().info("Cleared " + invalidEntities.size() + " no longer valid attributed entities.");
        for (UUID uuid : invalidEntities) {
            plugin.getEntityStatCache().removeEntity(uuid);
        }
    }
}
