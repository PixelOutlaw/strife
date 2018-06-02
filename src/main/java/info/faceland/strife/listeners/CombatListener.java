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
import info.faceland.strife.util.StatUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

import static info.faceland.strife.attributes.StrifeAttribute.*;

public class CombatListener implements Listener {

    private final StrifePlugin plugin;
    private final Random random;

    private static final String ATTACK_MISSED = TextUtils.color("&f&lMiss!");
    private static final String ATTACK_DODGED = TextUtils.color("&f&lDodge!");

    private static final String[] DOGE_MEMES =
            {"<aqua>wow", "<green>wow", "<light purple>wow", "<aqua>much pain", "<green>much pain",
                    "<light purple>much pain", "<aqua>many disrespects", "<green>many disrespects",
                    "<light purple>many disrespects", "<red>no u", "<red>2damage4me"};

    public CombatListener(StrifePlugin plugin) {
        this.plugin = plugin;
        this.random = new Random(System.currentTimeMillis());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDogeProc(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        AttributedEntity attacker = plugin.getEntityStatCache().getAttributedEntity((LivingEntity)event.getEntity());
        if (random.nextDouble() <= attacker.getAttribute(StrifeAttribute.DOGE) / 100) {
            MessageUtils.sendMessage(event.getEntity(), DOGE_MEMES[random.nextInt(DOGE_MEMES.length)]);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void strifeDamageHandler(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (!(event.getEntity() instanceof LivingEntity) || event.getEntity() instanceof ArmorStand) {
            return;
        }
        if (event.getDamager() instanceof EvokerFangs) {
            event.setDamage(((LivingEntity) event.getEntity()).getAttribute(
                Attribute.GENERIC_MAX_HEALTH).getBaseValue() * 0.15);
            return;
        }

        LivingEntity defendEntity = (LivingEntity) event.getEntity();
        LivingEntity attackEntity;

        Projectile projectile = null;
        if (event.getDamager() instanceof Projectile) {
            projectile = (Projectile) event.getDamager();
            ProjectileSource shooter = projectile.getShooter();
            if (defendEntity.hasMetadata("NPC")) {
                projectile.remove();
                return;
            }
            if (shooter instanceof LivingEntity) {
                attackEntity = (LivingEntity) shooter;
            } else {
                return;
            }
        } else {
            attackEntity = (LivingEntity) event.getDamager();
        }

        hashUpdates(attackEntity);
        hashUpdates(defendEntity);

        double attackMultiplier = 1D;
        double healMultiplier = 1D;
        double explosionMult = 1D;

        DamageType damageType = DamageType.MELEE;
        if (event.getCause() == DamageCause.ENTITY_EXPLOSION) {
            damageType = DamageType.EXPLOSION;
            double distance = event.getDamager().getLocation().distance(event.getEntity().getLocation());
            explosionMult = Math.max(0.3, 4 / (distance + 3));
            healMultiplier = 0.3D;
        } else if (event.getDamager() instanceof ShulkerBullet || event.getDamager() instanceof SmallFireball ||
            event.getDamager() instanceof WitherSkull) {
            damageType = DamageType.MAGIC;
        } else if (event.getDamager() instanceof Projectile) {
            damageType = DamageType.RANGED;
        }

        AttributedEntity attacker = plugin.getEntityStatCache().getAttributedEntity(attackEntity);
        AttributedEntity defender = plugin.getEntityStatCache().getAttributedEntity(defendEntity);

        if (damageType == DamageType.MELEE) {
            attackMultiplier = plugin.getAttackSpeedTask().getAttackMultiplier(attacker);
        } else if (projectile != null && projectile.hasMetadata("AS_MULT")) {
            attackMultiplier = projectile.getMetadata("AS_MULT").get(0).asDouble();
        }

        if (attackMultiplier <= 0.1) {
            event.setDamage(0);
            event.setCancelled(true);
            return;
        }

        boolean blocked = false;
        for (EntityDamageEvent.DamageModifier modifier : EntityDamageEvent.DamageModifier.values()) {
            if (event.isApplicable(modifier)) {
                if (modifier == EntityDamageEvent.DamageModifier.BLOCKING) {
                    if (event.getDamage(EntityDamageEvent.DamageModifier.BLOCKING) != 0) {
                        blocked = true;
                    }
                }
                event.setDamage(modifier, 0D);
            }
        }

        double evasionMultiplier = StatUtil.getEvasion(attacker, defender);
        evasionMultiplier = evasionMultiplier + (rollDouble() * (1 - evasionMultiplier));
        if (evasionMultiplier <= 0.5) {
          doEvasion(attackEntity, defendEntity);
          event.setCancelled(true);
          return;
        }

        double pvpMult = 1D;
        if (attackEntity instanceof Player && defendEntity instanceof Player) {
            pvpMult = plugin.getSettings().getDouble("config.pvp-multiplier", 0.5);
        }

        double blockAmount = 0D;
        if (blocked && defendEntity instanceof Player) {
            blockAmount = getBlockAmount(defender);
        }

        double potionMult = getPotionMult(attackEntity, defendEntity);

        double physicalBaseDamage = 0;
        double magicBaseDamage = 0;

        switch (damageType) {
            case MELEE:
                physicalBaseDamage = StatUtil.getBaseMeleeDamage(attacker, defender);
                break;
            case RANGED:
                physicalBaseDamage = StatUtil.getBaseRangedDamage(attacker, defender);
                break;
            case MAGIC:
                magicBaseDamage = StatUtil.getBaseMagicDamage(attacker, defender);
                break;
            case EXPLOSION:
                physicalBaseDamage = StatUtil.getBaseExplosionDamage(attacker, defender);
        }
        double fireBaseDamage = StatUtil.getBaseFireDamage(attacker, defender);
        double iceBaseDamage = StatUtil.getBaseIceDamage(attacker, defender);
        double lightningBaseDamage = StatUtil.getBaseLightningDamage(attacker, defender);
        double shadowBaseDamage = StatUtil.getBaseShadowDamage(attacker, defender);

        double bonusFireDamage = attemptIgnite(fireBaseDamage, attacker, defendEntity);
        double bonusIceDamage = attemptFreeze(iceBaseDamage, attacker, defendEntity);
        double bonusLightningDamage = attemptShock(lightningBaseDamage, attacker, defendEntity);
        boolean corruptEffect = attemptCorrupt(shadowBaseDamage, attacker, defendEntity);

        fireBaseDamage += bonusFireDamage;
        iceBaseDamage += bonusIceDamage;
        lightningBaseDamage += bonusLightningDamage;
        shadowBaseDamage += shadowBaseDamage * (DarknessManager.getCorruptionStacks(defendEntity) * 0.02);

        double bonusCriticalMultiplier = 0;
        double bonusOverchargeMultiplier = 0;

        if (doCriticalHit(attacker, defender)) {
            bonusCriticalMultiplier = attacker.getAttribute(CRITICAL_DAMAGE) / 100;
        }
        if (doOvercharge(attackMultiplier)) {
            bonusOverchargeMultiplier = attacker.getAttribute(OVERCHARGE) / 100;
        }

        double standardDamage = physicalBaseDamage + magicBaseDamage;
        double elementalDamage = fireBaseDamage + iceBaseDamage + lightningBaseDamage + shadowBaseDamage;

        standardDamage += standardDamage * bonusCriticalMultiplier + standardDamage * bonusOverchargeMultiplier;
        standardDamage *= evasionMultiplier;
        standardDamage *= attackMultiplier;
        standardDamage *= explosionMult;
        standardDamage *= potionMult;
        standardDamage *= StatUtil.getDamageMult(attacker);
        standardDamage -= blockAmount;

        // Block is removed from standard damage first.
        // The remainder is still needed for elemental.
        if (standardDamage < 0) {
            blockAmount = Math.abs(standardDamage);
            standardDamage = 0;
        } else {
            blockAmount = 0;
        }

        standardDamage *= pvpMult;

        applyLifeSteal(attacker, standardDamage, healMultiplier);
        applyHealthOnHit(attacker, attackMultiplier * explosionMult, healMultiplier);

        elementalDamage *= evasionMultiplier;
        elementalDamage *= attackMultiplier;
        elementalDamage *= potionMult;
        elementalDamage *= explosionMult;
        elementalDamage *= StatUtil.getDamageMult(attacker);
        elementalDamage -= blockAmount;
        elementalDamage *= pvpMult;

        double damageReduction = defender.getAttribute(StrifeAttribute.DAMAGE_REDUCTION) * pvpMult;
        double rawDamage = Math.max(0D, standardDamage + elementalDamage - damageReduction);

        double finalDamage = plugin.getBarrierManager().damageBarrier(defender, rawDamage);
        plugin.getBarrierManager().updateShieldDisplay(defender);

        double bleedAmount = 0;
        if (physicalBaseDamage > 0 && attacker.getAttribute(BLEED_CHANCE) / 100 >= rollDouble()) {
            bleedAmount = physicalBaseDamage * 0.5D * attackMultiplier * pvpMult * (1 + attacker.getAttribute(BLEED_DAMAGE));
            bleedAmount += bleedAmount * bonusCriticalMultiplier;
            if (!plugin.getBarrierManager().hasBarrierUp(defender)) {
                plugin.getBleedManager().applyBleed(defendEntity, bleedAmount, 30);
                defendEntity.getWorld().playSound(defendEntity.getEyeLocation(), Sound.ENTITY_SHEEP_SHEAR, 1f, 1f);
            }
        }

        if (defender.getAttribute(DAMAGE_REFLECT) > 0.1) {
            double reflectDamage = defender.getAttribute(DAMAGE_REFLECT);
            reflectDamage = damageType == DamageType.MELEE ? reflectDamage : reflectDamage * 0.6D;
            if (attackEntity.getHealth() > reflectDamage) {
                attackEntity.setHealth(attackEntity.getHealth() - reflectDamage);
            } else {
                attackEntity.damage(reflectDamage);
            }
        }

        sendActionbarDamage(attackEntity, rawDamage, bonusOverchargeMultiplier, bonusCriticalMultiplier,
            bonusFireDamage, bonusIceDamage, bonusLightningDamage, corruptEffect, bleedAmount);

        event.setDamage(EntityDamageEvent.DamageModifier.BASE, finalDamage);
    }

    private void sendActionbarDamage(LivingEntity entity, double damage, double overBonus, double critBonus, double fireBonus,
        double iceBonus, double lightningBonus, boolean corrupt, double bleedBonus) {
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
        if (corrupt) {
            damageString.append("&8❂");
        }
        if (bleedBonus > 0) {
            damageString.append("&4♦");
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

    private boolean doCriticalHit(AttributedEntity attacker, AttributedEntity defender) {
        if (attacker.getAttribute(StrifeAttribute.CRITICAL_RATE) / 100 >= rollDouble(hasLuck(attacker.getEntity()))) {
            callCritEvent(attacker.getEntity(), attacker.getEntity());
            defender.getEntity().getWorld().playSound(
                defender.getEntity().getEyeLocation(),
                Sound.ENTITY_GENERIC_BIG_FALL,
                2f,
                0.8f
            );
            return true;
        }
        return false;
    }

    private boolean doOvercharge(double attackSpeedMult) {
        return attackSpeedMult >= 0.99;
    }

    private double attemptIgnite(double damage, AttributedEntity attacker, LivingEntity defender) {
        if (damage == 0 || rollDouble() >= attacker.getAttribute(StrifeAttribute.IGNITE_CHANCE) / 100) {
            return 0D;
        }
        double bonusDamage = defender.getFireTicks() > 0 ? damage : 1D;
        defender.setFireTicks(Math.max(60 + (int) damage, defender.getFireTicks()));
        defender.getWorld().playSound(defender.getEyeLocation(),Sound.ITEM_FLINTANDSTEEL_USE, 1f, 1f);
        defender.getWorld().spawnParticle(Particle.FLAME, defender.getEyeLocation(), 6 + (int) damage / 2,
            0.3, 0.3, 0.3, 0.03);
        return bonusDamage;
    }

    private double attemptShock(double damage, AttributedEntity attacker, LivingEntity defender) {
        if (damage == 0 || rollDouble() >= attacker.getAttribute(StrifeAttribute.SHOCK_CHANCE) / 100) {
            return 0D;
        }
        double multiplier = 0.5;
        double percentHealth = defender.getHealth() / defender.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        if (percentHealth < 0.5) {
            multiplier = 1 / Math.max(0.16, percentHealth * 2);
        }
        double particles = damage * multiplier * 0.5;
        double particleRange = 0.8 + multiplier * 0.2;
        defender.getWorld().playSound(defender.getEyeLocation(), Sound.ENTITY_LIGHTNING_THUNDER, 0.7f, 2f);
        defender.getWorld().spawnParticle(Particle.CRIT_MAGIC, defender.getEyeLocation(), 10 + (int) particles,
                particleRange, particleRange, particleRange, 0.12);
        if (defender instanceof Creeper) {
            ((Creeper) defender).setPowered(true);
        }
        return damage * multiplier;
    }

    private double attemptFreeze(double damage, AttributedEntity attacker, LivingEntity defender) {
        if (damage == 0 || rollDouble() >= attacker.getAttribute(StrifeAttribute.FREEZE_CHANCE) / 100) {
            return 0D;
        }
        double multiplier = 0.25 + 0.25 * (StatUtil.getHealth(attacker) / 100);
        if (!defender.hasPotionEffect(PotionEffectType.SLOW)) {
            defender.getActivePotionEffects().add(new PotionEffect(PotionEffectType.SLOW, 30, 1));
        }
        defender.getWorld().playSound(defender.getEyeLocation(), Sound.BLOCK_GLASS_BREAK, 1f, 1.0f);
        defender.getWorld().spawnParticle(Particle.SNOWBALL, defender.getEyeLocation(), 4 + (int) damage / 2,
                0.3, 0.3, 0.2, 0.0);
        return damage * multiplier;
    }

    private boolean attemptCorrupt(double damage, AttributedEntity attacker, LivingEntity defender) {
        if (damage == 0 || rollDouble() >= attacker.getAttribute(StrifeAttribute.CORRUPT_CHANCE) / 100) {
            return false;
        }
        defender.getWorld().playSound(defender.getEyeLocation(), Sound.ENTITY_WITHER_SHOOT, 0.7f, 2f);
        defender.getWorld().spawnParticle(Particle.SMOKE_NORMAL, defender.getEyeLocation(), 10,0.4, 0.4, 0.5, 0.1);
        DarknessManager.applyCorruptionStacks(defender, damage);
        return true;
    }

    private void doEvasion(LivingEntity attacker, LivingEntity defender) {
        callEvadeEvent(defender, attacker);
        defender.getWorld().playSound(defender.getEyeLocation(), Sound.ENTITY_GHAST_SHOOT, 0.5f, 2f);
        if (defender instanceof Player) {
            ChatAPI.sendJsonMsg(ChatAPI.ChatMessageType.ACTION_BAR, ATTACK_DODGED, (Player) defender);
        }
        if (attacker instanceof Player) {
            ChatAPI.sendJsonMsg(ChatAPI.ChatMessageType.ACTION_BAR, ATTACK_MISSED, (Player) attacker);
        }
    }

    private double getBlockAmount(AttributedEntity defender) {
        double blockedAmount;
        double maxBlockAmount = defender.getAttribute(StrifeAttribute.BLOCK);
        double blockTimeLeft = plugin.getBlockTask().getTimeLeft(defender.getEntity().getUniqueId());
        if (blockTimeLeft > 0) {
            blockedAmount = maxBlockAmount * Math.max(1 - (blockTimeLeft / 6), 0.1);
        } else {
            blockedAmount = maxBlockAmount;
            plugin.getAttackSpeedTask().setTicksLeft(defender.getEntity().getUniqueId(), 0L);
            plugin.getAttackSpeedTask().endTask(defender.getEntity().getUniqueId());
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

    private void applyLifeSteal(AttributedEntity attacker, double damage, double healMultiplier) {
        double lifeSteal = StatUtil.getLifestealPercentage(attacker);
        if (lifeSteal <= 0 || attacker.getEntity().getHealth() <= 0 || attacker.getEntity().isDead()) {
            return;
        }
        double lifeStolen = damage * lifeSteal;
        if (attacker instanceof Player) {
            lifeStolen *= Math.min(((Player)attacker).getFoodLevel() / 7.0D, 1.0D);
        }
        if (attacker.getEntity().hasPotionEffect(PotionEffectType.POISON)) {
            lifeStolen *= 0.3;
        }
        restoreHealth(attacker.getEntity(), lifeStolen * healMultiplier);
    }

    private void applyHealthOnHit(AttributedEntity attacker, double attackMultiplier, double healMultiplier) {
        double health = attacker.getAttribute(HP_ON_HIT) * attackMultiplier;
        if (health <= 0 || attacker.getEntity().getHealth() <= 0 || attacker.getEntity().isDead()) {
            return;
        }
        if (attacker instanceof Player) {
            health *= Math.min(((Player)attacker).getFoodLevel() / 7.0D, 1.0D);
        }
        if (attacker.getEntity().hasPotionEffect(PotionEffectType.POISON)) {
            health *= 0.3;
        }
        restoreHealth(attacker.getEntity(), health * healMultiplier);
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

    private void hashUpdates(LivingEntity entity) {
        if (!(entity instanceof Player)) {
            return;
        }
        Champion champion = plugin.getChampionManager().getChampion(entity.getUniqueId());
        if (champion.isEquipmentHashMatching()) {
            return;
        }
        champion.updateHashedEquipment();
        plugin.getChampionManager().updateAll(champion);
    }

    private enum DamageType {
        MELEE, RANGED, MAGIC, EXPLOSION, OTHER
    }
}
