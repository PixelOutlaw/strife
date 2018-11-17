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
package info.faceland.strife.listeners;

import com.tealcube.minecraft.bukkit.TextUtils;
import gyurix.spigotlib.ChatAPI;
import info.faceland.strife.StrifePlugin;
import info.faceland.strife.attributes.StrifeAttribute;
import info.faceland.strife.data.AttributedEntity;
import info.faceland.strife.util.ItemUtil;
import org.bukkit.Sound;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.ShulkerBullet;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import java.util.Random;

public class WandListener implements Listener {

  private final StrifePlugin plugin;
  private final Random random;

  private static final String ATTACK_UNCHARGED = TextUtils.color("&e&lNot charged enough!");

  public WandListener(StrifePlugin plugin) {
    this.plugin = plugin;
    this.random = new Random(System.currentTimeMillis());
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

  @EventHandler(priority = EventPriority.MONITOR)
  public void onSwing(PlayerInteractEvent event) {
    if (event.getAction() != Action.LEFT_CLICK_AIR) {
      return;
    }
    Player playerEntity = event.getPlayer();

    AttributedEntity pStats = plugin.getEntityStatCache().getAttributedEntity(playerEntity);
    double attackMultiplier = plugin.getAttackSpeedManager().getAttackMultiplier(pStats);

    if (attackMultiplier == 0) {
      event.setCancelled(true);
      return;
    }

    ItemStack wand = playerEntity.getEquipment().getItemInMainHand();

    if (!ItemUtil.isWand(wand)) {
      return;
    }

    if (attackMultiplier <= 0.25) {
      ChatAPI.sendJsonMsg(ChatAPI.ChatMessageType.ACTION_BAR, ATTACK_UNCHARGED, playerEntity);
      playerEntity.getWorld()
          .playSound(playerEntity.getLocation(), Sound.ENTITY_BLAZE_AMBIENT, 0.5f, 2.0f);
      event.setCancelled(true);
      return;
    }

    plugin.getChampionManager().updateEquipmentAttributes(
        plugin.getChampionManager().getChampion(playerEntity.getUniqueId()));

    double projectileSpeed = 1 + (pStats.getAttribute(StrifeAttribute.PROJECTILE_SPEED) / 100);
    double multiShot = pStats.getAttribute(StrifeAttribute.MULTISHOT) / 100;

    if (pStats.getAttribute(StrifeAttribute.EXPLOSION_MAGIC) > 0.1) {
      createGhastBall(playerEntity, attackMultiplier, projectileSpeed, multiShot);
      event.setCancelled(true);
      return;
    }

    createMagicMissile(playerEntity, attackMultiplier, projectileSpeed, 0, 0, 0);

    if (multiShot > 0) {
      int bonusProjectiles = (int) (multiShot - (multiShot % 1));
      if (multiShot % 1 >= random.nextDouble()) {
        bonusProjectiles++;
      }
      for (int i = bonusProjectiles; i > 0; i--) {
        createMagicMissile(playerEntity, attackMultiplier, projectileSpeed,
            randomOffset(bonusProjectiles),
            randomOffset(bonusProjectiles), randomOffset(bonusProjectiles));
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
    magnitude = 0.1 + magnitude * 0.005;
    return (random.nextDouble() * magnitude * 2) - magnitude;
  }
}
