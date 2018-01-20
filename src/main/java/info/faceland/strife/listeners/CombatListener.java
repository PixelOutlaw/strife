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

import com.tealcube.minecraft.bukkit.TextUtils;
import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;

import gyurix.spigotlib.ChatAPI;
import info.faceland.strife.StrifePlugin;
import info.faceland.strife.attributes.StrifeAttribute;
import info.faceland.strife.data.AttributedEntity;
import info.faceland.strife.data.Champion;

import info.faceland.strife.events.CriticalEvent;
import info.faceland.strife.events.EvadeEvent;
import info.faceland.strife.managers.DarknessManager;
import org.bukkit.event.EventHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.text.DecimalFormat;
import java.util.*;

public class CombatListener implements Listener {

    private final StrifePlugin plugin;
    private final Random random;

    private static final String ATTACK_MISSED = TextUtils.color("&f&lMiss!");
    private static final String ATTACK_DODGED = TextUtils.color("&f&lDodge!");
    private static final String ATTACK_PARRIED = TextUtils.color("&f&lParry!");
    private static final String ATTACK_BLOCKED = TextUtils.color("&e&lBlocked!");
    private static final String ATTACK_NO_DAMAGE = TextUtils.color("&b&lInvulnerable!");
    private static final String PERFECT_GUARD = TextUtils.color("&b&lPerfect Block! &e&lAttack Recharged!");

    private static final DecimalFormat ONE_DECIMAL = new DecimalFormat("#.#");

    private static final String[] DOGE_MEMES =
            {"<aqua>wow", "<green>wow", "<light purple>wow", "<aqua>much pain", "<green>much pain",
                    "<light purple>much pain", "<aqua>many disrespects", "<green>many disrespects",
                    "<light purple>many disrespects", "<red>no u", "<red>2damage4me"};

