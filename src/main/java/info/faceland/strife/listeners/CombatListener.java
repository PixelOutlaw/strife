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
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;

public class CombatListener implements Listener {

    private static final String[] DOGE_MEMES =
            {"<aqua>wow", "<green>wow", "<light purple>wow", "<aqua>much pain", "<green>much pain",
                    "<light purple>much pain", "<aqua>many disrespects", "<green>many disrespects",
                    "<light purple>many disrespects", "<red>no u", "<red>2damage4me"};
    private final StrifePlugin plugin;
    private final Random random;

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

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSwing(PlayerInteractEvent event) {
        if (event.getAction() != Action.LEFT_CLICK_AIR) {
            return;
        }
        Champion playerChamp = plugin.getChampionManager().getChampion(event.getPlayer().getUniqueId());
        double attackSpeed = StrifeAttribute.ATTACK_SPEED.getBaseValue() * (1 / (1 + playerChamp.getCache()
                .getAttribute(StrifeAttribute.ATTACK_SPEED)));
        long timeToSet = Math.round(Math.max(4.0 * attackSpeed, 0D));
        plugin.getAttackSpeedTask().setTimeLeft(event.getPlayer().getUniqueId(), timeToSet);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (event.getEntity().getShooter() instanceof Entity) {
            event.getEntity().setVelocity(event.getEntity().getVelocity().add(((Entity) event.getEntity()
                    .getShooter()).getVelocity()));
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
        boolean melee = true;
        if (damagingEntity instanceof LivingEntity) {
            damagingLivingEntity = (LivingEntity) damagingEntity;
        } else if (damagingEntity instanceof Projectile && ((Projectile) damagingEntity).getShooter() instanceof
                LivingEntity) {
            damagingLivingEntity = (LivingEntity) ((Projectile) damagingEntity).getShooter();
            melee = false;
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
                oldBaseDamage, melee, event.getCause(), event);

        // set the base damage of the event
        event.setDamage(EntityDamageEvent.DamageModifier.BASE, newBaseDamage);
    }

    private double handleDamageCalculations(LivingEntity damagedLivingEntity,
                                            LivingEntity damagingLivingEntity,
                                            Entity damagingEntity,
                                            double oldBaseDamage,
                                            boolean melee,
                                            EntityDamageEvent.DamageCause cause,
                                            EntityDamageEvent event) {
        double retDamage = 0D;
        // four branches: PvP, PvE, EvP, EvE
        if (damagedLivingEntity instanceof Player && damagingLivingEntity instanceof Player) {
            // PvP branch
            retDamage = handlePlayerVersusPlayerCalculation((Player) damagedLivingEntity,
                    (Player) damagingLivingEntity, damagingEntity, melee, event);
        } else if (!(damagedLivingEntity instanceof Player) && damagingLivingEntity instanceof Player) {
            // PvE branch
            retDamage = handlePlayerVersusEnvironmentCalculation(damagedLivingEntity, (Player) damagingLivingEntity,
                    damagingEntity, melee);
        } else if (damagedLivingEntity instanceof Player) {
            // EvP branch
            retDamage = handleEnvironmentVersusPlayerCalculation((Player) damagedLivingEntity, damagingLivingEntity,
                    damagingEntity, oldBaseDamage, melee, event);
        } else {
            // EvE branch
            retDamage = handleEnvironmentVersusEnvironmentCalculation(damagedLivingEntity, damagingLivingEntity,
                    damagingEntity, oldBaseDamage, cause);
        }
        return retDamage;
    }

    private double handleEnvironmentVersusEnvironmentCalculation(LivingEntity damagedLivingEntity,
                                                                 LivingEntity damagingLivingEntity,
                                                                 Entity damagingEntity,
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
                                                            boolean melee,
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
            if (melee) {
                if (random.nextDouble() < damagedChampion.getCache().getAttribute(StrifeAttribute.PARRY)) {
                    damagingLivingEntity.damage(damage * 0.2);
                    damagedPlayer.getWorld().playSound(damagedPlayer.getEyeLocation(), Sound.BLOCK_ANVIL_LAND, 1f, 2f);
                    ActionBarMessage.send(damagedPlayer, ChatColor.WHITE + "Parry!");
                    event.setCancelled(true);
                    return 0D;
                }
            } else {
                if (random.nextDouble() < 2 * damagedChampion.getCache().getAttribute(StrifeAttribute.PARRY)) {
                    if (damagingEntity instanceof Projectile) {
                        damagingEntity.remove();
                    }
                    damagedPlayer.getWorld().playSound(damagedPlayer.getEyeLocation(), Sound.BLOCK_ANVIL_LAND, 1f, 2f);
                    ActionBarMessage.send(damagedPlayer, ChatColor.WHITE + "Parry!");
                    event.setCancelled(true);
                    return 0D;
                }
            }
            damage *= 1 - (damagedChampion.getCache().getAttribute(StrifeAttribute.BLOCK));
        }
        damage *= getArmorMult(damagedChampion.getCache().getAttribute(StrifeAttribute.ARMOR), 0);
        return damage;
    }

