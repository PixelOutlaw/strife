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
package land.face.strife.listeners;

import static org.bukkit.event.block.Action.LEFT_CLICK_AIR;
import static org.bukkit.event.block.Action.LEFT_CLICK_BLOCK;

import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.stats.StrifeStat;
import land.face.strife.util.ItemUtil;
import land.face.strife.util.ProjectileUtil;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class WandListener implements Listener {

  private final StrifePlugin plugin;
  private static String notChargedMessage;

  public WandListener(StrifePlugin plugin) {
    this.plugin = plugin;
    notChargedMessage = plugin.getSettings().getString("language.wand.not-charged", "");
  }

  @EventHandler(priority = EventPriority.NORMAL)
  public void onSwing(PlayerInteractEvent event) {
    if (event.getHand() != EquipmentSlot.HAND) {
      return;
    }
    if (!(event.getAction() == LEFT_CLICK_AIR || event.getAction() == LEFT_CLICK_BLOCK)) {
      return;
    }
    if (ItemUtil.isWandOrStaff(event.getPlayer().getEquipment().getItemInMainHand())) {
      StrifeMob mob = plugin.getStrifeMobManager().getStatMob(event.getPlayer());
      double attackMult = plugin.getAttackSpeedManager().getAttackMultiplier(mob);
      shootWand(mob, attackMult, event);
    }
    plugin.getStatUpdateManager().updateAttackAttrs(
        plugin.getStrifeMobManager().getStatMob(event.getPlayer()));
  }

  public static void shootWand(StrifeMob strifeMob, double attackMult, Cancellable event) {
    LivingEntity entity = strifeMob.getEntity();
    if (strifeMob.getEntity() instanceof Player) {
      attackMult = Math.pow(attackMult, 1.5D);
      if (attackMult < 0.1) {
        MessageUtils.sendActionBar((Player) entity, notChargedMessage);
        entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_BLAZE_AMBIENT, 0.5f, 2.0f);
        event.setCancelled(true);
        return;
      }
    }

    event.setCancelled(true);

    if (strifeMob.getEntity() instanceof Player) {
      StrifePlugin.getInstance().getChampionManager().updateEquipmentStats(strifeMob.getChampion());
    }
    double projectileSpeed = 1 + (strifeMob.getStat(StrifeStat.PROJECTILE_SPEED) / 100);
    ProjectileUtil.createMagicMissile(entity, attackMult, projectileSpeed, 0, 0, 0);
    int projectiles = ProjectileUtil
        .getTotalProjectiles(1, strifeMob.getStat(StrifeStat.MULTISHOT));

    for (int i = projectiles - 1; i > 0; i--) {
      ProjectileUtil.createMagicMissile(entity, attackMult, projectileSpeed,
          randomOffset(projectiles), randomOffset(projectiles), randomOffset(projectiles));
    }
    entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_BLAZE_HURT, 0.7f, 2f);
    StrifePlugin.getInstance().getSneakManager().tempDisableSneak(entity);
  }

  private static double randomOffset(double magnitude) {
    magnitude = 0.12 + magnitude * 0.005;
    return (Math.random() * magnitude * 2) - magnitude;
  }
}
