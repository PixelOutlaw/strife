package info.faceland.strife.listeners;

import static info.faceland.strife.stats.StrifeStat.HEALTH;
import static info.faceland.strife.stats.StrifeStat.HEALTH_MULT;
import static info.faceland.strife.stats.StrifeStat.MOVEMENT_SPEED;
import static org.bukkit.attribute.Attribute.GENERIC_FLYING_SPEED;
import static org.bukkit.attribute.Attribute.GENERIC_FOLLOW_RANGE;
import static org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH;
import static org.bukkit.attribute.Attribute.GENERIC_MOVEMENT_SPEED;

import com.tealcube.minecraft.bukkit.TextUtils;
import info.faceland.strife.StrifePlugin;
import info.faceland.strife.stats.StrifeStat;
import info.faceland.strife.util.LogUtil;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.util.Map;
import java.util.Random;
import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Rabbit;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Witch;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

public class SpawnListener implements Listener {

  private final StrifePlugin plugin;
  private final Random random;

  private final ItemStack skeletonSword;
  private final ItemStack skeletonWand;
  private final ItemStack witchHat;

  public SpawnListener(StrifePlugin plugin) {
    this.plugin = plugin;
    this.random = new Random(System.currentTimeMillis());
    this.skeletonSword = buildSkeletonSword();
    this.skeletonWand = buildSkeletonWand();
    this.witchHat = buildWitchHat();
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onCreatureSpawnHighest(CreatureSpawnEvent event) {
    if (event.isCancelled() || event.getEntity().hasMetadata("BOSS") ||
        event.getEntity().hasMetadata("NPC")) {
      return;
    }
    LivingEntity entity = event.getEntity();
    if (entity instanceof Zombie) {
      entity.getAttribute(GENERIC_FOLLOW_RANGE).setBaseValue(22);
    } else if (entity instanceof Witch) {
      if (random.nextDouble() < plugin.getSettings()
          .getDouble("config.leveled-monsters.replace-witch-evoker", 0.1)) {
        entity.getWorld().spawnEntity(event.getLocation(), EntityType.EVOKER);
        event.setCancelled(true);
        return;
      }
      if (random.nextDouble() < plugin.getSettings()
          .getDouble("config.leveled-monsters.replace-witch-illusioner", 0.1)) {
        entity.getWorld().spawnEntity(event.getLocation(), EntityType.ILLUSIONER);
        event.setCancelled(true);
        return;
      }
    } else if (entity instanceof Rabbit) {
      if (random.nextDouble() > plugin.getSettings()
          .getDouble("config.leveled-monsters.killer-bunny-chance", 0.05)) {
        return;
      }
      if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.BREEDING) {
        return;
      }
      Rabbit rabbit = (Rabbit) entity;
      rabbit.setRabbitType(Rabbit.Type.THE_KILLER_BUNNY);
      rabbit.setAdult();
      rabbit.setAgeLock(true);
    }
    if (!plugin.getMonsterManager().containsEntityType(entity.getType())) {
      return;
    }
    int startingLevel = plugin.getSettings().getInt(
        "config.leveled-monsters.enabled-worlds." + event.getLocation().getWorld().getName()
            + ".starting-level",
        -1);
    if (startingLevel <= 0) {
      return;
    }
    double distance = event.getLocation()
        .distance(event.getLocation().getWorld().getSpawnLocation());

    double pow = plugin.getSettings().getInt(
        "config.leveled-monsters.enabled-worlds." + event.getLocation().getWorld().getName()
            + ".distance-per-level",
        150);
    int level = (int) (startingLevel + distance / pow);
    level += -2 + random.nextInt(5);
    level = Math.max(level, 1);

    Map<StrifeStat, Double> statMap = plugin.getMonsterManager().getBaseStats(entity, level);
    if (statMap.isEmpty()) {
      return;
    }

    entity.getEquipment().clear();
    equipEntity(entity);

    String rankName = "";
    if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER) {
      rankName = ChatColor.WHITE + "Spawned ";
    }

