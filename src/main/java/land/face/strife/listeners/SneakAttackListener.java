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
import land.face.strife.data.champion.LifeSkillType;
import land.face.strife.events.SneakAttackEvent;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class SneakAttackListener implements Listener {

  private final StrifePlugin plugin;

  public SneakAttackListener(StrifePlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler(priority = EventPriority.NORMAL)
  public void onSneakAttack(final SneakAttackEvent event) {
    event.getVictim().getEntity().getWorld()
        .playSound(event.getVictim().getEntity().getEyeLocation(),
            Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 1f, 1.5f);
    if (event.getSneakAttackDamage() < event.getVictim().getEntity().getHealth()) {
      plugin.getSneakManager().tempDisableSneak(event.getAttacker().getEntity());
    }
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onSneakAttackExp(final SneakAttackEvent event) {
    if (event.getAttacker().getEntity() instanceof Player) {
      return;
    }
    if (event.getVictim().getEntity() instanceof Player || (!(event.getVictim()
        .getEntity() instanceof Mob))) {
      return;
    }
    if (((Player) event.getAttacker().getEntity()).getGameMode() == GameMode.CREATIVE) {
      return;
    }
    boolean finishingBlow =
        event.getSneakAttackDamage() > event.getVictim().getEntity().getHealth();
    float gainedXp = plugin.getSneakManager()
        .getSneakAttackExp(event.getVictim().getEntity(), event.getSneakSkill(), finishingBlow);
    plugin.getSkillExperienceManager()
        .addExperience((Player) event.getAttacker().getEntity(), LifeSkillType.SNEAK, gainedXp,
            false);
  }
}
