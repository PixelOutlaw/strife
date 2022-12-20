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

import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.util.DamageUtil;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public class CreeperExplodeListener implements Listener {

  private final StrifePlugin plugin;

  public CreeperExplodeListener(StrifePlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler(ignoreCancelled = true)
  public void spawnerCreeperExplode(EntityExplodeEvent event) {
    if (event.getEntity() instanceof Creeper) {
      plugin.getSpawnerManager().addRespawnTime((LivingEntity) event.getEntity());
    }
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void creeperExplosionAdditionalEffects(EntityDamageByEntityEvent event) {
    if (event.isCancelled() || !(event.getDamager() instanceof Creeper creeper)) {
      return;
    }
    if (!(event.getEntity() instanceof LivingEntity target)) {
      return;
    }

    StrifeMob mob = plugin.getStrifeMobManager().getStatMob((Creeper) event.getDamager());
    StrifeMob victim = plugin.getStrifeMobManager().getStatMob((LivingEntity) event.getEntity());
    if (mob.isBleeding()) {
      float amount = mob.getBleed();
      DamageUtil.applyBleed(mob, victim, amount + 5, false);
    }
    if (mob.getCorruption() > 0) {
      victim.addCorruption(mob.getCorruption() * 0.5f + 20);
    }
    if (mob.getFrost() > 0) {
      victim.addFrost((int) (10 + mob.getFrost() * 0.5f));
    }
    if (event.getDamager().getFireTicks() > 0) {
      int ticks = event.getDamager().getFireTicks();
      event.getEntity().setFireTicks(Math.max(event.getEntity().getFireTicks(), ticks + 40));
    }
  }
}