    private double handlePlayerVersusEnvironmentCalculation(LivingEntity damagedLivingEntity,
                                                            Player damagingPlayer,
                                                            Entity damagingEntity,
                                                            boolean melee) {
        double retDamage;

        // get the champions
        Champion damagingChampion = plugin.getChampionManager().getChampion(damagingPlayer.getUniqueId());

        // ensure that they have the correct caches
        damagingChampion.getWeaponAttributeValues();
        damagingChampion.getCache().recombine();

        // calculating attack speed and velocity
        double attackSpeedMult = 1D;
        double velocityMult = 1D;
        if (melee) {
            double attackSpeed = StrifeAttribute.ATTACK_SPEED.getBaseValue() * (1 / (1 + damagingChampion
                    .getCache().getAttribute(StrifeAttribute.ATTACK_SPEED)));
            long timeLeft = plugin.getAttackSpeedTask().getTimeLeft(damagingPlayer.getUniqueId());
            long timeToSet = Math.round(Math.max(4.0 * attackSpeed, 0D));
            if (timeLeft > 0) {
                attackSpeedMult = Math.max(1.0 - 1.0 * ((timeLeft * 1D) / timeToSet), 0.1);
            }

            retDamage = damagingChampion.getCache().getAttribute(StrifeAttribute.MELEE_DAMAGE) * attackSpeedMult;
        } else {
            velocityMult = Math.min(damagingEntity.getVelocity().lengthSquared() / 9D, 1);
            retDamage = damagingChampion.getCache().getAttribute(StrifeAttribute.RANGED_DAMAGE) * velocityMult;
        }

        // critical damage time!
        double critBonus = retDamage * getCritBonus(damagingChampion.getCache().getAttribute(StrifeAttribute.CRITICAL_RATE),
                damagingChampion.getCache().getAttribute(StrifeAttribute.CRITICAL_DAMAGE), damagingPlayer);

        // overbonus time!
        double overBonus = 0D;
        if (melee) {
            if (attackSpeedMult > 0.94) {
                overBonus = damagingChampion.getCache().getAttribute(StrifeAttribute.OVERCHARGE) * retDamage;
            }
        } else {
            if (velocityMult > 0.94) {
                overBonus = damagingChampion.getCache().getAttribute(StrifeAttribute.OVERCHARGE) * retDamage;
            }
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
                trueDamage += fireDamage * 0.10D;
                damagedLivingEntity.setFireTicks(Math.max(10 + (int) Math.round(fireDamage * 20), damagedLivingEntity.getFireTicks()));
                damagedLivingEntity.getWorld().playSound(damagedLivingEntity.getEyeLocation(), Sound.BLOCK_FIRE_AMBIENT, 1f, 1f);
            }
        }
        if (lightningDamage > 0D) {
            double shockCalc = damagingChampion.getCache().getAttribute(StrifeAttribute.SHOCK_CHANCE) * attackSpeedMult;
            if (random.nextDouble() < shockCalc) {
                trueDamage += lightningDamage * 1.5D;
                damagedLivingEntity.getWorld().playSound(damagedLivingEntity.getEyeLocation(), Sound.ENTITY_LIGHTNING_THUNDER, 1f, 1.5f);
            }
        }
        if (iceDamage > 0D) {
            double freezeCalc = damagingChampion.getCache().getAttribute(StrifeAttribute.FREEZE_CHANCE) * attackSpeedMult;
            if (random.nextDouble() < freezeCalc) {
                retDamage += iceDamage + iceDamage * (damagedLivingEntity.getMaxHealth() / 300);
                damagedLivingEntity.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 5 + (int) iceDamage * 3, 1));
                damagedLivingEntity.getWorld().playSound(damagedLivingEntity.getEyeLocation(), Sound.BLOCK_GLASS_BREAK, 1f, 1f);
            }
        }

        // potion effects mults
        double potionMult = 1D;
        if (damagedLivingEntity.hasPotionEffect(PotionEffectType.WITHER)) {
            potionMult += 0.1D;
        }
        if (damagedLivingEntity.hasPotionEffect(PotionEffectType.DAMAGE_RESISTANCE)) {
            potionMult -= 0.1D;
        }
        if (melee) {
            if (damagingPlayer.hasPotionEffect(PotionEffectType.INCREASE_DAMAGE)) {
                potionMult += 0.1D;
            }
            if (damagingPlayer.hasPotionEffect(PotionEffectType.WEAKNESS)) {
                potionMult -= 0.1D;
            }
        } else {
            double snareChance = damagingChampion.getCache().getAttribute(StrifeAttribute.SNARE_CHANCE);
            if (snareChance > 0) {
                if (random.nextDouble() < snareChance) {
                    damagedLivingEntity.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 30, 5));
                    damagedLivingEntity.getWorld().playSound(damagedLivingEntity.getEyeLocation(), Sound.BLOCK_FIRE_AMBIENT, 1f, 1f);
                }
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
            if (damagingPlayer.getHealth() > 0) {
                damagingPlayer.setHealth(Math.min(damagingPlayer.getHealth() + lifeStolen,
                        damagingPlayer.getMaxHealth()));
            }
        }

        String msg;
        if (damagedLivingEntity.getHealth() > retDamage) {
            msg = "&c&lHealth: &f" + (int) (damagedLivingEntity.getHealth() - retDamage) + "&7/&f" + (int)
                    (damagedLivingEntity.getMaxHealth());
        } else {
            msg = "&c&lHealth: &fDEAD &7/ &fKILLED";
        }
        ActionBarMessage.send(damagingPlayer, msg);

        return retDamage;
    }

    private double handlePlayerVersusPlayerCalculation(Player damagedPlayer,
                                                       Player damagingPlayer,
                                                       Entity damagingEntity,
                                                       boolean melee,
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

        // calculating attack speed and velocity
        double attackSpeedMult = 1D;
        double velocityMult = 1D;
        if (melee) {
            double attackSpeed = StrifeAttribute.ATTACK_SPEED.getBaseValue() * (1 / (1 + damagingChampion
                    .getCache().getAttribute(StrifeAttribute.ATTACK_SPEED)));
            long timeLeft = plugin.getAttackSpeedTask().getTimeLeft(damagingPlayer.getUniqueId());
            long timeToSet = Math.round(Math.max(4.0 * attackSpeed, 0D));
            if (timeLeft > 0) {
                attackSpeedMult = Math.max(1.0 - 1.0 * ((timeLeft * 1D) / timeToSet), 0.1);
            }

            retDamage = damagingChampion.getCache().getAttribute(StrifeAttribute.MELEE_DAMAGE) * attackSpeedMult;
        } else {
            velocityMult = Math.min(damagingEntity.getVelocity().lengthSquared() / 9D, 1);
            retDamage = damagingChampion.getCache().getAttribute(StrifeAttribute.RANGED_DAMAGE) * velocityMult;
        }

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
            if (melee) {
                if (random.nextDouble() < damagedChampion.getCache().getAttribute(StrifeAttribute.PARRY)) {
                    damagingPlayer.damage(retDamage * 0.2 * pvpMult);
                    damagedPlayer.getWorld().playSound(damagedPlayer.getEyeLocation(), Sound.BLOCK_ANVIL_LAND, 1f, 2f);
                    ActionBarMessage.send(damagedPlayer, ChatColor.WHITE + "Parry!");
                    ActionBarMessage.send(damagingPlayer, ChatColor.WHITE + "Parried!");
                    event.setCancelled(true);
                    return 0D;
                }
            } else {
                if (random.nextDouble() < 2 * damagedChampion.getCache().getAttribute(StrifeAttribute.PARRY)) {
                    if (damagingEntity instanceof Projectile) {
                        damagingEntity.remove();
                    }
                    damagedPlayer.getWorld().playSound(damagedPlayer.getEyeLocation(), Sound.BLOCK_ANVIL_LAND, 1f, 2f);
                    ActionBarMessage.send(damagedPlayer, ChatColor.WHITE + "Parry!");
                    ActionBarMessage.send(damagingPlayer, ChatColor.WHITE + "Parried!");
                    event.setCancelled(true);
                    return 0D;
                }
            }
            retDamage *= 1 - (damagedChampion.getCache().getAttribute(StrifeAttribute.BLOCK));
        }

        // critical damage time!
        double critBonus = retDamage * getCritBonus(damagingChampion.getCache().getAttribute(StrifeAttribute.CRITICAL_RATE),
                damagingChampion.getCache().getAttribute(StrifeAttribute.CRITICAL_DAMAGE), damagingPlayer);

        // overbonus time!
        double overBonus = 0D;
        if (melee) {
            if (attackSpeedMult > 0.94) {
                overBonus = damagingChampion.getCache().getAttribute(StrifeAttribute.OVERCHARGE) * retDamage;
            }
        } else {
            if (velocityMult > 0.94) {
                overBonus = damagingChampion.getCache().getAttribute(StrifeAttribute.OVERCHARGE) * retDamage;
            }
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
                trueDamage += fireDamage * 0.10D * (1 - damagedChampion.getCache().getAttribute(StrifeAttribute
                        .RESISTANCE));
                fireDamage *= 1 - damagedChampion.getCache().getAttribute(StrifeAttribute.RESISTANCE);
                damagedPlayer.setFireTicks(Math.max(10 + (int) Math.round(fireDamage * 20), damagedPlayer.getFireTicks()));
                damagedPlayer.playSound(damagedPlayer.getEyeLocation(), Sound.BLOCK_FIRE_AMBIENT, 1f, 1f);
            }
        }
        if (lightningDamage > 0D) {
            double shockCalc = damagingChampion.getCache().getAttribute(StrifeAttribute.SHOCK_CHANCE) * attackSpeedMult;
            if (random.nextDouble() < shockCalc) {
                trueDamage += lightningDamage * 0.75D * (1 - damagedChampion.getCache().getAttribute(StrifeAttribute
                        .RESISTANCE));
                damagedPlayer.getWorld().playSound(damagedPlayer.getEyeLocation(), Sound.ENTITY_LIGHTNING_THUNDER, 1f, 1.5f);
            }
        }
        if (iceDamage > 0D) {
            double freezeCalc = damagingChampion.getCache().getAttribute(StrifeAttribute.FREEZE_CHANCE) * attackSpeedMult;
            if (random.nextDouble() < freezeCalc) {
                retDamage += iceDamage + iceDamage * (damagedPlayer.getMaxHealth() / 300) * (1 - damagedChampion
                        .getCache().getAttribute(StrifeAttribute.RESISTANCE));
                damagedPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 5 + (int) iceDamage * 3, 1));
                damagedPlayer.getWorld().playSound(damagedPlayer.getEyeLocation(), Sound.BLOCK_GLASS_BREAK, 1f, 1f);
            }
        }

        // potion effects mults
        double potionMult = 1D;
        if (damagedPlayer.hasPotionEffect(PotionEffectType.WITHER)) {
            potionMult += 0.1D;
        }
        if (damagedPlayer.hasPotionEffect(PotionEffectType.DAMAGE_RESISTANCE)) {
            potionMult -= 0.1D;
        }
        if (melee) {
            if (damagingPlayer.hasPotionEffect(PotionEffectType.INCREASE_DAMAGE)) {
                potionMult += 0.1D;
            }
            if (damagingPlayer.hasPotionEffect(PotionEffectType.WEAKNESS)) {
                potionMult -= 0.1D;
            }
        } else {
            double snareChance = damagingChampion.getCache().getAttribute(StrifeAttribute.SNARE_CHANCE);
            if (snareChance > 0) {
                if (random.nextDouble() < snareChance) {
                    damagedPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 30, 5));
                    damagedPlayer.getWorld().playSound(damagedPlayer.getEyeLocation(), Sound.BLOCK_FIRE_AMBIENT, 1f, 1f);
                }
            }
        }

        // combine!
        retDamage *= potionMult;
        retDamage *= getArmorMult(damagedChampion.getCache().getAttribute(StrifeAttribute.ARMOR), damagingChampion
                .getCache().getAttribute(StrifeAttribute.ARMOR_PENETRATION));
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

        String msg;
        if (damagedPlayer.getHealth() > retDamage) {
            msg = "&c&lHealth: &f" + (int) (damagedPlayer.getHealth() - retDamage) + "&7/&f" + (int)
                    (damagedPlayer.getMaxHealth());
        } else {
            msg = "&c&lHealth: &fDEAD &7/ &fKILLED";
        }
        ActionBarMessage.send(damagingPlayer, msg);

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

    private double getCritBonus(double rate, double damage, Player a) {
        if (random.nextDouble() < rate) {
            a.getWorld().playSound(a.getEyeLocation(), Sound.ENTITY_GENERIC_BIG_FALL, 2f, 0.8f);
            return damage - 1.0;
        }
        return 0;
    }

}
