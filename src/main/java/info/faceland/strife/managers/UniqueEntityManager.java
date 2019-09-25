package info.faceland.strife.managers;

import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import info.faceland.strife.StrifePlugin;
import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.data.UniqueEntity;
import info.faceland.strife.data.ability.EntityAbilitySet;
import info.faceland.strife.data.ability.EntityAbilitySet.TriggerAbilityType;
import info.faceland.strife.stats.StrifeStat;
import info.faceland.strife.tasks.ParticleTask;
import info.faceland.strife.util.ItemUtil;
import info.faceland.strife.util.LogUtil;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
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

  public StrifeMob spawnUnique(String unique, Location location) {
    UniqueEntity uniqueEntity = loadedUniquesMap.get(unique);
    if (uniqueEntity == null) {
      plugin.getLogger().warning("Attempted to spawn non-existing unique: " + unique);
      return null;
    }
    return spawnUnique(uniqueEntity, location);
  }

  StrifeMob spawnUnique(UniqueEntity uniqueEntity, Location location) {
    if (uniqueEntity.getType() == null) {
      LogUtil.printWarning("Null entity type: " + uniqueEntity.getName());
      return null;
    }
    LogUtil.printDebug("Spawning unique entity " + uniqueEntity.getId());

    assert uniqueEntity.getType().getEntityClass() != null;
    Entity entity = Objects.requireNonNull(location.getWorld())
        .spawn(location, uniqueEntity.getType().getEntityClass(),
            e -> e.setMetadata("BOSS", new FixedMetadataValue(plugin, true)));

    if (!entity.isValid()) {
      LogUtil.printWarning(
          "Attempted to spawn unique " + uniqueEntity.getName() + " but entity is invalid?");
      return null;
    }

    LivingEntity spawnedUnique = (LivingEntity) entity;
    spawnedUnique.setRemoveWhenFarAway(false);

    double health = uniqueEntity.getAttributeMap().getOrDefault(StrifeStat.HEALTH, 5f);
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
          uniqueEntity.getAttributeMap().getOrDefault(StrifeStat.MOVEMENT_SPEED, 100f) / 100f;
      spawnedUnique.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(
          spawnedUnique.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getBaseValue() * speed);
    }

    if (spawnedUnique.getAttribute(Attribute.GENERIC_FLYING_SPEED) != null) {
      double speed =
          uniqueEntity.getAttributeMap().getOrDefault(StrifeStat.MOVEMENT_SPEED, 100f) / 100f;
      spawnedUnique.getAttribute(Attribute.GENERIC_FLYING_SPEED).setBaseValue(
          spawnedUnique.getAttribute(Attribute.GENERIC_FLYING_SPEED).getBaseValue() * speed);
    }

    ItemUtil.delayedEquip(uniqueEntity.getEquipment(), spawnedUnique);

    spawnedUnique.setCustomName(uniqueEntity.getName());
    spawnedUnique.setCustomNameVisible(true);

    plugin.getStrifeMobManager().setEntityStats(spawnedUnique, uniqueEntity.getAttributeMap());
    StrifeMob strifeMob = plugin.getStrifeMobManager().getStatMob(spawnedUnique);
    strifeMob.setUniqueEntityId(uniqueEntity.getId());
    strifeMob.setDespawnOnUnload(true);
    strifeMob.setCharmImmune(uniqueEntity.isCharmImmune());
    strifeMob.setAbilitySet(new EntityAbilitySet(uniqueEntity.getAbilitySet()));
    plugin.getAbilityManager().createCooldownContainer(spawnedUnique);
    plugin.getAbilityManager().abilityCast(strifeMob, TriggerAbilityType.PHASE_SHIFT);
    plugin.getAbilityManager().createCooldownContainer(spawnedUnique);
    ParticleTask.addParticle(spawnedUnique, uniqueEntity.getSpawnParticle());

    if (cachedDisguises.containsKey(uniqueEntity)) {
      DisguiseAPI.disguiseToAll(spawnedUnique, cachedDisguises.get(uniqueEntity));
    }
    plugin.getAbilityManager().startAbilityTimerTask(strifeMob);
    return strifeMob;
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
    mobDisguise.setShowName(true);
    cachedDisguises.put(uniqueEntity, mobDisguise);
  }
}
