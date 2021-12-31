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

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.util.ItemUtil;
import land.face.strife.util.ProjectileUtil;
import org.bukkit.entity.Player;
import org.bukkit.entity.ShulkerBullet;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public record SwingListener(StrifePlugin plugin) implements Listener {

  private static final Set<UUID> FAKE_SWINGS = new HashSet<>();

  @EventHandler(priority = EventPriority.LOWEST)
  public void onSwingLowest(PlayerInteractEvent event) {
    if (FAKE_SWINGS.contains(event.getPlayer().getUniqueId())) {
      FAKE_SWINGS.remove(event.getPlayer().getUniqueId());
      event.setCancelled(true);
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

  @EventHandler(priority = EventPriority.NORMAL)
  public void onSwingLeft(PlayerInteractEvent event) {
    if (event.useItemInHand() == Result.DENY || event.getHand() == EquipmentSlot.OFF_HAND) {
      return;
    }
    if (event.getAction() == LEFT_CLICK_AIR || event.getAction() == LEFT_CLICK_BLOCK) {
      StrifeMob mob = plugin.getStrifeMobManager().getStatMob(event.getPlayer());
      if (ItemUtil.isTool(event.getPlayer().getEquipment().getItemInMainHand())) {
        return;
      }
      double attackMult = plugin.getAttackSpeedManager().getAttackMultiplier(mob);
      if (ItemUtil.isWandOrStaff(event.getPlayer().getEquipment().getItemInMainHand())) {
        if (attackMult > 0.08) {
          ProjectileUtil.shootWand(mob, Math.pow(attackMult, 1.2D));
        }
        event.setCancelled(true);
      }
    }
  }

  public static void addFakeSwing(UUID uuid) {
    FAKE_SWINGS.add(uuid);
  }
}