    String mobName = WordUtils.capitalizeFully(entity.getType().toString().replace("_", " "));
    String name = TextUtils.color(
        plugin.getSettings()
            .getString("config.leveled-monsters.name-format", "&f%ENTITY% - %LEVEL%")
            .replace("%ENTITY%", mobName).replace("%LEVEL%", "" + level));

    name = rankName + name;

    entity.setCustomName(name);
    entity.setCanPickupItems(false);

    double health = statMap.getOrDefault(HEALTH, 10D) * (1 + statMap
        .getOrDefault(HEALTH_MULT, 0D));
    if (entity instanceof Slime) {
      health *= 0.6 + (double) ((Slime) entity).getSize() / 3.0;
    }
    entity.getAttribute(GENERIC_MAX_HEALTH).setBaseValue(health);
    entity.setHealth(health);

    double speed = entity.getAttribute(GENERIC_MOVEMENT_SPEED).getBaseValue() *
        statMap.getOrDefault(MOVEMENT_SPEED, 80D) / 100;

    if (entity.getAttribute(GENERIC_MOVEMENT_SPEED) != null) {
      entity.getAttribute(GENERIC_MOVEMENT_SPEED).setBaseValue(speed);
    }
    if (entity.getAttribute(GENERIC_FLYING_SPEED) != null) {
      entity.getAttribute(GENERIC_FLYING_SPEED).setBaseValue(speed);
    }
    entity.setMetadata("LVL", new FixedMetadataValue(plugin, level));
  }

  private void equipEntity(LivingEntity livingEntity) {
    EntityEquipment entityEquipment = livingEntity.getEquipment();
    if (entityEquipment == null) {
      LogUtil.printWarning("Attempting to equip entity with no equipment slots!");
      return;
    }
    switch (livingEntity.getType()) {
      case PIG_ZOMBIE:
        entityEquipment.setHelmet(new ItemStack(Material.GOLDEN_HELMET));
        if (random.nextDouble() < 0.5) {
          entityEquipment.setItemInMainHand(new ItemStack(Material.GOLDEN_AXE));
        } else {
          entityEquipment.setItemInMainHand(new ItemStack(Material.GOLDEN_SWORD));
        }
        entityEquipment.setItemInMainHandDropChance(0f);
        entityEquipment.setHelmetDropChance(0f);
        break;
      case SKELETON:
        if (random.nextDouble() < plugin.getSettings()
            .getDouble("config.leveled-monsters.give-skeletons-sword-chance", 0.1)) {
          entityEquipment.setItemInMainHand(skeletonSword);
        } else if (random.nextDouble() < plugin.getSettings()
            .getDouble("config.leveled-monsters.give-skeletons-wand-chance", 0.1)) {
          entityEquipment.setItemInMainHand(skeletonWand);
          entityEquipment.setHelmet(witchHat);
          entityEquipment.setHelmetDropChance(0f);
        } else {
          entityEquipment.setItemInMainHand(new ItemStack(Material.BOW));
        }
        entityEquipment.setItemInMainHandDropChance(0f);
        break;
      case VINDICATOR:
        entityEquipment.setItemInMainHand(new ItemStack(Material.IRON_AXE));
        entityEquipment.setItemInMainHandDropChance(0f);
        break;
      case ILLUSIONER:
        entityEquipment.setItemInMainHand(new ItemStack(Material.BOW));
        entityEquipment.setItemInMainHandDropChance(0f);
        break;
    }
  }

  static ItemStack buildSkeletonWand() {
    ItemStack wand = new ItemStack(Material.BOW);
    ItemStackExtensionsKt.setDisplayName(wand, "WAND");
    return wand;
  }

  private static ItemStack buildSkeletonSword() {
    return new ItemStack(Material.STONE_SWORD);
  }

  private static ItemStack buildWitchHat() {
    ItemStack hat = new ItemStack(Material.SHEARS);
    hat.setDurability((short) 2);
    ItemStackExtensionsKt.setUnbreakable(hat, true);
    return hat;
  }
}
