package info.faceland.strife.managers;

import info.faceland.strife.StrifePlugin;
import info.faceland.strife.attributes.StrifeAttribute;
import info.faceland.strife.data.ability.EntityAbilitySet;
import info.faceland.strife.data.UniqueEntity;
import info.faceland.strife.data.UniqueEntityData;
import info.faceland.strife.util.LogUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Zombie;
import org.bukkit.metadata.FixedMetadataValue;

public class UniqueEntityManager {

  private final StrifePlugin plugin;
  private final Map<LivingEntity, UniqueEntityData> liveUniquesMap;
  private final Map<String, UniqueEntity> loadedUniquesMap;

  public UniqueEntityManager(StrifePlugin plugin) {
    this.plugin = plugin;
    this.liveUniquesMap = new HashMap<>();
    this.loadedUniquesMap = new HashMap<>();
  }

  public UniqueEntity getLivingUnique(LivingEntity livingEntity) {
    if (!liveUniquesMap.containsKey(livingEntity)) {
      return null;
    }
    return liveUniquesMap.get(livingEntity).getUniqueEntity();
  }

  public Map<LivingEntity, UniqueEntityData> getLiveUniquesMap() {
    return liveUniquesMap;
  }

  public Map<String, UniqueEntity> getLoadedUniquesMap() {
    return loadedUniquesMap;
  }

  public void addUniqueEntity(String key, UniqueEntity uniqueEntity) {
    loadedUniquesMap.put(key, uniqueEntity);
  }

  public void removeEntity(LivingEntity entity, boolean purge, boolean triggerTimer) {
    UniqueEntityData data = getData(entity);
    if (data == null) {
      return;
    }
    if (triggerTimer && data.getSpawner() != null) {
      data.getSpawner()
          .setRespawnTime(System.currentTimeMillis() + data.getSpawner().getRespawnMillis());
    }
    liveUniquesMap.remove(entity);
    if (purge) {
      entity.remove();
    }
  }

  public int getPhase(LivingEntity livingEntity) {
    if (!liveUniquesMap.containsKey(livingEntity)) {
      LogUtil.printWarning("Attempting to get phase of non-unique entity...");
      return 0;
    }
    return liveUniquesMap.get(livingEntity).getPhase();
  }

  public LivingEntity getMaster(LivingEntity livingEntity) {
    if (!liveUniquesMap.containsKey(livingEntity)) {
      return null;
    }
    return liveUniquesMap.get(livingEntity).getMaster();
  }

  public UniqueEntityData getData(LivingEntity livingEntity) {
    return liveUniquesMap.getOrDefault(livingEntity, null);
  }

  public EntityAbilitySet getAbilitySet(LivingEntity livingEntity) {
    if (!liveUniquesMap.containsKey(livingEntity)) {
      LogUtil.printWarning("Attempting to get ability set of non-unique entity...");
      return null;
    }
    return liveUniquesMap.get(livingEntity).getUniqueEntity().getAbilitySet();
  }

  public EntityAbilitySet getAbilitySet(UniqueEntity uniqueEntity) {
    return uniqueEntity.getAbilitySet();
  }

  public void killAllSpawnedUniques() {
    for (LivingEntity le : liveUniquesMap.keySet()) {
      le.remove();
    }
    liveUniquesMap.clear();
  }

  public boolean isUnique(LivingEntity entity) {
    return liveUniquesMap.containsKey(entity);
  }

  public boolean isLoadedUnique(String name) {
    return loadedUniquesMap.containsKey(name);
  }

  public LivingEntity spawnUnique(String unique, Location location) {
    UniqueEntity uniqueEntity = loadedUniquesMap.get(unique);
    if (uniqueEntity == null) {
      plugin.getLogger().warning("Attempted to spawn non-existing unique: " + unique);
      return null;
    }
    return spawnUnique(uniqueEntity, location);
  }

