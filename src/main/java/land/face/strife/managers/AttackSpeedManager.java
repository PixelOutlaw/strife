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
package land.face.strife.managers;

import com.sentropic.guiapi.gui.Alignment;
import com.sentropic.guiapi.gui.GUIComponent;
import com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor;
import com.tealcube.minecraft.bukkit.facecore.utilities.PaletteUtil;
import com.tealcube.minecraft.bukkit.facecore.utilities.TitleUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import land.face.strife.StrifePlugin;
import land.face.strife.data.AttackTracker;
import land.face.strife.data.StrifeMob;
import land.face.strife.stats.StrifeTrait;
import land.face.strife.util.StatUtil;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class AttackSpeedManager {

  private final StrifePlugin plugin;

  private final Map<UUID, AttackTracker> lastAttackMap;
  private final float attackCost;
  private final int warnLevel;

  private final String attackFastMsg;

  public AttackSpeedManager(StrifePlugin plugin) {
    this.plugin = plugin;
    this.lastAttackMap = new HashMap<>();
    attackCost = (float) plugin.getSettings().getDouble("config.mechanics.energy.attack-cost", 5);
    warnLevel = plugin.getSettings().getInt("config.mechanics.energy.warn-level", 10) + 1;
    attackFastMsg = plugin.getSettings().getString("language.generic.attack-too-fast");
  }

  public void resetAttack(StrifeMob mob, float ratio, boolean hardReset) {
    resetAttack(mob, ratio, 0, hardReset);
  }

  public void resetAttack(StrifeMob mob, float ratio, float delaySeconds, boolean hardReset) {
    if (mob.getEntity().getType() != EntityType.PLAYER) {
      return;
    }
    plugin.getBlockManager().setGraceTicks(mob.getEntity().getUniqueId());
    float attackSeconds = StatUtil.getAttackTime(mob) * ratio;
    setAttackTime(
        mob.getEntity().getUniqueId(),
        (long) (1000 * attackSeconds),
        (long) delaySeconds * 1000,
        hardReset
    );
    int ticks = (int) Math.max(4, attackSeconds * 20 - 5);
    if (hardReset || ((Player) mob.getEntity()).getCooldown(Material.DIAMOND_CHESTPLATE) < ticks) {
      ((Player) mob.getEntity()).setCooldown(Material.DIAMOND_CHESTPLATE, ticks);
    }
  }

  public void setAttackTime(UUID uuid, long fullAttackMillis, long delay, boolean hardReset) {
    if (!lastAttackMap.containsKey(uuid)) {
      lastAttackMap.put(uuid, new AttackTracker(fullAttackMillis + delay, fullAttackMillis));
      return;
    }
    lastAttackMap.get(uuid).reset(fullAttackMillis, delay, hardReset);
  }

  public void wipeAttackRecord(Player player) {
    plugin.getGuiManager().updateComponent(player,
        new GUIComponent("attack-bar", GuiManager.EMPTY, 0, 0, Alignment.CENTER));
    lastAttackMap.remove(player.getUniqueId());
  }

  public float getAttackMultiplier(StrifeMob attacker, float resetRatio) {
    if (!(attacker.getEntity() instanceof Player)
        || ((Player) attacker.getEntity()).getGameMode() == GameMode.CREATIVE) {
      return 1f;
    }
    long finalAttackTime = (long) (StatUtil.getAttackTime(attacker) * resetRatio * 1000);
    AttackTracker attackTracker;
    float attackMult;
    if (!lastAttackMap.containsKey(attacker.getEntity().getUniqueId())) {
      attackTracker = new AttackTracker(finalAttackTime, 0L);
      lastAttackMap.put(attacker.getEntity().getUniqueId(), attackTracker);
      attackMult = 1f;
    } else {
      attackTracker = lastAttackMap.get(attacker.getEntity().getUniqueId());
      attackMult = attackTracker.getRechargePercent();
    }

    attackTracker.reset(finalAttackTime, 0L, false);
    resetAttack(attacker, resetRatio, false);

    if (attacker.hasTrait(StrifeTrait.NO_ENERGY_BASICS)) {
      return attackMult;
    }

    float energyCost = (0.4f + 0.6f * attackMult) * attackCost;
    float energyMult = Math.min(1, attacker.getEnergy() / energyCost);

    if (resetRatio != -1 && energyMult < 0.9 && ((Player) attacker.getEntity()).getLevel() < warnLevel) {
      TitleUtils.sendTitle((Player) attacker.getEntity(), "  ", FaceColor.ORANGE + "Low Energy!", 10, 0, 8);
      PaletteUtil.sendMessage(attacker.getEntity(), attackFastMsg);
    }

    StatUtil.changeEnergy(attacker, -energyCost);

    return attackMult * energyMult;
  }

  public float getRawMultiplier(UUID uuid) {
    if (lastAttackMap.containsKey(uuid)) {
      return lastAttackMap.get(uuid).getRechargePercent();
    }
    return 1f;
  }
}
