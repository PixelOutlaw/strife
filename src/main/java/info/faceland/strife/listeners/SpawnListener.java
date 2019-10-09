package info.faceland.strife.listeners;

import static info.faceland.strife.stats.StrifeStat.HEALTH;
import static info.faceland.strife.stats.StrifeStat.HEALTH_MULT;
import static info.faceland.strife.stats.StrifeStat.MAGIC_DAMAGE;
import static info.faceland.strife.stats.StrifeStat.MOVEMENT_SPEED;
import static info.faceland.strife.stats.StrifeStat.PHYSICAL_DAMAGE;
import static org.bukkit.attribute.Attribute.GENERIC_FLYING_SPEED;
import static org.bukkit.attribute.Attribute.GENERIC_FOLLOW_RANGE;
import static org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH;
import static org.bukkit.attribute.Attribute.GENERIC_MOVEMENT_SPEED;

import com.tealcube.minecraft.bukkit.TextUtils;
import info.faceland.strife.StrifePlugin;
import info.faceland.strife.data.MobMod;
import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.data.ability.EntityAbilitySet.TriggerAbilityType;
import info.faceland.strife.util.LogUtil;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.util.Random;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Rabbit;
import org.bukkit.entity.Slime;
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

  private final String MOB_LEVEL_NAME;
  private final String MOB_LEVEL_SUFFIX;
  private final double MOB_MOD_UP_CHANCE;
  private final int MOB_MOD_MAX_MODS;
  private final double WITCH_TO_EVOKER_CHANCE;
  private final double WITCH_TO_ILLUSIONER_CHANCE;
  private final double KILLER_BUNNY_CHANCE;
  private final double SKELETON_SWORD_CHANCE;
  private final double SKELETON_WAND_CHANCE;
  private final double WITHER_SKELETON_SWORD_CHANCE;
  private final double WITHER_SKELETON_WAND_CHANCE;

  private final int STARTING_WEIGHT = 100000;

  private final ItemStack SKELETON_SWORD;
  private final ItemStack WITHER_SKELETON_SWORD;
  private final ItemStack SKELETON_WAND;
  private final ItemStack WITCH_HAT;

  public SpawnListener(StrifePlugin plugin) {
    this.plugin = plugin;

    random = new Random(System.currentTimeMillis());
    SKELETON_SWORD = buildSkeletonSword();
    WITHER_SKELETON_SWORD = buildWitherSkeletonSword();
    SKELETON_WAND = buildSkeletonWand();
    WITCH_HAT = buildWitchHat();

    MOB_LEVEL_NAME = TextUtils.color(plugin.getSettings()
        .getString("config.leveled-monsters.name-format", "&f%ENTITY% -"));
    MOB_LEVEL_SUFFIX = TextUtils.color(plugin.getSettings()
        .getString("config.leveled-monsters.suffix-format", " &7%LEVEL%"));
    MOB_MOD_UP_CHANCE = plugin.getSettings()
        .getDouble("config.leveled-monsters.add-mod-chance", 0.1);
    MOB_MOD_MAX_MODS = plugin.getSettings()
        .getInt("config.leveled-monsters.max-mob-mods", 4);
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

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onCreatureSpawnHighest(CreatureSpawnEvent event) {
    if (event.isCancelled() || event.getEntity().hasMetadata("BOSS") ||
        event.getEntity().hasMetadata("NPC")
        || event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.BREEDING) {
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

    switch (entity.getType()) {
      case ZOMBIE:
      case HUSK:
      case ZOMBIE_VILLAGER:
      case DROWNED:
        entity.getAttribute(GENERIC_FOLLOW_RANGE).setBaseValue(22);
        break;
      case WITCH:
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
        break;
      case RABBIT:
        if (random.nextDouble() > KILLER_BUNNY_CHANCE) {
          return;
        }
        Rabbit rabbit = (Rabbit) entity;
        rabbit.setRabbitType(Rabbit.Type.THE_KILLER_BUNNY);
        rabbit.setAdult();
    }

    int level = getLevelFromWorldLocation(event, startingLevel);

    String mobName = WordUtils.capitalizeFully(entity.getType().toString().replace("_", " "));
    String name = MOB_LEVEL_NAME.replace("%ENTITY%", mobName).replace("%LEVEL%", "" + level);
    String levelSuffix = MOB_LEVEL_SUFFIX.replace("%ENTITY%", mobName)
        .replace("%LEVEL%", "" + level);

    entity.setMetadata("LVL", new FixedMetadataValue(plugin, level));
    entity.setCanPickupItems(false);
    entity.getEquipment().clear();
    equipEntity(entity);

    StrifeMob strifeMob = plugin.getStrifeMobManager().getStatMob(entity);

    String prefix = "";
    String suffix = "";
    int prefixWeight = STARTING_WEIGHT;
    int suffixWeight = STARTING_WEIGHT;
    Set<MobMod> mods = plugin.getMobModManager().getRandomMods(entity, event.getLocation(), getModCount());
    for (MobMod mod : mods) {
      plugin.getMobModManager().applyMobMod(strifeMob, mod);
      if (mod.getWeight() < prefixWeight && StringUtils.isNotBlank(mod.getPrefix())) {
        prefix = mod.getPrefix() + " ";
        prefixWeight = mod.getWeight();
      }
      if (mod.getWeight() < suffixWeight && StringUtils.isNotBlank(mod.getSuffix())) {
        suffix = " " + mod.getSuffix();
        suffixWeight = mod.getWeight();
      }
      strifeMob.setDespawnOnUnload(true);
    }
    setEntityAttributes(strifeMob, entity);
    entity.setCustomName(getPrefixColor(mods.size()) + prefix + name + suffix + levelSuffix);
    plugin.getAbilityManager().abilityCast(strifeMob, TriggerAbilityType.PHASE_SHIFT);
    plugin.getAbilityManager().startAbilityTimerTask(strifeMob);
  }

  private ChatColor getPrefixColor(int modCount) {
    switch (modCount) {
      case 0:
        return ChatColor.WHITE;
      case 1:
        return ChatColor.BLUE;
      case 2:
        return ChatColor.DARK_PURPLE;
      case 3:
        return ChatColor.RED;
      default:
        return ChatColor.WHITE;
    }
  }

  private int getModCount() {
    int mods = 0;
    for (int i = 0; i < MOB_MOD_MAX_MODS; i++) {
      if (random.nextDouble() < MOB_MOD_UP_CHANCE) {
        mods++;
        continue;
      }
      break;
    }
    return mods;
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
          float damage = mob.getStat(PHYSICAL_DAMAGE);
          mob.forceSetStat(PHYSICAL_DAMAGE, 0);
          mob.forceSetStat(MAGIC_DAMAGE, damage);
          break;
        }
        entityEquipment.setItemInMainHand(new ItemStack(Material.BOW));
        break;
      case WITHER_SKELETON:
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
          float damage = mob.getStat(PHYSICAL_DAMAGE);
          mob.forceSetStat(PHYSICAL_DAMAGE, 0);
          mob.forceSetStat(MAGIC_DAMAGE, damage);
          break;
        }
        entityEquipment.setItemInMainHand(new ItemStack(Material.BOW));
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

  private void setEntityAttributes(StrifeMob strifeMob, LivingEntity entity) {
    double health = strifeMob.getStat(HEALTH) * (1 + strifeMob.getStat(HEALTH_MULT));
    if (entity instanceof Slime) {
      health *= 0.6 + (double) ((Slime) entity).getSize() / 3.0;
    }
    entity.getAttribute(GENERIC_MAX_HEALTH).setBaseValue(health);
    entity.setHealth(health);

    double speed = entity.getAttribute(GENERIC_MOVEMENT_SPEED).getBaseValue() *
        strifeMob.getFinalStats().getOrDefault(MOVEMENT_SPEED, 80f) / 100f;

    if (entity.getAttribute(GENERIC_MOVEMENT_SPEED) != null) {
      entity.getAttribute(GENERIC_MOVEMENT_SPEED).setBaseValue(speed);
    }
    if (entity.getAttribute(GENERIC_FLYING_SPEED) != null) {
      entity.getAttribute(GENERIC_FLYING_SPEED).setBaseValue(speed);
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
