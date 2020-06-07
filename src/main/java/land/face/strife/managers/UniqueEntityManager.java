package land.face.strife.managers;

import static org.bukkit.attribute.Attribute.GENERIC_FOLLOW_RANGE;

import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.UniqueEntity;
import land.face.strife.data.ability.EntityAbilitySet;
import land.face.strife.data.ability.EntityAbilitySet.TriggerAbilityType;
import land.face.strife.events.UniqueSpawnEvent;
import land.face.strife.stats.StrifeStat;
import land.face.strife.tasks.ItemPassengerTask;
import land.face.strife.util.ItemUtil;
import land.face.strife.util.LogUtil;
import land.face.strife.util.SpecialStatusUtil;
import land.face.strife.util.StatUtil;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Bee;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.Rabbit;
import org.bukkit.entity.Raider;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Vindicator;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.Zombie;
import org.bukkit.entity.ZombieVillager;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class UniqueEntityManager {

  private final StrifePlugin plugin;
  private final Map<String, UniqueEntity> loadedUniquesMap;
  private final Map<UniqueEntity, Disguise> cachedDisguises;

  public UniqueEntityManager(StrifePlugin plugin) {
    this.plugin = plugin;
    this.loadedUniquesMap = new HashMap<>();
    this.cachedDisguises = new HashMap<>();
  }

  public UniqueEntity getUnique(String uniqueId) {
    return loadedUniquesMap.getOrDefault(uniqueId, null);
  }

  public Map<String, UniqueEntity> getLoadedUniquesMap() {
    return loadedUniquesMap;
  }

  public void addUniqueEntity(String key, UniqueEntity uniqueEntity) {
    loadedUniquesMap.put(key, uniqueEntity);
  }

  public boolean isLoadedUnique(String name) {
    return loadedUniquesMap.containsKey(name);
  }

  public StrifeMob spawnUnique(String unique, Location location) {
    UniqueEntity uniqueEntity = loadedUniquesMap.get(unique);
    if (uniqueEntity == null) {
      plugin.getLogger().warning("Attempted to spawn non-existing unique: " + unique);
      return null;
    }
    return spawnUnique(uniqueEntity, location);
  }

  private void lambdaSetup(Entity e, UniqueEntity uniqueEntity) {
    SpecialStatusUtil.setUniqueId(e, uniqueEntity.getId());
    if (cachedDisguises.containsKey(uniqueEntity)) {
      DisguiseAPI.disguiseToAll(e, cachedDisguises.get(uniqueEntity));
    }
  }

  StrifeMob spawnUnique(UniqueEntity uniqueEntity, Location location) {
    if (uniqueEntity.getType() == null) {
      LogUtil.printWarning("Null entity type: " + uniqueEntity.getName());
      return null;
    }
    LogUtil.printDebug("Spawning unique entity " + uniqueEntity.getId());

    assert uniqueEntity.getType().getEntityClass() != null;
    Entity entity = Objects.requireNonNull(location.getWorld()).spawn(location,
        uniqueEntity.getType().getEntityClass(), e -> lambdaSetup(e, uniqueEntity));

    if (!entity.isValid()) {
      LogUtil.printWarning(
          "Attempted to spawn unique " + uniqueEntity.getName() + " but entity is invalid?");
      return null;
    }

    LivingEntity le = (LivingEntity) entity;
    le.setRemoveWhenFarAway(true);

    if (le instanceof Zombie) {
      ((Zombie) le).setBaby(uniqueEntity.isBaby());
      ((Zombie) le).setArmsRaised(uniqueEntity.isArmsRaised());
    } else if (le instanceof Slime) {
      int size = uniqueEntity.getSize();
      if (size < 1) {
        size = 2 + (int) (Math.random() * 3);
      }
      ((Slime) le).setSize(size);
    } else if (le instanceof Phantom) {
      int size = uniqueEntity.getSize();
      if (size < 1) {
        size = 1 + (int) (Math.random() * 3);
      }
      ((Phantom) le).setSize(size);
    } else if (le instanceof Rabbit) {
      ((Rabbit) le).setRabbitType(Rabbit.Type.THE_KILLER_BUNNY);
      ((Rabbit) le).setAdult();
    } else if (le instanceof Creeper) {
      ((Creeper) le).setPowered(uniqueEntity.isPowered());
    }

    if (le instanceof Raider) {
      ((Raider) le).setCanJoinRaid(false);
      ((Raider) le).setPatrolLeader(false);
    }

    if (le instanceof Ageable) {
      ((Ageable) le).setAgeLock(true);
      if (uniqueEntity.isBaby()) {
        ((Ageable) le).setBaby();
      } else {
        ((Ageable) le).setAdult();
      }
    }

    if (le instanceof Bee) {
      ((Bee) le).setCannotEnterHiveTicks(Integer.MAX_VALUE);
    }

    if (uniqueEntity.isAngry()) {
      if (le instanceof Vindicator) {
        ((Vindicator) le).setJohnny(true);
      } else if (le instanceof Wolf) {
        ((Wolf) le).setAngry(true);
      } else if (le instanceof Bee) {
        ((Bee) le).setAnger(500000);
      }
    }

    if (uniqueEntity.getProfession() != null) {
      if (le instanceof ZombieVillager) {
        ((ZombieVillager) le).setVillagerProfession(uniqueEntity.getProfession());
      } else if (le instanceof Villager) {
        ((Villager) le).setProfession(uniqueEntity.getProfession());
      }
    }

    if (uniqueEntity.isRemoveFollowMods()) {
      SpecialStatusUtil.setWeakAggro(le);
    }

    if (uniqueEntity.getFollowRange() != -1) {
      AttributeInstance attributeInstance = le.getAttribute(GENERIC_FOLLOW_RANGE);
      if (attributeInstance != null) {
        attributeInstance.setBaseValue(uniqueEntity.getFollowRange());
        if (uniqueEntity.isRemoveFollowMods()) {
          for (AttributeModifier mod : attributeInstance.getModifiers()) {
            attributeInstance.removeModifier(mod);
          }
        }
      }
    }

    if (uniqueEntity.isKnockbackImmune()
        && le.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE) != null) {
      le.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(100);
    }

    le.setCanPickupItems(false);
    if (le.getEquipment() != null) {
      Map<EquipmentSlot, ItemStack> equipmentMap = plugin.getEquipmentManager()
          .getEquipmentMap(uniqueEntity.getEquipment());
      ItemUtil.delayedEquip(equipmentMap, le, true);
    }

    if (uniqueEntity.getItemPassenger() != null) {
      Item item = Objects.requireNonNull(location.getWorld()).spawn(location, Item.class,
          i -> modifyPassengerItem(le, i));
      item.setItemStack(uniqueEntity.getItemPassenger());
      le.addPassenger(item);
    }

    le.setCustomName(uniqueEntity.getName());
    le.setCustomNameVisible(uniqueEntity.isShowName());

    int mobLevel = uniqueEntity.getBaseLevel();
    if (mobLevel == -1) {
      mobLevel = StatUtil.getMobLevel(le);
    }

    Map<StrifeStat, Float> stats = new HashMap<>();
    if (mobLevel != 0) {
      stats.putAll(plugin.getMonsterManager().getBaseStats(le, mobLevel));
    }

    stats = StatUpdateManager.combineMaps(stats, uniqueEntity.getAttributeMap());

    StrifeMob strifeMob = plugin.getStrifeMobManager().setEntityStats(le, stats);

    if (uniqueEntity.getMaxMods() > 0) {
      plugin.getMobModManager().doModApplication(strifeMob, uniqueEntity.getMaxMods());
    }

    strifeMob.setUniqueEntityId(uniqueEntity.getId());
    strifeMob.setFactions(uniqueEntity.getFactions());
    strifeMob.setAlliedGuild(null);
    strifeMob.setDespawnOnUnload(true);
    strifeMob.setCharmImmune(uniqueEntity.isCharmImmune());

    if (uniqueEntity.isBurnImmune()) {
      SpecialStatusUtil.setBurnImmune(le);
    }
    if (uniqueEntity.isFallImmune()) {
      SpecialStatusUtil.setFallImmune(le);
    }
    if (uniqueEntity.isIgnoreSneak()) {
      SpecialStatusUtil.setSneakImmune(le);
    }
    if (StringUtils.isNotBlank(uniqueEntity.getMount())) {
      StrifeMob mountMob = spawnUnique(uniqueEntity.getMount(), location);
      if (mountMob != null) {
        mountMob.getEntity().addPassenger(strifeMob.getEntity());
        strifeMob.addMinion(mountMob);
        StrifePlugin.getInstance().getMinionManager().addMinion(mountMob.getEntity(), 10);
      }
    }

    plugin.getStatUpdateManager().updateAttributes(strifeMob);

    strifeMob.setAbilitySet(new EntityAbilitySet(uniqueEntity.getAbilitySet()));
    plugin.getAbilityManager().abilityCast(strifeMob, TriggerAbilityType.PHASE_SHIFT);
    plugin.getParticleTask().addParticle(le, uniqueEntity.getStrifeParticle());

    plugin.getAbilityManager().startAbilityTimerTask(strifeMob);

    UniqueSpawnEvent event = new UniqueSpawnEvent(strifeMob);
    Bukkit.getPluginManager().callEvent(event);

    return strifeMob;
  }

  private void modifyPassengerItem(LivingEntity rider, Item item) {
    item.setOwner(rider.getUniqueId());
    item.setCanMobPickup(false);
    item.setPickupDelay(Integer.MAX_VALUE);
    item.setGravity(false);
    new ItemPassengerTask(item);
  }

  public void cacheDisguise(UniqueEntity uniqueEntity, Disguise disguise) {
    cachedDisguises.put(uniqueEntity, disguise);
  }
}
