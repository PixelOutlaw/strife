package land.face.strife.managers;

import static org.bukkit.attribute.Attribute.GENERIC_FOLLOW_RANGE;

import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import io.pixeloutlaw.minecraft.spigot.config.VersionedSmartYamlConfiguration;
import io.pixeloutlaw.minecraft.spigot.garbage.StringExtensionsKt;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.UniqueEntity;
import land.face.strife.data.ability.EntityAbilitySet;
import land.face.strife.data.ability.EntityAbilitySet.TriggerAbilityType;
import land.face.strife.data.effects.Effect;
import land.face.strife.data.effects.StrifeParticle;
import land.face.strife.events.UniqueSpawnEvent;
import land.face.strife.stats.StrifeStat;
import land.face.strife.tasks.ItemPassengerTask;
import land.face.strife.util.ItemUtil;
import land.face.strife.util.LogUtil;
import land.face.strife.util.PlayerDataUtil;
import land.face.strife.util.SpecialStatusUtil;
import land.face.strife.util.StatUtil;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Bee;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Hoglin;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.Piglin;
import org.bukkit.entity.Rabbit;
import org.bukkit.entity.Raider;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;
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
      //System.out.println(DisguiseParser.parseToString(cachedDisguises.get(uniqueEntity)));
      DisguiseAPI.disguiseToAll(e, cachedDisguises.get(uniqueEntity));
      //System.out.println(DisguiseParser.parseToString(DisguiseAPI.getDisguise(e)));
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
      ((Ageable) le).setBreed(false);
      if (uniqueEntity.isBaby()) {
        ((Ageable) le).setBaby();
      } else {
        ((Ageable) le).setAdult();
      }
    }

    if (uniqueEntity.isZombificationImmune()) {
      if (le instanceof Piglin) {
        ((Piglin) le).setImmuneToZombification(true);
      }
      if (le instanceof Hoglin) {
        ((Hoglin) le).setImmuneToZombification(true);
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

    StrifeMob mob = plugin.getStrifeMobManager().getStatMob(le);

    int mobLevel = uniqueEntity.getBaseLevel();
    if (mobLevel == -1) {
      mobLevel = StatUtil.getMobLevel(le);
    }

    if (mobLevel == 0) {
      mob.setStats(uniqueEntity.getAttributeMap());
    } else {
      mob.setStats(StatUpdateManager.combineMaps(mob.getBaseStats(), uniqueEntity.getAttributeMap()));
    }

    if (uniqueEntity.getMaxMods() > 0) {
      plugin.getMobModManager().doModApplication(mob, uniqueEntity.getMaxMods());
    }

    mob.setUniqueEntityId(uniqueEntity.getId());
    mob.setFactions(uniqueEntity.getFactions());
    mob.setAlliedGuild(null);
    SpecialStatusUtil.setDespawnOnUnload(mob.getEntity());
    mob.setCharmImmune(uniqueEntity.isCharmImmune());

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
        mountMob.getEntity().addPassenger(mob.getEntity());
        mob.addMinion(mountMob);
        StrifePlugin.getInstance().getMinionManager().addMinion(mountMob.getEntity(), 10);
      }
    }

    plugin.getStatUpdateManager().updateVanillaAttributes(mob);

    mob.setAbilitySet(new EntityAbilitySet(uniqueEntity.getAbilitySet()));
    plugin.getAbilityManager().abilityCast(mob, TriggerAbilityType.PHASE_SHIFT);
    plugin.getParticleTask().addParticle(le, uniqueEntity.getStrifeParticle());

    plugin.getAbilityManager().startAbilityTimerTask(mob);

    UniqueSpawnEvent event = new UniqueSpawnEvent(mob);
    Bukkit.getPluginManager().callEvent(event);

    return mob;
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

  public void loadUniques(VersionedSmartYamlConfiguration uniqueEnemiesYAML) {
    for (String entityNameKey : uniqueEnemiesYAML.getKeys(false)) {
      if (!uniqueEnemiesYAML.isConfigurationSection(entityNameKey)) {
        continue;
      }
      ConfigurationSection cs = uniqueEnemiesYAML.getConfigurationSection(entityNameKey);

      UniqueEntity uniqueEntity = new UniqueEntity();

      assert cs != null;
      String type = cs.getString("type");
      try {
        uniqueEntity.setType(EntityType.valueOf(type));
      } catch (Exception e) {
        Bukkit.getLogger().severe("Failed to parse entity " + entityNameKey + ". Invalid type: " + type);
        continue;
      }

      uniqueEntity.setId(entityNameKey);
      uniqueEntity.setName(
          StringExtensionsKt.chatColorize(Objects.requireNonNull(cs.getString("name", "&fSET &cA &9NAME"))));
      uniqueEntity.setBonusExperience(cs.getInt("bonus-experience", 0));
      uniqueEntity.setDisplaceMultiplier(cs.getDouble("displace-multiplier", 1.0));
      uniqueEntity.setExperienceMultiplier((float) cs.getDouble("experience-multiplier", 1));
      uniqueEntity.setCharmImmune(cs.getBoolean("charm-immune", false));
      uniqueEntity.setBurnImmune(cs.getBoolean("burn-immune", false));
      uniqueEntity.setFallImmune(cs.getBoolean("fall-immune", false));
      uniqueEntity.setPushImmune(cs.getBoolean("push-immune", false));
      uniqueEntity.setIgnoreSneak(cs.getBoolean("ignore-sneak", false));
      uniqueEntity.setMaxMods(cs.getInt("max-mods", 3));
      uniqueEntity.setRemoveFollowMods(cs.getBoolean("remove-range-modifiers", false));
      if (uniqueEntity.getType() == EntityType.CREEPER) {
        uniqueEntity.setPowered(cs.getBoolean("powered", false));
      }
      uniqueEntity.setShowName(cs.getBoolean("show-name", true));
      uniqueEntity.setMount(cs.getString("mount-id", ""));
      uniqueEntity.setFollowRange(cs.getInt("follow-range", -1));
      uniqueEntity.setSize(cs.getInt("size", -1));
      uniqueEntity.getFactions().addAll(cs.getStringList("factions"));
      uniqueEntity.setBaby(cs.getBoolean("baby", false));
      uniqueEntity.setAngry(cs.getBoolean("angry", false));
      uniqueEntity.setZombificationImmune(cs.getBoolean("zombification-immune", true));
      uniqueEntity.setArmsRaised(cs.getBoolean("arms-raised", true));
      if (uniqueEntity.getType() == EntityType.VILLAGER || uniqueEntity.getType() == EntityType.ZOMBIE_VILLAGER) {
        String prof = cs.getString("profession");
        if (prof != null) {
          uniqueEntity.setProfession(Profession.valueOf(prof.toUpperCase()));
        }
      }
      uniqueEntity.setBaseLevel(cs.getInt("base-level", -1));

      Disguise disguise = PlayerDataUtil
          .parseDisguise(cs.getConfigurationSection("disguise"), uniqueEntity.getName(), uniqueEntity.getMaxMods() > 0);

      if (disguise != null) {
        cacheDisguise(uniqueEntity, disguise);
      }

      ConfigurationSection statCs = cs.getConfigurationSection("stats");
      Map<StrifeStat, Float> attributeMap = StatUtil.getStatMapFromSection(statCs);
      uniqueEntity.setAttributeMap(attributeMap);

      uniqueEntity.setEquipment(
          plugin.getEquipmentManager().buildEquipmentFromConfigSection(cs.getConfigurationSection("equipment")));

      String passengerItem = cs.getString("item-passenger", "");
      if (org.apache.commons.lang.StringUtils.isNotBlank(passengerItem)) {
        uniqueEntity.setItemPassenger(plugin.getEquipmentManager().getItem(passengerItem));
      }

      String particle = cs.getString("particle", "");
      if (org.apache.commons.lang.StringUtils.isNotBlank(particle)) {
        Effect effect = plugin.getEffectManager().getEffect(particle);
        if (effect instanceof StrifeParticle) {
          uniqueEntity.setStrifeParticle((StrifeParticle) effect);
        }
      }

      ConfigurationSection abilityCS = cs.getConfigurationSection("abilities");
      uniqueEntity.setAbilitySet(null);
      if (abilityCS != null) {
        uniqueEntity.setAbilitySet(new EntityAbilitySet(abilityCS));
      }
      addUniqueEntity(entityNameKey, uniqueEntity);
    }
  }
}
