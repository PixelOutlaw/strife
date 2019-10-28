package land.face.strife.managers;

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

    if (spawnedUnique.getEquipment() != null) {
      spawnedUnique.getEquipment().clear();
      ItemUtil.delayedEquip(uniqueEntity.getEquipment(), spawnedUnique);
    }

    spawnedUnique.setCustomName(uniqueEntity.getName());
    spawnedUnique.setCustomNameVisible(uniqueEntity.isShowName());

    int mobLevel = uniqueEntity.getBaseLevel();
    if (mobLevel == -1) {
      mobLevel = StatUtil.getMobLevel(spawnedUnique);
    }

    Map<StrifeStat, Float> stats = new HashMap<>();
    if (mobLevel != 0) {
      stats.putAll(plugin.getMonsterManager().getBaseStats(spawnedUnique, mobLevel));
    }

    stats = StatUpdateManager.combineMaps(stats, uniqueEntity.getAttributeMap());

    StrifeMob strifeMob = plugin.getStrifeMobManager().setEntityStats(spawnedUnique, stats);

    if (uniqueEntity.isAllowMods()) {
      plugin.getMobModManager().doModApplication(strifeMob);
    }

    strifeMob.setUniqueEntityId(uniqueEntity.getId());
    strifeMob.setDespawnOnUnload(true);
    strifeMob.setCharmImmune(uniqueEntity.isCharmImmune());
    if (uniqueEntity.isBurnImmune()) {
      spawnedUnique.setMetadata("NO_BURN", new FixedMetadataValue(plugin, true));
    }
    if (uniqueEntity.isIgnoreSneak()) {
      spawnedUnique.setMetadata("IGNORE_SNEAK", new FixedMetadataValue(plugin, true));
    }
    double health = strifeMob.getStat(StrifeStat.HEALTH) * (1 + strifeMob.getStat(StrifeStat.HEALTH_MULT) / 100);
    health = Math.max(health, 1);
    spawnedUnique.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(health);
    spawnedUnique.setHealth(health);

    double speed = strifeMob.getStat(StrifeStat.MOVEMENT_SPEED) / 100f;
    if (spawnedUnique.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED) != null) {
      double base = spawnedUnique.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getBaseValue();
      spawnedUnique.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(base * speed);
    }
    if (spawnedUnique.getAttribute(Attribute.GENERIC_FLYING_SPEED) != null) {
      double base = spawnedUnique.getAttribute(Attribute.GENERIC_FLYING_SPEED).getBaseValue();
      spawnedUnique.getAttribute(Attribute.GENERIC_FLYING_SPEED).setBaseValue(base * speed);
    }
    strifeMob.setAbilitySet(new EntityAbilitySet(uniqueEntity.getAbilitySet()));
    plugin.getAbilityManager().abilityCast(strifeMob, TriggerAbilityType.PHASE_SHIFT);
    plugin.getParticleTask().addParticle(spawnedUnique, uniqueEntity.getStrifeParticle());

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
