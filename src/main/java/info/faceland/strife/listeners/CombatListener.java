/*
 * This file is part of Strife, licensed under the ISC License.
 *
 * Copyright (c) 2014 Richard Harrah
 *
 * Permission to use, copy, modify, and/or distribute this software for any purpose with or without fee is hereby granted,
 * provided that the above copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING ALL
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT,
 * INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF
 * THIS SOFTWARE.
 */
package info.faceland.strife.listeners;

import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;
import com.tealcube.minecraft.bukkit.kern.apache.commons.lang3.math.NumberUtils;
import com.tealcube.minecraft.bukkit.kern.shade.google.common.base.CharMatcher;
import info.faceland.beast.BeastData;
import info.faceland.strife.StrifePlugin;
import info.faceland.strife.attributes.StrifeAttribute;
import info.faceland.strife.data.Champion;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;
import java.util.Random;

public class CombatListener implements Listener {

    private final StrifePlugin plugin;
    private final Random random;
    private static final String[] DOGE_MEMES = {"<aqua>wow", "<green>wow", "<light purple>wow", "<aqua>much pain", "<green>much pain", "<light purple>much pain", "<aqua>many disrespects", "<green>many disrespects", "<light purple>many disrespects", "<red>no u", "<red>2damage4me"};

