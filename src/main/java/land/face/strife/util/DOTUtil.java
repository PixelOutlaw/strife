package land.face.strife.util;

import static land.face.strife.util.UberConfig.BLEED_FLAT;
import static land.face.strife.util.UberConfig.BLEED_PERCENT;
import static land.face.strife.util.UberConfig.FIRE_DAMAGE_MAP;
import static land.face.strife.util.UberConfig.POISON_DAMAGE_MAP;
import static org.bukkit.potion.PotionEffectType.POISON;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.buff.LoadedBuff;
import land.face.strife.stats.StrifeStat;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

public class DOTUtil {

  private static StrifePlugin plugin;
  private static final ItemStack BLOCK_DATA = new ItemStack(Material.REDSTONE);

  private static LoadedBuff LAVA_DEBUFF;

  public static void refresh(StrifePlugin refreshedPlugin) {
    plugin = refreshedPlugin;

    Map<StrifeStat, Float> debuffMap = new HashMap<>();
    debuffMap.put(StrifeStat.BURNING_RESIST, -20f);
    LAVA_DEBUFF = new LoadedBuff("BUILT-IN-LAVA-DEBUFF", debuffMap, "", -1, null, 20, 3, false);
  }

  public static float tickBleedDamage(StrifeMob mob) {
    float amount = mob.getBleed() * BLEED_PERCENT + BLEED_FLAT;
    mob.setBleed(Math.max(0, mob.getBleed() - amount));
    if (mob.isInvincible()) {
      return 0;
    }
    spawnBleedParticles(mob.getEntity(), amount);
    return amount;
  }

  public static float getPoisonDamage(StrifeMob mob) {
    if (mob.isInvincible()) {
      return 0;
    }
    LivingEntity le = mob.getEntity();
    int poisonPower = Objects.requireNonNull(le.getPotionEffect(POISON)).getAmplifier() + 1;
    float damage = poisonPower * POISON_DAMAGE_MAP.getOrDefault(mob.getLevel(), POISON_DAMAGE_MAP.get(300));
    damage *= 1 - mob.getStat(StrifeStat.POISON_RESIST) / 100;
    return damage;
  }

  public static float getFireDamage(StrifeMob mob) {
    if (mob.isInvincible()) {
      return 0;
    }
    float damage = FIRE_DAMAGE_MAP.getOrDefault(mob.getLevel(), FIRE_DAMAGE_MAP.get(300));
    damage *= 1 - mob.getStat(StrifeStat.BURNING_RESIST) / 100;
    damage *= 1 - mob.getStat(StrifeStat.FIRE_RESIST) / 100;
    return damage;
  }

  public static void doLava(LivingEntity le) {
    if (le.getFireTicks() > 0 && le.getWorld().getBlockAt(le.getLocation()).getType() == Material.LAVA) {
      StrifeMob mob = plugin.getStrifeMobManager().getStatMob(le);
      mob.addBuff(LAVA_DEBUFF, null, 10);
    }
  }

  public static void spawnBleedParticles(LivingEntity entity, double damage) {
    int particleAmount = Math.min(3 + (int) (damage * 10), 40);
    entity.getWorld().spawnParticle(
        Particle.ITEM,
        entity.getEyeLocation().clone().add(0, -entity.getEyeHeight() / 2, 0),
        particleAmount,
        0.0, 0.0, 0.0,
        0.15,
        BLOCK_DATA
    );
  }

  public static void spawnInstantBleedParticles(LivingEntity entity) {
    entity.getWorld().spawnParticle(
        Particle.ITEM,
        entity.getEyeLocation().clone().add(0, -entity.getEyeHeight() / 2, 0),
        12,
        0.0, 0.0, 0.0,
        0.3,
        BLOCK_DATA
    );
  }
}
