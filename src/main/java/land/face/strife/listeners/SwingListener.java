/**
 * The MIT License Copyright (c) 2015 Teal Cube Games
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package land.face.strife.listeners;

import io.papermc.paper.event.player.PlayerArmSwingEvent;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.util.ItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.event.player.PlayerInteractEvent;

public record SwingListener(StrifePlugin plugin) implements Listener {

  public static boolean FAKE_SWING = false;

  @EventHandler(priority = EventPriority.LOWEST)
  public void onInteractLowest(PlayerInteractEvent event) {
    if (FAKE_SWING) {
      event.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onDamageLowest(EntityDamageByEntityEvent event) {
    if (FAKE_SWING &&
        (event.getCause() == DamageCause.ENTITY_ATTACK || event.getCause() == DamageCause.ENTITY_SWEEP_ATTACK)) {
      event.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.LOW)
  public void onSwingLowest(PlayerAnimationEvent event) {
    if (FAKE_SWING) {
      event.setCancelled(true);
      return;
    }
    if (event.getAnimationType() != PlayerAnimationType.ARM_SWING) {
      return;
    }
    if (ItemUtil.isTool(event.getPlayer().getEquipment().getItemInMainHand())) {
      return;
    }
    StrifeMob mob = plugin.getStrifeMobManager().getStatMob(event.getPlayer());
    if (ItemUtil.isMeleeWeapon(event.getPlayer().getEquipment().getItemInMainHand().getType())) {
      plugin.getAttackSpeedManager().resetAttack(mob, 0.5f, false);
      /*
      if (ItemUtil.isDualWield(event.getPlayer().getEquipment())) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
          FAKE_SWING = true;
          event.getPlayer().swingOffHand();
          FAKE_SWING = false;
        }, plugin.getAttackSpeedManager().getOffswingTicks());
      }
      */
    } else {
      plugin.getAttackSpeedManager().resetAttack(mob, 1, false);
    }
  }

  @EventHandler(priority = EventPriority.NORMAL)
  public void onDamageLowest(PlayerArmSwingEvent event) {
    if (FAKE_SWING) {
      event.setCancelled(true);
    }
  }
}
