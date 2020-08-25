/**
 * The MIT License Copyright (c) 2015 Teal Cube Games
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package land.face.strife.listeners;

import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.UniqueEntity;
import land.face.strife.data.ability.EntityAbilitySet.TriggerAbilityType;
import land.face.strife.data.effects.EndlessEffect;
import land.face.strife.stats.AbilitySlot;
import land.face.strife.stats.StrifeStat;
import land.face.strife.util.SpecialStatusUtil;
import land.face.strife.util.StatUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

public class DeathListener implements Listener {

  private final StrifePlugin plugin;

  public DeathListener(StrifePlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onEntityDeathEvent(EntityDeathEvent event) {
    if (event instanceof PlayerDeathEvent) {
      plugin.getSoulManager().createSoul((Player) event.getEntity());
      EndlessEffect.cancelEffects(event.getEntity());
      return;
    }

    if (SpecialStatusUtil.isSpawnerMob(event.getEntity())) {
      return;
    }

    if (!plugin.getStrifeMobManager().isTrackedEntity(event.getEntity())) {
      event.setDroppedExp(0);
      return;
    }

    StrifeMob mob = plugin.getStrifeMobManager().getStatMob(event.getEntity());

    plugin.getAbilityManager().abilityCast(mob, TriggerAbilityType.DEATH);

    if (mob.getMaster() != null || (mob.getUniqueEntityId() == null && SpecialStatusUtil
        .isDespawnOnUnload(mob.getEntity()))) {
      event.setDroppedExp(0);
      return;
    }

    int mobLevel = StatUtil.getMobLevel(event.getEntity());
    float exp = plugin.getMonsterManager().getBaseExp(event.getEntity(), mobLevel);

    UniqueEntity uniqueEntity = plugin.getUniqueEntityManager().getUnique(mob.getUniqueEntityId());
    if (mob.getUniqueEntityId() != null) {
      exp *= uniqueEntity.getExperienceMultiplier();
      exp += uniqueEntity.getBonusExperience();
    }
    exp *= 1 + mob.getStat(StrifeStat.XP_GAIN) / 100;
    event.setDroppedExp((int) exp);
  }

  @EventHandler(priority = EventPriority.LOW)
  public void onEntityDeathClearIconsAndStrifeMobs(final EntityDeathEvent event) {
    plugin.getAbilityManager().unToggleAll(event.getEntity());
    if (event.getEntity() instanceof Player) {
      plugin.getAbilityManager().savePlayerCooldowns((Player) event.getEntity());
      plugin.getAbilityIconManager().removeIconItem((Player) event.getEntity(), AbilitySlot.SLOT_A);
      plugin.getAbilityIconManager().removeIconItem((Player) event.getEntity(), AbilitySlot.SLOT_B);
      plugin.getAbilityIconManager().removeIconItem((Player) event.getEntity(), AbilitySlot.SLOT_C);
    } else {
      Bukkit.getScheduler().runTaskLater(plugin,
          () -> plugin.getStrifeMobManager().removeStrifeMob(event.getEntity()), 2L);
    }
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onEntityDeathClearData(final EntityDeathEvent event) {
    if (event.getEntity().getKiller() != null) {
      plugin.getBossBarManager().doBarDeath(event.getEntity().getKiller());
    }
    plugin.getBarrierManager().removeEntity(event.getEntity());
    plugin.getRageManager().clearRage(event.getEntity().getUniqueId());
    plugin.getBleedManager().clearBleed(event.getEntity().getUniqueId());
    plugin.getSpawnerManager().addRespawnTime(event.getEntity());
    plugin.getCounterManager().clearCounters(event.getEntity());
  }
}
