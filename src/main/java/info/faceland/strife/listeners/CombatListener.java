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

import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;

import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.math.NumberUtils;
import com.tealcube.minecraft.bukkit.shade.google.common.base.CharMatcher;
import info.faceland.beast.BeastData;
import info.faceland.strife.StrifePlugin;
import info.faceland.strife.attributes.StrifeAttribute;
import info.faceland.strife.data.Champion;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;
import java.util.Random;

public class CombatListener implements Listener {

    private static final String[]
        DOGE_MEMES =
        {"<aqua>wow", "<green>wow", "<light purple>wow", "<aqua>much pain", "<green>much pain",
         "<light purple>much pain",
         "<aqua>many disrespects", "<green>many disrespects", "<light purple>many disrespects", "<red>no u",
         "<red>2damage4me"};
    private final StrifePlugin plugin;
    private final Random random;

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
        if (p.hasMetadata("NPC")) {
            return;
        }
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
            event.getEntity()
                .setVelocity(
                    event.getEntity().getVelocity().add(((Entity) event.getEntity().getShooter()).getVelocity()));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity() == null || event.getEntity().getKiller() == null) {
            return;
        }
        double chance = plugin.getChampionManager().getChampion(event.getEntity().getKiller().getUniqueId())
                .getAttributeValues().get(StrifeAttribute.HEAD_DROP);
        if (chance == 0) {
            return;
        }
        if (random.nextDouble() < chance) {
            LivingEntity e = event.getEntity();
            if (e.getType() == EntityType.SKELETON) {
                if (((Skeleton)e).getSkeletonType() == Skeleton.SkeletonType.NORMAL) {
                    ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short)0);
                    e.getWorld().dropItemNaturally(e.getLocation(), skull);
                }
            }
            else if ((e.getType() == EntityType.ZOMBIE)) {
                ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short)2);
                e.getWorld().dropItemNaturally(e.getLocation(), skull);
            }
            else if ((e.getType() == EntityType.CREEPER)) {
                ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short)4);
                e.getWorld().dropItemNaturally(e.getLocation(), skull);
            }
            else if ((e.getType() == EntityType.PLAYER)) {
                ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());
                SkullMeta skullMeta = (SkullMeta)skull.getItemMeta();
                skullMeta.setOwner(event.getEntity().getKiller().getName());
                skullMeta.setDisplayName(ChatColor.RED + "Head of " + event.getEntity().getKiller().getName());
                skull.setItemMeta(skullMeta);
                e.getWorld().dropItemNaturally(e.getLocation(), skull);
            }
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
        boolean aPlayer = false;
        boolean bPlayer = false;
        double poisonMult = 1.0;
        double meleeMult = 1.0;
        double rangedMult = 1.0;
        if (event.getDamager() instanceof LivingEntity) {
            a = (LivingEntity) event.getDamager();
        } else if (event.getDamager() instanceof Projectile && ((Projectile) event.getDamager())
            .getShooter() instanceof LivingEntity) {
            a = (LivingEntity) ((Projectile) event.getDamager()).getShooter();
            melee = false;
        } else {
            return;
        }
        LivingEntity b = (LivingEntity) event.getEntity();
        if (a == null || b == null) {
            return;
        }
        if (a instanceof Player) {
            aPlayer = true;
        }

        if (b instanceof Player) {
            bPlayer = true;
        }

        if (bPlayer) {
            double chance = plugin.getChampionManager().getChampion(b.getUniqueId()).getAttributeValues()
                .get(StrifeAttribute.EVASION);
            double accuracy;
            if (aPlayer) {
                accuracy = plugin.getChampionManager().getChampion(a.getUniqueId()).getAttributeValues()
                        .get(StrifeAttribute.ACCURACY);
                chance = chance * (1 - accuracy);
            }
            chance = 1 - (100 / (100 + (Math.pow((chance * 100), 1.2))));
            if (random.nextDouble() < chance) {
                event.setCancelled(true);
                b.getWorld().playSound(a.getEyeLocation(), Sound.GHAST_FIREBALL, 1f, 2f);
                return;
            }
        }
        double damage;
        double pvpMult = 1.0;
        double hungerMult = 1.0;
        double critbonus = 0, overbonus = 0, trueDamage = 0;
        double meleeDamageA = StrifeAttribute.MELEE_DAMAGE.getBaseValue(), attackSpeedA;
        double overchargeA = StrifeAttribute.OVERCHARGE.getBaseValue();
        double armorPenA = StrifeAttribute.ARMOR_PENETRATION.getBaseValue();
        double lifeStealA = StrifeAttribute.LIFE_STEAL.getBaseValue(), lifeStolenA;
        double rangedDamageA = StrifeAttribute.RANGED_DAMAGE.getBaseValue(), snarechanceA = StrifeAttribute.SNARE_CHANCE.getBaseValue();
        double criticalRateA = StrifeAttribute.CRITICAL_RATE.getBaseValue(), criticalDamageA = StrifeAttribute.CRITICAL_DAMAGE.getBaseValue();
        double attackSpeedMultA = 1D;
        double fireDamageA = StrifeAttribute.FIRE_DAMAGE.getBaseValue(), igniteChanceA = StrifeAttribute.IGNITE_CHANCE.getBaseValue();
        double lightningDamageA = StrifeAttribute.LIGHTNING_DAMAGE.getBaseValue(), shockChanceA = StrifeAttribute.SHOCK_CHANCE.getBaseValue();
        double iceDamageA = StrifeAttribute.ICE_DAMAGE.getBaseValue(), freezeChanceA = StrifeAttribute.FREEZE_CHANCE.getBaseValue();
        double armorB = StrifeAttribute.ARMOR.getBaseValue(), reflectDamageB = StrifeAttribute.DAMAGE_REFLECT.getBaseValue();
        double armorA = StrifeAttribute.ARMOR.getBaseValue();
        double healthB = b.getMaxHealth();
        double resistB = 0;
        double parryB, blockB = StrifeAttribute.BLOCK.getBaseValue();
        boolean blocking = false;
        boolean parried = false;
        if (b.hasPotionEffect(PotionEffectType.WITHER)) {
            meleeMult += 0.1D;
            rangedMult += 0.1D;
        }
        if (b.hasPotionEffect(PotionEffectType.DAMAGE_RESISTANCE)) {
            meleeMult -= 0.1D;
            rangedMult -= 0.1D;
        }
        if (aPlayer) {
            hungerMult = Math.min(((double) (((Player) a).getFoodLevel()))/7.0D, 1.0D);
            if (b instanceof Player) {
                pvpMult = 0.5;
            }
            if (a.hasPotionEffect(PotionEffectType.POISON)) {
                poisonMult = 0.33D;
            }
            if (a.hasPotionEffect(PotionEffectType.INCREASE_DAMAGE)) {
                meleeMult += 0.1D;
            }
            if (a.hasPotionEffect(PotionEffectType.WEAKNESS)) {
                meleeMult -= 0.1D;
            }
            if (a.hasPotionEffect(PotionEffectType.NIGHT_VISION)) {
                rangedMult = 1.1D;
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
            attackSpeedA =
                (StrifeAttribute.ATTACK_SPEED.getBaseValue() * (1 / (1 + vals.get(StrifeAttribute.ATTACK_SPEED))));
            criticalDamageA = vals.get(StrifeAttribute.CRITICAL_DAMAGE);
            armorPenA = vals.get(StrifeAttribute.ARMOR_PENETRATION);
            armorA = vals.get(StrifeAttribute.ARMOR);
            overchargeA = vals.get(StrifeAttribute.OVERCHARGE);
            lifeStealA = vals.get(StrifeAttribute.LIFE_STEAL);
            rangedDamageA = vals.get(StrifeAttribute.RANGED_DAMAGE);
            criticalRateA = vals.get(StrifeAttribute.CRITICAL_RATE);
            snarechanceA = vals.get(StrifeAttribute.SNARE_CHANCE);
            fireDamageA = vals.get(StrifeAttribute.FIRE_DAMAGE);
            lightningDamageA = vals.get(StrifeAttribute.LIGHTNING_DAMAGE);
            iceDamageA = vals.get(StrifeAttribute.ICE_DAMAGE);
            igniteChanceA = vals.get(StrifeAttribute.IGNITE_CHANCE);
            shockChanceA = vals.get(StrifeAttribute.SHOCK_CHANCE);
            freezeChanceA = vals.get(StrifeAttribute.FREEZE_CHANCE);
            long timeLeft = plugin.getAttackSpeedTask().getTimeLeft(a.getUniqueId());
            long timeToSet = Math.round(Math.max(4.0 * attackSpeedA, 0.0));
            if (timeLeft > 0) {
                attackSpeedMultA = Math.max(1.0 - 1.0 * timeLeft / timeToSet, 0.0);
            }
            plugin.getAttackSpeedTask().setTimeLeft(a.getUniqueId(), timeToSet);
        } else {
            if (a.hasMetadata("DAMAGE")) {
                meleeDamageA = a.getMetadata("DAMAGE").get(0).asDouble();
                rangedDamageA = meleeDamageA;
            } else {
                if (a.getType() != null) {
                    BeastData data = plugin.getBeastPlugin().getData(a.getType());
                    String name = a.getCustomName() != null ? ChatColor.stripColor(a.getCustomName()) : "0";
                    if (data != null && a.getCustomName() != null) {
                        int level = NumberUtils.toInt(CharMatcher.DIGIT.retainFrom(name));
                        meleeDamageA = (data.getDamageExpression().setVariable("LEVEL", level).evaluate());
                        rangedDamageA = meleeDamageA;
                        a.setMetadata("DAMAGE", new FixedMetadataValue(plugin, meleeDamageA));
                    }
                }
            }
        }
        if (bPlayer) {
            Player p = (Player) b;
            Champion champ = plugin.getChampionManager().getChampion(p.getUniqueId());
            Map<StrifeAttribute, Double> vals = champ.getAttributeValues();
            armorB = vals.get(StrifeAttribute.ARMOR);
            resistB = vals.get(StrifeAttribute.RESISTANCE);
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
            if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {
                damage = meleeDamageA * Math.max(0.3, 2.5 / (a.getLocation().distanceSquared(b.getLocation()) + 1));
            } else {
                damage = meleeDamageA * attackSpeedMultA * meleeMult;
            }
            if (parried) {
                double attackerArmor = 100 / (100 + (Math.pow((armorA * 100), 1.36)));
                a.damage(damage * 0.80 * attackerArmor * pvpMult);
                event.setCancelled(true);
                b.getWorld().playSound(b.getEyeLocation(), Sound.ANVIL_LAND, 1f, 2f);
                return;
            }
            if (random.nextDouble() < criticalRateA) {
                critbonus = damage * (criticalDamageA - 1.0);
                b.getWorld().playSound(b.getEyeLocation(), Sound.FALL_BIG, 2f, 1f);
            }
            if (attackSpeedMultA >= 1D) {
                overbonus = overchargeA * damage;
            }
            damage = damage + critbonus + overbonus;
            double blockReducer = 1;
            double armorReduction;
            if (armorB > 0) {
                if (armorPenA < 1) {
                    armorReduction = 100 / (100 + (Math.pow(((armorB * (1 - armorPenA)) * 100), 1.3)));
                } else {
                    armorReduction = 1 + ((armorPenA - 1) / 5);
                }
            } else {
                armorReduction = 1 + (armorPenA / 5);
            }
            if (blocking) {
                blockReducer = (1 - blockB);
            }
            if (reflectDamageB > 0) {
                a.damage(damage * reflectDamageB * pvpMult);
                a.getWorld().playSound(a.getEyeLocation(), Sound.GLASS, 0.6f, 2f);
            }
            if (fireDamageA > 0) {
                if (random.nextDouble() < ((igniteChanceA * (0.25 + attackSpeedMultA * 0.75)) * (1 - resistB))) {
                    b.setFireTicks(20 + (int) Math.round(fireDamageA * 20));
                    b.getWorld().playSound(b.getEyeLocation(), Sound.FIRE_IGNITE, 1f, 1f);
                }
            }
            if (lightningDamageA > 0) {
                if (random.nextDouble() < ((shockChanceA * (0.25 + attackSpeedMultA * 0.75)) * (1 - resistB))) {
                    trueDamage = lightningDamageA;
                    b.getWorld().playSound(b.getEyeLocation(), Sound.AMBIENCE_THUNDER, 1f, 1.5f);
                }
            }
            if (iceDamageA > 0) {
                if (random.nextDouble() < ((freezeChanceA * (0.25 + attackSpeedMultA * 0.75)) * (1 - resistB))) {
                    damage = damage + iceDamageA + (healthB / 200);
                    b.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 5 + (int)iceDamageA*3, 1));
                    b.getWorld().playSound(b.getEyeLocation(), Sound.GLASS, 1f, 1f);
                }
            }
            if (!(b instanceof ArmorStand)) {
                event.setDamage(EntityDamageEvent.DamageModifier.ARMOR, 0);
                event.setDamage(EntityDamageEvent.DamageModifier.RESISTANCE, 0);
            }
            event.setDamage(EntityDamageEvent.DamageModifier.BASE, ((damage * armorReduction * blockReducer) + trueDamage) *
                    pvpMult);
            if (a instanceof Player) {
                lifeStolenA = event.getFinalDamage() * lifeStealA * poisonMult * hungerMult;
                a.setHealth(Math.min(a.getHealth() + lifeStolenA, a.getMaxHealth()));
            }
        } else {
            if (parried) {
                event.setCancelled(true);
                b.getWorld().playSound(b.getEyeLocation(), Sound.ANVIL_LAND, 1f, 2f);
                return;
            }
            damage = rangedDamageA * rangedMult * (a instanceof Player ? (event.getDamager().getVelocity().lengthSquared() / Math.pow(3, 2)) : 1);
            double blockReducer = 1;
            double armorReduction;
            if (armorB > 0) {
                if (armorPenA < 1) {
                    armorReduction = 100 / (100 + (Math.pow(((armorB * (1 - armorPenA)) * 100), 1.36)));
                } else {
                    armorReduction = 1 + ((armorPenA - 1) / 5);
                }
            } else {
                armorReduction = 1 + (armorPenA / 5);
            }
            if (random.nextDouble() < criticalRateA) {
                damage = damage * criticalDamageA;
                b.getWorld().playSound(b.getEyeLocation(), Sound.FALL_BIG, 2f, 1f);
            }
            if (random.nextDouble() < snarechanceA) {
                b.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 5));
            }
            if (blocking) {
                blockReducer = (1 - blockB);
            }
            if (fireDamageA > 0) {
                if (random.nextDouble() < (igniteChanceA * (1 - resistB))) {
                    b.setFireTicks(20 + (int) Math.round(fireDamageA * 20));
                    b.getWorld().playSound(b.getEyeLocation(), Sound.FIRE_IGNITE, 1f, 1f);
                }
            }
            if (lightningDamageA > 0) {
                if (random.nextDouble() < (shockChanceA * (1 - resistB))) {
                    trueDamage = lightningDamageA;
                    b.getWorld().playSound(b.getEyeLocation(), Sound.AMBIENCE_THUNDER, 1f, 1.5f);
                    a.getWorld().playSound(a.getEyeLocation(), Sound.AMBIENCE_THUNDER, 1f, 1.5f);
                }
            }
            if (iceDamageA > 0) {
                if (random.nextDouble() < (freezeChanceA * (1 - resistB))) {
                    damage = damage + iceDamageA + (healthB / 200);
                    b.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 5 + (int)iceDamageA*3, 1));
                    b.getWorld().playSound(b.getEyeLocation(), Sound.GLASS, 1f, 1f);
                }
            }
            if (!(b instanceof ArmorStand)) {
                event.setDamage(EntityDamageEvent.DamageModifier.ARMOR, 0);
                event.setDamage(EntityDamageEvent.DamageModifier.RESISTANCE, 0);
            }
            event.setDamage(EntityDamageEvent.DamageModifier.BASE, ((damage * armorReduction * blockReducer) +
                    trueDamage) * pvpMult);
            if (a instanceof Player) {
                lifeStolenA = event.getFinalDamage() * lifeStealA * poisonMult * hungerMult;
                a.setHealth(Math.min(a.getHealth() + lifeStolenA, a.getMaxHealth()));
            }
        }
    }

}
