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

import static info.faceland.strife.attributes.StrifeAttribute.MULTISHOT;
import static info.faceland.strife.attributes.StrifeAttribute.PROJECTILE_SPEED;

import info.faceland.strife.StrifePlugin;
import info.faceland.strife.data.AttributedEntity;
import info.faceland.strife.util.ProjectileUtil;
import java.util.Random;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

public class BowListener implements Listener {

  private final StrifePlugin plugin;
  private final Random random;

  public BowListener(StrifePlugin plugin) {
    this.plugin = plugin;
    this.random = new Random(System.currentTimeMillis());
  }

  @EventHandler(priority = EventPriority.HIGH)
  public void onPlayerShoot(ProjectileLaunchEvent event) {
    if (!(event.getEntity().getShooter() instanceof Player)) {
      return;
    }
    if (!(event.getEntity() instanceof Arrow)) {
      return;
    }

    event.setCancelled(true);

    Player playerEntity = (Player) event.getEntity().getShooter();
    AttributedEntity pStats = plugin.getAttributedEntityManager().getAttributedEntity(playerEntity);
    double attackMultiplier = plugin.getAttackSpeedManager().getAttackMultiplier(pStats);
    attackMultiplier = Math.pow(attackMultiplier, 1.5D);

    if (attackMultiplier <= 0.05) {
      event.setCancelled(true);
      return;
    }

    plugin.getChampionManager().updateEquipmentAttributes(
        plugin.getChampionManager().getChampion(playerEntity));

    Location location = event.getEntity().getLocation().clone();
    double projectileSpeed = 2.5 * (1 + pStats.getAttribute(PROJECTILE_SPEED) / 100);

    createArrow(playerEntity, location, attackMultiplier, projectileSpeed);

    double multiShot = pStats.getAttribute(MULTISHOT) / 100;
    if (multiShot > 0) {
      int bonusProjectiles = (int) (multiShot - (multiShot % 1));
      if (multiShot % 1 >= random.nextDouble()) {
        bonusProjectiles++;
      }
      for (int i = bonusProjectiles; i > 0; i--) {
        createArrow(playerEntity, location, attackMultiplier, randomOffset(bonusProjectiles),
            randomOffset(bonusProjectiles), randomOffset(bonusProjectiles), projectileSpeed);
      }
    }
    double bowPitch = 0.9 + random.nextDouble() * 0.2;
    playerEntity.getWorld().playSound(location, Sound.ENTITY_ARROW_SHOOT, 1f, (float) bowPitch);
    plugin.getSneakManager().tempDisableSneak(playerEntity);
  }

  private void createArrow(Player shooter, Location location, double attackMult, double power) {
    createArrow(shooter, location, attackMult, 0, 0, 0, power);
  }

  private void createArrow(Player shooter, Location location, double attackMult, double xOff,
      double yOff, double zOff, double power) {
    Arrow arrow = shooter.getWorld().spawn(location.clone(), Arrow.class);
    arrow.setShooter(shooter);

    Vector vector = shooter.getLocation().getDirection();
    xOff = vector.getX() * power + xOff;
    yOff = vector.getY() * power + 0.19 + yOff;
    zOff = vector.getZ() * power + zOff;
    arrow.setVelocity(new Vector(xOff, yOff, zOff));
    ProjectileUtil.setProjctileAttackSpeedMeta(arrow, attackMult);
    if (shooter.isSneaking()) {
      ProjectileUtil.setProjectileSneakMeta(arrow);
    }
  }

  private double randomOffset(double magnitude) {
    magnitude = 0.11 + magnitude * 0.005;
    return (random.nextDouble() * magnitude * 2) - magnitude;
  }
}