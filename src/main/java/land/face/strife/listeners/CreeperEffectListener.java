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
import org.bukkit.entity.Creeper;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class CreeperEffectListener implements Listener {

  private StrifePlugin plugin;

  public CreeperEffectListener(StrifePlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void creeperExplosionAdditionalEffects(EntityDamageByEntityEvent event) {
    if (event.isCancelled() || !(event.getDamager() instanceof Creeper)) {
      return;
    }
    if (!(event.getEntity() instanceof LivingEntity)) {
      return;
    }

    Creeper creeper = (Creeper) event.getDamager();
    LivingEntity target = (LivingEntity) event.getEntity();

    if (plugin.getBleedManager().isBleeding(creeper)) {
      float amount = plugin.getBleedManager().getBleedOnEntity(creeper);
      plugin.getBleedManager()
          .addBleed(plugin.getStrifeMobManager().getStatMob(target), amount + 5);
    }
    if (plugin.getCorruptionManager().isCorrupted((LivingEntity) event.getDamager())) {
      float amount = plugin.getCorruptionManager().getCorruption((LivingEntity) event.getDamager());
      plugin.getCorruptionManager().applyCorruption((LivingEntity) event.getEntity(), amount + 10);
    }
    if (event.getDamager().getFireTicks() > 0) {
      int ticks = event.getDamager().getFireTicks();
      event.getEntity().setFireTicks(Math.max(event.getEntity().getFireTicks(), ticks + 40));
    }
  }
}