  public LivingEntity spawnUnique(UniqueEntity uniqueEntity, Location location) {
    if (uniqueEntity.getType() == null) {
      LogUtil.printWarning("Null entity type: " + uniqueEntity.getName());
      return null;
    }
    LogUtil.printDebug("Spawning unique entity " + uniqueEntity.getId());

    Entity entity = location.getWorld().spawn(location, uniqueEntity.getType().getEntityClass(),
        e -> e.setMetadata("BOSS", new FixedMetadataValue(plugin, true)));

    LivingEntity spawnedUnique = (LivingEntity) entity;
    spawnedUnique.setRemoveWhenFarAway(false);

    double health = uniqueEntity.getAttributeMap().getOrDefault(StrifeAttribute.HEALTH, 5D);
    spawnedUnique.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(health);
    spawnedUnique.setHealth(health);

    if (spawnedUnique instanceof Zombie) {
      ((Zombie) spawnedUnique).setBaby(uniqueEntity.isBaby());
    }
    if (spawnedUnique instanceof Slime) {
      ((Slime) spawnedUnique).setSize(uniqueEntity.getSize());
    }

    if (spawnedUnique.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE) != null && uniqueEntity
        .isKnockbackImmune()) {
      spawnedUnique.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(100);
    }

    if (spawnedUnique.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED) != null) {
      double speed =
          uniqueEntity.getAttributeMap().getOrDefault(StrifeAttribute.MOVEMENT_SPEED, 100D) / 100D;
      spawnedUnique.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(
          spawnedUnique.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getBaseValue() * speed);
    }

    if (spawnedUnique.getAttribute(Attribute.GENERIC_FLYING_SPEED) != null) {
      double speed =
          uniqueEntity.getAttributeMap().getOrDefault(StrifeAttribute.MOVEMENT_SPEED, 100D) / 100D;
      spawnedUnique.getAttribute(Attribute.GENERIC_FLYING_SPEED).setBaseValue(
          spawnedUnique.getAttribute(Attribute.GENERIC_FLYING_SPEED).getBaseValue() * speed);
    }

    delayedEquip(uniqueEntity, spawnedUnique);

    spawnedUnique.setCustomName(uniqueEntity.getName());
    spawnedUnique.setCustomNameVisible(true);

    plugin.getAttributedEntityManager()
        .setEntityStats(spawnedUnique, uniqueEntity.getAttributeMap());
    liveUniquesMap.put(spawnedUnique, new UniqueEntityData(uniqueEntity));
    plugin.getAbilityManager()
        .checkPhaseChange(plugin.getAttributedEntityManager().getAttributedEntity(spawnedUnique));
    return spawnedUnique;
  }

  private void delayedEquip(UniqueEntity uniqueEntity, LivingEntity spawnedEntity) {
    Bukkit.getScheduler().runTaskLater(plugin, () -> {
      spawnedEntity.getEquipment().clear();
      spawnedEntity.setCanPickupItems(false);
      spawnedEntity.getEquipment().setHelmetDropChance(0f);
      spawnedEntity.getEquipment().setChestplateDropChance(0f);
      spawnedEntity.getEquipment().setLeggingsDropChance(0f);
      spawnedEntity.getEquipment().setBootsDropChance(0f);
      spawnedEntity.getEquipment().setItemInMainHandDropChance(0f);
      spawnedEntity.getEquipment().setItemInOffHandDropChance(0f);
      spawnedEntity.getEquipment().setHelmet(uniqueEntity.getHelmetItem());
      spawnedEntity.getEquipment().setChestplate(uniqueEntity.getChestItem());
      spawnedEntity.getEquipment().setLeggings(uniqueEntity.getLegsItem());
      spawnedEntity.getEquipment().setBoots(uniqueEntity.getBootsItem());
      spawnedEntity.getEquipment().setItemInMainHand(uniqueEntity.getMainHandItem());
      spawnedEntity.getEquipment().setItemInOffHand(uniqueEntity.getOffHandItem());
    }, 1L);
  }
}
