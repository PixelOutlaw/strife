package land.face.strife.listeners;

import static org.bukkit.attribute.Attribute.GENERIC_FOLLOW_RANGE;
import static org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH;

import io.pixeloutlaw.minecraft.spigot.garbage.StringExtensionsKt;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.util.Random;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.ability.EntityAbilitySet.TriggerAbilityType;
import land.face.strife.managers.MobModManager;
import land.face.strife.stats.StrifeStat;
import land.face.strife.util.LogUtil;
import land.face.strife.util.SpecialStatusUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Rabbit;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

public class SpawnListener implements Listener {

  private final StrifePlugin plugin;
  private final Random random;

  private final double WITCH_TO_EVOKER_CHANCE;
  private final double WITCH_TO_ILLUSIONER_CHANCE;
  private final double KILLER_BUNNY_CHANCE;
  private final double SKELETON_SWORD_CHANCE;
  private final double SKELETON_WAND_CHANCE;
  private final double WITHER_SKELETON_SWORD_CHANCE;
  private final double WITHER_SKELETON_WAND_CHANCE;

  private final String MOB_LEVEL_NAME;
  private final String MOB_LEVEL_SUFFIX;

  private final ItemStack SKELETON_SWORD;
  private final ItemStack WITHER_SKELETON_SWORD;
  public static ItemStack SKELETON_WAND;
  private final ItemStack WITCH_HAT;