    public CombatListener(StrifePlugin plugin) {
        this.plugin = plugin;
        random = new Random(System.currentTimeMillis());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDamageEvent(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        Player p = (Player) event.getEntity();
        Champion champ = plugin.getChampionManager().getChampion(p.getUniqueId());
        Map<StrifeAttribute, Double> vals = champ.getAttributeValues();
        double chance = vals.get(StrifeAttribute.DOGE);
        if (random.nextDouble() > chance) {
            return;
        }
        MessageUtils.sendMessage(p, DOGE_MEMES[random.nextInt(DOGE_MEMES.length)]);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (event.getEntity().getShooter() instanceof Entity) {
            event.getEntity().setVelocity(event.getEntity().getVelocity().add(((Entity) event.getEntity().getShooter()).getVelocity()));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        LivingEntity a;
        if (event.isCancelled() || !(event.getEntity() instanceof LivingEntity)) {
            return;
        }
        if (event.getEntity().hasMetadata("NPC")) {
            return;
        }
        // LET THE DATA GATHERING COMMENCE
        boolean melee = true;
        double poisonMult = 1.0;
        if (event.getDamager() instanceof LivingEntity) {
            a = (LivingEntity) event.getDamager();
        } else if (event.getDamager() instanceof Projectile && ((Projectile) event.getDamager()).getShooter() instanceof LivingEntity) {
            a = (LivingEntity) ((Projectile) event.getDamager()).getShooter();
            melee = false;
        } else {
            return;
        }
        LivingEntity b = (LivingEntity) event.getEntity();
        if (a == null || b == null) {
            return;
        }
        if (b instanceof Player) {
            double chance = plugin.getChampionManager().getChampion(b.getUniqueId()).getAttributeValues().get(StrifeAttribute.EVASION);
            if (random.nextDouble() < chance) {
                event.setCancelled(true);
                b.getWorld().playSound(a.getEyeLocation(), Sound.GHAST_FIREBALL, 1f, 2f);
                return;
            }
        }
        double damage;
        double meleeDamageA = StrifeAttribute.MELEE_DAMAGE.getBaseValue(), attackSpeedA;
        double criticalDamageA = StrifeAttribute.CRITICAL_DAMAGE.getBaseValue(), armorPenA = StrifeAttribute.ARMOR_PENETRATION.getBaseValue();
        double lifeStealA = StrifeAttribute.LIFE_STEAL.getBaseValue(), lifeStolenA;
        double rangedDamageA = StrifeAttribute.RANGED_DAMAGE.getBaseValue(), criticalRateA = StrifeAttribute.CRITICAL_RATE.getBaseValue();
        double attackSpeedMultA = 1D, fireDamageA = StrifeAttribute.FIRE_DAMAGE.getBaseValue();
        double armorB = StrifeAttribute.ARMOR.getBaseValue(), reflectDamageB = StrifeAttribute.DAMAGE_REFLECT.getBaseValue();
        double parryB, blockB = StrifeAttribute.BLOCK.getBaseValue();
        boolean blocking = false;
        boolean parried = false;
        if (a instanceof Player) {
            if (a.hasPotionEffect(PotionEffectType.POISON)) {
                poisonMult = 0.25;
            }
            for (EntityDamageEvent.DamageModifier modifier : EntityDamageEvent.DamageModifier.values()) {
                if (event.isApplicable(modifier)) {
                    event.setDamage(modifier, 0D);
                }
            }
            Player p = (Player) a;
            Champion champ = plugin.getChampionManager().getChampion(p.getUniqueId());
            Map<StrifeAttribute, Double> vals = champ.getAttributeValues();
            meleeDamageA = vals.get(StrifeAttribute.MELEE_DAMAGE);
            attackSpeedA = (StrifeAttribute.ATTACK_SPEED.getBaseValue() * (1 / (1 + vals.get(StrifeAttribute.ATTACK_SPEED))));
            criticalDamageA = vals.get(StrifeAttribute.CRITICAL_DAMAGE);
            armorPenA = vals.get(StrifeAttribute.ARMOR_PENETRATION);
            lifeStealA = vals.get(StrifeAttribute.LIFE_STEAL);
            rangedDamageA = vals.get(StrifeAttribute.RANGED_DAMAGE);
            criticalRateA = vals.get(StrifeAttribute.CRITICAL_RATE);
            fireDamageA = vals.get(StrifeAttribute.FIRE_DAMAGE);
            long timeLeft = plugin.getAttackSpeedTask().getTimeLeft(a.getUniqueId());
            long timeToSet = Math.round(Math.max(4.0 * attackSpeedA, 0.0));
            if (timeLeft > 0) {
                attackSpeedMultA = Math.max(1.0 - 1.0 * timeLeft / timeToSet, 0.0);
            }
            plugin.getAttackSpeedTask().setTimeLeft(a.getUniqueId(), timeToSet);
        } else {
            if (a.getType() != null) {
                BeastData data = plugin.getBeastPlugin().getData(a.getType());
                String name = a.getCustomName() != null ? ChatColor.stripColor(a.getCustomName()) : "0";
                if (data != null && a.getCustomName() != null) {
                    int level = NumberUtils.toInt(CharMatcher.DIGIT.retainFrom(name));
                    meleeDamageA = data.getDamageExpression().setVariable("LEVEL", level).evaluate();
                    rangedDamageA = meleeDamageA;
                }
            }
        }
        if (b instanceof Player) {
            Player p = (Player) b;
            Champion champ = plugin.getChampionManager().getChampion(p.getUniqueId());
            Map<StrifeAttribute, Double> vals = champ.getAttributeValues();
            if (a instanceof Player) {
                meleeDamageA = (meleeDamageA / 2);
                rangedDamageA = (rangedDamageA / 2);
            }
            armorB = vals.get(StrifeAttribute.ARMOR);
            reflectDamageB = vals.get(StrifeAttribute.DAMAGE_REFLECT);
            parryB = vals.get(StrifeAttribute.PARRY);
            blockB = vals.get(StrifeAttribute.BLOCK);
            if (((Player) b).isBlocking()) {
                blocking = true;
                if (random.nextDouble() < parryB) {
                    parried = true;
                }
            }
        }

        // LET THE DAMAGE CALCULATION COMMENCE

        if (melee) {
            if (blocking) {
                if (parried) {
                    damage = meleeDamageA * attackSpeedMultA;
                    if (a instanceof Player) {
                        damage *= 0.5;
                    }
                    a.damage(damage * 1.25);
                    event.setCancelled(true);
                    b.getWorld().playSound(b.getEyeLocation(), Sound.ANVIL_LAND, 1f, 2f);
                    return;
                }
                damage = meleeDamageA * attackSpeedMultA;
                if (random.nextDouble() < criticalRateA) {
                    damage = damage * criticalDamageA;
                    b.getWorld().playSound(b.getEyeLocation(), Sound.FALL_BIG, 2f, 1f);
                }
                double damageReducer = (1 - (armorB * (1 - armorPenA)));
                double blockReducer = (1 - blockB);
                if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {
                    damage += damage / (a.getLocation().distanceSquared(b.getLocation()) / 2);
                }
                event.setDamage(EntityDamageEvent.DamageModifier.BASE, damage * damageReducer * blockReducer);
                if (a instanceof Player) {
                    lifeStolenA = event.getFinalDamage() * lifeStealA * poisonMult;
                    a.setHealth(Math.min(a.getHealth() + lifeStolenA, a.getMaxHealth()));
                }
                if (reflectDamageB > 0) {
                    a.damage(damage * reflectDamageB);
                    a.getWorld().playSound(a.getEyeLocation(), Sound.GLASS, 0.8f, 2f);
                }
                b.setFireTicks((int) Math.round(fireDamageA * 20));
                return;
            }
            damage = meleeDamageA * attackSpeedMultA;
            if (random.nextDouble() < criticalRateA) {
                damage = damage * criticalDamageA;
                b.getWorld().playSound(b.getEyeLocation(), Sound.FALL_BIG, 2f, 1f);
            }
            double damageReducer = (1 - (armorB * (1 - armorPenA)));
            if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {
                damage += damage / (a.getLocation().distanceSquared(b.getLocation()) / 2);
            }
            event.setDamage(EntityDamageEvent.DamageModifier.BASE, damage * damageReducer);
            if (a instanceof Player) {
                lifeStolenA = event.getFinalDamage() * lifeStealA * poisonMult;
                a.setHealth(Math.min(a.getHealth() + lifeStolenA, a.getMaxHealth()));
            }
            if (reflectDamageB > 0) {
                a.damage(damage * reflectDamageB);
                a.getWorld().playSound(a.getEyeLocation(), Sound.GLASS, 0.8f, 2f);
            }
            b.setFireTicks((int) Math.round(fireDamageA * 20));
            return;
        }
        if (blocking) {
            if (parried) {
                event.setCancelled(true);
                b.getWorld().playSound(b.getEyeLocation(), Sound.ANVIL_LAND, 1f, 2f);
                return;
            }
            damage = rangedDamageA * (a instanceof Player ? (event.getDamager().getVelocity().lengthSquared() / Math.pow(3, 2)) : 1);
            if (random.nextDouble() < criticalRateA) {
                damage = damage * criticalDamageA;
                b.getWorld().playSound(b.getEyeLocation(), Sound.FALL_BIG, 2f, 1f);
            }
            double damageReducer = (1 - (armorB * (1 - armorPenA)));
            double blockReducer = (1 - blockB);
            if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {
                damage += damage / (a.getLocation().distanceSquared(b.getLocation()) / 2);
            }
            event.setDamage(EntityDamageEvent.DamageModifier.BASE, damage * damageReducer * blockReducer);
            if (a instanceof Player) {
                lifeStolenA = event.getFinalDamage() * lifeStealA * poisonMult;
                a.setHealth(Math.min(a.getHealth() + lifeStolenA, a.getMaxHealth()));
            }
            if (reflectDamageB > 0) {
                a.damage(damage * reflectDamageB);
                a.getWorld().playSound(a.getEyeLocation(), Sound.GLASS, 0.8f, 2f);
            }
            b.setFireTicks((int) Math.round(fireDamageA * 20));
            return;
        }
        damage = rangedDamageA * (a instanceof Player ? (event.getDamager().getVelocity().lengthSquared() / Math.pow(3, 2)) : 1);
        if (random.nextDouble() < criticalRateA) {
            damage = damage * criticalDamageA;
            b.getWorld().playSound(b.getEyeLocation(), Sound.FALL_BIG, 2f, 1f);
        }
        double damageReducer = (1 - (armorB * (1 - armorPenA)));
        if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {
            damage += damage / (a.getLocation().distanceSquared(b.getLocation()) / 2);
        }
        event.setDamage(EntityDamageEvent.DamageModifier.BASE, damage * damageReducer);
        if (a instanceof Player) {
            lifeStolenA = event.getFinalDamage() * lifeStealA * poisonMult;
            a.setHealth(Math.min(a.getHealth() + lifeStolenA, a.getMaxHealth()));
        }
        if (reflectDamageB > 0) {
            a.damage(damage * reflectDamageB);
            a.getWorld().playSound(a.getEyeLocation(), Sound.GLASS, 0.8f, 2f);
        }
        b.setFireTicks((int) Math.round(fireDamageA * 20));
    }

}
