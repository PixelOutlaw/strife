package land.face.strife.util;

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
  private static float BLEED_FLAT;
  private static float BLEED_PERCENT;
  public static float PHYSICAL_BLEED_PERCENT;

  private static LoadedBuff lavaDebuff;

  private static final Map<Integer, Float> fireDamageMap = new HashMap<>();
  private static final Map<Integer, Float> poisonDamageMap = new HashMap<>();

  public static void refresh(StrifePlugin refreshedPlugin) {
    plugin = refreshedPlugin;

    float BURN_FLAT_DAMAGE = (float) plugin.getSettings().getDouble("config.mechanics.burn-flat-damage", 6);
    float POISON_FLAT_DAMAGE = (float) plugin.getSettings().getDouble("config.mechanics.poison-flat-damage");
    float BURN_LEVEL_DAMAGE = (float) plugin.getSettings().getDouble("config.mechanics.burn-level-damage", 6);
    float POISON_LEVEL_DAMAGE = (float) plugin.getSettings().getDouble("config.mechanics.poison-level-damage");

    BLEED_FLAT = (float) StrifePlugin.getInstance().getSettings()
        .getDouble("config.mechanics.bleed-flat-damage", 1);
    BLEED_PERCENT = (float) StrifePlugin.getInstance().getSettings()
        .getDouble("config.mechanics.bleed-percent-damage", 0.04);
    PHYSICAL_BLEED_PERCENT = (float) StrifePlugin.getInstance().getSettings()
        .getDouble("config.mechanics.bleed-base-physical-percent", 0.04);

    Map<StrifeStat, Float> debuffMap = new HashMap<>();
    debuffMap.put(StrifeStat.BURNING_RESIST, -20f);
    lavaDebuff = new LoadedBuff("BUILT-IN-LAVA-DEBUFF", debuffMap, "", -1, null, 20, 3);

    for (int i = 0; i <= 300; i++) {
      poisonDamageMap.put(i, POISON_FLAT_DAMAGE + i * POISON_LEVEL_DAMAGE);
      fireDamageMap.put(i, BURN_FLAT_DAMAGE + i * BURN_LEVEL_DAMAGE);
    }
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
    float damage = poisonPower * poisonDamageMap.getOrDefault(mob.getLevel(), poisonDamageMap.get(300));
    damage *= 1 - mob.getStat(StrifeStat.POISON_RESIST) / 100;
    return damage;
  }

  public static float getFireDamage(StrifeMob mob) {
    if (mob.isInvincible()) {
      return 0;
    }
    float damage = fireDamageMap.getOrDefault(mob.getLevel(), fireDamageMap.get(300));
    damage *= 1 - mob.getStat(StrifeStat.BURNING_RESIST) / 100;
    damage *= 1 - mob.getStat(StrifeStat.FIRE_RESIST) / 100;
    return damage;
  }

  public static void doLava(LivingEntity le) {
    if (le.getFireTicks() > 0 && le.getWorld().getBlockAt(le.getLocation()).getType() == Material.LAVA) {
      StrifeMob mob = plugin.getStrifeMobManager().getStatMob(le);
      mob.addBuff(lavaDebuff, null, 10);
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
