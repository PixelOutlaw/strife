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

import info.faceland.strife.attributes.StrifeAttribute;
import info.faceland.strife.data.AttributedEntity;
import info.faceland.strife.managers.AttributedEntityManager;
import info.faceland.strife.util.DamageUtil;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class HeadDropListener implements Listener {

  private final AttributedEntityManager aeManager;

  public HeadDropListener(AttributedEntityManager attributedEntityManager) {
    this.aeManager = attributedEntityManager;
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onEntityDeath(EntityDeathEvent event) {
    if (event.getEntity() == null || event.getEntity().getKiller() == null) {
      return;
    }
    AttributedEntity pStats = aeManager.getAttributedEntity(event.getEntity().getKiller());
    if (pStats.getAttribute(StrifeAttribute.HEAD_DROP) < 1) {
      return;
    }
    if (DamageUtil.rollBool(pStats.getAttribute(StrifeAttribute.HEAD_DROP) / 100)) {
      LivingEntity e = event.getEntity();
      if (e.getType() == EntityType.SKELETON) {
        if (((Skeleton) e).getSkeletonType() == Skeleton.SkeletonType.NORMAL) {
          ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 0);
          e.getWorld().dropItemNaturally(e.getLocation(), skull);
        }
      } else if ((e.getType() == EntityType.ZOMBIE)) {
        ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 2);
        e.getWorld().dropItemNaturally(e.getLocation(), skull);
      } else if ((e.getType() == EntityType.CREEPER)) {
        ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 4);
        e.getWorld().dropItemNaturally(e.getLocation(), skull);
      } else if ((e.getType() == EntityType.PLAYER)) {
        ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
        skullMeta.setOwner(event.getEntity().getName());
        skull.setItemMeta(skullMeta);
        e.getWorld().dropItemNaturally(e.getLocation(), skull);
      }
    }
  }
}