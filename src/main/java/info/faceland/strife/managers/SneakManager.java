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
import org.bukkit.entity.Creature;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.util.Vector;

public class SneakManager {

  private final Map<UUID, Integer> sneakTickMap = new ConcurrentHashMap<>();
  // Disable duration is in half seconds
  private final static int SNEAK_DISABLE_DURATION = 6;

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

  public boolean isMeleeSneakAttack(LivingEntity attacker, LivingEntity target) {
    if (!(attacker instanceof Player) || !((Player) attacker).isSneaking()) {
      return false;
    }
    return target instanceof Creature && ((Creature) target).getTarget() == null;
  }

  public boolean isProjectileSneakAttack(Projectile projectile, LivingEntity target) {
    if (!projectile.hasMetadata(SNEAK_ATTACK_META)) {
      return false;
    }
    if (!(target instanceof Creature) || ((Creature) target).getTarget() != null) {
      return false;
    }
    Vector entitySightVector = target.getLocation().getDirection();
    float angle = entitySightVector.angle(projectile.getLocation().getDirection());
    return angle > 0.6;
  }

  public float getSneakActionExp(float enemyLevel, float sneakLevel, float distMult) {
    double levelPenaltyMult = 1;
    if (enemyLevel + 10 < sneakLevel * 2) {
      levelPenaltyMult = Math.max(0.1, 1 - (0.15 * ((sneakLevel*2)-(enemyLevel+10))));
    }
    float gainedXp = 0.1f + enemyLevel / 10f;
    gainedXp *= 1 - distMult;
    return gainedXp * (float)levelPenaltyMult;
  }

  public float getSneakAttackExp(LivingEntity victim, float sneakLevel, boolean finishingBlow) {
    double victimLevel = StatUtil.getMobLevel(victim);
    double levelPenaltyMult = 1;
    if (victimLevel + 10 < sneakLevel * 2) {
      levelPenaltyMult = Math.max(0.1, 1 - (0.15 * ((sneakLevel*2)-(victimLevel+10))));
    }
    float gainedXp = 1f + (float) (victimLevel / 10f);
    if (finishingBlow) {
      gainedXp *= 2;
    }
    return gainedXp * (float)levelPenaltyMult;
  }
}
