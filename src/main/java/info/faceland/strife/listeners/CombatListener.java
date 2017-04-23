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
import info.faceland.strife.data.Champion;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.SkullType;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Random;

public class CombatListener implements Listener {

    private final StrifePlugin plugin;
    private final Random random;

    private static final String ATTACK_MISSED = TextUtils.color("&f&lMiss!");
    private static final String ATTACK_DODGED = TextUtils.color("&f&lDodge!");
    private static final String ATTACK_PARRIED = TextUtils.color("&f&lParry!");
    private static final String ATTACK_BLOCKED = TextUtils.color("&e&lBlocked!");
    private static final String ATTACK_NO_DAMAGE = TextUtils.color("&b&lInvulnerable!");
    private static final String PERFECT_GUARD = TextUtils.color("&b&lPerfect Block! &e&lAttack Recharged!");

    private static final DecimalFormat INT = new DecimalFormat("#");
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
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity() == null || event.getEntity().getKiller() == null) {
            return;
        }
        double chance = plugin.getChampionManager().getChampion(event.getEntity().getKiller().getUniqueId())
                .getCache().getAttribute(StrifeAttribute.HEAD_DROP);
        if (chance == 0) {
            return;
        }
        if (random.nextDouble() < chance) {
            LivingEntity e = event.getEntity();
            if (e.getType() == EntityType.SKELETON) {
                if (((Skeleton) e).getSkeletonType() == Skeleton.SkeletonType.NORMAL) {
                    ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 0);
                    e.getWorld().dropItemNaturally(e.getLocation(), skull);
                }
            } else if ((e.getType() == EntityType.ZOMBIE)) {
                ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 2);
                e.getWorld().dropItemNaturally(e.getLocation(), skull);
            } else if ((e.getType() == EntityType.CREEPER)) {
                ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 4);
                e.getWorld().dropItemNaturally(e.getLocation(), skull);
            } else if ((e.getType() == EntityType.PLAYER)) {
                ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());
                SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
                skullMeta.setOwner(event.getEntity().getName());
                skull.setItemMeta(skullMeta);
                e.getWorld().dropItemNaturally(e.getLocation(), skull);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        // check if the event is cancelled
        // if it is, we do nothing
        if (event.isCancelled()) {
            return;
        }

        Entity damagingEntity = event.getDamager();
        Entity damagedEntity = event.getEntity();
        // check if the damaged entity is a living entity
        // if it isn't, we do nothing
        if (!(damagedEntity instanceof LivingEntity)) {
            return;
        }

        if (damagedEntity instanceof ArmorStand) {
            return;
        }
        // check if the entity is an NPC
        // if it is, we do nothing
        if (damagedEntity.hasMetadata("NPC")) {
            if (damagingEntity instanceof Projectile) {
                damagingEntity.remove();
            }
            return;
        }

        // find the living entities and if this is melee
        LivingEntity damagedLivingEntity = (LivingEntity) damagedEntity;
        LivingEntity damagingLivingEntity;
        Projectile damagingProjectile = null;
        if (damagingEntity instanceof LivingEntity) {
            damagingLivingEntity = (LivingEntity) damagingEntity;
        } else if (damagingEntity instanceof Projectile && ((Projectile) damagingEntity).getShooter() instanceof
                LivingEntity) {
            damagingLivingEntity = (LivingEntity) ((Projectile) damagingEntity).getShooter();
            if (damagingEntity.hasMetadata("handled")) {
                damagingProjectile = (Projectile) damagingEntity;
            }
        } else {
            // there are no living entities, back out of this shit
            // we ain't doin' nothin'
            return;
        }

        // save the old base damage in case we need it
        double oldBaseDamage = event.getDamage(EntityDamageEvent.DamageModifier.BASE);
        boolean isBlocked = false;

        if (event.isApplicable(EntityDamageEvent.DamageModifier.BLOCKING)) {
            if (event.getDamage(EntityDamageEvent.DamageModifier.BLOCKING) != 0) {
                isBlocked = true;
            }
        }

        // cancel out all damage from the old event
        for (EntityDamageEvent.DamageModifier modifier : EntityDamageEvent.DamageModifier.values()) {
            if (event.isApplicable(modifier)) {
                if (modifier == EntityDamageEvent.DamageModifier.ABSORPTION) {
                    continue;
                }
                event.setDamage(modifier, 0D);
            }
        }

        // pass information to a new calculator
        double newBaseDamage = handleDamageCalculations(damagedLivingEntity, damagingLivingEntity, damagingEntity,
                oldBaseDamage, damagingProjectile, isBlocked, event);

        // set the base damage of the event
        if (event.getDamage(EntityDamageEvent.DamageModifier.ABSORPTION) != 0) {
            event.setDamage(EntityDamageEvent.DamageModifier.ABSORPTION, -newBaseDamage);
        }
        event.setDamage(EntityDamageEvent.DamageModifier.BASE, newBaseDamage);
    }

    private double handleDamageCalculations(LivingEntity damagedLivingEntity,
                                            LivingEntity damagingLivingEntity,
                                            Entity damagingEntity,
                                            double oldBaseDamage,
                                            Projectile damagingProjectile,
                                            boolean isBlocked,
                                            EntityDamageEvent event) {
        double retDamage;
        // Five branches: PvP, PvE, EvP, EvE, Projectile
        if (damagingProjectile != null) {
            // Projectile branch
            retDamage = handleProjectileCalculation(damagedLivingEntity, damagingProjectile, isBlocked, event);
        } else if (damagedLivingEntity instanceof Player && damagingLivingEntity instanceof Player) {
            // PvP branch
            retDamage = handlePlayerVersusPlayerCalculation((Player) damagedLivingEntity,
                    (Player) damagingLivingEntity, damagingEntity, isBlocked, event);
        } else if (!(damagedLivingEntity instanceof Player) && damagingLivingEntity instanceof Player) {
            // PvE branch
            retDamage = handlePlayerVersusEnvironmentCalculation(event, damagedLivingEntity, (Player)
                    damagingLivingEntity);
        } else if (damagedLivingEntity instanceof Player) {
            // EvP branch
            retDamage = handleEnvironmentVersusPlayerCalculation((Player) damagedLivingEntity, damagingLivingEntity,
                    damagingEntity, oldBaseDamage, isBlocked, event);
        } else {
            // EvE branch
            retDamage = handleEnvironmentVersusEnvironmentCalculation(damagedLivingEntity, damagingLivingEntity,
                    oldBaseDamage);
        }
        return retDamage;
    }

    private double handleProjectileCalculation(LivingEntity damagedEntity, Projectile damagingProjectile, boolean
            isBlocked, EntityDamageEvent event) {
        double retDamage = damagingProjectile.getMetadata("damage").get(0).asDouble();

        LivingEntity damagingEntity = (LivingEntity) damagingProjectile.getShooter();
        double armorMult = 1.0;
        double pvpMult = 1.0;
        boolean overcharge = true;
        boolean magic = false;
        if (damagingEntity instanceof Player) {
            if (damagedEntity instanceof Player) {
                pvpMult = plugin.getSettings().getDouble("config.pvp-multiplier", 0.5);
                double attackerLevelAdv = ((Player) damagingEntity).getLevel() - ((Player) damagedEntity).getLevel();
                attackerLevelAdv -= 5;
                if (attackerLevelAdv > 0) {
                    pvpMult -= (attackerLevelAdv / 100);
                    pvpMult = Math.max(pvpMult, 0.2);
                }
            }
        }
        if (damagingProjectile instanceof ShulkerBullet) {
            magic = true;
            if (!(damagingProjectile.getShooter() instanceof Shulker)) {
                damagedEntity.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 1, 2, true, false));
            }
        }

        double evasion = 0;
        double armor = 0;
        double block = 0;
        double resist = 0;
        double parry = 0;
        double absorb = 0;
        if (damagedEntity instanceof Player) {
            Champion defendingChampion = plugin.getChampionManager().getChampion((damagedEntity).getUniqueId());
            evasion = defendingChampion.getCache().getAttribute(StrifeAttribute.EVASION);
            armor = defendingChampion.getCache().getAttribute(StrifeAttribute.ARMOR);
            block = defendingChampion.getCache().getAttribute(StrifeAttribute.BLOCK);
            resist = defendingChampion.getCache().getAttribute(StrifeAttribute.RESISTANCE);
            parry = defendingChampion.getCache().getAttribute(StrifeAttribute.PARRY);
            absorb = defendingChampion.getCache().getAttribute(StrifeAttribute.ABSORB_CHANCE);
        }

        if (evasion > 0) {
            double accuracy = 1.0;
            accuracy = damagingProjectile.getMetadata("accuracy").get(0).asDouble();
            if (getEvadeChance(evasion, accuracy)) {
                damagingProjectile.remove();
                damagedEntity.getWorld().playSound(damagedEntity.getEyeLocation(), Sound.ENTITY_GHAST_SHOOT, 0.5f, 2f);
                ChatAPI.sendJsonMsg(ChatAPI.ChatMessageType.ACTION_BAR, ATTACK_DODGED, (Player) damagedEntity);
                if (damagingEntity instanceof Player) {
                    ChatAPI.sendJsonMsg(ChatAPI.ChatMessageType.ACTION_BAR, ATTACK_MISSED, (Player) damagingEntity);
                }
                event.setCancelled(true);
                return 0D;
            }
        }
        double finalBlockMult = 1.0;
        if (isBlocked) {
            finalBlockMult = blockCalculations((Player)damagedEntity, damagingProjectile, retDamage, event);
        }

        double armorPen = 0;
        if (damagingProjectile.hasMetadata("armorPen")) {
            armorPen = damagingProjectile.getMetadata("armorPen").get(0).asDouble();
        }
        armorMult = getArmorMult(armor, armorPen);

        StringBuilder damageStats = new StringBuilder();
        damageStats.append(ChatColor.RESET + "(" + ONE_DECIMAL.format(retDamage * armorMult * pvpMult));
        boolean damageDetails = false;

        double critBonus = 0;
        if (damagingProjectile.hasMetadata("critical")) {
            critBonus = damagingProjectile.getMetadata("critical").get(0).asDouble();
            if (critBonus > 0) {
                critBonus = retDamage * critBonus;
                damageStats.append(ChatColor.RED + " +" + ONE_DECIMAL.format(critBonus * armorMult * pvpMult) + "✶");
                damageDetails = true;
                damagedEntity.getWorld().playSound(damagedEntity.getEyeLocation(), Sound.ENTITY_GENERIC_BIG_FALL, 2f, 0.8f);
            }
        }

        double overBonus = 0;
        if (damagingProjectile.hasMetadata("overcharge")) {
            overBonus = damagingProjectile.getMetadata("overcharge").get(0).asDouble();
            if (overBonus > 0 && overcharge) {
                overBonus = retDamage * overBonus;
                damageStats.append(ChatColor.YELLOW + " +" + ONE_DECIMAL.format(overBonus * armorMult * pvpMult) + "✦");
                damageDetails = true;
            }
        }

        retDamage += critBonus;
        retDamage += overBonus;

        double trueDamage = 0D;
        if (damagingProjectile.hasMetadata("fireDamage")) {
            double fireDamage = damagingProjectile.getMetadata("fireDamage").get(0).asDouble();
            fireDamage = getFireDamage(fireDamage, damagedEntity, pvpMult, resist);
            damageStats.append(ChatColor.GOLD + " +" + ONE_DECIMAL.format(fireDamage) + "☀");
            damageDetails = true;
            trueDamage += fireDamage;
        }
        if (damagingProjectile.hasMetadata("lightningDamage")) {
            double lightningDamage = damagingProjectile.getMetadata("lightningDamage").get(0).asDouble();
            lightningDamage = getLightningDamage(lightningDamage, damagedEntity, pvpMult, resist);
            damageStats.append(ChatColor.GRAY + " +" + ONE_DECIMAL.format(lightningDamage) + "⚡");
            damageDetails = true;
            trueDamage += lightningDamage;
        }
        if (damagingProjectile.hasMetadata("iceDamage")) {
            double iceDamage = damagingProjectile.getMetadata("iceDamage").get(0).asDouble();
            iceDamage = getIceDamage(iceDamage, damagingEntity, damagedEntity, pvpMult, resist);
            damageStats.append(ChatColor.AQUA + " +" + ONE_DECIMAL.format(iceDamage) + "❊");
            damageDetails = true;
            trueDamage += iceDamage;
        }

        double potionMult = getPotionMult(damagingEntity, damagedEntity);
        String multiplierString = "";

        if (potionMult != 1.0) {
            multiplierString = " x" + ONE_DECIMAL.format(potionMult);
            if (potionMult <= 0) {
                if (damagingEntity instanceof Player) {
                    ChatAPI.sendJsonMsg(ChatAPI.ChatMessageType.ACTION_BAR, ATTACK_NO_DAMAGE, (Player) damagingEntity);
                }
                event.setCancelled(true);
                return 0D;
            }
        }

        retDamage *= potionMult;
        retDamage *= armorMult;
        if (magic) {
            retDamage *= 1 - (resist / 2);
        }
        retDamage += trueDamage;
        retDamage *= finalBlockMult;

        retDamage *= pvpMult;

        if (damagingProjectile.hasMetadata("lifeSteal")) {
            double lifeSteal = damagingProjectile.getMetadata("lifeSteal").get(0).asDouble();
            double lifeStolen = retDamage * lifeSteal;
            if (damagingEntity instanceof Player) {
                lifeStolen *= Math.min(((Player) damagingEntity).getFoodLevel() / 7.0D, 1.0D);
            }
            if (damagingEntity.hasPotionEffect(PotionEffectType.POISON)) {
                lifeStolen *= 0.34;
            }
            if (damagingEntity.getHealth() > 0 && !damagingEntity.isDead()) {
                damagingEntity.setHealth(Math.min(damagingEntity.getHealth() + lifeStolen, damagingEntity.getMaxHealth()));
            }
        }
        if (damagingEntity instanceof Player) {
            if (damageDetails) {
                damageStats.append(ChatColor.RESET + ")");
                damageStats.append(multiplierString);
                String combatString = TextUtils.color("&f&l" + INT.format(retDamage) + " Damage! " + damageStats);
                ChatAPI.sendJsonMsg(ChatAPI.ChatMessageType.ACTION_BAR, combatString, (Player) damagingEntity);
            } else {
                String combatString = TextUtils.color("&f&l" + INT.format(retDamage) + " Damage!" + multiplierString);
                ChatAPI.sendJsonMsg(ChatAPI.ChatMessageType.ACTION_BAR, combatString, (Player) damagingEntity);
            }
        }
        return retDamage;
    }

    private double handleEnvironmentVersusEnvironmentCalculation(LivingEntity damagedLivingEntity,
                                                                 LivingEntity damagingLivingEntity,
                                                                 double oldBaseDamage) {
        return oldBaseDamage;
    }

    private double handleEnvironmentVersusPlayerCalculation(Player damagedPlayer,
                                                            LivingEntity damagingLivingEntity,
                                                            Entity damagingEntity,
                                                            double oldBaseDamage,
                                                            boolean isBlocked,
                                                            EntityDamageEvent event) {
        double damage = oldBaseDamage;

        Champion damagedChampion = plugin.getChampionManager().getChampion(damagedPlayer.getUniqueId());

        double evasion = damagedChampion.getCache().getAttribute(StrifeAttribute.EVASION);
        if (evasion > 0) {
            if (getEvadeChance(evasion, 0)) {
                if (damagingEntity instanceof Projectile) {
                    damagingEntity.remove();
                }
                damagedPlayer.getWorld().playSound(damagedPlayer.getEyeLocation(), Sound.ENTITY_GHAST_SHOOT,
                        0.5f, 2f);
                ChatAPI.sendJsonMsg(ChatAPI.ChatMessageType.ACTION_BAR, ATTACK_DODGED, damagedPlayer);
                event.setCancelled(true);
                return 0D;
            }
        }

        if (isBlocked) {
            damage *= blockCalculations(damagedPlayer, damagingLivingEntity, damage, event);
        }
        damage *= getPotionMult(damagingLivingEntity, damagedPlayer);
        damage *= getArmorMult(damagedChampion.getCache().getAttribute(StrifeAttribute.ARMOR), 0);
        return damage;
    }

    private double handlePlayerVersusEnvironmentCalculation(EntityDamageEvent event,
                                                            LivingEntity damagedEntity,
                                                            Player damagingPlayer) {
        double retDamage;

        // get the champions
        Champion damagingChampion = plugin.getChampionManager().getChampion(damagingPlayer.getUniqueId());

        // ensure that they have the correct caches
        damagingChampion.getWeaponAttributeValues();
        damagingChampion.getCache().recombine();

        // calculating attack speed and velocity
        double attackSpeedMult = 1D;
        double attackSpeed = StrifeAttribute.ATTACK_SPEED.getBaseValue() * (1 / (1 + damagingChampion
                .getCache().getAttribute(StrifeAttribute.ATTACK_SPEED)));
        long timeLeft = plugin.getAttackSpeedTask().getTimeLeft(damagingPlayer.getUniqueId());
        long timeToSet = Math.round(Math.max(4.0 * attackSpeed, 0D));
        if (timeLeft == timeToSet) {
            return 0;
        }
        if (timeLeft > 0) {
            attackSpeedMult = Math.max(1.0 - 1.0 * ((timeLeft * 1D) / timeToSet), 0.1);
        }
        plugin.getAttackSpeedTask().setTimeLeft(damagingPlayer.getUniqueId(), timeToSet);

        retDamage = damagingChampion.getCache().getAttribute(StrifeAttribute.MELEE_DAMAGE) * attackSpeedMult;

        StringBuilder damageStats = new StringBuilder();
        damageStats.append(ChatColor.RESET + "(" + ONE_DECIMAL.format(retDamage));
        boolean damageDetails = false;

        // critical damage time!
        double critBonus = 0D;
        if (random.nextDouble() <= damagingChampion.getCache().getAttribute(StrifeAttribute.CRITICAL_RATE)) {
            critBonus = (damagingChampion.getCache().getAttribute(StrifeAttribute.CRITICAL_DAMAGE) - 1) * retDamage;
            damageStats.append(ChatColor.RED + " +" + ONE_DECIMAL.format(critBonus) + "✶");
            damageDetails = true;
            damagedEntity.getWorld().playSound(damagedEntity.getEyeLocation(), Sound.ENTITY_GENERIC_BIG_FALL, 2f, 0.8f);
        }
        // overbonus time!
        double overBonus = 0D;
        if (attackSpeedMult > 0.94) {
            overBonus = damagingChampion.getCache().getAttribute(StrifeAttribute.OVERCHARGE) * retDamage;
            damageStats.append(ChatColor.YELLOW + " +" + ONE_DECIMAL.format(overBonus) + "✦");
            damageDetails = true;
        }

        // adding critBonus and overBonus to damage
        retDamage += critBonus;
        retDamage += overBonus;

        // elements calculations
        double trueDamage = 0D;
        double fireDamage = damagingChampion.getCache().getAttribute(StrifeAttribute.FIRE_DAMAGE);
        double lightningDamage = damagingChampion.getCache().getAttribute(StrifeAttribute.LIGHTNING_DAMAGE);
        double iceDamage = damagingChampion.getCache().getAttribute(StrifeAttribute.ICE_DAMAGE);

        if (fireDamage * attackSpeedMult > 0D) {
            double igniteCalc = damagingChampion.getCache().getAttribute(StrifeAttribute.IGNITE_CHANCE);
            if (random.nextDouble() < igniteCalc) {
                fireDamage = getFireDamage(fireDamage * attackSpeedMult, damagedEntity, 1.0D, 0D);
                damageStats.append(ChatColor.GOLD + " +" + ONE_DECIMAL.format(fireDamage) + "☀");
                damageDetails = true;
                trueDamage += fireDamage;
            }
        }
        if (lightningDamage * attackSpeedMult > 0D) {
            double shockCalc = damagingChampion.getCache().getAttribute(StrifeAttribute.SHOCK_CHANCE);
            if (random.nextDouble() < shockCalc) {
                lightningDamage = getLightningDamage(lightningDamage * attackSpeedMult, damagedEntity, 1.0D, 0D);
                damageStats.append(ChatColor.GRAY + " +" + ONE_DECIMAL.format(lightningDamage) + "⚡");
                damageDetails = true;
                trueDamage += lightningDamage;
            }
        }
        if (iceDamage * attackSpeedMult > 0D) {
            double freezeCalc = damagingChampion.getCache().getAttribute(StrifeAttribute.FREEZE_CHANCE);
            if (random.nextDouble() < freezeCalc) {
                iceDamage = getIceDamage(iceDamage * attackSpeedMult, damagingPlayer, damagedEntity, 1.0D, 0D);
                damageStats.append(ChatColor.AQUA + " +" + ONE_DECIMAL.format(iceDamage) + "❊");
                damageDetails = true;
                trueDamage += iceDamage;
            }
        }

        // potion effects mults
        double potionMult = getPotionMult(damagingPlayer, damagedEntity);
        String multiplierString = "";

        if (potionMult != 1.0) {
            multiplierString = " x" + ONE_DECIMAL.format(potionMult);
            if (potionMult <= 0) {
                ChatAPI.sendJsonMsg(ChatAPI.ChatMessageType.ACTION_BAR, ATTACK_NO_DAMAGE, damagingPlayer);
                event.setCancelled(true);
                return 0D;
            }
        }

        // combine!
        retDamage *= potionMult;
        retDamage += trueDamage;

        // life steal
        double lifeSteal = damagingChampion.getCache().getAttribute(StrifeAttribute.LIFE_STEAL);
        if (lifeSteal > 0) {
            double lifeStolen = retDamage * lifeSteal;
            lifeStolen *= Math.min(damagingPlayer.getFoodLevel() / 7.0D, 1.0D);
            if (damagingPlayer.hasPotionEffect(PotionEffectType.POISON)) {
                lifeStolen *= 0.34;
            }
            if (damagingPlayer.getHealth() > 0 && !damagingPlayer.isDead()) {
                damagingPlayer.setHealth(Math.min(damagingPlayer.getHealth() + lifeStolen,
                        damagingPlayer.getMaxHealth()));
            }
        }
        if (damageDetails) {
            damageStats.append(ChatColor.RESET + ")");
            damageStats.append(multiplierString);
            String combatString = TextUtils.color("&f&l" + INT.format(retDamage) + " Damage! " + damageStats);
            ChatAPI.sendJsonMsg(ChatAPI.ChatMessageType.ACTION_BAR, combatString, damagingPlayer);
        } else {
            String combatString = TextUtils.color("&f&l" + INT.format(retDamage) + " Damage!" + multiplierString);
            ChatAPI.sendJsonMsg(ChatAPI.ChatMessageType.ACTION_BAR, combatString, damagingPlayer);
        }
        return retDamage;
    }

    private double handlePlayerVersusPlayerCalculation(Player damagedPlayer,
                                                       Player damagingPlayer,
                                                       Entity damagingEntity,
                                                       boolean isBlocked,
                                                       EntityDamageEvent event) {
        double retDamage;

        // get the champions
        Champion damagedChampion = plugin.getChampionManager().getChampion(damagedPlayer.getUniqueId());
        Champion damagingChampion = plugin.getChampionManager().getChampion(damagingPlayer.getUniqueId());

        // ensure that they have the correct caches
        damagingChampion.getWeaponAttributeValues();
        damagingChampion.getCache().recombine();

        // get the PvP damage multiplier
        double pvpMult = plugin.getSettings().getDouble("config.pvp-multiplier", 0.5);
        double attackerLevelAdv = damagingPlayer.getLevel() - damagedPlayer.getLevel();
        attackerLevelAdv -= 5;
        if (attackerLevelAdv > 0) {
            pvpMult -= (attackerLevelAdv / 100);
            pvpMult = Math.max(pvpMult, 0.2);
        }

        // calculating attack speed
        double attackSpeedMult = 1D;
        double attackSpeed = StrifeAttribute.ATTACK_SPEED.getBaseValue() * (1 / (1 + damagingChampion
                .getCache().getAttribute(StrifeAttribute.ATTACK_SPEED)));
        long timeLeft = plugin.getAttackSpeedTask().getTimeLeft(damagingPlayer.getUniqueId());
        long timeToSet = Math.round(Math.max(4.0 * attackSpeed, 0D));
        if (timeLeft == timeToSet) {
            return 0;
        }
        if (timeLeft > 0) {
            attackSpeedMult = Math.max(1.0 - 1.0 * ((timeLeft * 1D) / timeToSet), 0.1);
        }

        // get the evasion chance of the damaged champion and check if evaded
        double evasion = damagedChampion.getCache().getAttribute(StrifeAttribute.EVASION);
        if (evasion > 0) {
            // get the accuracy of the damaging champion and check if still hits
            double accuracy = damagingChampion.getCache().getAttribute(StrifeAttribute.ACCURACY);
            if (getEvadeChance(evasion, accuracy)) {
                if (damagingEntity instanceof Projectile) {
                    damagingEntity.remove();
                }
                damagedPlayer.getWorld().playSound(damagedPlayer.getEyeLocation(), Sound.ENTITY_GHAST_SHOOT, 0.5f, 2f);
                ChatAPI.sendJsonMsg(ChatAPI.ChatMessageType.ACTION_BAR, ATTACK_DODGED, damagedPlayer);
                ChatAPI.sendJsonMsg(ChatAPI.ChatMessageType.ACTION_BAR, ATTACK_MISSED, damagingPlayer);
                plugin.getAttackSpeedTask().setTimeLeft(damagingPlayer.getUniqueId(), Math.max(timeToSet / 2, timeLeft));
                event.setCancelled(true);
                return 0D;
            }
        }
        plugin.getAttackSpeedTask().setTimeLeft(damagingPlayer.getUniqueId(), timeToSet);

        double armorMult = getArmorMult(damagedChampion.getCache().getAttribute(StrifeAttribute.ARMOR), damagingChampion
                .getCache().getAttribute(StrifeAttribute.ARMOR_PENETRATION));

        retDamage = damagingChampion.getCache().getAttribute(StrifeAttribute.MELEE_DAMAGE) * attackSpeedMult;

        // check if damaged player is blocking
        double finalBlockMult = 1.0;
        if (isBlocked) {
            finalBlockMult = blockCalculations(damagedPlayer, damagingPlayer, retDamage, event);
        }

        StringBuilder damageStats = new StringBuilder();
        damageStats.append(ChatColor.RESET + "(" + ONE_DECIMAL.format(retDamage * armorMult));
        boolean damageDetails = false;

        // critical damage time!
        double critBonus = 0D;
        if (random.nextDouble() <= damagingChampion.getCache().getAttribute(StrifeAttribute.CRITICAL_RATE)) {
            critBonus = (damagingChampion.getCache().getAttribute(StrifeAttribute.CRITICAL_DAMAGE) - 1) * retDamage;
            damageStats.append(ChatColor.RED + " +" + ONE_DECIMAL.format(critBonus * armorMult) + "✶");
            damageDetails = true;
            damagedPlayer.getWorld().playSound(damagedPlayer.getEyeLocation(), Sound.ENTITY_GENERIC_BIG_FALL, 2f, 0.8f);
        }

        // overbonus time!
        double overBonus = 0D;
        if (attackSpeedMult > 0.94) {
            overBonus = damagingChampion.getCache().getAttribute(StrifeAttribute.OVERCHARGE) * retDamage;
            damageStats.append(ChatColor.YELLOW + " +" + ONE_DECIMAL.format(overBonus * armorMult) + "✦");
            damageDetails = true;
        }

        // adding critBonus and overBonus to damage
        retDamage += critBonus;
        retDamage += overBonus;

        // elements calculations
        double trueDamage = 0D;
        double fireDamage = damagingChampion.getCache().getAttribute(StrifeAttribute.FIRE_DAMAGE);
        double lightningDamage = damagingChampion.getCache().getAttribute(StrifeAttribute.LIGHTNING_DAMAGE);
        double iceDamage = damagingChampion.getCache().getAttribute(StrifeAttribute.ICE_DAMAGE);
        double resist = damagedChampion.getCache().getAttribute(StrifeAttribute.RESISTANCE);
        if (fireDamage * attackSpeedMult > 0D) {
            double igniteCalc = damagingChampion.getCache().getAttribute(StrifeAttribute.IGNITE_CHANCE);
            if (random.nextDouble() < igniteCalc) {
                fireDamage = getFireDamage(fireDamage * attackSpeedMult, damagedPlayer, pvpMult, resist);
                damageStats.append(ChatColor.GOLD + " +" + ONE_DECIMAL.format(fireDamage) + "☀");
                damageDetails = true;
                trueDamage += fireDamage;
            }
        }
        if (lightningDamage * attackSpeedMult > 0D) {
            double shockCalc = damagingChampion.getCache().getAttribute(StrifeAttribute.SHOCK_CHANCE);
            if (random.nextDouble() < shockCalc) {
                lightningDamage = getLightningDamage(lightningDamage * attackSpeedMult, damagedPlayer, pvpMult, resist);
                damageStats.append(ChatColor.GRAY + " +" + ONE_DECIMAL.format(lightningDamage) + "⚡");
                damageDetails = true;
                trueDamage += lightningDamage;
            }
        }
        if (iceDamage * attackSpeedMult > 0D) {
            double freezeCalc = damagingChampion.getCache().getAttribute(StrifeAttribute.FREEZE_CHANCE);
            if (random.nextDouble() < freezeCalc) {
                iceDamage = getIceDamage(iceDamage * attackSpeedMult, damagingPlayer, damagedPlayer, pvpMult, resist);
                damageStats.append(ChatColor.AQUA + " +" + ONE_DECIMAL.format(iceDamage) + "❊");
                damageDetails = true;
                trueDamage += iceDamage;
            }
        }

        // potion effects mults
        double potionMult = getPotionMult(damagingPlayer, damagedPlayer);

        String multiplierString = "";

        if (potionMult != 1.0) {
            multiplierString = " x" + ONE_DECIMAL.format(potionMult);
            if (potionMult <= 0) {
                ChatAPI.sendJsonMsg(ChatAPI.ChatMessageType.ACTION_BAR, ATTACK_NO_DAMAGE, damagingPlayer);
                event.setCancelled(true);
                return 0D;
            }
        }

        // combine!
        retDamage *= potionMult;
        retDamage *= armorMult;
        retDamage += trueDamage;
        retDamage *= finalBlockMult;
        retDamage *= pvpMult;

        // life steal
        double lifeSteal = damagingChampion.getCache().getAttribute(StrifeAttribute.LIFE_STEAL);
        if (lifeSteal > 0) {
            double lifeStolen = retDamage * lifeSteal;
            lifeStolen *= Math.min(damagingPlayer.getFoodLevel() / 7.0D, 1.0D);
            if (damagingPlayer.hasPotionEffect(PotionEffectType.POISON)) {
                lifeStolen *= 0.34;
            }
            if (damagingPlayer.getHealth() > 0 && !damagingPlayer.isDead()) {
                damagingPlayer.setHealth(Math.min(damagingPlayer.getHealth() + lifeStolen,
                        damagingPlayer.getMaxHealth()));
            }
        }

        if (damageDetails) {
            damageStats.append(ChatColor.RESET + ")");
            damageStats.append(multiplierString);
            String combatString = TextUtils.color("&f&l" + INT.format(retDamage) + " Damage! " + damageStats);
            ChatAPI.sendJsonMsg(ChatAPI.ChatMessageType.ACTION_BAR, combatString, (Player) damagingEntity);
        } else {
            String combatString = TextUtils.color("&f&l" + INT.format(retDamage) + " Damage!" + multiplierString);
            ChatAPI.sendJsonMsg(ChatAPI.ChatMessageType.ACTION_BAR, combatString, (Player) damagingEntity);
        }
        return retDamage;
    }

    private double getArmorMult(double armor, double apen) {
        if (armor > 0) {
            double adjustedArmor = armor * (1 - apen);
            if (adjustedArmor > 0) {
                return Math.pow(100 / (100 + adjustedArmor), 1.6);
            }
        }
        return 1 + (apen / 5);
    }

    private boolean getEvadeChance(double evasion, double accuacy) {
        evasion *= 1 - accuacy;
        double evadeChance = 1 - Math.pow(100 / (100 + evasion), 1.5);
        if (random.nextDouble() <= evadeChance) {
            return true;
        }
        return false;
    }

    private double getFireDamage(double fireDamage, LivingEntity target, double pvpMult, double resist) {
        if (target.getFireTicks() > 0) {
            fireDamage *= 2;
        }
        fireDamage *= 1 - resist;
        target.setFireTicks(Math.max(35, target.getFireTicks()));
        target.getWorld().playSound(target.getEyeLocation(),Sound.ITEM_FLINTANDSTEEL_USE, 1f, 1f);
        target.getWorld().spawnParticle(Particle.FLAME, target.getEyeLocation(), 6 + (int) fireDamage / 2,
                0.3, 0.3, 0.3, 0.03);
        return fireDamage * pvpMult;
    }

    private double getLightningDamage(double lightningDamage, LivingEntity target, double pvpMult, double resist) {
        double missingHpMult = -0.7 + Math.min((target.getMaxHealth() / target.getHealth()), 6.7);
        lightningDamage = Math.max(lightningDamage, lightningDamage * missingHpMult) * (1 - resist);
        target.getWorld().playSound(target.getEyeLocation(), Sound.ENTITY_LIGHTNING_THUNDER, 0.7f, 2f);
        target.getWorld().spawnParticle(Particle.CRIT_MAGIC, target.getEyeLocation(), 6 + (int) lightningDamage / 2,
                0.8,0.8,0.8, 0.1);
        if (target instanceof Creeper) {
            ((Creeper) target).setPowered(true);
        }
        return lightningDamage * pvpMult;
    }

    private double getIceDamage(double iceDamage, LivingEntity attacker, LivingEntity target, double pvpMult, double
            resist) {
        iceDamage = (iceDamage + attacker.getMaxHealth() * 0.01 * iceDamage) * (1 - resist);
        target.getActivePotionEffects().add(new PotionEffect(PotionEffectType.SLOW, 10, 2));
        target.getWorld().playSound(target.getEyeLocation(), Sound.BLOCK_GLASS_BREAK, 1f, 1f);
        target.getWorld().spawnParticle(Particle.SNOWBALL, target.getEyeLocation(), 4 + (int) iceDamage / 3,
                0.3, 0.3, 0.2, 0.0);
        return iceDamage * pvpMult;
    }

    private double blockCalculations (Player damagedPlayer, Entity damagingEntity, double damage, EntityDamageEvent event) {
        double blockMult = 1.0;
        double blockTimeLeft = plugin.getBlockTask().getTimeLeft(damagedPlayer.getUniqueId());
        Champion playerChampion = plugin.getChampionManager().getChampion(damagedPlayer.getUniqueId());
        if (blockTimeLeft > 0) {
            blockMult = Math.max(1 - (blockTimeLeft / 6), 0.25);
            ChatAPI.sendJsonMsg(ChatAPI.ChatMessageType.ACTION_BAR, ATTACK_BLOCKED, damagedPlayer);
        } else {
            plugin.getAttackSpeedTask().setTimeLeft(damagedPlayer.getUniqueId(), 0L);
            plugin.getAttackSpeedTask().endTask(damagedPlayer.getUniqueId());
            ChatAPI.sendJsonMsg(ChatAPI.ChatMessageType.ACTION_BAR, PERFECT_GUARD, damagedPlayer);
        }
        plugin.getBlockTask().setTimeLeft(damagedPlayer.getUniqueId(), 6L);
        if (random.nextDouble() < blockMult * playerChampion.getCache().getAttribute(StrifeAttribute.ABSORB_CHANCE)) {
            if (damagingEntity instanceof Projectile) {
                damagingEntity.remove();
            }
            double healAmount = damage * 0.3 + damagedPlayer.getMaxHealth() * 0.015;
            if (damagedPlayer.hasPotionEffect(PotionEffectType.POISON)) {
                healAmount *= 0.33;
            }
            damagedPlayer.setHealth(Math.min(damagedPlayer.getHealth() + healAmount, damagedPlayer.getMaxHealth()));
            damagedPlayer.getWorld().playSound(damagedPlayer.getEyeLocation(), Sound.ENTITY_BLAZE_HURT, 1f, 2f);
            return 0D;
        }
        if (random.nextDouble() < blockMult * playerChampion.getCache().getAttribute(StrifeAttribute.PARRY)) {
            if (damagingEntity instanceof Projectile) {
                damagingEntity.remove();
            } else if (damagingEntity instanceof LivingEntity){
                ((LivingEntity) damagingEntity).damage(damage * 0.2);
            }
            damagedPlayer.getWorld().playSound(damagedPlayer.getEyeLocation(), Sound.BLOCK_ANVIL_LAND, 1f, 2f);
            ChatAPI.sendJsonMsg(ChatAPI.ChatMessageType.ACTION_BAR, ATTACK_PARRIED, damagedPlayer);
            event.setCancelled(true);
            return 0D;
        }
        return 1 - (playerChampion.getCache().getAttribute(StrifeAttribute.BLOCK) * blockMult);
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
        return potionMult;
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
}
