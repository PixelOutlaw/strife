/**
 * The MIT License Copyright (c) 2015 Teal Cube Games
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package info.faceland.strife.listeners.combat;

import static org.bukkit.event.block.Action.LEFT_CLICK_AIR;
import static org.bukkit.event.block.Action.LEFT_CLICK_BLOCK;

import com.tealcube.minecraft.bukkit.TextUtils;
import gyurix.spigotlib.ChatAPI;
import info.faceland.strife.StrifePlugin;
import info.faceland.strife.attributes.StrifeAttribute;
import info.faceland.strife.data.AttributedEntity;
import info.faceland.strife.util.ItemUtil;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.ShulkerBullet;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import java.util.Random;

public class SwingListener implements Listener {

  private final StrifePlugin plugin;
  private final Random random;
  private final Set<Material> ignoredMaterials;

  private static final String ATTACK_UNCHARGED = TextUtils.color("&e&lNot charged enough!");

  public SwingListener(StrifePlugin plugin) {
    this.plugin = plugin;
    this.random = new Random(System.currentTimeMillis());
    this.ignoredMaterials = new HashSet<>();
    this.ignoredMaterials.add(Material.AIR);
    this.ignoredMaterials.add(Material.LONG_GRASS);
    this.ignoredMaterials.add(Material.WALL_SIGN);
    this.ignoredMaterials.add(Material.SIGN_POST);
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onGhastBallHit(EntityDamageByEntityEvent event) {
    if (!(event.getEntity() instanceof Fireball)) {
      return;
    }
    Fireball fireball = (Fireball) event.getEntity();
    if (fireball.getShooter() instanceof Ghast) {
      return;
    }
    if (event.getDamager() instanceof Projectile) {
      event.getDamager().remove();
    }
    event.setCancelled(true);
  }

  @EventHandler(priority = EventPriority.NORMAL)
  public void onSwing(PlayerInteractEvent event) {
    if (event.getHand() != EquipmentSlot.HAND) {
      return;
    }
    if (event.getClickedBlock() != null && event.getClickedBlock().getType().isItem()) {
      return;
    }
    if (!(event.getAction() == LEFT_CLICK_AIR || event.getAction() == LEFT_CLICK_BLOCK)) {
      return;
    }
    if (ItemUtil.isWand(event.getPlayer().getEquipment().getItemInMainHand())) {
      shootWand(event.getPlayer(), event);
    } else {
      doMeleeSwing(event.getPlayer(), event, true);
    }
    plugin.getAttributeUpdateManager().updateAttackSpeed(
        plugin.getAttributedEntityManager().getAttributedEntity(event.getPlayer()));
  }

  @EventHandler(priority = EventPriority.NORMAL)
  public void onEnemyHit(EntityDamageByEntityEvent event) {
    if (!(event.getDamager() instanceof Player)) {
      return;
    }
    if (ItemUtil.isWand(((Player) event.getDamager()).getEquipment().getItemInMainHand())) {
      shootWand((Player) event.getDamager(), event);
    } else {
      doMeleeSwing((Player) event.getDamager(), event, false);
    }
  }

  private void doMeleeSwing(Player player, Cancellable event, boolean resetAttack) {
    AttributedEntity attacker = plugin.getAttributedEntityManager().getAttributedEntity(player);

    double attackMultiplier = plugin.getAttackSpeedManager()
        .getAttackMultiplier(attacker, resetAttack);

    double range = attacker.getAttribute(StrifeAttribute.SPELL_STRIKE_RANGE);
    if (attacker.getAttribute(StrifeAttribute.SPELL_STRIKE_RANGE) < 0.5) {
      return;
    }
    if (attackMultiplier < 0.95) {
      return;
    }
    LivingEntity target = selectFirstEntityInSight(player, range);
    if (target == null) {
      return;
    }
    //AttributedEntity defender = plugin.getAttributedEntityManager().getAttributedEntity(target);
    spawnSparkle(target);
    event.setCancelled(true);
  }

  private void shootWand(Player player, Cancellable event) {
    AttributedEntity pStats = plugin.getAttributedEntityManager().getAttributedEntity(player);
    double attackMultiplier = plugin.getAttackSpeedManager().getAttackMultiplier(pStats);
    attackMultiplier = Math.pow(attackMultiplier, 1.5D);

    if (attackMultiplier < 0.1) {
      ChatAPI.sendJsonMsg(ChatAPI.ChatMessageType.ACTION_BAR, ATTACK_UNCHARGED, player);
      player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_AMBIENT, 0.5f, 2.0f);
      event.setCancelled(true);
      return;
    }

    plugin.getChampionManager().updateEquipmentAttributes(
        plugin.getChampionManager().getChampion(player));

    double projectileSpeed = 1 + (pStats.getAttribute(StrifeAttribute.PROJECTILE_SPEED) / 100);
    double multiShot = pStats.getAttribute(StrifeAttribute.MULTISHOT) / 100;

    if (pStats.getAttribute(StrifeAttribute.EXPLOSION_MAGIC) > 0.1) {
      createGhastBall(player, attackMultiplier, projectileSpeed, multiShot);
      event.setCancelled(true);
      return;
    }

    createMagicMissile(player, attackMultiplier, projectileSpeed, 0, 0, 0);

    if (multiShot > 0) {
      int bonusProjectiles = (int) (multiShot - (multiShot % 1));
      if (multiShot % 1 >= random.nextDouble()) {
        bonusProjectiles++;
      }
      for (int i = bonusProjectiles; i > 0; i--) {
        createMagicMissile(player, attackMultiplier, projectileSpeed,
            randomOffset(bonusProjectiles), randomOffset(bonusProjectiles),
            randomOffset(bonusProjectiles));
      }
    }
    event.setCancelled(true);
  }

  private void createMagicMissile(LivingEntity shooter, double attackMult, double power,
      double xOff, double yOff, double zOff) {
    shooter.getWorld().playSound(shooter.getLocation(), Sound.ENTITY_BLAZE_HURT, 0.7f, 2f);
    ShulkerBullet magicProj = shooter.getWorld()
        .spawn(shooter.getEyeLocation().clone().add(0, -0.5, 0), ShulkerBullet.class);
    magicProj.setShooter(shooter);

    Vector vec = shooter.getLocation().getDirection();
    xOff = vec.getX() * power + xOff;
    yOff = vec.getY() * power + yOff + 0.25;
    zOff = vec.getZ() * power + zOff;
    magicProj.setVelocity(new Vector(xOff, yOff, zOff));
    magicProj.setMetadata("AS_MULT", new FixedMetadataValue(plugin, attackMult));
  }

  private void createGhastBall(LivingEntity shooter, double attackMult, double power,
      double radius) {
    shooter.getWorld().playSound(shooter.getLocation(), Sound.ENTITY_GHAST_SHOOT, 0.7f, 1.1f);
    Fireball fireball = shooter.getWorld()
        .spawn(shooter.getEyeLocation().clone().add(0, -0.5, 0), Fireball.class);
    fireball.setShooter(shooter);
    fireball.setBounce(false);
    fireball.setIsIncendiary(false);
    fireball.setYield((float) (2 + radius * 0.5));

    Vector vec = shooter.getLocation().getDirection().multiply(0.05 * power);
    fireball.setVelocity(vec);
    fireball.setMetadata("AS_MULT", new FixedMetadataValue(plugin, attackMult));
  }

  private double randomOffset(double magnitude) {
    magnitude = 0.12 + magnitude * 0.005;
    return (random.nextDouble() * magnitude * 2) - magnitude;
  }

  private LivingEntity selectFirstEntityInSight(Player player, double range) {
    ArrayList<Entity> entities = (ArrayList<Entity>) player.getNearbyEntities(range, range, range);
    ArrayList<Block> sightBlock = (ArrayList<Block>) player
        .getLineOfSight(ignoredMaterials, (int) range);
    ArrayList<Location> sight = new ArrayList<>();
    for (Block b : sightBlock) {
      sight.add(b.getLocation());
    }
    for (Location loc : sight) {
      for (Entity entity : entities) {
        if (!(entity instanceof LivingEntity)) {
          continue;
        }
        if (Math.abs(entity.getLocation().getX() - loc.getX()) < 1 &&
            Math.abs(entity.getLocation().getY() - loc.getY()) < 1 &&
            Math.abs(entity.getLocation().getZ() - loc.getZ()) < 1) {
          return (LivingEntity) entity;
        }
      }
    }
    return null;
  }

  private void spawnSparkle(LivingEntity target) {
    Location location = target.getLocation().clone().add(
        target.getEyeLocation().clone().subtract(target.getLocation()).multiply(0.5));
    location.getWorld().spawnParticle(Particle.CRIT_MAGIC, location, 20, 5, 5, 5);
    location.getWorld().spawnParticle(Particle.SWEEP_ATTACK, location, 1);
    location.getWorld().playSound(location, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 2.0f, 1.0f);
    location.getWorld().playSound(location, Sound.ENTITY_BLAZE_HURT, 2.0f, 1.0f);
  }
}
