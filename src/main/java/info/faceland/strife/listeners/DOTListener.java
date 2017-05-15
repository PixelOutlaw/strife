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

import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

import static info.faceland.strife.listeners.CombatListener.getResistPotionMult;

public class DOTListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDOTEvent(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof LivingEntity)) {
            return;
        }
        if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
            return;
        }
        LivingEntity le = (LivingEntity) event.getEntity();
        double bonusDamage = 0;
        boolean isHandled = false;
        switch (event.getCause()) {
            case FIRE_TICK:
                bonusDamage = le.getHealth() * 0.03 * getResistPotionMult(le);
                isHandled = true;
                break;
            case FIRE:
                bonusDamage = le.getHealth() * 0.05 * getResistPotionMult(le);
                isHandled = true;
                break;
            case LAVA:
                bonusDamage = le.getHealth() * 0.05 * getResistPotionMult(le);
                isHandled = true;
                break;
            case WITHER:
                bonusDamage = le.getMaxHealth() * 0.02 * getResistPotionMult(le);
                isHandled = true;
                break;
        }

        if (isHandled) {
            for (EntityDamageEvent.DamageModifier modifier : EntityDamageEvent.DamageModifier.values()) {
                if (event.isApplicable(modifier)) {
                    if (modifier == EntityDamageEvent.DamageModifier.ABSORPTION) {
                        continue;
                    }
                    event.setDamage(modifier, 0D);
                }
            }
            le.setNoDamageTicks(Math.max(1, le.getNoDamageTicks()));
            event.setDamage(1 + bonusDamage);
        }

    }
}
