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

import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.managers.StrifeMobManager;
import info.faceland.strife.stats.StrifeStat;
import info.faceland.strife.util.DamageUtil;
import org.bukkit.Material;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class HeadDropListener implements Listener {

  private final StrifeMobManager aeManager;

  public HeadDropListener(StrifeMobManager strifeMobManager) {
    this.aeManager = strifeMobManager;
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onEntityDeath(EntityDeathEvent event) {
    if (event.getEntity().getKiller() == null) {
      return;
    }
    StrifeMob pStats = aeManager.getAttributedEntity(event.getEntity().getKiller());
    if (pStats.getStat(StrifeStat.HEAD_DROP) < 1) {
      return;
    }
    if (DamageUtil.rollBool(pStats.getStat(StrifeStat.HEAD_DROP) / 100)) {
      LivingEntity livingEntity = event.getEntity();
      ItemStack skull = null;
      if (livingEntity instanceof Skeleton) {
        if (livingEntity.getType() == EntityType.SKELETON) {
          skull = new ItemStack(Material.SKELETON_SKULL);
        }
      } else if (livingEntity instanceof Zombie) {
        skull = new ItemStack(Material.ZOMBIE_HEAD);
      } else if (livingEntity instanceof Creeper) {
        skull = new ItemStack(Material.CREEPER_HEAD);
      } else if (livingEntity instanceof Player) {
        skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
        skullMeta.setOwningPlayer((Player) event.getEntity());
        skull.setItemMeta(skullMeta);
      }
      if (skull == null) {
        return;
      }
      livingEntity.getWorld().dropItemNaturally(livingEntity.getLocation(), skull);
    }
  }
}