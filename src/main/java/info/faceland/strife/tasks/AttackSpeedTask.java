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

import com.tealcube.minecraft.bukkit.TextUtils;
import gyurix.spigotlib.ChatAPI;
import info.faceland.strife.attributes.StrifeAttribute;
import info.faceland.strife.data.AttributedEntity;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class AttackSpeedTask extends BukkitRunnable {

    private static final String ATTACK_RECHARGED = TextUtils.color("&e&lAttack Recharged!");
    private Map<UUID, Long> attackTimeMap;
    private long tickRate;

    public AttackSpeedTask(long tickRate) {
        this.attackTimeMap = new HashMap<>();
        this.tickRate = tickRate;
    }

    @Override
    public void run() {
        Iterator<Map.Entry<UUID, Long>> iterator = attackTimeMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, Long> entry = iterator.next();
            if (entry.getValue() < 1L) {
                Player p = Bukkit.getPlayer(entry.getKey());
                if (p != null && p.isOnline()) {
                    ChatAPI.sendJsonMsg(ChatAPI.ChatMessageType.ACTION_BAR, ATTACK_RECHARGED, p);
                }
                iterator.remove();
                continue;
            }
            entry.setValue(entry.getValue() - 1L);
        }
    }

    public void endTask(UUID uuid) {
        attackTimeMap.remove(uuid);
    }

    public void setTicksLeft(UUID uuid, long ticks) {
        attackTimeMap.put(uuid, ticks);
    }

    public void setSecondsLeft(UUID uuid, double seconds) {
        attackTimeMap.put(uuid, (long) (seconds * (20L / tickRate)));
    }

    public long getTicksLeft(UUID uuid) {
        return attackTimeMap.containsKey(uuid) ? attackTimeMap.get(uuid) : 0;
    }

    public double getSecondsLeft(UUID uuid) {
        return (getTicksLeft(uuid) * tickRate) / (double) 20L;
    }

    public double getAttackMultiplier(AttributedEntity attacker) {
        if (!(attacker.getEntity() instanceof Player)) {
            return 1.0;
        }
        double attacksPerSecond = 2;
        if (attacker.getAttribute(StrifeAttribute.ATTACK_SPEED) > 0) {
            attacksPerSecond /= 1 + attacker.getAttribute(StrifeAttribute.ATTACK_SPEED) / 100;
        } else {
            attacksPerSecond *= 1 + Math.abs(attacker.getAttribute(StrifeAttribute.ATTACK_SPEED) / 100);
        }
        double attackMult = Math.min(1.0 - (getSecondsLeft(attacker.getEntity().getUniqueId()) / attacksPerSecond), 1.0);

        setSecondsLeft(attacker.getEntity().getUniqueId(), attacksPerSecond);

        return attackMult;
    }
}
