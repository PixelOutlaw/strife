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

import com.tealcube.minecraft.bukkit.facecore.utilities.ItemUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.champion.LifeSkillType;
import land.face.strife.stats.StrifeStat;
import land.face.strife.util.StatUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class ConsumeItemListener implements Listener {

  private final StrifePlugin plugin;
  private final Map<UUID, Integer> boneTicksLeft = new HashMap<>();

  public ConsumeItemListener(StrifePlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler
  public void onPlayerClick(PlayerInteractEvent event) {
    if (event.getHand() == EquipmentSlot.HAND) {
      ItemStack stack = event.getPlayer().getEquipment().getItemInMainHand();
      if (stack.getType() == Material.BOWL) {
        event.setCancelled(true);
        int data = ItemUtils.getModelData(stack);
        switch (data) {
          case 2000 -> plugin.getSmallBottleMenu().open(event.getPlayer());
          case 2001 -> plugin.getMediumBottleMenu().open(event.getPlayer());
          case 2002 -> plugin.getBigBottleMenu().open(event.getPlayer());
          case 2003 -> plugin.getGiantBottleMenu().open(event.getPlayer());
        }
      }
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onBoneConsume(PlayerInteractEvent event) {
    if (event.isCancelled()) {
      return;
    }
    if (event.getHand() == EquipmentSlot.HAND) {
      if (event.getPlayer().getCooldown(Material.BONE) > 0) {
        return;
      }
      if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
        ItemStack stack = event.getPlayer().getEquipment().getItemInMainHand();
        if (stack.getType() != Material.BONE) {
          return;
        }
        event.setCancelled(true);
        StrifeMob mob = plugin.getStrifeMobManager().getStatMob(event.getPlayer());
        StatUtil.getStat(mob, StrifeStat.MAX_PRAYER_POINTS);
        mob.setPrayer(Math.min(mob.getMaxPrayer(), mob.getPrayer() + (mob.getMaxPrayer() * 0.1f)));
        stack.setAmount(stack.getAmount() - 1);
        event.getPlayer().updateInventory();
        plugin.getPrayerManager().sendPrayerUpdate(event.getPlayer(), mob.getPrayer() / mob.getMaxPrayer(),
            plugin.getPrayerManager().isPrayerActive(event.getPlayer()));
        plugin.getSkillExperienceManager().addExperience(mob, LifeSkillType.PRAYER, 120, false, true);
        event.getPlayer().getWorld().playSound(event.getPlayer().getLocation(), Sound.ENTITY_TURTLE_EGG_CRACK, 1, 1.25f);
        event.getPlayer().setCooldown(Material.BONE, 6000);
      }
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onPlayerClick(PlayerInteractEntityEvent event) {
    if (event.getRightClicked() instanceof Cow) {
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onJoin(final PlayerJoinEvent event) {
    reapplyCooldown(event.getPlayer());
  }

  @EventHandler
  public void onQuitSaveCooldown(final PlayerQuitEvent event) {
    if (event.getPlayer().isOnline() && event.getPlayer().getCooldown(Material.BONE) > 0) {
      boneTicksLeft.put(event.getPlayer().getUniqueId(), event.getPlayer().getCooldown(Material.BONE));
    }
  }

  @EventHandler
  public void onKickSaveCooldown(final PlayerKickEvent event) {
    if (event.getPlayer().isOnline() && event.getPlayer().getCooldown(Material.BONE) > 0) {
      boneTicksLeft.put(event.getPlayer().getUniqueId(), event.getPlayer().getCooldown(Material.BONE));
    }
  }

  @EventHandler
  public void onDeathSaveCooldown(final PlayerDeathEvent event) {
    if (event.getPlayer().isOnline() && event.getEntity().getCooldown(Material.BONE) > 0) {
      boneTicksLeft.put(event.getEntity().getUniqueId(), event.getEntity().getCooldown(Material.BONE));
    }
  }

  private void reapplyCooldown(Player player) {
    if (boneTicksLeft.containsKey(player.getUniqueId())) {
      Bukkit.getScheduler().runTaskLater(StrifePlugin.getInstance(), () ->
          player.setCooldown(Material.BONE, boneTicksLeft.remove(player.getUniqueId())), 2L);
    }
  }
}
