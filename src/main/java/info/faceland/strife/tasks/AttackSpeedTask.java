/******************************************************************************
 * Copyright (c) 2014, Richard Harrah                                         *
 *                                                                            *
 * Permission to use, copy, modify, and/or distribute this software for any purpose with or without fee is hereby granted, provided that the above copyright notice and this permission notice appear in all copies.
 *                                                                            *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 ******************************************************************************/

package info.faceland.strife.tasks;

import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class AttackSpeedTask extends BukkitRunnable {

    private Map<UUID, Long> timeLeftMap;

    public AttackSpeedTask() {
        timeLeftMap = new HashMap<>();
    }

    @Override
    public void run() {
        Iterator<Map.Entry<UUID, Long>> iterator = timeLeftMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, Long> entry = iterator.next();
            if (entry.getValue() < 1L) {
                iterator.remove();
                continue;
            }
            entry.setValue(entry.getValue() - 1L);
        }
    }

    public void setTimeLeft(UUID uuid, long timeToSet) {
        timeLeftMap.put(uuid, timeToSet);
    }

    public long getTimeLeft(UUID uuid) {
        return timeLeftMap.containsKey(uuid) ? timeLeftMap.get(uuid) : 0;
    }

}