  public SpawnListener(StrifePlugin plugin) {
    this.plugin = plugin;

    random = new Random(System.currentTimeMillis());
    SKELETON_SWORD = buildSkeletonSword();
    WITHER_SKELETON_SWORD = buildWitherSkeletonSword();
    SKELETON_WAND = buildSkeletonWand();
    WITCH_HAT = buildWitchHat();

    MOB_LEVEL_NAME = StringExtensionsKt.chatColorize(plugin.getSettings()
        .getString("config.leveled-monsters.name-format", "&f%ENTITY% -"));
    MOB_LEVEL_SUFFIX = StringExtensionsKt.chatColorize(plugin.getSettings()
        .getString("config.leveled-monsters.suffix-format", " &7%LEVEL%"));

    WITCH_TO_EVOKER_CHANCE = plugin.getSettings()
        .getDouble("config.leveled-monsters.replace-witch-evoker", 0.1);
    WITCH_TO_ILLUSIONER_CHANCE = plugin.getSettings()
        .getDouble("config.leveled-monsters.replace-witch-illusioner", 0.02);
    KILLER_BUNNY_CHANCE = plugin.getSettings()
        .getDouble("config.leveled-monsters.killer-bunny-chance", 0.05);
    SKELETON_SWORD_CHANCE = plugin.getSettings()
        .getDouble("config.leveled-monsters.give-skeletons-sword-chance", 0.1);
    SKELETON_WAND_CHANCE = plugin.getSettings()
        .getDouble("config.leveled-monsters.give-skeletons-wand-chance", 0.1);
    WITHER_SKELETON_SWORD_CHANCE = plugin.getSettings()
        .getDouble("config.leveled-monsters.give-wither-skeletons-sword-chance", 0.8);
    WITHER_SKELETON_WAND_CHANCE = plugin.getSettings()
        .getDouble("config.leveled-monsters.give-wither-skeletons-wand-chance", 0.1);
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onJockeySpawn(CreatureSpawnEvent event) {
    if (event.getSpawnReason() != SpawnReason.JOCKEY) {
      return;
    }
    if (event.getEntity().getVehicle() instanceof LivingEntity && StringUtils.isNotBlank(
        plugin.getStrifeMobManager().getStatMob((LivingEntity) event.getEntity().getVehicle())
            .getUniqueEntityId())) {
      event.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onNaturalCatSpawn(CreatureSpawnEvent event) {
    if (event.getEntity().getType() == EntityType.CAT &&
        event.getSpawnReason() == SpawnReason.NATURAL) {
      event.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onCreatureSpawnHighest(CreatureSpawnEvent event) {
    if (event.isCancelled() || event.getEntity().hasMetadata("NPC") ||
        event.getEntity().hasMetadata("pet")
        || event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.BREEDING) {
      return;
    }
    if (StringUtils.isNotBlank(SpecialStatusUtil.getUniqueId(event.getEntity()))) {
      return;
    }

    LivingEntity entity = event.getEntity();
    if (!plugin.getMonsterManager().containsEntityType(event.getEntityType())) {
      return;
    }

    int startingLevel = plugin.getSettings().getInt("config.leveled-monsters.enabled-worlds." +
        event.getLocation().getWorld().getName() + ".starting-level", -1);
    if (startingLevel <= 0) {
      return;
    }

    entity.getAttribute(GENERIC_FOLLOW_RANGE).setBaseValue(15);
    entity.setRemoveWhenFarAway(true);

    switch (entity.getType()) {
      case WITCH -> {
        if (random.nextDouble() < WITCH_TO_EVOKER_CHANCE) {
          entity.getWorld().spawnEntity(event.getLocation(), EntityType.EVOKER);
          event.setCancelled(true);
          return;
        }
        if (random.nextDouble() < WITCH_TO_ILLUSIONER_CHANCE) {
          entity.getWorld().spawnEntity(event.getLocation(), EntityType.ILLUSIONER);
          event.setCancelled(true);
          return;
        }
      }
      case RABBIT -> {
        if (random.nextDouble() > KILLER_BUNNY_CHANCE) {
          return;
        }
        Rabbit rabbit = (Rabbit) entity;
        rabbit.setRabbitType(Rabbit.Type.THE_KILLER_BUNNY);
        rabbit.setAdult();
      }
    }

    int level = getLevelFromWorldLocation(event, startingLevel);

    String mobName = WordUtils.capitalizeFully(entity.getType().toString().replace("_", " "));
    String name = MOB_LEVEL_NAME.replace("%ENTITY%", mobName).replace("%LEVEL%", "" + level);
    String levelSuffix = MOB_LEVEL_SUFFIX.replace("%ENTITY%", mobName)
        .replace("%LEVEL%", "" + level);

    entity.setCustomName(name + levelSuffix);
    SpecialStatusUtil.setMobLevel(entity, level);
    entity.setCanPickupItems(false);
    entity.getEquipment().clear();
    equipEntity(entity);

    StrifeMob strifeMob = plugin.getStrifeMobManager().getStatMob(entity);
    plugin.getMobModManager().doModApplication(strifeMob, MobModManager.MOB_MOD_MAX_MODS);
    setEntityAttributes(strifeMob, entity);

    plugin.getAbilityManager().abilityCast(strifeMob, TriggerAbilityType.PHASE_SHIFT);
    plugin.getAbilityManager().startAbilityTimerTask(strifeMob);
  }

  private void equipEntity(LivingEntity livingEntity) {
    EntityEquipment entityEquipment = livingEntity.getEquipment();
    if (entityEquipment == null) {
      LogUtil.printWarning("Attempting to equip entity with no equipment slots!");
      return;
    }
    switch (livingEntity.getType()) {
      case ZOMBIFIED_PIGLIN -> {
        entityEquipment.setHelmet(new ItemStack(Material.GOLDEN_HELMET));
        if (random.nextDouble() < 0.5) {
          entityEquipment.setItemInMainHand(new ItemStack(Material.GOLDEN_AXE));
        } else {
          entityEquipment.setItemInMainHand(new ItemStack(Material.GOLDEN_SWORD));
        }
        entityEquipment.setItemInMainHandDropChance(0f);
        entityEquipment.setHelmetDropChance(0f);
      }
      case SKELETON -> {
        entityEquipment.setItemInMainHandDropChance(0f);
        if (random.nextDouble() < SKELETON_SWORD_CHANCE) {
          entityEquipment.setItemInMainHand(SKELETON_SWORD);
          break;
        }
        if (random.nextDouble() < SKELETON_WAND_CHANCE) {
          entityEquipment.setItemInMainHand(SKELETON_WAND);
          entityEquipment.setHelmet(WITCH_HAT);
          entityEquipment.setHelmetDropChance(0f);
          StrifeMob mob = plugin.getStrifeMobManager().getStatMob(livingEntity);
          float damage = mob.getStat(StrifeStat.PHYSICAL_DAMAGE);
          mob.forceSetStat(StrifeStat.PHYSICAL_DAMAGE, 0);
          mob.forceSetStat(StrifeStat.MAGIC_DAMAGE, damage);
          break;
        }
        entityEquipment.setItemInMainHand(new ItemStack(Material.BOW));
      }
      case WITHER_SKELETON -> {
        entityEquipment.setItemInMainHandDropChance(0f);
        if (random.nextDouble() < WITHER_SKELETON_SWORD_CHANCE) {
          entityEquipment.setItemInMainHand(WITHER_SKELETON_SWORD);
          break;
        }
        if (random.nextDouble() < WITHER_SKELETON_WAND_CHANCE) {
          entityEquipment.setItemInMainHand(SKELETON_WAND);
          entityEquipment.setHelmet(WITCH_HAT);
          entityEquipment.setHelmetDropChance(0f);
          StrifeMob mob = plugin.getStrifeMobManager().getStatMob(livingEntity);
          float damage = mob.getStat(StrifeStat.PHYSICAL_DAMAGE);
          mob.forceSetStat(StrifeStat.PHYSICAL_DAMAGE, 0);
          mob.forceSetStat(StrifeStat.MAGIC_DAMAGE, damage);
          break;
        }
        entityEquipment.setItemInMainHand(new ItemStack(Material.BOW));
      }
      case VINDICATOR -> {
        entityEquipment.setItemInMainHand(new ItemStack(Material.IRON_AXE));
        entityEquipment.setItemInMainHandDropChance(0f);
      }
      case ILLUSIONER -> {
        entityEquipment.setItemInMainHand(new ItemStack(Material.BOW));
        entityEquipment.setItemInMainHandDropChance(0f);
      }
    }
  }

  private void setEntityAttributes(StrifeMob strifeMob, LivingEntity entity) {
    plugin.getStatUpdateManager().updateAllAttributes(strifeMob);
    double health = entity.getAttribute(GENERIC_MAX_HEALTH).getBaseValue();
    if (entity instanceof Slime) {
      health *= 0.6 + (double) ((Slime) entity).getSize() / 3.0;
    }
    entity.getAttribute(GENERIC_MAX_HEALTH).setBaseValue(health);
    entity.setHealth(health);
  }

  static ItemStack buildSkeletonWand() {
    ItemStack wand = new ItemStack(Material.BOW);
    ItemStackExtensionsKt.setCustomModelData(wand, 4000);
    return wand;
  }

  private static ItemStack buildSkeletonSword() {
    return new ItemStack(Material.STONE_SWORD);
  }

  private static ItemStack buildWitherSkeletonSword() {
    return new ItemStack(Material.IRON_SWORD);
  }

  private static ItemStack buildWitchHat() {
    ItemStack hat = new ItemStack(Material.SHEARS);
    hat.setDurability((short) 2);
    ItemStackExtensionsKt.setUnbreakable(hat, true);
    return hat;
  }

  private int getLevelFromWorldLocation(CreatureSpawnEvent event, int startingLevel) {
    double distance = event.getLocation()
        .distance(event.getLocation().getWorld().getSpawnLocation());
    double pow = plugin.getSettings().getInt("config.leveled-monsters.enabled-worlds." +
        event.getLocation().getWorld().getName() + ".distance-per-level", 150);

    int level = (int) (startingLevel + distance / pow);
    level += -2 + random.nextInt(5);
    return Math.max(level, 1);
  }
}