    public CombatListener(StrifePlugin plugin) {
        this.plugin = plugin;
        random = new Random(System.currentTimeMillis());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDogeProc(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Champion playerChamp = plugin.getChampionManager().getChampion(event.getEntity().getUniqueId());
        if (random.nextDouble() <= playerChamp.getCache().getAttribute(StrifeAttribute.DOGE)) {
            MessageUtils.sendMessage(event.getEntity(), DOGE_MEMES[random.nextInt(DOGE_MEMES.length)]);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void strifeDamageHandler(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) {
            return;
        }
        DamageType damageType = DamageType.MELEE;

        if (!(event.getEntity() instanceof LivingEntity) || event.getEntity() instanceof ArmorStand) {
            return;
        }

        LivingEntity defendEntity = (LivingEntity) event.getEntity();
        LivingEntity attackEntity;
        Projectile projectile = null;

        if (event.getDamager() instanceof Projectile) {
            projectile = (Projectile) event.getDamager();
            if (event.getDamager() instanceof ShulkerBullet) {
                damageType = DamageType.MAGIC;
            } else {
                damageType = DamageType.RANGED;
            }
            ProjectileSource shooter = projectile.getShooter();
            if (shooter instanceof LivingEntity) {
                attackEntity = (LivingEntity) shooter;
            } else {
                return;
            }
        } else {
            attackEntity = (LivingEntity) event.getDamager();
        }

        if (defendEntity.hasMetadata("NPC")) {
            if (projectile != null) {
                projectile.remove();
            }
            return;
        }

        for (EntityDamageEvent.DamageModifier modifier : EntityDamageEvent.DamageModifier.values()) {
            if (event.isApplicable(modifier)) {
                if (modifier == EntityDamageEvent.DamageModifier.ABSORPTION) {
                    continue;
                }
                event.setDamage(modifier, 0D);
            }
        }

        AttributedEntity attacker = plugin.getEntityStatCache().getEntity(attackEntity, true);
        AttributedEntity defender = plugin.getEntityStatCache().getEntity(defendEntity, true);

        double evasionMult = getEvasionMult(attacker, defender);

        if (evasionMult <= 0) {
            event.setCancelled(true);
            return;
        }

        double physicalBaseDamage = 0;
        double magicBaseDamage = 0;

        switch (damageType) {
            case MELEE:
                physicalBaseDamage = attacker.getAttribute(StrifeAttribute.MELEE_DAMAGE);
                break;
            case RANGED:
                physicalBaseDamage = attacker.getAttribute(StrifeAttribute.RANGED_DAMAGE);
                break;
            case MAGIC:
                magicBaseDamage = attacker.getAttribute(StrifeAttribute.MAGIC_DAMAGE);
        }

        double fireBaseDamage = attacker.getAttribute(StrifeAttribute.FIRE_DAMAGE);
        double iceBaseDamage = attacker.getAttribute(StrifeAttribute.ICE_DAMAGE);
        double lightningBaseDamage = attacker.getAttribute(StrifeAttribute.LIGHTNING_DAMAGE);
        double shadowBaseDamage = attacker.getAttribute(StrifeAttribute.DARK_DAMAGE);

        double attackSpeedMult = 1D;
        if (damageType == DamageType.MELEE) {
            attackSpeedMult = plugin.getAttackSpeedTask().getAttackMultiplier(attacker);
        } else if (projectile.hasMetadata("AS_MULT")) {
            attackSpeedMult = projectile.getMetadata("AS_MULT").get(0).asDouble();
        }

        double pvpMult = 1D;
        if (attackEntity instanceof Player && defendEntity instanceof Player) {
            pvpMult = plugin.getSettings().getDouble("config.pvp-multiplier", 0.5);
        }

        double blockAmount = 0D;
        if (defendEntity instanceof Player) {
            blockAmount = getBlockAmount(defender, event);
        }

        double potionMult = getPotionMult(attackEntity, defendEntity);

        physicalBaseDamage = applyArmorReduction(physicalBaseDamage, attacker, defender);
        magicBaseDamage = applyWardReduction(magicBaseDamage, attacker, defender);
        fireBaseDamage = applyFireResist(fireBaseDamage, defender);
        iceBaseDamage = applyIceResist(iceBaseDamage, defender);
        lightningBaseDamage = applyLightningResist(lightningBaseDamage, defender);
        shadowBaseDamage = applyShadowResist(shadowBaseDamage, defender);

        double bonusFireDamage = attemptIgnite(fireBaseDamage, attacker, defendEntity);
        double bonusIceDamage = attemptFreeze(iceBaseDamage, attacker, defendEntity);
        double bonusLightningDamage = attemptShock(lightningBaseDamage, attacker, defendEntity);
        double bonusShadowDamage = attemptCorrupt(shadowBaseDamage, attacker, defendEntity);

        fireBaseDamage += bonusFireDamage;
        iceBaseDamage += bonusIceDamage;
        lightningBaseDamage += bonusLightningDamage;
        shadowBaseDamage += bonusShadowDamage;

        double standardDamage = physicalBaseDamage + magicBaseDamage;
        double elementalDamage = fireBaseDamage + iceBaseDamage + lightningBaseDamage + shadowBaseDamage;
        System.out.println("d3: " + standardDamage);
        System.out.println("d04: " + elementalDamage);

        double bonusCriticalDamage = getCriticalDamage(attacker, defender, standardDamage);
        double bonusOverchargeDamage = getOverchargeDamage(attacker, standardDamage, attackSpeedMult);
        System.out.println("d1: " + standardDamage);
        System.out.println("d1: " + elementalDamage);
        standardDamage += bonusCriticalDamage;
        standardDamage += bonusOverchargeDamage;
        System.out.println("d0: " + standardDamage);
        System.out.println("d0: " + elementalDamage);
        standardDamage *= evasionMult;
        standardDamage *= attackSpeedMult;
        standardDamage *= potionMult;
        standardDamage -= blockAmount;

        // Block is removed from standard damage first.
        // The remainder is still needed for elemental.
        if (standardDamage < 0) {
            blockAmount = -standardDamage;
            standardDamage = 0;
        } else {
            blockAmount = 0;
        }

        standardDamage *= pvpMult;
        applyLifeSteal(attacker, standardDamage);

        elementalDamage = applyGenericResist(elementalDamage, defender);
        elementalDamage *= evasionMult;
        elementalDamage *= attackSpeedMult;
        elementalDamage *= potionMult;
        elementalDamage -= blockAmount;
        if (elementalDamage < 0) {
            event.setDamage(0);
            event.setCancelled(true);
            return;
        }
        elementalDamage *= pvpMult;

        double finalDamage = standardDamage + elementalDamage;

        if (event.getDamage(EntityDamageEvent.DamageModifier.ABSORPTION) != 0) {
            event.setDamage(EntityDamageEvent.DamageModifier.ABSORPTION, -finalDamage);
        }
        sendActionbarDamage(attackEntity, finalDamage, bonusOverchargeDamage, bonusCriticalDamage, bonusFireDamage, bonusIceDamage,
            bonusLightningDamage, bonusShadowDamage);
        event.setDamage(EntityDamageEvent.DamageModifier.BASE, finalDamage);
    }

    private void sendActionbarDamage(LivingEntity entity, double damage, double overBonus, double critBonus, double fireBonus,
        double iceBonus, double lightningBonus, double shadowBonus) {
        if (!(entity instanceof Player)) {
            return;
        }
        StringBuilder damageString = new StringBuilder("&f&l" + (int) Math.ceil(damage) + " Damage! ");
        if (overBonus > 0) {
            damageString.append("&e✦");
        }
        if (critBonus > 0) {
            damageString.append("&c✶");
        }
        if (fireBonus > 0) {
            damageString.append("&6☀");
        }
        if (iceBonus > 0) {
            damageString.append("&b❊");
        }
        if (lightningBonus > 0) {
            damageString.append("&7⚡");
        }
        if (shadowBonus > 0) {
            damageString.append("&8❂");
        }
        ChatAPI.sendJsonMsg(ChatAPI.ChatMessageType.ACTION_BAR, TextUtils.color(damageString.toString()), (Player) entity);
    }

    private double rollDouble(boolean lucky) {
        return lucky ? Math.max(rollDouble(), rollDouble()) : rollDouble();
    }

    private double rollDouble() {
        return random.nextDouble();
    }

    private boolean rollBool(double chance, boolean lucky) {
        return lucky ? rollBool(chance) || rollBool(chance) : rollBool(chance);
    }

    private boolean rollBool(double chance) {
        return random.nextDouble() <= chance;
    }

    private double getEvasionMult(AttributedEntity attacker, AttributedEntity defender) {
        double accuracy = attacker.getAttribute(StrifeAttribute.ACCURACY);
        double evasion = Math.max(defender.getAttribute(StrifeAttribute.EVASION), 1);
        double minimumMult = ((evasion * accuracy) / (evasion * evasion)) - 0.2;
        double evasionMult = minimumMult + ((1 - minimumMult) * rollDouble(hasLuck(defender.getEntity())));
        if (evasionMult <= 0.5) {
            callEvadeEvent(defender.getEntity(), attacker.getEntity());
            defender.getEntity().getWorld().playSound(defender.getEntity().getEyeLocation(), Sound.ENTITY_GHAST_SHOOT, 0.5f, 2f);
            if (defender.getEntity() instanceof Player) {
                ChatAPI.sendJsonMsg(ChatAPI.ChatMessageType.ACTION_BAR, ATTACK_DODGED, (Player) defender.getEntity());
            }
            if (attacker.getEntity() instanceof Player) {
                ChatAPI.sendJsonMsg(ChatAPI.ChatMessageType.ACTION_BAR, ATTACK_MISSED, (Player) attacker.getEntity());
            }
            return -1;
        }
        return Math.min(evasionMult, 1.0);
    }

    private double applyArmorReduction(double physicalDamage, AttributedEntity attacker, AttributedEntity defender) {
        if (physicalDamage == 0) {
            return 0D;
        }
        double mult = getArmorMult(
            defender.getAttribute(StrifeAttribute.ARMOR), attacker.getAttribute(StrifeAttribute.ARMOR_PENETRATION));
        return physicalDamage * mult;
    }

    private double applyWardReduction(double magicDamage, AttributedEntity attacker, AttributedEntity defender) {
        if (magicDamage == 0) {
            return 0D;
        }
        double mult = getWardingMult(
            defender.getAttribute(StrifeAttribute.WARDING), attacker.getAttribute(StrifeAttribute.WARD_PENETRATION));
        return magicDamage * mult;
    }

    private double getArmorMult(double armor, double apen) {
        double adjustedArmor = armor - apen;
        return Math.min(1, 75 / (75 + adjustedArmor));
    }

    private double getWardingMult(double warding, double wpen) {
        double adjustedWarding = warding - wpen;
        return Math.min(1, 50 / (50 + adjustedWarding));
    }

    private double applyFireResist(double damage, AttributedEntity defender) {
        if (damage == 0 ) {
            return 0;
        }
        return damage * (1 - defender.getAttribute(StrifeAttribute.FIRE_RESIST) / 100);
    }

    private double applyIceResist(double damage, AttributedEntity defender) {
        if (damage == 0 ) {
            return 0;
        }
        return damage * (1 - defender.getAttribute(StrifeAttribute.ICE_RESIST) / 100);
    }

    private double applyLightningResist(double damage, AttributedEntity defender) {
        if (damage == 0 ) {
            return 0;
        }
        return damage * (1 - defender.getAttribute(StrifeAttribute.LIGHTNING_RESIST) / 100);
    }

    private double applyShadowResist(double damage, AttributedEntity defender) {
        if (damage == 0 ) {
            return 0;
        }
        return damage * (1 - defender.getAttribute(StrifeAttribute.DARK_RESIST) / 100);
    }

    private double applyGenericResist(double damage, AttributedEntity defender) {
        if (damage == 0 ) {
            return 0;
        }
        return damage * (1 - defender.getAttribute(StrifeAttribute.RESISTANCE) / 100);
    }

    private double getCriticalDamage(AttributedEntity attacker, AttributedEntity defender, double damage) {
        if (attacker.getAttribute(StrifeAttribute.CRITICAL_RATE) / 100 >= rollDouble(hasLuck(attacker.getEntity()))) {
            callCritEvent(attacker.getEntity(), attacker.getEntity());
            defender.getEntity().getWorld().playSound(defender.getEntity().getEyeLocation(), Sound.ENTITY_GENERIC_BIG_FALL, 2f, 0.8f);
            return damage * (attacker.getAttribute(StrifeAttribute.CRITICAL_DAMAGE) / 100);
        }
        return 0;
    }

    private double getOverchargeDamage(AttributedEntity attacker, double damage, double attackSpeedMult) {
        if (attackSpeedMult > 0.95) {
            return damage * (attacker.getAttribute(StrifeAttribute.OVERCHARGE) / 100);
        }
        return 0;
    }

    private double attemptIgnite(double damage, AttributedEntity attacker, LivingEntity defender) {
        if (damage == 0 || rollDouble() >= attacker.getAttribute(StrifeAttribute.IGNITE_CHANCE) / 100) {
            return 0D;
        }
        defender.setFireTicks(Math.max(60 + (int) damage, defender.getFireTicks()));
        defender.getWorld().playSound(defender.getEyeLocation(),Sound.ITEM_FLINTANDSTEEL_USE, 1f, 1f);
        defender.getWorld().spawnParticle(Particle.FLAME, defender.getEyeLocation(), 6 + (int) damage / 2,
            0.3, 0.3, 0.3, 0.03);
        if (defender.getFireTicks() > 0) {
            return damage;
        }
        return 0D;
    }

    private double attemptShock(double damage, AttributedEntity attacker, LivingEntity defender) {
        if (damage == 0 || rollDouble() >= attacker.getAttribute(StrifeAttribute.SHOCK_CHANCE) / 100) {
            return 0D;
        }
        double hpMult = 4 - 7.5 * (defender.getHealth() / defender.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
        defender.getWorld().playSound(defender.getEyeLocation(), Sound.ENTITY_LIGHTNING_THUNDER, 0.7f, 2f);
        defender.getWorld().spawnParticle(Particle.CRIT_MAGIC, defender.getEyeLocation(), 10 + (int) damage / 2,
                0.8,0.8,0.8, 0.1);
        if (defender instanceof Creeper) {
            ((Creeper) defender).setPowered(true);
        }
        return Math.max(damage, damage * hpMult) - damage;
    }

    private double attemptFreeze(double damage, AttributedEntity attacker, LivingEntity defender) {
        if (damage == 0 || rollDouble() >= attacker.getAttribute(StrifeAttribute.FREEZE_CHANCE) / 100) {
            return 0D;
        }
        double bonusHp = attacker.getAttribute(StrifeAttribute.HEALTH) - 30;
        if (!defender.hasPotionEffect(PotionEffectType.SLOW)) {
            defender.getActivePotionEffects().add(new PotionEffect(PotionEffectType.SLOW, 30, 1));
        }
        defender.getWorld().playSound(defender.getEyeLocation(), Sound.BLOCK_GLASS_BREAK, 1f, 1.0f);
        defender.getWorld().spawnParticle(Particle.SNOWBALL, defender.getEyeLocation(), 4 + (int) damage / 2,
                0.3, 0.3, 0.2, 0.0);
        return (damage * 1.3 + (bonusHp / 5)) - damage;
    }

    private double attemptCorrupt(double damage, AttributedEntity attacker, LivingEntity defender) {
        if (damage == 0 || rollDouble() >= attacker.getAttribute(StrifeAttribute.CORRUPT_CHANCE) / 100) {
            return 0D;
        }
        defender.getWorld().playSound(defender.getEyeLocation(), Sound.ENTITY_WITHER_SHOOT, 0.7f, 2f);
        defender.getWorld().spawnParticle(Particle.SMOKE_NORMAL, defender.getEyeLocation(), 10,0.4, 0.4, 0.5, 0.1);
        DarknessManager.updateEntity(defender, damage);
        return (damage * (1 + DarknessManager.getEntity(defender) / 50)) - damage;
    }

    private double getBlockAmount(AttributedEntity defender, EntityDamageEvent event) {
        if (!event.isApplicable(EntityDamageEvent.DamageModifier.BLOCKING) ||
            event.getDamage(EntityDamageEvent.DamageModifier.BLOCKING) == 0) {
            return 0;
        }
        double blockedAmount = 0;
        double maxBlockAmount = defender.getAttribute(StrifeAttribute.BLOCK);
        double blockTimeLeft = plugin.getBlockTask().getTimeLeft(defender.getEntity().getUniqueId());
        if (blockTimeLeft > 0) {
            blockedAmount = maxBlockAmount * Math.max(1 - (blockTimeLeft / 6), 0.25);
            ChatAPI.sendJsonMsg(ChatAPI.ChatMessageType.ACTION_BAR, ATTACK_BLOCKED, (Player) defender.getEntity());
        } else {
            plugin.getAttackSpeedTask().setTicksLeft(defender.getEntity().getUniqueId(), 0L);
            plugin.getAttackSpeedTask().endTask(defender.getEntity().getUniqueId());
            ChatAPI.sendJsonMsg(ChatAPI.ChatMessageType.ACTION_BAR, PERFECT_GUARD, (Player) defender.getEntity());
        }
        plugin.getBlockTask().setTimeLeft(defender.getEntity().getUniqueId(), 6L);
        return blockedAmount;
    }

    private double getPotionMult(LivingEntity attacker, LivingEntity defender) {
        double potionMult = 1.0;
        Collection<PotionEffect> attackerEffects = attacker.getActivePotionEffects();
        Collection<PotionEffect> defenderEffects = defender.getActivePotionEffects();
        for (PotionEffect effect : attackerEffects) {
            if (effect.getType().equals(PotionEffectType.INCREASE_DAMAGE)) {
                potionMult += 0.1 * (effect.getAmplifier() + 1);
                continue;
            }
            if (effect.getType().equals(PotionEffectType.WEAKNESS)) {
                potionMult -= 0.1 * (effect.getAmplifier() + 1);
                continue;
            }
        }

        for (PotionEffect effect : defenderEffects) {
            if (effect.getType().equals(PotionEffectType.WITHER)) {
                potionMult += 0.15 * (effect.getAmplifier() + 1);
                continue;
            }
            if (effect.getType().equals(PotionEffectType.DAMAGE_RESISTANCE)) {
                potionMult -= 0.1 * (effect.getAmplifier() + 1);
                continue;
            }
        }
        return Math.max(0, potionMult);
    }

    public static double getResistPotionMult(LivingEntity defender) {
        double mult = 1.0;
        Collection<PotionEffect> defenderEffects = defender.getActivePotionEffects();
        for (PotionEffect effect : defenderEffects) {
            if (effect.getType().equals(PotionEffectType.DAMAGE_RESISTANCE)) {
                mult -= 0.1 * (effect.getAmplifier() + 1);
                continue;
            }
        }
        return mult;
    }

    private void applyLifeSteal(AttributedEntity attacker, double damage) {
        double lifeSteal = attacker.getAttribute(StrifeAttribute.LIFE_STEAL) / 100;
        if (lifeSteal <= 0 || attacker.getEntity().getHealth() <= 0 || attacker.getEntity().isDead()) {
            return;
        }
        double lifeStolen = damage * lifeSteal;
        if (attacker instanceof Player) {
            lifeStolen *= Math.min(((Player)attacker).getFoodLevel() / 7.0D, 1.0D);
        }
        if (attacker.getEntity().hasPotionEffect(PotionEffectType.POISON)) {
            lifeStolen *= 0.34;
        }
        restoreHealth(attacker.getEntity(), lifeStolen);
    }

    private void callCritEvent(LivingEntity attacker, LivingEntity victim) {
        CriticalEvent c = new CriticalEvent(attacker, victim);
        Bukkit.getPluginManager().callEvent(c);
    }

    private void callEvadeEvent(LivingEntity evader, LivingEntity attacker) {
        EvadeEvent ev = new EvadeEvent(evader, attacker);
        Bukkit.getPluginManager().callEvent(ev);
    }

    private boolean hasLuck(LivingEntity entity) {
        return entity.hasPotionEffect(PotionEffectType.LUCK);
    }

    private void restoreHealth(LivingEntity livingEntity, double amount) {
        livingEntity.setHealth(Math.min(livingEntity.getHealth() + amount, livingEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()));
    }

    private enum DamageType {
        MELEE, RANGED, MAGIC, TRUE, OTHER
    }
}
