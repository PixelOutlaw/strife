package land.face.strife.managers;

import static org.bukkit.attribute.Attribute.GENERIC_FOLLOW_RANGE;

import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import io.pixeloutlaw.minecraft.spigot.config.VersionedSmartYamlConfiguration;
import io.pixeloutlaw.minecraft.spigot.garbage.StringExtensionsKt;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.util.HashMap;
import java.util.HashSet;
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
import land.face.strife.patch.AttackGoalPatcher;
import land.face.strife.stats.StrifeStat;
import land.face.strife.tasks.ItemPassengerTask;
import land.face.strife.util.ItemUtil;
import land.face.strife.util.LogUtil;
import land.face.strife.util.PlayerDataUtil;
import land.face.strife.util.SpecialStatusUtil;
import land.face.strife.util.StatUtil;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import org.bukkit.*;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Bee;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Hoglin;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.Piglin;
import org.bukkit.entity.Rabbit;
import org.bukkit.entity.Raider;
import org.bukkit.entity.Shulker;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.entity.Vindicator;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.Zombie;
import org.bukkit.entity.ZombieVillager;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class UniqueEntityManager {

  private final StrifePlugin plugin;
  private final Map<String, UniqueEntity> loadedUniquesMap;
  private final Map<UniqueEntity, Disguise> cachedDisguises;

  public static ItemStack DEV_SADDLE;

  public UniqueEntityManager(StrifePlugin plugin) {
    this.plugin = plugin;
    this.loadedUniquesMap = new HashMap<>();
    this.cachedDisguises = new HashMap<>();
    DEV_SADDLE = new ItemStack(Material.SADDLE);
    ItemStackExtensionsKt.setCustomModelData(DEV_SADDLE, 3000);
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
    return plugin.getStrifeMobManager()
        .getStatMob((LivingEntity) spawnUnique(uniqueEntity, location));
  }

  public Entity spawnUnique(UniqueEntity uniqueEntity, Location location) {
    if (uniqueEntity.getType() == null) {
      LogUtil.printWarning("Null entity type: " + uniqueEntity.getName());
      return null;
    }
    LogUtil.printDebug("Spawning unique entity " + uniqueEntity.getId());

    assert uniqueEntity.getType().getEntityClass() != null;
    return Objects.requireNonNull(location.getWorld()).spawn(location,
        uniqueEntity.getType().getEntityClass(), e -> lambdaSetup(e, uniqueEntity, location));
  }

  private void lambdaSetup(Entity entity, UniqueEntity uniqueEntity, Location location) {
    SpecialStatusUtil.setUniqueId(entity, uniqueEntity.getId());
    if (cachedDisguises.containsKey(uniqueEntity)) {
      //System.out.println(DisguiseParser.parseToString(cachedDisguises.get(uniqueEntity)));
      DisguiseAPI.disguiseToAll(entity, cachedDisguises.get(uniqueEntity));
      //System.out.println(DisguiseParser.parseToString(DisguiseAPI.getDisguise(e)));
    }

    LivingEntity le = (LivingEntity) entity;
    le.setRemoveWhenFarAway(true);

    if (!uniqueEntity.isGravity()) {
      le.setGravity(false);
    }
    if (!uniqueEntity.isHasAI()) {
      le.setAI(false);
    }

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
      if (uniqueEntity.isAngry()) {
        ((Rabbit) le).setRabbitType(Rabbit.Type.THE_KILLER_BUNNY);
      }
      ((Rabbit) le).setAdult();
    } else if (le instanceof Creeper) {
      ((Creeper) le).setPowered(uniqueEntity.isPowered());
    }

    if (le instanceof Mob) {
      if (!uniqueEntity.getRemoveGoals().isEmpty()) {
        AttackGoalPatcher.removeGoals((Mob) le, uniqueEntity.getRemoveGoals());
      }
      if (!uniqueEntity.getAddGoals().isEmpty()) {
        AttackGoalPatcher.addGoals((Mob) le, uniqueEntity);
      }
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

    if (uniqueEntity.getColor() != null && le instanceof Shulker) {
      ((Shulker) le).setColor(uniqueEntity.getColor());
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

    if (uniqueEntity.isInvisible()) {
      le.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 99999999, 10));
    }

    if (uniqueEntity.isSilent()) {
      le.setSilent(true);
    }

    if (uniqueEntity.isGuildMob()) {
      SpecialStatusUtil.setIsGuildMob(le);
    }

    le.setCanPickupItems(false);
    if (le.getEquipment() != null) {
      Map<EquipmentSlot, ItemStack> equipmentMap = plugin.getEquipmentManager()
          .getEquipmentMap(uniqueEntity.getEquipment());
      ItemUtil.equipMob(equipmentMap, le, true, true);
      Bukkit.getScheduler().runTaskLater(StrifePlugin.getInstance(), () ->
          ItemUtil.equipMob(equipmentMap, le, true, true), 4L);
    }

    if (uniqueEntity.isSaddled() && le.getType() == EntityType.HORSE) {
      assert le instanceof Horse;
      Horse horse = (Horse) le;
      horse.getInventory().setSaddle(DEV_SADDLE);
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
      mob.setStats(
          StatUpdateManager.combineMaps(mob.getBaseStats(), uniqueEntity.getAttributeMap()));
    }

    if (uniqueEntity.getMaxMods() > 0) {
      plugin.getMobModManager().doModApplication(mob, uniqueEntity.getMaxMods());
    }

    mob.setUniqueEntityId(uniqueEntity.getId());
    mob.setFactions(new HashSet<>(uniqueEntity.getFactions()));
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
      Bukkit.getScheduler().runTaskLater(StrifePlugin.getInstance(), () -> {
        StrifeMob mountMob = spawnUnique(uniqueEntity.getMount(), location);
        if (mountMob != null) {
          mountMob.getEntity().addPassenger(mob.getEntity());
          mob.addMinion(mountMob, 0);
        }
      }, 2L);
    }

    plugin.getStatUpdateManager().updateVanillaAttributes(mob);

    mob.setAbilitySet(new EntityAbilitySet(uniqueEntity.getAbilitySet()));

    Bukkit.getScheduler().runTaskLater(StrifePlugin.getInstance(), () -> {
      plugin.getAbilityManager().abilityCast(mob, TriggerAbilityType.PHASE_SHIFT);
      plugin.getParticleTask().addParticle(le, uniqueEntity.getStrifeParticle());
      plugin.getAbilityManager().startAbilityTimerTask(mob);
    }, 0L);

    UniqueSpawnEvent event = new UniqueSpawnEvent(mob);
    Bukkit.getPluginManager().callEvent(event);
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
        Bukkit.getLogger()
            .severe("Failed to parse entity " + entityNameKey + ". Invalid type: " + type);
        continue;
      }

      uniqueEntity.setId(entityNameKey);
      uniqueEntity.setName(
          StringExtensionsKt
              .chatColorize(Objects.requireNonNull(cs.getString("name", "&fSET &cA &9NAME"))));
      uniqueEntity.setBonusExperience(cs.getInt("bonus-experience", 0));
      uniqueEntity.setDisplaceMultiplier(cs.getDouble("displace-multiplier", 1.0));
      uniqueEntity.setExperienceMultiplier((float) cs.getDouble("experience-multiplier", 1));
      uniqueEntity.setCharmImmune(cs.getBoolean("charm-immune", false));
      uniqueEntity.setBurnImmune(cs.getBoolean("burn-immune", false));
      uniqueEntity.setFallImmune(cs.getBoolean("fall-immune", false));
      uniqueEntity.setPushImmune(cs.getBoolean("push-immune", false));
      uniqueEntity.setIgnoreSneak(cs.getBoolean("ignore-sneak", false));
      uniqueEntity.setSaddled(cs.getBoolean("saddled", false));
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
      String color = cs.getString("color", null);
      if (color != null) {
        uniqueEntity.setColor(DyeColor.valueOf(color));
      }
      uniqueEntity.setArmsRaised(cs.getBoolean("arms-raised", true));
      uniqueEntity.setGravity(cs.getBoolean("gravity", true));
      uniqueEntity.setHasAI(cs.getBoolean("has-ai", true));
      uniqueEntity.setInvisible(cs.getBoolean("invisible", false));
      uniqueEntity.setSilent(cs.getBoolean("silent", false));
      uniqueEntity.setGuildMob(cs.getBoolean("guild-mob", false));
      boolean adaptiveName = cs.getBoolean("adaptive-name", false);
      if (uniqueEntity.getType() == EntityType.VILLAGER
          || uniqueEntity.getType() == EntityType.ZOMBIE_VILLAGER) {
        String prof = cs.getString("profession");
        if (prof != null) {
          uniqueEntity.setProfession(Profession.valueOf(prof.toUpperCase()));
        }
      }
      uniqueEntity.setBaseLevel(cs.getInt("base-level", -1));

      uniqueEntity.setCustomAi(cs.getBoolean("custom-ai.enabled", false));
      uniqueEntity.setAggressiveAi(cs.getBoolean("custom-ai.aggressive", true));
      try {
        uniqueEntity.setAttackSound(Sound.valueOf(cs.getString("custom-ai.attack-sound")));
      } catch (Exception e) {
        uniqueEntity.setAttackSound(Sound.ENTITY_DOLPHIN_ATTACK);
      }
      uniqueEntity.setRemoveGoals(cs.getStringList("custom-ai.remove-goals"));
      uniqueEntity.setAddGoals(cs.getStringList("custom-ai.add-goals"));

      Disguise disguise = PlayerDataUtil.parseDisguise(cs.getConfigurationSection("disguise"),
          uniqueEntity.getName(), uniqueEntity.getMaxMods() > 0 || adaptiveName);

      if (disguise != null) {
        cacheDisguise(uniqueEntity, disguise);
      }

      ConfigurationSection statCs = cs.getConfigurationSection("stats");
      Map<StrifeStat, Float> attributeMap = StatUtil.getStatMapFromSection(statCs);
      uniqueEntity.setAttributeMap(attributeMap);

      uniqueEntity.setEquipment(
          plugin.getEquipmentManager()
              .buildEquipmentFromConfigSection(cs.getConfigurationSection("equipment")));

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
