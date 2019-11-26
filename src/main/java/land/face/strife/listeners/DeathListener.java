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

import java.util.UUID;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.UniqueEntity;
import land.face.strife.events.UniqueKillEvent;
import land.face.strife.stats.AbilitySlot;
import land.face.strife.stats.StrifeStat;
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
      return;
    }

    StrifeMob mob = plugin.getStrifeMobManager().getMobUnsafe(event.getEntity().getUniqueId());

    if (mob == null) {
      return;
    }

    Player killer = mob.getKiller();
    if (killer == null) {
      killer = event.getEntity().getKiller();
      if (killer == null) {
        return;
      }
    }

    UniqueKillEvent ev = new UniqueKillEvent(mob, killer);
    Bukkit.getPluginManager().callEvent(ev);

    if (mob.getMaster() != null || (mob.getUniqueEntityId() == null && mob.isDespawnOnUnload())) {
      event.setDroppedExp(0);
      event.getDrops().clear();
      return;
    }

    if (event.getEntity().hasMetadata("SPAWNED")) {
      return;
    }

    int killerLevel = killer.getLevel();
    int mobLevel;
    if (killerLevel < 100) {
      int killerRange = Math.max(5, killerLevel / 5);
      mobLevel = Math.min(StatUtil.getMobLevel(event.getEntity()), killerLevel + killerRange);
    } else {
      mobLevel = StatUtil.getMobLevel(event.getEntity());
    }
    int levelDiff = Math.abs(mobLevel - killer.getLevel());
    float levelPenalty = 1;
    if (levelDiff > 6) {
      levelPenalty = Math.max(0.1f, 1 - ((levelDiff - 6) * 0.1f));
    }

    float exp = plugin.getMonsterManager().getBaseExp(event.getEntity(), mobLevel);

    UniqueEntity uniqueEntity = plugin.getUniqueEntityManager().getUnique(mob.getUniqueEntityId());
    if (mob.getUniqueEntityId() != null) {
      exp *= uniqueEntity.getExperienceMultiplier();
      exp += uniqueEntity.getBonusExperience();
    }
    exp *= 1 + mob.getStat(StrifeStat.XP_GAIN) / 100;
    plugin.getExperienceManager().addExperience(killer, (exp * levelPenalty), false);
    event.setDroppedExp(0);
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onEntityDeathClearIconsAndStrifeMobs(final EntityDeathEvent event) {
    if (event.getEntity() instanceof Player) {
      plugin.getAbilityManager().savePlayerCooldowns((Player) event.getEntity());
      plugin.getAbilityIconManager().removeIconItem((Player) event.getEntity(), AbilitySlot.SLOT_A);
      plugin.getAbilityIconManager().removeIconItem((Player) event.getEntity(), AbilitySlot.SLOT_B);
      plugin.getAbilityIconManager().removeIconItem((Player) event.getEntity(), AbilitySlot.SLOT_C);
    } else {
      UUID uuid = event.getEntity().getUniqueId();
      plugin.getStrifeMobManager().doSpawnerDeath(uuid);
      Bukkit.getScheduler().runTaskLater(plugin,
          () -> plugin.getStrifeMobManager().removeMob(uuid), 20L * 30);
    }
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onEntityDeathClearData(final EntityDeathEvent event) {
    plugin.getBossBarManager().doBarDeath(event.getEntity());
    plugin.getBarrierManager().removeEntity(event.getEntity());
    plugin.getRageManager().clearRage(event.getEntity().getUniqueId());
    plugin.getBleedManager().clearBleed(event.getEntity().getUniqueId());
    plugin.getSpawnerManager().addRespawnTime(event.getEntity());
  }
}
