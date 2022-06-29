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

import static org.bukkit.event.block.Action.LEFT_CLICK_AIR;
import static org.bukkit.event.block.Action.LEFT_CLICK_BLOCK;

import io.papermc.paper.event.player.PlayerArmSwingEvent;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.util.ItemUtil;
import land.face.strife.util.ProjectileUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.entity.ShulkerBullet;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public record SwingListener(StrifePlugin plugin) implements Listener {

  private static final Set<UUID> FAKE_SWINGS = new HashSet<>();

  @EventHandler(priority = EventPriority.LOWEST)
  public void onInteractLowest(PlayerInteractEvent event) {
    if (FAKE_SWINGS.contains(event.getPlayer().getUniqueId())) {
      event.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onDamageLowest(EntityDamageByEntityEvent event) {
    if (FAKE_SWINGS.contains(event.getDamager().getUniqueId())) {
      event.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onDamageLowest(PlayerArmSwingEvent event) {
    if (FAKE_SWINGS.contains(event.getPlayer().getUniqueId())) {
      event.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onSwingLowest(PlayerAnimationEvent event) {
    if (FAKE_SWINGS.contains(event.getPlayer().getUniqueId())) {
      event.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onSwingMONITOR(PlayerAnimationEvent event) {
    if (FAKE_SWINGS.contains(event.getPlayer().getUniqueId())) {
      event.setCancelled(false);
    }
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onAttackShulkerBullet(EntityDamageByEntityEvent event) {
    if (event.getEntity() instanceof ShulkerBullet && event.getDamager() instanceof Player) {
      if (((ShulkerBullet) event.getEntity()).getShooter() == event.getDamager()) {
        event.setCancelled(true);
      }
    }
  }

  @EventHandler(priority = EventPriority.LOW)
  public void onSwingLeft(PlayerInteractEvent event) {
    if (FAKE_SWINGS.contains(event.getPlayer().getUniqueId())) {
      return;
    }
    if (event.useItemInHand() == Result.DENY || event.getHand() == EquipmentSlot.OFF_HAND) {
      return;
    }
    if (event.getAction() == LEFT_CLICK_AIR || event.getAction() == LEFT_CLICK_BLOCK) {
      StrifeMob mob = plugin.getStrifeMobManager().getStatMob(event.getPlayer());
      if (ItemUtil.isTool(event.getPlayer().getEquipment().getItemInMainHand())) {
        return;
      }
      if (ItemUtil.isWandOrStaff(event.getPlayer().getEquipment().getItemInMainHand())) {
        float attackMult = plugin.getAttackSpeedManager().getAttackMultiplier(mob, 1);
        if (attackMult > 0.1f) {
          ProjectileUtil.shootWand(mob, Math.pow(attackMult, 1.25f));
        }
        event.setCancelled(true);
      } else if (ItemUtil.isMeleeWeapon(event.getMaterial())) {
        plugin.getAttackSpeedManager().resetAttack(mob, 0.5f, false);
      } else {
        plugin.getAttackSpeedManager().resetAttack(mob, 1, false);
      }
    }
  }

  public static void spoofSwing(UUID uuid) {
    FAKE_SWINGS.add(uuid);
  }

  public static void removeSwing(UUID uuid) {
    FAKE_SWINGS.remove(uuid);
  }
}
