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
import info.faceland.strife.attributes.StrifeAttribute;
import info.faceland.strife.data.Champion;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class HealthRegenTask extends BukkitRunnable {

    private final StrifePlugin plugin;

    public HealthRegenTask(StrifePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            Champion champion = plugin.getChampionManager().getChampion(p.getUniqueId());
            // Restore 40% of your regen per 2s tick (This task runs every 2s)
            // Equals out to be 200% regen healed per 10s, aka 100% per 5s average
            double amount = champion.getCache().getAttribute(StrifeAttribute.REGENERATION) * 0.4;
            // These are not 'penalties', they're 'mechanics' :^)
            if (p.hasPotionEffect(PotionEffectType.POISON)) {
                amount *= 0.33;
            }
            if (p.getFoodLevel() <= 6) {
                amount *= p.getFoodLevel() / 6;
            }
            p.setHealth(Math.min(p.getHealth() + amount, p.getMaxHealth()));
        }
    }
}
