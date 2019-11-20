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
import land.face.strife.stats.StrifeStat;
import land.face.strife.util.ItemUtil;
import land.face.strife.util.LogUtil;
import land.face.strife.util.StatUtil;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import me.libraryaddict.disguise.disguisetypes.RabbitType;
import me.libraryaddict.disguise.disguisetypes.watchers.FoxWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.RabbitWatcher;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fox;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.Rabbit;
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

  StrifeMob spawnUnique(UniqueEntity uniqueEntity, Location location) {
    if (uniqueEntity.getType() == null) {
      LogUtil.printWarning("Null entity type: " + uniqueEntity.getName());
      return null;
    }
    LogUtil.printDebug("Spawning unique entity " + uniqueEntity.getId());

    assert uniqueEntity.getType().getEntityClass() != null;
    Entity entity = Objects.requireNonNull(location.getWorld())
        .spawn(location, uniqueEntity.getType().getEntityClass(),
            e -> e.setMetadata("UNIQUE_ID", new FixedMetadataValue(plugin, uniqueEntity.getId())));

    if (!entity.isValid()) {
      LogUtil.printWarning(
          "Attempted to spawn unique " + uniqueEntity.getName() + " but entity is invalid?");
      return null;
    }

    LivingEntity le = (LivingEntity) entity;
    le.setRemoveWhenFarAway(true);

    if (le instanceof Zombie) {
      ((Zombie) le).setBaby(uniqueEntity.isBaby());
    } else if (le instanceof Slime) {
      ((Slime) le).setSize(uniqueEntity.getSize());
    } else if (le instanceof Phantom) {
      ((Phantom) le).setSize(uniqueEntity.getSize());
    } else if (le instanceof Rabbit) {
      ((Rabbit) le).setRabbitType(Rabbit.Type.THE_KILLER_BUNNY);
      ((Rabbit) le).setAdult();
    }

    if (uniqueEntity.getFollowRange() != -1) {
      if (le.getAttribute(GENERIC_FOLLOW_RANGE) != null) {
        le.getAttribute(GENERIC_FOLLOW_RANGE)
            .setBaseValue(uniqueEntity.getFollowRange());
      }
      if (le instanceof Zombie && uniqueEntity.isRemoveFollowMods()) {
        for (AttributeModifier mod : le.getAttribute(GENERIC_FOLLOW_RANGE).getModifiers()) {
          le.getAttribute(GENERIC_FOLLOW_RANGE).removeModifier(mod);
        }
      }
    }

    if (le.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE) != null && uniqueEntity
        .isKnockbackImmune()) {
      le.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(100);
    }

    if (le.getEquipment() != null) {
      le.getEquipment().clear();
      ItemUtil.delayedEquip(uniqueEntity.getEquipment(), le);
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

    if (uniqueEntity.isAllowMods()) {
      plugin.getMobModManager().doModApplication(strifeMob);
    }

    strifeMob.setUniqueEntityId(uniqueEntity.getId());
    strifeMob.setDespawnOnUnload(true);
    strifeMob.setCharmImmune(uniqueEntity.isCharmImmune());
    if (uniqueEntity.isBurnImmune()) {
      le.setMetadata("NO_BURN", new FixedMetadataValue(plugin, true));
    }
    if (uniqueEntity.isIgnoreSneak()) {
      le.setMetadata("IGNORE_SNEAK", new FixedMetadataValue(plugin, true));
    }
    if (StringUtils.isNotBlank(uniqueEntity.getMount())) {
      StrifeMob mountMob = spawnUnique(uniqueEntity.getMount(), location);
      if (mountMob != null) {
        mountMob.getEntity().addPassenger(strifeMob.getEntity());
      }
    }

    plugin.getStatUpdateManager().updateAttributes(strifeMob);

    strifeMob.setAbilitySet(new EntityAbilitySet(uniqueEntity.getAbilitySet()));
    plugin.getAbilityManager().abilityCast(strifeMob, TriggerAbilityType.PHASE_SHIFT);
    plugin.getParticleTask().addParticle(le, uniqueEntity.getStrifeParticle());

    if (cachedDisguises.containsKey(uniqueEntity)) {
      DisguiseAPI.disguiseToAll(le, cachedDisguises.get(uniqueEntity));
    }
    plugin.getAbilityManager().startAbilityTimerTask(strifeMob);
    return strifeMob;
  }

  public void cacheDisguise(UniqueEntity uniqueEntity, String disguiseType, String playerName,
      String typeData) {
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
    if (StringUtils.isNotBlank(typeData)) {
      FlagWatcher watcher = mobDisguise.getWatcher();
      try {
        switch (type) {
          case FOX:
            Fox.Type foxType = Fox.Type.valueOf(typeData);
            ((FoxWatcher) watcher).setType(foxType);
            break;
          case RABBIT:
            RabbitType rabbitType = RabbitType.valueOf(typeData);
            ((RabbitWatcher) watcher).setType(rabbitType);
            break;
        }
      } catch (Exception e) {
        LogUtil.printWarning("Cannot load type " + typeData + " for " + uniqueEntity.getId());
      }
    }
    mobDisguise.setShowName(true);
    mobDisguise.setReplaceSounds(true);
    cachedDisguises.put(uniqueEntity, mobDisguise);
  }
}
