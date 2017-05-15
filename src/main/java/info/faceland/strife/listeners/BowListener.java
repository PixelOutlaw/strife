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
package info.faceland.strife.listeners;

import info.faceland.strife.StrifePlugin;
import info.faceland.strife.attributes.StrifeAttribute;
import info.faceland.strife.data.Champion;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import java.util.Random;

public class BowListener implements Listener {

    private final StrifePlugin plugin;
    private final Random random;

    public BowListener(StrifePlugin plugin) {
        this.plugin = plugin;
        random = new Random(System.currentTimeMillis());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerShoot(ProjectileLaunchEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player)) {
            return;
        }
        if (!(event.getEntity() instanceof Arrow)) {
            return;
        }
        Player p = (Player) event.getEntity().getShooter();
        Champion playerChamp = plugin.getChampionManager().getChampion(p.getUniqueId());

        playerChamp.getWeaponAttributeValues();
        playerChamp.getCache().recombine();

        Projectile projectile = event.getEntity();

        double attackSpeedMult = Math.min(0.1 * playerChamp.getCache().getAttribute(StrifeAttribute.ATTACK_SPEED), 1.0);
        double shotPower = projectile.getVelocity().length();
        double shotMult = attackSpeedMult + ((1 - attackSpeedMult) * Math.min(shotPower / 2.9, 1.0));
        double vBonus = 1 + shotMult * 2;
        Vector vec = p.getLocation().getDirection();
        projectile.setVelocity(new Vector(vec.getX() * 1.2 * vBonus, vec.getY() * 1.3 * vBonus, vec.getZ() * 1.2 *
                vBonus));

        double damage = playerChamp.getCache().getAttribute(StrifeAttribute.RANGED_DAMAGE) * shotMult;
        double critMult = 0;
        double overMult = 0;
        if (shotMult == 1.0) {
            overMult = playerChamp.getCache().getAttribute(StrifeAttribute.OVERCHARGE);
        }
        if (random.nextDouble() <= playerChamp.getCache().getAttribute(StrifeAttribute.CRITICAL_RATE)) {
            critMult = playerChamp.getCache().getAttribute(StrifeAttribute.CRITICAL_DAMAGE) - 1;
        }

        projectile.setMetadata("handled", new FixedMetadataValue(plugin, true));
        projectile.setMetadata("damage", new FixedMetadataValue(plugin, damage));
        projectile.setMetadata("overcharge", new FixedMetadataValue(plugin, overMult));
        projectile.setMetadata("critical", new FixedMetadataValue(plugin, critMult));
        projectile.setMetadata("armorPen", new FixedMetadataValue(plugin, playerChamp.getCache()
                .getAttribute(StrifeAttribute.ARMOR_PENETRATION)));
        projectile.setMetadata("accuracy", new FixedMetadataValue(plugin, playerChamp.getCache()
                .getAttribute(StrifeAttribute.ACCURACY)));
        if (playerChamp.getCache().getAttribute(StrifeAttribute.FIRE_DAMAGE) > 0) {
            if (random.nextDouble() < playerChamp.getCache().getAttribute(StrifeAttribute.IGNITE_CHANCE)) {
                projectile.setMetadata("fireDamage", new FixedMetadataValue(plugin, playerChamp.getCache()
                        .getAttribute(StrifeAttribute.FIRE_DAMAGE) * shotMult));
            }
        }
        if (playerChamp.getCache().getAttribute(StrifeAttribute.ICE_DAMAGE) > 0) {
            if (random.nextDouble() < playerChamp.getCache().getAttribute(StrifeAttribute.FREEZE_CHANCE)) {
                projectile.setMetadata("iceDamage", new FixedMetadataValue(plugin, playerChamp.getCache()
                        .getAttribute(StrifeAttribute.ICE_DAMAGE) * shotMult));
            }
        }
        if (playerChamp.getCache().getAttribute(StrifeAttribute.LIGHTNING_DAMAGE) > 0) {
            if (random.nextDouble() < playerChamp.getCache().getAttribute(StrifeAttribute.SHOCK_CHANCE)) {
                projectile.setMetadata("lightningDamage", new FixedMetadataValue(plugin, playerChamp.getCache()
                        .getAttribute(StrifeAttribute.LIGHTNING_DAMAGE) * shotMult));
            }
        }
        if (playerChamp.getCache().getAttribute(StrifeAttribute.LIFE_STEAL) > 0) {
            projectile.setMetadata("lifeSteal", new FixedMetadataValue(plugin, playerChamp.getCache()
                    .getAttribute(StrifeAttribute.LIFE_STEAL)));
        }

    }
}
