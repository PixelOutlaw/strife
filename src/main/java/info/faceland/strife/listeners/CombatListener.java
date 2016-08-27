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
import com.tealcubegames.minecraft.spigot.versions.actionbars.ActionBarMessager;
import com.tealcubegames.minecraft.spigot.versions.api.actionbars.ActionBarMessage;

import info.faceland.strife.StrifePlugin;
import info.faceland.strife.attributes.StrifeAttribute;
import info.faceland.strife.data.Champion;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.SkullType;
import org.bukkit.Sound;
import org.bukkit.entity.*;
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
import org.bukkit.util.Vector;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Random;

public class CombatListener implements Listener {

    private final StrifePlugin plugin;
    private final Random random;

    private static final String ATTACK_UNCHARGED = TextUtils.color("<white>Not charged enough!");
    private static final String ATTACK_MISSED = TextUtils.color("<white>Miss!");
    private static final String ATTACK_DODGED = TextUtils.color("<white>Dodge!");
    private static final String ATTACK_PARRIED = TextUtils.color("<white>Parry!");
    private static final String ATTACK_BLOCKED = TextUtils.color("<white>Blocked!");
    private static final String ATTACK_NO_DAMAGE = TextUtils.color("<white>0 Damage!");

    private static final ActionBarMessage notCharged = ActionBarMessager.createActionBarMessage(ATTACK_UNCHARGED);
    private static final ActionBarMessage missed = ActionBarMessager.createActionBarMessage(ATTACK_MISSED);
    private static final ActionBarMessage dodged = ActionBarMessager.createActionBarMessage(ATTACK_DODGED);
    private static final ActionBarMessage parried = ActionBarMessager.createActionBarMessage(ATTACK_PARRIED);
    private static final ActionBarMessage blocked = ActionBarMessager.createActionBarMessage(ATTACK_BLOCKED);
    private static final ActionBarMessage noDamage = ActionBarMessager.createActionBarMessage(ATTACK_NO_DAMAGE);

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

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityBurnEvent(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof LivingEntity)) {
            return;
        }
        if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
            return;
        }
        if (event.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK) {
            double hpdmg = ((LivingEntity) event.getEntity()).getHealth() / 28;
            if (event.isApplicable(EntityDamageEvent.DamageModifier.ARMOR)) {
                event.setDamage(EntityDamageEvent.DamageModifier.ARMOR, 0);
            }
            event.setDamage(1 + hpdmg);
            return;
        }
        if (event.getCause() == EntityDamageEvent.DamageCause.FIRE) {
            double hpdmg = ((LivingEntity) event.getEntity()).getHealth() / 40;
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
        if (!(event.getEntity() instanceof Arrow)) {
            return;
        }
        Player p = (Player) event.getEntity().getShooter();
        Champion playerChamp = plugin.getChampionManager().getChampion(p.getUniqueId());
        playerChamp.getAttributeValues(true);
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
        if (attackSpeedMult <= 0.25) {
            notCharged.send(p);
            p.getWorld().playSound(p.getLocation(), Sound.ENTITY_BLAZE_AMBIENT, 0.8f, 0.8f);
            return;
        }

        // double attackspeed penalty for wands
        attackSpeedMult *= attackSpeedMult;
        attackSpeedMult = Math.max(0.15, attackSpeedMult);

        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_BLAZE_HURT, 0.9f, 2f);
        playerChamp.getAttributeValues(true);
        playerChamp.getWeaponAttributeValues();
        playerChamp.getCache().recombine();
        ShulkerBullet magicProj = p.getWorld().spawn(p.getEyeLocation().clone().add(0, -0.45, 0), ShulkerBullet.class);
        magicProj.setShooter(p);
        Vector vec = p.getLocation().getDirection();
        magicProj.setVelocity(new Vector(vec.getX() * 1.2, vec.getY() * 1.2 + 0.255, vec.getZ() * 1.2));
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
                        .getAttribute(StrifeAttribute.FIRE_DAMAGE) * attackSpeedMult));
            }
        }
        if (playerChamp.getCache().getAttribute(StrifeAttribute.ICE_DAMAGE) > 0) {
            if (random.nextDouble() < playerChamp.getCache().getAttribute(StrifeAttribute.FREEZE_CHANCE)) {
                magicProj.setMetadata("iceDamage", new FixedMetadataValue(plugin, playerChamp.getCache()
                        .getAttribute(StrifeAttribute.ICE_DAMAGE) * attackSpeedMult));
            }
        }
        if (playerChamp.getCache().getAttribute(StrifeAttribute.LIGHTNING_DAMAGE) > 0) {
            if (random.nextDouble() < playerChamp.getCache().getAttribute(StrifeAttribute.SHOCK_CHANCE)) {
                magicProj.setMetadata("lightningDamage", new FixedMetadataValue(plugin, playerChamp.getCache()
                        .getAttribute(StrifeAttribute.LIGHTNING_DAMAGE) * attackSpeedMult));
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
        double retDamage = 0D;
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
                if (attackerLevelAdv > 0) {
                    pvpMult *= 1 - (0.008 * attackerLevelAdv);
                } else {
                    pvpMult += 0.005 * (-attackerLevelAdv);
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
        double resist = 0;
        double parry = 0;
        double absorb = 0;
        if (damagedEntity instanceof Player) {
            Champion defendingChampion = plugin.getChampionManager().getChampion(((Player) damagedEntity).getUniqueId());
            evasion = defendingChampion.getCache().getAttribute(StrifeAttribute.EVASION);
            armor = defendingChampion.getCache().getAttribute(StrifeAttribute.ARMOR);
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
                dodged.send((Player) damagedEntity);
                if (damagingEntity instanceof Player) {
                    missed.send((Player) damagingEntity);
                }
                event.setCancelled(true);
                return 0D;
            }
        }

        if (isBlocked) {
            if (random.nextDouble() < absorb * 2) {
                damagedEntity.setHealth(Math.min(damagedEntity.getHealth() + (damagedEntity.getMaxHealth() * 0.025),
                        damagedEntity.getMaxHealth()));
                damagedEntity.getWorld().playSound(damagedEntity.getEyeLocation(), Sound.ENTITY_BLAZE_HURT, 1f, 2f);
                damagingProjectile.remove();
                event.setDamage(0);
                event.setCancelled(true);
                return 0D;
            }
            if (random.nextDouble() < parry * 2) {
                damagedEntity.getWorld().playSound(damagedEntity.getEyeLocation(), Sound.BLOCK_ANVIL_LAND, 1f, 2f);
                if (damagedEntity instanceof Player) {
                    parried.send((Player) damagedEntity);
                }
                damagingProjectile.remove();
                event.setDamage(0);
                event.setCancelled(true);
                return 0D;
            }
            blocked.send((Player) damagingProjectile.getShooter());
            return 0D;
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
                    noDamage.send((Player) damagingEntity);
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
                ActionBarMessage combatMsg = ActionBarMessager.createActionBarMessage(combatString);
                combatMsg.send((Player) damagingEntity);
            } else {
                String combatString = TextUtils.color("&f&l" + INT.format(retDamage) + " Damage!" + multiplierString);
                ActionBarMessage combatMsg = ActionBarMessager.createActionBarMessage(combatString);
                combatMsg.send((Player) damagingEntity);
            }
        }
        return retDamage;
    }

    private double handleEnvironmentVersusEnvironmentCalculation(LivingEntity damagedLivingEntity,
                                                                 LivingEntity damagingLivingEntity,
                                                                 double oldBaseDamage) {
        double damage;
        if (damagingLivingEntity.hasMetadata("DAMAGE")) {
            damage = getDamageFromMeta(damagingLivingEntity, damagedLivingEntity);
        } else {
            damage = oldBaseDamage;
        }
        return damage;
    }

    private double handleEnvironmentVersusPlayerCalculation(Player damagedPlayer,
                                                            LivingEntity damagingLivingEntity,
                                                            Entity damagingEntity,
                                                            double oldBaseDamage,
                                                            boolean isBlocked,
                                                            EntityDamageEvent event) {
        double damage;
        if (damagingLivingEntity.hasMetadata("DAMAGE")) {
            damage = getDamageFromMeta(damagingLivingEntity, damagedPlayer);
        } else {
            damage = oldBaseDamage;
        }
        Champion damagedChampion = plugin.getChampionManager().getChampion(damagedPlayer.getUniqueId());

        double evasion = damagedChampion.getCache().getAttribute(StrifeAttribute.EVASION);
        if (evasion > 0) {
            if (getEvadeChance(evasion, 0)) {
                if (damagingEntity instanceof Projectile) {
                    damagingEntity.remove();
                }
                damagedPlayer.getWorld().playSound(damagedPlayer.getEyeLocation(), Sound.ENTITY_GHAST_SHOOT,
                        0.5f, 2f);
                dodged.send(damagedPlayer);
                event.setCancelled(true);
                return 0D;
            }
        }

        if (isBlocked) {
            if (random.nextDouble() < damagedChampion.getCache().getAttribute(StrifeAttribute.ABSORB_CHANCE)) {
                if (damagingEntity instanceof Projectile) {
                    damagingEntity.remove();
                }
                damagedPlayer.setHealth(Math.min(damagedPlayer.getHealth() + (damagedPlayer.getMaxHealth() * 0.025),
                        damagedPlayer.getMaxHealth()));
                damagedPlayer.getWorld().playSound(damagedPlayer.getEyeLocation(), Sound.ENTITY_BLAZE_HURT, 1f, 2f);
                event.setCancelled(true);
                return 0D;
            }
            if (random.nextDouble() < damagedChampion.getCache().getAttribute(StrifeAttribute.PARRY)) {
                if (damagingEntity instanceof Projectile) {
                    damagingEntity.remove();
                }
                damagingLivingEntity.damage(damage * 0.2);
                damagedPlayer.getWorld().playSound(damagedPlayer.getEyeLocation(), Sound.BLOCK_ANVIL_LAND, 1f, 2f);
                parried.send(damagedPlayer);
                event.setCancelled(true);
                return 0D;
            }
            damage *= 1 - (damagedChampion.getCache().getAttribute(StrifeAttribute.BLOCK));
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
                noDamage.send(damagingPlayer);
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
            ActionBarMessage combatMsg = ActionBarMessager.createActionBarMessage(combatString);
            combatMsg.send(damagingPlayer);
        } else {
            String combatString = TextUtils.color("&f&l" + INT.format(retDamage) + " Damage!" + multiplierString);
            ActionBarMessage combatMsg = ActionBarMessager.createActionBarMessage(combatString);
            combatMsg.send(damagingPlayer);
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
        if (attackerLevelAdv > 0) {
            pvpMult *= 1 - (0.008 * attackerLevelAdv);
        } else {
            pvpMult += 0.005 * (-attackerLevelAdv);
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
                dodged.send(damagedPlayer);
                missed.send(damagingPlayer);
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
        if (isBlocked) {
            if (random.nextDouble() < damagedChampion.getCache().getAttribute(StrifeAttribute.ABSORB_CHANCE)) {
                if (damagingEntity instanceof Projectile) {
                    damagingEntity.remove();
                }
                damagedPlayer.setHealth(Math.min(damagedPlayer.getHealth() + (damagedPlayer.getMaxHealth() * 0.025),
                        damagedPlayer.getMaxHealth()));
                damagedPlayer.getWorld().playSound(damagingPlayer.getEyeLocation(), Sound.ENTITY_BLAZE_HURT, 1f, 2f);
                event.setCancelled(true);
                return 0D;
            }
            if (random.nextDouble() < damagedChampion.getCache().getAttribute(StrifeAttribute.PARRY)) {
                if (damagingEntity instanceof Projectile) {
                    damagingEntity.remove();
                }
                damagingPlayer.damage(retDamage * 0.2 * pvpMult);
                damagedPlayer.getWorld().playSound(damagedPlayer.getEyeLocation(), Sound.BLOCK_ANVIL_LAND, 1f, 2f);
                parried.send(damagedPlayer);
                blocked.send(damagingPlayer);
                event.setCancelled(true);
                return 0D;
            }

            retDamage *= 1 - (damagedChampion.getCache().getAttribute(StrifeAttribute.BLOCK));
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
                noDamage.send(damagingPlayer);
                event.setCancelled(true);
                return 0D;
            }
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
            if (damagingPlayer.getHealth() > 0 && !damagingPlayer.isDead()) {
                damagingPlayer.setHealth(Math.min(damagingPlayer.getHealth() + lifeStolen,
                        damagingPlayer.getMaxHealth()));
            }
        }

        if (damageDetails) {
            damageStats.append(ChatColor.RESET + ")");
            damageStats.append(multiplierString);
            String combatString = TextUtils.color("&f&l" + INT.format(retDamage) + " Damage! " + damageStats);
            ActionBarMessage combatMsg = ActionBarMessager.createActionBarMessage(combatString);
            combatMsg.send((Player) damagingEntity);
        } else {
            String combatString = TextUtils.color("&f&l" + INT.format(retDamage) + " Damage!" + multiplierString);
            ActionBarMessage combatMsg = ActionBarMessager.createActionBarMessage(combatString);
            combatMsg.send((Player) damagingEntity);
        }
        return retDamage;
    }

    private double getDamageFromMeta(LivingEntity a, LivingEntity b) {
        double damage = a.getMetadata("DAMAGE").get(0).asDouble();
        if (a instanceof Creeper) {
            if (a.getFireTicks() > 0) {
                b.setFireTicks(b.getFireTicks() + 100);
            }
            if (((Creeper) a).isPowered()) {
                damage = damage * Math.max(0.3, 3 - (a.getLocation().distance(b.getLocation()) / 2));
            } else {
                damage = damage * Math.max(0.3, 1 - (a.getLocation().distance(b.getLocation()) / 3));
            }
        }
        return damage;
    }

    private double getArmorMult(double armor, double apen) {
        if (armor > 0) {
            double adjustedArmor = armor * (1 - apen);
            if (adjustedArmor > 0) {
                return 420 / (420 + Math.pow(adjustedArmor, 1.65));
            }
        }
        return 1 + (apen / 5);
    }

    private boolean getEvadeChance(double evasion, double accuacy) {
        evasion *= 1 - accuacy;
        double evadeChance = 1 - (420 / (420 + Math.pow(evasion, 1.55)));
        if (random.nextDouble() <= evadeChance) {
            return true;
        }
        return false;
    }

    private double getFireDamage(double fireDamage, LivingEntity target, double pvpMult, double resist) {
        int fireTicks = 30 + (int) fireDamage * 5;
        double currentHpMult = 4 * (target.getHealth() / target.getMaxHealth()) - 1.5;
        fireDamage = Math.max(fireDamage, fireDamage * currentHpMult) * (1 - resist);
        target.setFireTicks(Math.max(fireTicks, target.getFireTicks()));
        target.getWorld().playSound(target.getEyeLocation(),Sound.ITEM_FLINTANDSTEEL_USE, 1f, 1f);
        target.getWorld().spawnParticle(Particle.FLAME, target.getEyeLocation(), 6 + (int) fireDamage / 2,
                0.3, 0.3, 0.3, 0.03);
        return fireDamage * pvpMult;
    }

    private double getLightningDamage(double lightningDamage, LivingEntity target, double pvpMult, double resist) {
        double missingHpMult = -0.7 + Math.min((target.getMaxHealth() / target.getHealth()), 6.7);
        lightningDamage = Math.max(lightningDamage, lightningDamage * missingHpMult) * (1 - resist);
        target.getWorld().playSound(target.getEyeLocation(), Sound.ENTITY_LIGHTNING_THUNDER, 0.7f, 1.5f);
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
                potionMult += 0.1 * (effect.getAmplifier() + 1);
                continue;
            }
            if (effect.getType().equals(PotionEffectType.DAMAGE_RESISTANCE)) {
                potionMult -= 0.1 * (effect.getAmplifier() + 1);
                continue;
            }
        }
        return potionMult;
    }
}
