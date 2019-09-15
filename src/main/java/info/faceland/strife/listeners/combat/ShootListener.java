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
import static info.faceland.strife.util.ProjectileUtil.createTrident;

import info.faceland.strife.StrifePlugin;
import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.data.ability.EntityAbilitySet.TriggerAbilityType;
import info.faceland.strife.stats.StrifeStat;
import info.faceland.strife.util.ProjectileUtil;
import java.util.Random;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;

public class ShootListener implements Listener {

  private final StrifePlugin plugin;
  private final Random random;

  public ShootListener(StrifePlugin plugin) {
    this.plugin = plugin;
    this.random = new Random(System.currentTimeMillis());
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onEntityShootAbility(ProjectileLaunchEvent event) {
    if (!(event.getEntity().getShooter() instanceof LivingEntity)) {
      return;
    }
    StrifeMob mob = plugin.getStrifeMobManager()
        .getStatMob((LivingEntity) event.getEntity().getShooter());
    if (mob.getAbilitySet() == null) {
      return;
    }
    if (mob.getAbilitySet().getAbilities(TriggerAbilityType.SHOOT) == null) {
      return;
    }
    boolean triggered = plugin.getAbilityManager().abilityCast(mob, TriggerAbilityType.SHOOT);
    if (triggered) {
      event.setCancelled(true);
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

    event.setCancelled(true);

    Player player = (Player) event.getEntity().getShooter();
    StrifeMob pStats = plugin.getStrifeMobManager().getStatMob(player);
    double attackMultiplier = plugin.getAttackSpeedManager().getAttackMultiplier(pStats);
    attackMultiplier = Math.pow(attackMultiplier, 1.5D);

    if (attackMultiplier <= 0.05) {
      event.setCancelled(true);
      return;
    }

    plugin.getChampionManager().updateEquipmentStats(
        plugin.getChampionManager().getChampion(player));

    float projectileSpeed = 2.5f * (1 + (pStats.getStat(StrifeStat.PROJECTILE_SPEED) / 100));
    int projectiles = ProjectileUtil.getTotalProjectiles(1, pStats.getStat(MULTISHOT));

    for (int i = projectiles; i > 0; i--) {
      createArrow(player, attackMultiplier, projectileSpeed, randomOffset(projectiles),
          randomOffset(projectiles), randomOffset(projectiles));
    }
    player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ARROW_SHOOT, 1f, 1f);
    plugin.getSneakManager().tempDisableSneak(player);
  }

  @EventHandler(priority = EventPriority.HIGH)
  public void onPlayerTridentThrow(ProjectileLaunchEvent event) {
    if (!(event.getEntity().getShooter() instanceof Player)) {
      return;
    }
    if (!(event.getEntity() instanceof Trident)) {
      return;
    }

    event.setCancelled(true);

    Player player = (Player) event.getEntity().getShooter();
    StrifeMob pStats = plugin.getStrifeMobManager().getStatMob(player);
    float attackMultiplier = plugin.getAttackSpeedManager().getAttackMultiplier(pStats);
    attackMultiplier = (float) Math.pow(attackMultiplier, 1.5D);

    ((Player) event.getEntity().getShooter()).setCooldown(Material.TRIDENT, 50);

    if (attackMultiplier <= 0.05) {
      event.setCancelled(true);
      return;
    }

    plugin.getChampionManager().updateEquipmentStats(
        plugin.getChampionManager().getChampion(player));

    double speedMult = 1 + (pStats.getStat(StrifeStat.PROJECTILE_SPEED) / 100);

    createTrident((Player) event.getEntity().getShooter(), (Trident) event.getEntity(),
        attackMultiplier, speedMult);

    player.getWorld().playSound(player.getLocation(), Sound.ITEM_TRIDENT_THROW, 1f, 1f);
    plugin.getSneakManager().tempDisableSneak(player);
  }

  private double randomOffset(double magnitude) {
    magnitude = 0.11 + magnitude * 0.005;
    return (random.nextDouble() * magnitude * 2) - magnitude;
  }
}