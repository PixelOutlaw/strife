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
package info.faceland.strife.managers;

import static info.faceland.strife.util.ProjectileUtil.SNEAK_ATTACK_META;

import info.faceland.strife.util.StatUtil;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.util.Vector;

public class SneakManager {

  private final Map<UUID, Integer> sneakTickMap = new ConcurrentHashMap<>();
  // Disable duration is in half seconds
  private final static int SNEAK_DISABLE_DURATION = 6;
  private final static float BASE_SNEAK_EXP = 0.2f;
  private final static float SNEAK_EXP_PER_LEVEL = 0.1f;
  private final static float BASE_SNEAK_ATTACK_EXP = 1f;
  private final static float SNEAK_ATTACK_EXP_PER_LEVEL = 0.1f;

  public void tempDisableSneak(Player player) {
    sneakTickMap.put(player.getUniqueId(), SNEAK_DISABLE_DURATION);
  }

  public boolean isUnstealthed(UUID uuid) {
    return sneakTickMap.containsKey(uuid);
  }

  public void tickAll() {
    for (UUID uuid : sneakTickMap.keySet()) {
      if (sneakTickMap.get(uuid) < 1) {
        sneakTickMap.remove(uuid);
        continue;
      }
      sneakTickMap.put(uuid, sneakTickMap.get(uuid) - 1);
    }
  }

  public boolean isSneakAttack(LivingEntity attacker, LivingEntity target) {
    if (!(attacker instanceof Player) || !((Player) attacker).isSneaking()) {
      return false;
    }
    return target instanceof Monster && ((Mob) target).getTarget() == null;
  }

  public boolean isSneakAttack(Projectile projectile, LivingEntity target) {
    if (!projectile.hasMetadata(SNEAK_ATTACK_META)) {
      return false;
    }
    if (!(target instanceof Monster) || ((Mob) target).getTarget() != null) {
      return false;
    }
    Vector entitySightVector = target.getLocation().getDirection();
    float angle = entitySightVector.angle(projectile.getLocation().getDirection());
    return angle > 0.6;
  }

  public float getSneakActionExp(float enemyLevel, float sneakLevel) {
    float levelPenaltyMult = 1;
    if (enemyLevel + 10 < sneakLevel * 2) {
      levelPenaltyMult = (float) Math.max(0.1, 1 - (0.15 * ((sneakLevel*2)-(enemyLevel+10))));
    }
    return (BASE_SNEAK_EXP + enemyLevel * SNEAK_EXP_PER_LEVEL) * levelPenaltyMult;
  }

  public float getSneakAttackExp(LivingEntity victim, float sneakLevel, boolean finishingBlow) {
    float victimLevel = StatUtil.getMobLevel(victim);
    float levelPenaltyMult = 1;
    if (victimLevel + 10 < sneakLevel * 2) {
      levelPenaltyMult = (float) Math.max(0.1, 1 - (0.15 * ((sneakLevel*2)-(victimLevel+10))));
    }
    float gainedXp = BASE_SNEAK_ATTACK_EXP + victimLevel * SNEAK_ATTACK_EXP_PER_LEVEL;
    if (finishingBlow) {
      gainedXp *= 2;
    }
    return gainedXp * levelPenaltyMult;
  }
}
