package land.face.strife.managers;

import static org.bukkit.attribute.Attribute.GENERIC_FOLLOW_RANGE;

import com.tealcube.minecraft.bukkit.facecore.utilities.ChunkUtil;
import com.tealcube.minecraft.bukkit.facecore.utilities.PaletteUtil;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import com.ticxo.modelengine.api.model.bone.BoneBehaviorTypes;
import io.pixeloutlaw.minecraft.spigot.config.SmartYamlConfiguration;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import land.face.learnin.LearninBooksPlugin;
import land.face.learnin.objects.LoadedKnowledge;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.UniqueEntity;
import land.face.strife.data.ability.EntityAbilitySet;
import land.face.strife.data.ability.EntityAbilitySet.TriggerAbilityType;
import land.face.strife.data.effects.Effect;
import land.face.strife.data.effects.StrifeParticle;
import land.face.strife.events.UniqueSpawnEvent;
import land.face.strife.patch.GoalPatcher;
import land.face.strife.stats.StrifeStat;
import land.face.strife.tasks.ItemPassengerTask;
import land.face.strife.util.DisguiseUtil;
import land.face.strife.util.ItemUtil;
import land.face.strife.util.LogUtil;
import land.face.strife.util.PlayerDataUtil;
import land.face.strife.util.SpecialStatusUtil;
import land.face.strife.util.StatUtil;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
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
import org.bukkit.entity.Player;
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
  private final Map<String, Disguise> cachedDisguises;
  private final List<String> globalRemoveGoals;

  public static ItemStack DEV_SADDLE;

  public UniqueEntityManager(StrifePlugin plugin) {
    this.plugin = plugin;
    this.loadedUniquesMap = new HashMap<>();
    this.cachedDisguises = new HashMap<>();
    DEV_SADDLE = new ItemStack(Material.SADDLE);
    ItemStackExtensionsKt.setCustomModelData(DEV_SADDLE, 3000);
    globalRemoveGoals = plugin.getConfig().getStringList("global-blocked-ai-goals");
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

    Entity entity = location.getWorld().spawn(location, uniqueEntity.getType().getEntityClass(),
        e -> lambdaSetup(e, uniqueEntity, location));

    if (uniqueEntity.getModelId() != null) {
      ActiveModel model = ModelEngineAPI.createActiveModel(uniqueEntity.getModelId());
      if (model == null) {
        Bukkit.getLogger().warning("Failed to load model: " + uniqueEntity.getModelId());
      } else {
        ModeledEntity modeledEntity = ModelEngineAPI.createModeledEntity(entity);
        if (modeledEntity == null) {
          Bukkit.getLogger().warning("Failed to create modelled entity");
        } else {
          modeledEntity.addModel(model, true);
          //modeledEntity.detectPlayers();
          modeledEntity.setBaseEntityVisible(false);
          StrifeMob mob = plugin.getStrifeMobManager().getStatMob((LivingEntity) entity);
          mob.setModelEntity(modeledEntity);
          if (StringUtils.isNotBlank(entity.getCustomName())) {
            TextComponent tc = LegacyComponentSerializer.legacySection().deserialize(entity.getCustomName());
            model.getBone("name").flatMap(modelBone ->
                modelBone.getBoneBehavior(BoneBehaviorTypes.NAMETAG)).ifPresent(head -> {
              head.setVisible(true);
              head.setComponent(tc);
            });
          }
        }
      }
    }

    return entity;
  }

  private void lambdaSetup(Entity entity, UniqueEntity uniqueEntity, Location location) {
    if (cachedDisguises.containsKey(uniqueEntity.getId())) {
      DisguiseAPI.disguiseToAll(entity, cachedDisguises.get(uniqueEntity.getId()).clone());
    }

    LivingEntity le = (LivingEntity) entity;
    SpecialStatusUtil.setUniqueId(entity, uniqueEntity.getId());
    le.setRemoveWhenFarAway(true);

    if (!uniqueEntity.isGravity()) {
      le.setGravity(false);
    }
    if (!uniqueEntity.isCollidable()) {
      le.getCollidableExemptions().addAll(Bukkit.getServer()
          .getOnlinePlayers().stream().map(Player::getUniqueId).toList());
    }
    if (uniqueEntity.isIgnoreTargetLevel()) {
      SpecialStatusUtil.setIgnoreTargetLevel(le);
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
        size = 2 + (int) (StrifePlugin.RNG.nextFloat() * 3);
      }
      ((Slime) le).setSize(size);
    } else if (le instanceof Phantom) {
      int size = uniqueEntity.getSize();
      if (size < 1) {
        size = 1 + (int) (StrifePlugin.RNG.nextFloat() * 3);
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
      GoalPatcher.removeGoals((Mob) le, globalRemoveGoals);
      GoalPatcher.removeGoals((Mob) le, uniqueEntity.getRemoveGoals());
      GoalPatcher.addGoals((Mob) le, uniqueEntity);
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
        for (AttributeModifier mod : attributeInstance.getModifiers()) {
          attributeInstance.removeModifier(mod);
        }
      }
    }

    if (uniqueEntity.isInvisible()) {
      le.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 99999999, 10, true, false));
    }
    if (uniqueEntity.isInvulnerable()) {
      le.setInvulnerable(true);
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

    int mobLevel = uniqueEntity.getBaseLevel();
    if (mobLevel < 0) {
      mobLevel = StatUtil.getMobLevel(le);
    }

    SpecialStatusUtil.setMobLevel(le, mobLevel);

    StrifeMob mob = plugin.getStrifeMobManager().getStatMob(le);

    if (mobLevel == 0) {
      mob.setStats(uniqueEntity.getAttributeMap());
    } else {
      mob.setStats(
          StatUpdateManager.combineMaps(mob.getBaseStats(), uniqueEntity.getAttributeMap()));
    }

    if (uniqueEntity.getMaxMods() > 0) {
      plugin.getMobModManager().doModApplication(mob, uniqueEntity.getMaxMods());
    }

    mob.setUniqueEntity(uniqueEntity);
    mob.setFactions(new HashSet<>(uniqueEntity.getFactions()));
    mob.setAlliedGuild(null);
    ChunkUtil.setDespawnOnUnload(mob.getEntity());
    mob.setCharmImmune(uniqueEntity.isCharmImmune());

    if (uniqueEntity.getBoundingBonus() != -1) {
      entity.getBoundingBox().expand(uniqueEntity.getBoundingBonus());
    }

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
          mob.addMinion(mountMob, 0, false);
        }
      }, 2L);
    }

    mob.setAbilitySet(new EntityAbilitySet(uniqueEntity.getAbilitySet()));

    Bukkit.getScheduler().runTaskLater(StrifePlugin.getInstance(), () -> {
      plugin.getStatUpdateManager().updateAllAttributes(mob);

      StatUtil.getStat(mob, StrifeStat.BARRIER);
      mob.restoreBarrier(200000);
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
    cachedDisguises.put(uniqueEntity.getId(), disguise);
  }

  public void loadUniques(List<SmartYamlConfiguration> files) {
    List<LoadedKnowledge> knowledge = new ArrayList<>();
    for (SmartYamlConfiguration uniqueEnemiesYAML : files) {
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
        uniqueEntity.setName(PaletteUtil.color(cs.getString("name", "|white|SET |red|A NAME")));
        uniqueEntity.setBonusExperience(cs.getInt("bonus-experience", 0));
        uniqueEntity.setDisplaceMultiplier(cs.getDouble("displace-multiplier", 1.0));
        uniqueEntity.setExperienceMultiplier((float) cs.getDouble("experience-multiplier", 1));
        uniqueEntity.setCharmImmune(cs.getBoolean("charm-immune", false));
        uniqueEntity.setBurnImmune(cs.getBoolean("burn-immune", false));
        uniqueEntity.setFallImmune(cs.getBoolean("fall-immune", false));
        uniqueEntity.setPushImmune(cs.getBoolean("push-immune", false));
        uniqueEntity.setIgnoreSneak(cs.getBoolean("ignore-sneak", false));
        uniqueEntity.setSaddled(cs.getBoolean("saddled", false));
        uniqueEntity.setCanTarget(cs.getBoolean("can-target", true));
        uniqueEntity.setBoundingBonus(cs.getDouble("bounding-bonus", -1));
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
        uniqueEntity.getEnemyUniques().addAll(cs.getStringList("enemy-uniques"));
        uniqueEntity.setBaby(cs.getBoolean("baby", false));
        uniqueEntity.setAngry(cs.getBoolean("angry", false));
        uniqueEntity.setZombificationImmune(cs.getBoolean("zombification-immune", true));
        String color = cs.getString("color", null);
        if (color != null) {
          uniqueEntity.setColor(DyeColor.valueOf(color));
        }
        uniqueEntity.setArmsRaised(cs.getBoolean("arms-raised", true));
        uniqueEntity.setModelId(cs.getString("model-id", null));
        uniqueEntity.setAttackDisabledOnGlobalCooldown(
            cs.getBoolean("disable-attacks-on-global-cooldown", false));
        uniqueEntity.setAlwaysRunTimer(
            cs.getBoolean("always-run-timer", false));
        uniqueEntity.setGravity(cs.getBoolean("gravity", true));
        uniqueEntity.setCollidable(cs.getBoolean("collidable", true));
        uniqueEntity.setIgnoreTargetLevel(cs.getBoolean("ignore-target-level", false));
        uniqueEntity.setVagabondAllowed(cs.getBoolean("vagabond-allowed", true));
        uniqueEntity.setMinLevelClampMult((float) cs.getDouble("min-level-clamp-mult", 0));
        uniqueEntity.setHasAI(cs.getBoolean("has-ai", true));
        uniqueEntity.setInvisible(cs.getBoolean("invisible", false));
        uniqueEntity.setInvulnerable(cs.getBoolean("invulnerable", false));
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
        uniqueEntity.setRemoveGoals(cs.getStringList("custom-ai.remove-goals"));
        uniqueEntity.setAddGoals(cs.getStringList("custom-ai.add-goals"));

        Disguise disguise = DisguiseUtil.parseDisguise(cs.getConfigurationSection("disguise"),
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
        if (StringUtils.isNotBlank(passengerItem)) {
          uniqueEntity.setItemPassenger(plugin.getEquipmentManager().getItem(passengerItem));
        }

        String particle = cs.getString("particle", "");
        if (StringUtils.isNotBlank(particle)) {
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
        uniqueEntity.getBonusKnowledge().addAll(cs.getStringList("bonus-knowledge"));
        ConfigurationSection section = cs.getConfigurationSection("knowledge");
        if (section != null) {
          uniqueEntity.getBonusKnowledge().clear();
          LoadedKnowledge loadedKnowledge = PlayerDataUtil
              .loadMobKnowledge(entityNameKey, uniqueEntity.getBaseLevel(), section);
          loadedKnowledge.getPerkOne().clear();
          loadedKnowledge.getPerkOne().add(PaletteUtil.color("|dgray|◇ -10% Dmg From This Mob"));
          loadedKnowledge.getPerkOne().add(PaletteUtil.color("|yellow|◆ -10% Dmg From This Mob"));
          loadedKnowledge.getPerkTwo().clear();
          loadedKnowledge.getPerkTwo().add(PaletteUtil.color("|dgray|◇ +10% Dmg To This Mob"));
          loadedKnowledge.getPerkTwo().add(PaletteUtil.color("|yellow|◆ +10% Dmg To This Mob"));
          loadedKnowledge.getPerkThree().clear();
          loadedKnowledge.getPerkThree().add(PaletteUtil.color("|dgray|◇ +10% Loot From This Mob"));
          loadedKnowledge.getPerkThree().add(PaletteUtil.color("|yellow|◆ +10% Loot From This Mob"));
          knowledge.add(loadedKnowledge);
        }
        if (loadedUniquesMap.containsKey(entityNameKey)) {
          Bukkit.getLogger().warning("[Strife] Loaded duplicate unique entity " + entityNameKey
              + " from " + uniqueEnemiesYAML.getFileName());
        }
        addUniqueEntity(entityNameKey, uniqueEntity);
      }
    }
    LearninBooksPlugin.instance.getKnowledgeManager().addExternalKnowledge(knowledge);
  }
}
