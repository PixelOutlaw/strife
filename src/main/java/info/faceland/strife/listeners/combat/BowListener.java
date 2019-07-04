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

import static info.faceland.strife.stats.StrifeStat.MULTISHOT;
import static info.faceland.strife.util.ProjectileUtil.createArrow;

import info.faceland.strife.StrifePlugin;
import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.stats.StrifeStat;
import info.faceland.strife.stats.StrifeTrait;
import java.util.Random;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;

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

    Player player = (Player) event.getEntity().getShooter();
    StrifeMob pStats = plugin.getStrifeMobManager().getAttributedEntity(player);
    double attackMultiplier = plugin.getAttackSpeedManager().getAttackMultiplier(pStats);
    attackMultiplier = Math.pow(attackMultiplier, 1.5D);

    if (attackMultiplier <= 0.05) {
      event.setCancelled(true);
      return;
    }

    plugin.getChampionManager().updateEquipmentAttributes(
        plugin.getChampionManager().getChampion(player));

    double projectileSpeed = 2.5 + (pStats.getStat(StrifeStat.PROJECTILE_SPEED) / 100);
    double multiShot = pStats.getStat(MULTISHOT) / 100;
    boolean gravity = !pStats.hasTrait(StrifeTrait.NO_GRAVITY_PROJECTILES);

    createArrow(player, attackMultiplier, projectileSpeed, 0, 0, 0, gravity);

    if (multiShot > 0) {
      int bonusProjectiles = (int) (multiShot - (multiShot % 1));
      if (multiShot % 1 >= random.nextDouble()) {
        bonusProjectiles++;
      }
      for (int i = bonusProjectiles; i > 0; i--) {
        createArrow(player, attackMultiplier, projectileSpeed, randomOffset(bonusProjectiles),
            randomOffset(bonusProjectiles), randomOffset(bonusProjectiles), gravity);
      }
    }
    double bowPitch = 0.9 + random.nextDouble() * 0.2;
    player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ARROW_SHOOT, 1f, (float) bowPitch);
    plugin.getSneakManager().tempDisableSneak(player);
  }

  private double randomOffset(double magnitude) {
    magnitude = 0.11 + magnitude * 0.005;
    return (random.nextDouble() * magnitude * 2) - magnitude;
  }
}