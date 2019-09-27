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
package info.faceland.strife.listeners.combat;

import static info.faceland.strife.stats.StrifeStat.HP_ON_KILL;
import static info.faceland.strife.stats.StrifeStat.RAGE_ON_KILL;
import static info.faceland.strife.util.DamageUtil.AttackType;
import static info.faceland.strife.util.DamageUtil.getAttacker;
import static info.faceland.strife.util.DamageUtil.restoreHealthWithPenalties;
import static info.faceland.strife.util.ProjectileUtil.ATTACK_SPEED_META;
import static org.bukkit.event.entity.EntityDamageEvent.DamageModifier.BLOCKING;

import info.faceland.strife.StrifePlugin;
import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.events.StrifeDamageEvent;
import info.faceland.strife.util.DamageUtil;
import info.faceland.strife.util.ItemUtil;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;

public class CombatListener implements Listener {

  private final StrifePlugin plugin;
  private static final Map<LivingEntity, Double> HANDLED_ATTACKS = new HashMap<>();
  private static final Set<Player> FRIENDLY_PLAYER_CHECKER = new HashSet<>();

  public CombatListener(StrifePlugin plugin) {
    this.plugin = plugin;
  }

  public static void addAttack(LivingEntity entity, double damage) {
    HANDLED_ATTACKS.put(entity, damage);
  }

  public static void addPlayer(Player player) {
    FRIENDLY_PLAYER_CHECKER.add(player);
  }

  public static void removePlayer(Player player) {
    FRIENDLY_PLAYER_CHECKER.remove(player);
  }

  public static boolean hasFriendlyPlayer(Player player) {
    return FRIENDLY_PLAYER_CHECKER.contains(player);
  }

  @EventHandler(priority = EventPriority.NORMAL)
  public void handleTNT(EntityDamageByEntityEvent event) {
    if (event.isCancelled()) {
      return;
    }
    if (event.getEntity() instanceof LivingEntity && event.getDamager() instanceof TNTPrimed) {
      double distance = event.getDamager().getLocation().distance(event.getEntity().getLocation());
      double multiplier = Math.max(0.3, 4 / (distance + 3));
      DamageUtil.removeDamageModifiers(event);
      event.setDamage(multiplier * (10 + ((LivingEntity) event.getEntity()).getMaxHealth() * 0.4));
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void strifeDamageHandler(EntityDamageByEntityEvent event) {
    if (event.isCancelled() || event.getCause() == DamageCause.CUSTOM) {
      return;
    }
    if (!(event.getEntity() instanceof LivingEntity) || event.getEntity() instanceof ArmorStand) {
      return;
    }

    boolean blocked = event.isApplicable(BLOCKING) && event.getDamage(BLOCKING) != 0;
    DamageUtil.removeDamageModifiers(event);

    LivingEntity defendEntity = (LivingEntity) event.getEntity();
    LivingEntity attackEntity = getAttacker(event.getDamager());

    if (attackEntity == null) {
      return;
    }

    if (attackEntity instanceof Player && FRIENDLY_PLAYER_CHECKER.contains(attackEntity)) {
      FRIENDLY_PLAYER_CHECKER.remove(attackEntity);
      event.setCancelled(true);
      return;
    }

    if (HANDLED_ATTACKS.containsKey(attackEntity)) {
      event.setDamage(HANDLED_ATTACKS.get(attackEntity));
      HANDLED_ATTACKS.remove(attackEntity);
      return;
    }

    Projectile projectile = null;
    String[] extraEffects = null;

    if (event.getDamager() instanceof Projectile) {
      projectile = (Projectile) event.getDamager();
      if (defendEntity.hasMetadata("NPC")) {
        event.getDamager().remove();
        event.setCancelled(true);
        return;
      }
      if (projectile.hasMetadata("EFFECT_PROJECTILE")) {
        extraEffects = projectile.getMetadata("EFFECT_PROJECTILE").get(0).asString().split("~");
      }
    }

    StrifeMob attacker = plugin.getStrifeMobManager().getStatMob(attackEntity);
    StrifeMob defender = plugin.getStrifeMobManager().getStatMob(defendEntity);

    float attackMultiplier = 1f;
    float healMultiplier = 1f;

    AttackType damageType = DamageUtil.getAttackType(event);

    if (projectile != null && projectile.hasMetadata(ATTACK_SPEED_META)) {
      attackMultiplier = projectile.getMetadata(ATTACK_SPEED_META).get(0).asFloat();
    }

    if (damageType == AttackType.MELEE) {
      if (ItemUtil.isWand(attackEntity.getEquipment().getItemInMainHand())) {
        event.setCancelled(true);
        return;
      }
      attackMultiplier = plugin.getAttackSpeedManager().getAttackMultiplier(attacker);
    } else if (damageType == AttackType.EXPLOSION) {
      double distance = event.getDamager().getLocation().distance(event.getEntity().getLocation());
      attackMultiplier *= Math.max(0.3, 4 / (distance + 3));
      healMultiplier = 0.3f;
    }

    if (attackMultiplier < 0.05 && extraEffects == null) {
      event.setCancelled(true);
      return;
    }

    StrifeDamageEvent strifeDamageEvent = new StrifeDamageEvent(attacker, defender, damageType);
    strifeDamageEvent.setExtraEffects(extraEffects);
    strifeDamageEvent.setHealMultiplier(healMultiplier);
    strifeDamageEvent.setAttackMultiplier(attackMultiplier);
    strifeDamageEvent.setBlocking(blocked);
    strifeDamageEvent.setConsumeEarthRunes(true);
    Bukkit.getPluginManager().callEvent(strifeDamageEvent);

    if (strifeDamageEvent.isCancelled()) {
      event.setCancelled(true);
      return;
    }
    event.setDamage(strifeDamageEvent.getFinalDamage());
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void strifeEntityDeath(EntityDeathEvent event) {
    if (event.getEntity().getKiller() == null) {
      return;
    }
    StrifeMob killer = plugin.getStrifeMobManager().getStatMob(event.getEntity().getKiller());
    if (killer.getStat(HP_ON_KILL) > 0.1) {
      restoreHealthWithPenalties(event.getEntity().getKiller(), killer.getStat(HP_ON_KILL));
    }
    if (killer.getStat(RAGE_ON_KILL) > 0.1) {
      plugin.getRageManager().addRage(killer, killer.getStat(RAGE_ON_KILL));
    }
  }
}
