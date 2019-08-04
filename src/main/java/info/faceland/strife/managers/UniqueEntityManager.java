package info.faceland.strife.managers;

import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import info.faceland.strife.StrifePlugin;
import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.data.UniqueEntity;
import info.faceland.strife.data.ability.EntityAbilitySet.AbilityType;
import info.faceland.strife.stats.StrifeStat;
import info.faceland.strife.tasks.ParticleTask;
import info.faceland.strife.util.LogUtil;
import java.util.HashMap;
import java.util.Map;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Zombie;
import org.bukkit.metadata.FixedMetadataValue;

public class UniqueEntityManager {

  private final StrifePlugin plugin;
  private final Map<String, UniqueEntity> loadedUniquesMap;
  private final Map<UniqueEntity, Disguise> cachedDisguises;

  public UniqueEntityManager(StrifePlugin plugin) {
    this.plugin = plugin;
    this.loadedUniquesMap = new HashMap<>();
    this.cachedDisguises = new HashMap<>();
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

    if (entity == null) {
      LogUtil.printWarning("Attempted to spawn unique " + uniqueEntity.getName() + " but entity is invalid?");
      return null;
    }

    LivingEntity spawnedUnique = (LivingEntity) entity;
    spawnedUnique.setRemoveWhenFarAway(false);

    if (cachedDisguises.containsKey(uniqueEntity)) {
      DisguiseAPI.disguiseToAll(spawnedUnique, cachedDisguises.get(uniqueEntity));
    }

    double health = uniqueEntity.getAttributeMap().getOrDefault(StrifeStat.HEALTH, 5D);
    spawnedUnique.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(health);
    spawnedUnique.setHealth(health);

    if (spawnedUnique instanceof Zombie) {
      ((Zombie) spawnedUnique).setBaby(uniqueEntity.isBaby());
    } else if (spawnedUnique instanceof Slime) {
      ((Slime) spawnedUnique).setSize(uniqueEntity.getSize());
    } else if (spawnedUnique instanceof Phantom) {
      ((Phantom) spawnedUnique).setSize(uniqueEntity.getSize());
    }

    if (uniqueEntity.getFollowRange() != -1) {
      if (spawnedUnique.getAttribute(Attribute.GENERIC_FOLLOW_RANGE) != null) {
        spawnedUnique.getAttribute(Attribute.GENERIC_FOLLOW_RANGE)
            .setBaseValue(uniqueEntity.getFollowRange());
      }
    }

    if (spawnedUnique.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE) != null && uniqueEntity
        .isKnockbackImmune()) {
      spawnedUnique.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(100);
    }

    if (spawnedUnique.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED) != null) {
      double speed =
          uniqueEntity.getAttributeMap().getOrDefault(StrifeStat.MOVEMENT_SPEED, 100D) / 100D;
      spawnedUnique.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(
          spawnedUnique.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getBaseValue() * speed);
    }

    if (spawnedUnique.getAttribute(Attribute.GENERIC_FLYING_SPEED) != null) {
      double speed =
          uniqueEntity.getAttributeMap().getOrDefault(StrifeStat.MOVEMENT_SPEED, 100D) / 100D;
      spawnedUnique.getAttribute(Attribute.GENERIC_FLYING_SPEED).setBaseValue(
          spawnedUnique.getAttribute(Attribute.GENERIC_FLYING_SPEED).getBaseValue() * speed);
    }

    delayedEquip(uniqueEntity, spawnedUnique);

    spawnedUnique.setCustomName(uniqueEntity.getName());
    spawnedUnique.setCustomNameVisible(true);

    plugin.getStrifeMobManager().setEntityStats(spawnedUnique, uniqueEntity.getAttributeMap());
    StrifeMob strifeMob = plugin.getStrifeMobManager().getStatMob(spawnedUnique);
    strifeMob.setDespawnOnUnload(true);
    plugin.getAbilityManager().abilityCast(strifeMob, AbilityType.PHASE_SHIFT);
    ParticleTask.addParticle(spawnedUnique, uniqueEntity.getSpawnParticle());
    return spawnedUnique;
  }

  public void cacheDisguise(UniqueEntity uniqueEntity, String disguiseType, String playerName) {
    DisguiseType type = DisguiseType.valueOf(disguiseType);
    if (type == DisguiseType.PLAYER) {
      if (StringUtils.isBlank(playerName)) {
        playerName = "Pur3p0w3r";
      }
      PlayerDisguise playerDisguise = new PlayerDisguise(uniqueEntity.getName(), playerName);
      cachedDisguises.put(uniqueEntity, playerDisguise);
      return;
    }
    MobDisguise mobDisguise = new MobDisguise(type);
    cachedDisguises.put(uniqueEntity, mobDisguise);
  }

  private void delayedEquip(UniqueEntity uniqueEntity, LivingEntity spawnedEntity) {
    if (spawnedEntity.getEquipment() == null) {
      return;
    }
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
