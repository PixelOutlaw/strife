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

import com.tealcube.minecraft.bukkit.facecore.ui.ActionBarMessage;

import info.faceland.strife.StrifePlugin;
import info.faceland.strife.attributes.StrifeAttribute;
import info.faceland.strife.data.Champion;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.ShulkerBullet;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.text.DecimalFormat;
import java.util.Random;

public class CombatListener implements Listener {

    private static final String[] DOGE_MEMES =
            {"<aqua>wow", "<green>wow", "<light purple>wow", "<aqua>much pain", "<green>much pain",
                    "<light purple>much pain", "<aqua>many disrespects", "<green>many disrespects",
                    "<light purple>many disrespects", "<red>no u", "<red>2damage4me"};
    private final StrifePlugin plugin;
    private final Random random;
    private static final DecimalFormat INT = new DecimalFormat("#");
    private static final DecimalFormat ONE_DECIMAL = new DecimalFormat("#.#");

    public CombatListener(StrifePlugin plugin) {
        this.plugin = plugin;
        random = new Random(System.currentTimeMillis());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityBurnEvent(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof LivingEntity)) {
            return;
        }
        if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
            return;
        }
        if (event.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK) {
            double hpdmg = ((LivingEntity) event.getEntity()).getHealth() / 25;
            if (event.isApplicable(EntityDamageEvent.DamageModifier.ARMOR)) {
                event.setDamage(EntityDamageEvent.DamageModifier.ARMOR, 0);
            }
            event.setDamage(1 + hpdmg);
            return;
        }
        if (event.getCause() == EntityDamageEvent.DamageCause.FIRE) {
            double hpdmg = ((LivingEntity) event.getEntity()).getHealth() / 25;
            if (event.isApplicable(EntityDamageEvent.DamageModifier.ARMOR)) {
                event.setDamage(EntityDamageEvent.DamageModifier.ARMOR, 0);
            }
            event.setDamage(1 + hpdmg);
            return;
        }
        if (event.getCause() == EntityDamageEvent.DamageCause.LAVA) {
            double hpdmg = ((LivingEntity) event.getEntity()).getHealth() / 20;
            if (event.isApplicable(EntityDamageEvent.DamageModifier.ARMOR)) {
                event.setDamage(EntityDamageEvent.DamageModifier.ARMOR, 0);
            }
            event.setDamage(1 + hpdmg);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerShoot(ProjectileLaunchEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player)) {
            return;
        }
        Player p = (Player) event.getEntity().getShooter();
        Champion playerChamp = plugin.getChampionManager().getChampion(p.getUniqueId());
        playerChamp.getAttributeValues(true);
        Projectile projectile = event.getEntity();
        double damage = playerChamp.getCache().getAttribute(StrifeAttribute.RANGED_DAMAGE);
        double critMult = 0;
        double overMult = playerChamp.getCache().getAttribute(StrifeAttribute.OVERCHARGE);
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
                        .getAttribute(StrifeAttribute.FIRE_DAMAGE)));
            }
        }
        if (playerChamp.getCache().getAttribute(StrifeAttribute.ICE_DAMAGE) > 0) {
            if (random.nextDouble() < playerChamp.getCache().getAttribute(StrifeAttribute.FREEZE_CHANCE)) {
                projectile.setMetadata("iceDamage", new FixedMetadataValue(plugin, playerChamp.getCache()
                        .getAttribute(StrifeAttribute.ICE_DAMAGE)));
            }
        }
        if (playerChamp.getCache().getAttribute(StrifeAttribute.LIGHTNING_DAMAGE) > 0) {
            if (random.nextDouble() < playerChamp.getCache().getAttribute(StrifeAttribute.SHOCK_CHANCE)) {
                projectile.setMetadata("lightningDamage", new FixedMetadataValue(plugin, playerChamp.getCache()
                        .getAttribute(StrifeAttribute.LIGHTNING_DAMAGE)));
            }
        }
        if (playerChamp.getCache().getAttribute(StrifeAttribute.LIFE_STEAL) > 0) {
            projectile.setMetadata("lifeSteal", new FixedMetadataValue(plugin, playerChamp.getCache()
                    .getAttribute(StrifeAttribute.LIGHTNING_DAMAGE)));
        }

    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSwing(PlayerInteractEvent event) {
        if (event.getAction() != Action.LEFT_CLICK_AIR) {
            return;
        }
        Player p = event.getPlayer();
        Champion playerChamp = plugin.getChampionManager().getChampion(p.getUniqueId());
        double attackSpeed = StrifeAttribute.ATTACK_SPEED.getBaseValue() * (1 / (1 + playerChamp.getCache()
                .getAttribute(StrifeAttribute.ATTACK_SPEED)));
        long timeToSet = Math.round(Math.max(4.0 * attackSpeed, 0D));
        long timeLeft = plugin.getAttackSpeedTask().getTimeLeft(p.getUniqueId());
        double attackSpeedMult = 1.0D;
        if (timeLeft > 0) {
            attackSpeedMult = Math.max(1.0 - 1.0 * ((timeLeft * 1D) / timeToSet), 0.1);
        }
        plugin.getAttackSpeedTask().setTimeLeft(p.getUniqueId(), timeToSet);
        ItemStack wand = p.getEquipment().getItemInMainHand();
        if (wand.getType() != Material.WOOD_SWORD) {
            return;
        }
        if (wand.getItemMeta().getLore().size() < 2) {
            return;
        }
        if (!wand.getItemMeta().getLore().get(1).endsWith("Wand")) {
            return;
        }
        if (attackSpeedMult < 0.3) {
            ActionBarMessage.send(p, ChatColor.WHITE + "Not Charged Enough!");
            p.getWorld().playSound(p.getLocation(), Sound.ENTITY_BLAZE_AMBIENT, 0.8f, 0.8f);
            return;
        }
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_BLAZE_HURT, 0.9f, 2f);
        playerChamp.getAttributeValues(true);
        playerChamp.getWeaponAttributeValues();
        playerChamp.getCache().recombine();
        ShulkerBullet magicProj = p.getWorld().spawn(p.getEyeLocation(), ShulkerBullet.class);
        magicProj.setShooter(p);
        magicProj.setVelocity(p.getLocation().getDirection().multiply(1.7));
        double damage = playerChamp.getCache().getAttribute(StrifeAttribute.MAGIC_DAMAGE) * attackSpeedMult;
        double critMult = 0;
        double overMult = 0;
        if (random.nextDouble() <= playerChamp.getCache().getAttribute(StrifeAttribute.CRITICAL_RATE)) {
            critMult = playerChamp.getCache().getAttribute(StrifeAttribute.CRITICAL_DAMAGE) - 1;
        }
        if (attackSpeedMult == 1.0D) {
            overMult = playerChamp.getCache().getAttribute(StrifeAttribute.OVERCHARGE);
        }
        magicProj.setMetadata("handled", new FixedMetadataValue(plugin, true));
        magicProj.setMetadata("damage", new FixedMetadataValue(plugin, damage));
        magicProj.setMetadata("overcharge", new FixedMetadataValue(plugin, overMult));
        magicProj.setMetadata("critical", new FixedMetadataValue(plugin, critMult));
        magicProj.setMetadata("armorPen", new FixedMetadataValue(plugin, playerChamp.getCache()
                .getAttribute(StrifeAttribute.ARMOR_PENETRATION)));
        magicProj.setMetadata("accuracy", new FixedMetadataValue(plugin, playerChamp.getCache()
                .getAttribute(StrifeAttribute.ACCURACY)));
        if (playerChamp.getCache().getAttribute(StrifeAttribute.FIRE_DAMAGE) > 0) {
            if (random.nextDouble() < playerChamp.getCache().getAttribute(StrifeAttribute.IGNITE_CHANCE)) {
                magicProj.setMetadata("fireDamage", new FixedMetadataValue(plugin, playerChamp.getCache()
                        .getAttribute(StrifeAttribute.FIRE_DAMAGE)));
            }
        }
        if (playerChamp.getCache().getAttribute(StrifeAttribute.ICE_DAMAGE) > 0) {
            if (random.nextDouble() < playerChamp.getCache().getAttribute(StrifeAttribute.FREEZE_CHANCE)) {
                magicProj.setMetadata("iceDamage", new FixedMetadataValue(plugin, playerChamp.getCache()
                        .getAttribute(StrifeAttribute.ICE_DAMAGE)));
            }
        }
        if (playerChamp.getCache().getAttribute(StrifeAttribute.LIGHTNING_DAMAGE) > 0) {
            if (random.nextDouble() < playerChamp.getCache().getAttribute(StrifeAttribute.SHOCK_CHANCE)) {
                magicProj.setMetadata("lightningDamage", new FixedMetadataValue(plugin, playerChamp.getCache()
                        .getAttribute(StrifeAttribute.LIGHTNING_DAMAGE)));
            }
        }
        if (playerChamp.getCache().getAttribute(StrifeAttribute.LIFE_STEAL) > 0) {
            magicProj.setMetadata("lifeSteal", new FixedMetadataValue(plugin, playerChamp.getCache()
                    .getAttribute(StrifeAttribute.LIFE_STEAL)));
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

        // cancel out all damage from the old event
        for (EntityDamageEvent.DamageModifier modifier : EntityDamageEvent.DamageModifier.values()) {
            if (event.isApplicable(modifier)) {
                event.setDamage(modifier, 0D);
            }
        }

        // pass information to a new calculator
        double newBaseDamage = handleDamageCalculations(damagedLivingEntity, damagingLivingEntity, damagingEntity,
                oldBaseDamage, damagingProjectile, event.getCause(), event);

        // set the base damage of the event
        event.setDamage(EntityDamageEvent.DamageModifier.BASE, newBaseDamage);
    }

    private double handleDamageCalculations(LivingEntity damagedLivingEntity,
                                            LivingEntity damagingLivingEntity,
                                            Entity damagingEntity,
                                            double oldBaseDamage,
                                            Projectile damagingProjectile,
                                            EntityDamageEvent.DamageCause cause,
                                            EntityDamageEvent event) {
        double retDamage = 0D;
        // Five branches: PvP, PvE, EvP, EvE, Projectile
        if (damagingProjectile != null) {
            // Projectile branch
            retDamage = handleProjectileCalculation(damagedLivingEntity, damagingProjectile, event);
        } else if (damagedLivingEntity instanceof Player && damagingLivingEntity instanceof Player) {
            // PvP branch
            retDamage = handlePlayerVersusPlayerCalculation((Player) damagedLivingEntity,
                    (Player) damagingLivingEntity, damagingEntity, event);
        } else if (!(damagedLivingEntity instanceof Player) && damagingLivingEntity instanceof Player) {
            // PvE branch
            retDamage = handlePlayerVersusEnvironmentCalculation(damagedLivingEntity, (Player) damagingLivingEntity);
        } else if (damagedLivingEntity instanceof Player) {
            // EvP branch
            retDamage = handleEnvironmentVersusPlayerCalculation((Player) damagedLivingEntity, damagingLivingEntity,
                    damagingEntity, oldBaseDamage, event);
        } else {
            // EvE branch
            retDamage = handleEnvironmentVersusEnvironmentCalculation(damagedLivingEntity, damagingLivingEntity,
                    oldBaseDamage, cause);
        }
        return retDamage;
    }

    private double handleProjectileCalculation(LivingEntity damagedEntity, Projectile damagingProjectile, EntityDamageEvent event) {
        double retDamage = 0;

        LivingEntity damagingEntity = (LivingEntity) damagingProjectile.getShooter();
        double armorMult = 1.0;
        double pvpMult = 1.0;
        double velocityMult = 1.0;
        double resist = 0;
        boolean overcharge = true;
        if (damagingEntity instanceof Player) {
            if (damagedEntity instanceof Player) {
                pvpMult = plugin.getSettings().getDouble("config.pvp-multiplier", 0.5);
            }
            if (damagingProjectile instanceof Arrow) {
                velocityMult = Math.min(damagingProjectile.getVelocity().lengthSquared() / 9D, 1);
                if (velocityMult < 0.85) {
                    overcharge = false;
                }
            } else {
                if (damagingProjectile instanceof ShulkerBullet) {
                    damagedEntity.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 2, 5, true, false));
                }
            }
        }

        double evadeChance = 0;
        double armor = 0;
        if (damagedEntity instanceof Player) {
            Champion defendingChampion = plugin.getChampionManager().getChampion(((Player)damagedEntity).getUniqueId());
            evadeChance = defendingChampion.getCache().getAttribute(StrifeAttribute.EVASION);
            armor = defendingChampion.getCache().getAttribute(StrifeAttribute.ARMOR);
            resist = defendingChampion.getCache().getAttribute(StrifeAttribute.RESISTANCE);
        }

        if (evadeChance > 0) {
            double evasionCalc = 1 - (100 / (100 + (Math.pow(evadeChance * 100, 1.1))));
            double accuracy = 1.0;
            accuracy = 1 - damagingProjectile.getMetadata("accuracy").get(0).asDouble();
            evasionCalc *= accuracy;
            if (random.nextDouble() < evasionCalc) {
                damagingProjectile.remove();
                damagedEntity.getWorld().playSound(damagedEntity.getEyeLocation(), Sound.ENTITY_GHAST_SHOOT, 0.5f, 2f);
                ActionBarMessage.send((Player) damagedEntity, ChatColor.WHITE + "Dodge!");
                if (damagingEntity instanceof Player) {
                    ActionBarMessage.send((Player) damagingEntity, ChatColor.WHITE + "Miss!");
                }
                event.setCancelled(true);
                return 0D;
            }
        }

        double armorPen = damagingProjectile.getMetadata("armorPen").get(0).asDouble();
        armorMult = getArmorMult(armor, armorPen);

        retDamage = damagingProjectile.getMetadata("damage").get(0).asDouble();
        retDamage *= velocityMult;

        StringBuffer damageStats = new StringBuffer();
        damageStats.append(ChatColor.RESET + "(" + ONE_DECIMAL.format(retDamage * armorMult * pvpMult));
        boolean damageDetails = false;

        double critBonus = damagingProjectile.getMetadata("critical").get(0).asDouble();
        if (critBonus > 0) {
            critBonus = retDamage * critBonus;
            damageStats.append(ChatColor.RED + " +" + ONE_DECIMAL.format(critBonus * velocityMult * armorMult * pvpMult) + "✶");
            damageDetails = true;
            damagedEntity.getWorld().playSound(damagedEntity.getEyeLocation(), Sound.ENTITY_GENERIC_BIG_FALL, 2f, 0.8f);
        }

        double overBonus = damagingProjectile.getMetadata("overcharge").get(0).asDouble();
        if (overBonus > 0 && overcharge) {
            overBonus = retDamage * overBonus;
            damageStats.append(ChatColor.YELLOW + " +" + ONE_DECIMAL.format(overBonus * armorMult * pvpMult) + "✦");
            damageDetails = true;
        }

        retDamage += critBonus;
        retDamage += overBonus;

        double trueDamage = 0D;
        if (damagingProjectile.hasMetadata("fireDamage")) {
            double fireDamage = damagingProjectile.getMetadata("fireDamage").get(0).asDouble();
            int fireTicks = 30 + (int)fireDamage * 5;
            fireDamage = 1 + fireDamage * (damagedEntity.getHealth() / 200) * (1 - resist);
            damageStats.append(ChatColor.GOLD + " +" + ONE_DECIMAL.format(fireDamage * pvpMult) + "☀");
            damageDetails = true;
            trueDamage += fireDamage;
            damagedEntity.setFireTicks(Math.max(fireTicks, damagedEntity.getFireTicks()));
            damagedEntity.getWorld().playSound(damagedEntity.getEyeLocation(), Sound.ITEM_FLINTANDSTEEL_USE, 1f, 1f);
        }
        if (damagingProjectile.hasMetadata("lightningDamage")) {
            double lightningDamage = damagingProjectile.getMetadata("lightningDamage").get(0).asDouble();
            double hpMult = 2 - (damagedEntity.getHealth() / damagedEntity.getMaxHealth());
            lightningDamage = lightningDamage * hpMult * (1 - resist);
            damageDetails = true;
            trueDamage += lightningDamage;
            damageStats.append(ChatColor.GRAY + " +" + ONE_DECIMAL.format(lightningDamage * pvpMult) + "⚡");
            damagedEntity.getWorld().playSound(damagedEntity.getEyeLocation(), Sound.ENTITY_LIGHTNING_THUNDER,
                    0.7f, 1.5f);
        }
        if (damagingProjectile.hasMetadata("iceDamage")) {
            double iceDamage = damagingProjectile.getMetadata("iceDamage").get(0).asDouble();
            int slowDuration = 30 + (int) iceDamage * 3;
            iceDamage = 1 + iceDamage * (damagedEntity.getMaxHealth() / 300) * (1 - resist);
            damageStats.append(ChatColor.AQUA + " +" + ONE_DECIMAL.format(iceDamage * pvpMult) + "❊");
            damageDetails = true;
            trueDamage += iceDamage;
            damagedEntity.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, slowDuration, 1));
            damagedEntity.getWorld().playSound(damagedEntity.getEyeLocation(), Sound.BLOCK_GLASS_BREAK, 1f, 1f);
        }

        retDamage *= armorMult;
        retDamage += trueDamage;

        double potionMult = 1D;
        if (damagedEntity.hasPotionEffect(PotionEffectType.WITHER)) {
            potionMult += 0.2D;
        }
        if (damagedEntity.hasPotionEffect(PotionEffectType.DAMAGE_RESISTANCE)) {
            potionMult -= 0.1D;
        }

        retDamage *= potionMult;
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
            if (damagingEntity.getHealth() > 0) {
                damagingEntity.setHealth(Math.min(damagingEntity.getHealth() + lifeStolen, damagingEntity.getMaxHealth()));
            }
        }
        if (damagingEntity instanceof Player) {
            if (damageDetails) {
                damageStats.append(ChatColor.RESET + ")");
                ActionBarMessage.send((Player) damagingEntity, "&f&l" + INT.format(retDamage) + " Damage! " + damageStats);
            } else {
                ActionBarMessage.send((Player) damagingEntity, "&f&l" + INT.format(retDamage) + " Damage!");
            }
        }
        return retDamage;
    }

    private double handleEnvironmentVersusEnvironmentCalculation(LivingEntity damagedLivingEntity,
                                                                 LivingEntity damagingLivingEntity,
                                                                 double oldBaseDamage,
                                                                 EntityDamageEvent.DamageCause cause) {
        double damage;
        if (damagingLivingEntity.hasMetadata("DAMAGE")) {
            damage = getDamageFromMeta(damagingLivingEntity, damagedLivingEntity, cause);
        } else {
            damage = oldBaseDamage;
        }
        return damage;
    }

    private double handleEnvironmentVersusPlayerCalculation(Player damagedPlayer,
                                                            LivingEntity damagingLivingEntity,
                                                            Entity damagingEntity,
                                                            double oldBaseDamage,
                                                            EntityDamageEvent event) {
        double damage;
        if (damagingLivingEntity.hasMetadata("DAMAGE")) {
            damage = getDamageFromMeta(damagingLivingEntity, damagedPlayer, event.getCause());
        } else {
            damage = oldBaseDamage;
        }
        Champion damagedChampion = plugin.getChampionManager().getChampion(damagedPlayer.getUniqueId());

        double evadeChance = damagedChampion.getCache().getAttribute(StrifeAttribute.EVASION);
        if (evadeChance > 0) {
            double evasionCalc = 1 - (100 / (100 + (Math.pow(evadeChance * 100, 1.1))));
            if (random.nextDouble() < evasionCalc) {
                if (damagingEntity instanceof Projectile) {
                    damagingEntity.remove();
                }
                damagedPlayer.getWorld().playSound(damagedPlayer.getEyeLocation(), Sound.ENTITY_GHAST_SHOOT,
                        0.5f, 2f);
                ActionBarMessage.send(damagedPlayer, ChatColor.WHITE + "Dodge!");
                event.setCancelled(true);
                return 0D;
            }
        }

        if (damagedPlayer.isBlocking()) {
            if (random.nextDouble() < damagedChampion.getCache().getAttribute(StrifeAttribute.ABSORB_CHANCE)) {
                if (damagingEntity instanceof Projectile) {
                    damagingEntity.remove();
                }
                damagedPlayer.setHealth(Math.min(damagedPlayer.getHealth() + (damagedPlayer.getMaxHealth() / 25),
                        damagedPlayer.getMaxHealth()));
                damagedPlayer.getWorld().playSound(damagedPlayer.getEyeLocation(), Sound.ENTITY_BLAZE_HURT, 1f, 2f);
                event.setCancelled(true);
                return 0D;
            }
            if (random.nextDouble() < damagedChampion.getCache().getAttribute(StrifeAttribute.PARRY)) {
                damagingLivingEntity.damage(damage * 0.2);
                damagedPlayer.getWorld().playSound(damagedPlayer.getEyeLocation(), Sound.BLOCK_ANVIL_LAND, 1f, 2f);
                ActionBarMessage.send(damagedPlayer, ChatColor.WHITE + "Parry!");
                event.setCancelled(true);
                return 0D;
            }
            damage *= 1 - (damagedChampion.getCache().getAttribute(StrifeAttribute.BLOCK));
        }
        damage *= getArmorMult(damagedChampion.getCache().getAttribute(StrifeAttribute.ARMOR), 0);
        return damage;
    }

    private double handlePlayerVersusEnvironmentCalculation(LivingEntity damagedEntity,
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
        plugin.getAttackSpeedTask().setTimeLeft(damagingPlayer.getUniqueId(), timeToSet);
        if (timeLeft > 0) {
            attackSpeedMult = Math.max(1.0 - 1.0 * ((timeLeft * 1D) / timeToSet), 0.1);
        }

        retDamage = damagingChampion.getCache().getAttribute(StrifeAttribute.MELEE_DAMAGE) * attackSpeedMult;

        StringBuffer damageStats = new StringBuffer();
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

        if (fireDamage > 0D) {
            double igniteCalc = damagingChampion.getCache().getAttribute(StrifeAttribute.IGNITE_CHANCE) * attackSpeedMult;
            if (random.nextDouble() < igniteCalc) {
                int fireTicks = 30 + (int)fireDamage * 5;
                fireDamage = 1 + fireDamage * (damagedEntity.getHealth() / 200);
                damageStats.append(ChatColor.GOLD + " +" + ONE_DECIMAL.format(fireDamage) + "☀");
                damageDetails = true;
                trueDamage += fireDamage;
                damagedEntity.setFireTicks(Math.max(fireTicks, damagedEntity.getFireTicks()));
                damagedEntity.getWorld().playSound(damagedEntity.getEyeLocation(), Sound.ITEM_FLINTANDSTEEL_USE, 1f, 1f);
            }
        }
        if (lightningDamage > 0D) {
            double shockCalc = damagingChampion.getCache().getAttribute(StrifeAttribute.SHOCK_CHANCE) * attackSpeedMult;
            if (random.nextDouble() < shockCalc) {
                double hpMult = 2 - (damagedEntity.getHealth() / damagedEntity.getMaxHealth());
                lightningDamage = lightningDamage * hpMult;
                trueDamage += lightningDamage;
                damageDetails = true;
                damageStats.append(ChatColor.GRAY + " +" + ONE_DECIMAL.format(lightningDamage) + "⚡");
                damagedEntity.getWorld().playSound(damagedEntity.getEyeLocation(), Sound.ENTITY_LIGHTNING_THUNDER, 0.7f, 1.5f);
            }
        }
        if (iceDamage > 0D) {
            double freezeCalc = damagingChampion.getCache().getAttribute(StrifeAttribute.FREEZE_CHANCE) * attackSpeedMult;
            if (random.nextDouble() < freezeCalc) {
                int slowDuration = 30 + (int) iceDamage * 3;
                iceDamage = 1 + iceDamage * (damagedEntity.getMaxHealth() / 300);
                damageStats.append(ChatColor.AQUA + " +" + ONE_DECIMAL.format(iceDamage) + "❊");
                damageDetails = true;
                trueDamage += iceDamage;
                damagedEntity.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, slowDuration, 1));
                damagedEntity.getWorld().playSound(damagedEntity.getEyeLocation(), Sound
                        .BLOCK_GLASS_BREAK, 1f, 1f);
            }
        }

        // potion effects mults
        double potionMult = 1D;
        if (damagedEntity.hasPotionEffect(PotionEffectType.WITHER)) {
            potionMult += 0.20D;
        }
        if (damagedEntity.hasPotionEffect(PotionEffectType.DAMAGE_RESISTANCE)) {
            potionMult -= 0.1D;
        }
        if (damagingPlayer.hasPotionEffect(PotionEffectType.INCREASE_DAMAGE)) {
            potionMult += 0.1D;
        }
        if (damagingPlayer.hasPotionEffect(PotionEffectType.WEAKNESS)) {
            potionMult -= 0.1D;
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
            if (damagingPlayer.getHealth() > 0) {
                damagingPlayer.setHealth(Math.min(damagingPlayer.getHealth() + lifeStolen,
                        damagingPlayer.getMaxHealth()));
            }
        }
        if (damageDetails) {
            damageStats.append(ChatColor.RESET + ")");
            ActionBarMessage.send(damagingPlayer, "&f&l" + INT.format(retDamage) + " Damage! " + damageStats);
        } else {
            ActionBarMessage.send(damagingPlayer, "&f&l" + INT.format(retDamage) + " Damage!");
        }

        return retDamage;
    }

    private double handlePlayerVersusPlayerCalculation(Player damagedPlayer,
                                                       Player damagingPlayer,
                                                       Entity damagingEntity,
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

        // get the evasion chance of the damaged champion and check if evaded
        double evadeChance = damagedChampion.getCache().getAttribute(StrifeAttribute.EVASION);
        if (evadeChance > 0) {
            // get the accuracy of the damaging champion and check if still hits
            double accuracy =  damagingChampion.getCache().getAttribute(StrifeAttribute.ACCURACY);
            double normalizedEvadeChance = Math.max(evadeChance * (1 - accuracy), 0);
            double evasionCalc = 1 - (100 / (100 + (Math.pow(normalizedEvadeChance * 100, 1.1))));
            if (random.nextDouble() < evasionCalc) {
                if (damagingEntity instanceof Projectile) {
                    damagingEntity.remove();
                }
                damagedPlayer.getWorld().playSound(damagedPlayer.getEyeLocation(), Sound.ENTITY_GHAST_SHOOT, 0.5f, 2f);
                ActionBarMessage.send(damagedPlayer, ChatColor.WHITE + "Dodge!");
                ActionBarMessage.send(damagingPlayer, ChatColor.WHITE + "Miss!");
                event.setCancelled(true);
                return 0D;
            }
        }

        double armorMult = getArmorMult(damagedChampion.getCache().getAttribute(StrifeAttribute.ARMOR), damagingChampion
                .getCache().getAttribute(StrifeAttribute.ARMOR_PENETRATION));

        // calculating attack speed
        double attackSpeedMult = 1D;
        double attackSpeed = StrifeAttribute.ATTACK_SPEED.getBaseValue() * (1 / (1 + damagingChampion
                .getCache().getAttribute(StrifeAttribute.ATTACK_SPEED)));
        long timeLeft = plugin.getAttackSpeedTask().getTimeLeft(damagingPlayer.getUniqueId());
        long timeToSet = Math.round(Math.max(4.0 * attackSpeed, 0D));
        plugin.getAttackSpeedTask().setTimeLeft(damagingPlayer.getUniqueId(), timeToSet);
        if (timeLeft > 0) {
            attackSpeedMult = Math.max(1.0 - 1.0 * ((timeLeft * 1D) / timeToSet), 0.1);
        }

        retDamage = damagingChampion.getCache().getAttribute(StrifeAttribute.MELEE_DAMAGE) * attackSpeedMult;

        // check if damaged player is blocking
        if (damagedPlayer.isBlocking()) {
            if (random.nextDouble() < damagedChampion.getCache().getAttribute(StrifeAttribute.ABSORB_CHANCE)) {
                if (damagingEntity instanceof Projectile) {
                    damagingEntity.remove();
                }
                damagedPlayer.setHealth(Math.min(damagedPlayer.getHealth() + (damagedPlayer.getMaxHealth() / 25),
                        damagedPlayer.getMaxHealth()));
                damagedPlayer.getWorld().playSound(damagingPlayer.getEyeLocation(), Sound.ENTITY_BLAZE_HURT, 1f, 2f);
                event.setCancelled(true);
                return 0D;
            }
            if (random.nextDouble() < damagedChampion.getCache().getAttribute(StrifeAttribute.PARRY)) {
                damagingPlayer.damage(retDamage * 0.2 * pvpMult);
                damagedPlayer.getWorld().playSound(damagedPlayer.getEyeLocation(), Sound.BLOCK_ANVIL_LAND, 1f, 2f);
                ActionBarMessage.send(damagedPlayer, ChatColor.WHITE + "Parry!");
                ActionBarMessage.send(damagingPlayer, ChatColor.WHITE + "Parried!");
                event.setCancelled(true);
                return 0D;
            }

            retDamage *= 1 - (damagedChampion.getCache().getAttribute(StrifeAttribute.BLOCK));
        }

        StringBuffer damageStats = new StringBuffer();
        damageStats.append(ChatColor.RESET + "(" + ONE_DECIMAL.format(retDamage * armorMult));
        boolean damageDetails = false;

        // critical damage time!
        double critBonus = 0D;
        if (random.nextDouble() >= damagingChampion.getCache().getAttribute(StrifeAttribute.CRITICAL_RATE)) {
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
        if (fireDamage > 0D) {
            double igniteCalc = damagingChampion.getCache().getAttribute(StrifeAttribute.IGNITE_CHANCE) * attackSpeedMult;
            if (random.nextDouble() < igniteCalc) {
                int fireTicks = 30 + (int)fireDamage * 5;
                fireDamage = 1 + fireDamage * (damagedPlayer.getHealth() / 200) * (1 - damagedChampion.getCache()
                        .getAttribute(StrifeAttribute.RESISTANCE));
                damageStats.append(ChatColor.GOLD + " +" + ONE_DECIMAL.format(fireDamage * pvpMult) + "☀");
                damageDetails = true;
                trueDamage += fireDamage;
                damagedPlayer.setFireTicks(Math.max(fireTicks, damagedPlayer.getFireTicks()));
                damagedPlayer.playSound(damagedPlayer.getEyeLocation(), Sound.ITEM_FLINTANDSTEEL_USE, 1f, 1f);
            }
        }
        if (lightningDamage > 0D) {
            double shockCalc = damagingChampion.getCache().getAttribute(StrifeAttribute.SHOCK_CHANCE) * attackSpeedMult;
            if (random.nextDouble() < shockCalc) {
                double hpMult = 2 - (damagedPlayer.getHealth() / damagedPlayer.getMaxHealth());
                lightningDamage = lightningDamage * hpMult * (1 - damagedChampion.getCache().getAttribute(StrifeAttribute
                        .RESISTANCE));
                damageDetails = true;
                trueDamage += lightningDamage;
                damageStats.append(ChatColor.GRAY + " +" + ONE_DECIMAL.format(lightningDamage * pvpMult) + "⚡");
                damagedPlayer.getWorld().playSound(damagedPlayer.getEyeLocation(), Sound.ENTITY_LIGHTNING_THUNDER,
                        0.7f, 1.5f);
            }
        }
        if (iceDamage > 0D) {
            double freezeCalc = damagingChampion.getCache().getAttribute(StrifeAttribute.FREEZE_CHANCE) * attackSpeedMult;
            if (random.nextDouble() < freezeCalc) {
                int slowDuration = 30 + (int) iceDamage * 3;
                iceDamage = iceDamage + iceDamage * (damagedPlayer.getMaxHealth() / 300) * (1 - damagedChampion
                        .getCache().getAttribute(StrifeAttribute.RESISTANCE));
                damageStats.append(ChatColor.AQUA + " +" + ONE_DECIMAL.format(iceDamage * pvpMult) + "❊");
                damageDetails = true;
                trueDamage += iceDamage;
                damagedPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, slowDuration, 1));
                damagedPlayer.getWorld().playSound(damagedPlayer.getEyeLocation(), Sound.BLOCK_GLASS_BREAK, 1f, 1f);
            }
        }

        // potion effects mults
        double potionMult = 1D;
        if (damagedPlayer.hasPotionEffect(PotionEffectType.WITHER)) {
            potionMult += 0.2D;
        }
        if (damagedPlayer.hasPotionEffect(PotionEffectType.DAMAGE_RESISTANCE)) {
            potionMult -= 0.1D;
        }
        if (damagingPlayer.hasPotionEffect(PotionEffectType.INCREASE_DAMAGE)) {
            potionMult += 0.1D;
        }
        if (damagingPlayer.hasPotionEffect(PotionEffectType.WEAKNESS)) {
            potionMult -= 0.1D;
        }

        // combine!
        retDamage *= potionMult;
        retDamage *= armorMult;
        retDamage += trueDamage;
        retDamage *= pvpMult;

        // life steal
        double lifeSteal = damagingChampion.getCache().getAttribute(StrifeAttribute.LIFE_STEAL);
        if (lifeSteal > 0) {
            double lifeStolen = retDamage * lifeSteal;
            lifeStolen *= Math.min(damagingPlayer.getFoodLevel() / 7.0D, 1.0D);
            if (damagingPlayer.hasPotionEffect(PotionEffectType.POISON)) {
                lifeStolen *= 0.34;
            }
            if (damagingPlayer.getHealth() > 0) {
                damagingPlayer.setHealth(Math.min(damagingPlayer.getHealth() + lifeStolen,
                        damagingPlayer.getMaxHealth()));
            }
        }

        if (damageDetails) {
            damageStats.append(ChatColor.RESET + ")");
            ActionBarMessage.send(damagingPlayer, "&f&l" + INT.format(retDamage) + " Damage! " + damageStats);
        } else {
            ActionBarMessage.send(damagingPlayer, "&f&l" + INT.format(retDamage) + " Damage!");
        }

        return retDamage;
    }

    private double getDamageFromMeta(LivingEntity a, LivingEntity b, EntityDamageEvent.DamageCause d) {
        double damage = a.getMetadata("DAMAGE").get(0).asDouble();
        if (d == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {
            damage = damage * Math.max(0.3, 2.5 / (a.getLocation().distanceSquared(b.getLocation()) + 1));
        }
        return damage;
    }

    private double getArmorMult(double armor, double apen) {
        if (armor > 0) {
            if (apen < 1) {
                return 100 / (100 + (Math.pow(((armor * (1 - apen)) * 100), 1.2)));
            } else {
                return 1 + ((apen - 1) / 5);
            }
        } else {
            return 1 + (apen / 5);
        }
    }
}
