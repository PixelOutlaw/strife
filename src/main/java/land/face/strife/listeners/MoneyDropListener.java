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

import com.tealcube.minecraft.bukkit.bullion.GoldDropEvent;
import com.tealcube.minecraft.bukkit.bullion.PlayerDeathDropEvent;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.stats.StrifeStat;
import land.face.strife.util.SpecialStatusUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public record MoneyDropListener(StrifePlugin plugin) implements Listener {

  @EventHandler(priority = EventPriority.MONITOR)
  public void onEntityDeathEvent(PlayerDeathDropEvent event) {
    StrifeMob mob = plugin.getStrifeMobManager().getStatMob(event.getVictim());
    if (mob.diedFromPvp()) {
      event.setCancelled(true);
    }
    if (event.getAmountProtected() <= 0) {
      return;
    }
    double multiplier = 1 + mob.getStat(StrifeStat.MONEY_KEPT) / 100;
    event.setAmountProtected(event.getAmountProtected() * multiplier);
  }

  @EventHandler(priority = EventPriority.NORMAL)
  public void onGoldDrop(GoldDropEvent event) {
    // Do not drop for non-uniques
    if (StringUtils.isBlank(SpecialStatusUtil.getUniqueId(event.getLivingEntity()))) {
      event.setCancelled(true);
      return;
    }
    if (SpecialStatusUtil.isGuildMob(event.getLivingEntity()) || event.getKiller() == null) {
      event.setCancelled(true);
      return;
    }
    StrifeMob victimMob = plugin.getStrifeMobManager().getStatMob(event.getLivingEntity());
    if (victimMob.getMaster() != null) {
      event.setCancelled(true);
      return;
    }
    StrifeMob killerMob = plugin.getStrifeMobManager().getStatMob(event.getKiller());
    float bonus = victimMob.getStat(StrifeStat.GOLD_FIND) + killerMob.getStat(StrifeStat.GOLD_FIND);
    event.setAmount(event.getAmount() * (1 + bonus / 100));
  }
}
