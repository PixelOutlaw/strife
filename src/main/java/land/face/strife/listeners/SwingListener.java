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
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.util.ItemUtil;
import land.face.strife.util.ProjectileUtil;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class SwingListener implements Listener {

  private final StrifePlugin plugin;
  private static String notChargedMessage;

  private static Set<UUID> FAKE_SWINGS = new HashSet<>();

  public SwingListener(StrifePlugin plugin) {
    this.plugin = plugin;
    notChargedMessage = plugin.getSettings().getString("language.wand.not-charged", "");
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onSwingLowest(PlayerInteractEvent event) {
    if (FAKE_SWINGS.contains(event.getPlayer().getUniqueId())) {
      FAKE_SWINGS.remove(event.getPlayer().getUniqueId());
      event.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.NORMAL)
  public void onSwingLeft(PlayerInteractEvent event) {
    if (event.useItemInHand() == Result.DENY) {
      return;
    }
    if (event.getHand() == EquipmentSlot.OFF_HAND) {
      return;
    }
    if (event.getAction() == LEFT_CLICK_AIR || event.getAction() == LEFT_CLICK_BLOCK) {
      StrifeMob mob = plugin.getStrifeMobManager().getStatMob(event.getPlayer());
      double attackMult = plugin.getAttackSpeedManager().getAttackMultiplier(mob);
      if (ItemUtil.isWandOrStaff(event.getPlayer().getEquipment().getItemInMainHand())) {
        if (attackMult > 0.2) {
          ProjectileUtil.shootWand(mob, Math.pow(attackMult, 1.5D));
        } else {
          MessageUtils.sendActionBar((Player) mob.getEntity(), notChargedMessage);
          mob.getEntity().getWorld()
              .playSound(mob.getEntity().getLocation(), Sound.ENTITY_BLAZE_AMBIENT, 0.5f, 2.0f);
        }
        event.setCancelled(true);
      }
      plugin.getStatUpdateManager().updateAttackAttrs(
          plugin.getStrifeMobManager().getStatMob(event.getPlayer()));
    }
  }

  public static void addFakeSwing(UUID uuid) {
    FAKE_SWINGS.add(uuid);
  }
}
