package land.face.strife.managers;

import static com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils.sendMessage;
import static org.bukkit.inventory.EquipmentSlot.HAND;

import com.tealcube.minecraft.bukkit.facecore.utilities.PaletteUtil;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import land.face.dinvy.pojo.PlayerData;
import land.face.dinvy.windows.equipment.EquipmentMenu.DeluxeSlot;
import land.face.strife.StrifePlugin;
import land.face.strife.data.ItemDataBundle;
import land.face.strife.data.LoreAbility;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.champion.EquipmentCache;
import land.face.strife.stats.StrifeStat;
import land.face.strife.stats.StrifeTrait;
import land.face.strife.util.ItemUtil;
import land.face.strife.util.SpecialStatusUtil;
import land.face.strife.util.StatUtil;
import lombok.Getter;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class StrifeMobManager {

  private final StrifePlugin plugin;
  private final Map<LivingEntity, StrifeMob> trackedEntities = new WeakHashMap<>();

  private final String levelReqGeneric;
  private final float dualWieldAttackSpeed;
  private final Map<String, String> levelReqMap = new HashMap<>();

  @Getter
  private final float baseAirTicks;

  public StrifeMobManager(StrifePlugin plugin) {
    this.plugin = plugin;
    dualWieldAttackSpeed =
        (float) plugin.getSettings().getDouble("config.mechanics.dual-wield-attack-speed", 0) / 2;
    levelReqGeneric = PaletteUtil.color(plugin.getSettings()
        .getString("language.level-req.generic", ""));
    baseAirTicks = plugin.getSettings().getInt("config.mechanics.base-oxygen-ticks", 300);
    for (EquipmentSlot slot : EquipmentCache.EQUIPMENT_SLOTS) {
      levelReqMap.put(slot.toString(), PaletteUtil.color(
          plugin.getSettings().getString("language.level-req." + slot, "")));
    }
    for (DeluxeSlot slot : EquipmentCache.DELUXE_SLOTS) {
      levelReqMap.put(slot.toString(), PaletteUtil.color(
          plugin.getSettings().getString("language.level-req." + slot, "")));
    }
  }

  public Map<LivingEntity, StrifeMob> getMobs() {
    return trackedEntities;
  }

  public StrifeMob getStatMob(LivingEntity entity) {
    if (entity == null) {
      return null;
    }
    if (!trackedEntities.containsKey(entity)) {
      StrifeMob mob;
      if (entity.getType() == EntityType.PLAYER) {
        mob = new StrifeMob(plugin.getChampionManager().getChampion((Player) entity));
      } else {
        mob = new StrifeMob(entity);
      }
      int level = StatUtil.getMobLevel(entity);
      mob.setStats(plugin.getMonsterManager().getBaseStats(entity.getType(), level));
      StatUtil.getStat(mob, StrifeStat.BARRIER);
      StatUtil.getStat(mob, StrifeStat.HEALTH);
      StatUtil.getStat(mob, StrifeStat.ENERGY);
      mob.restoreBarrier(200000);
      trackedEntities.put(entity, mob);
      if (entity instanceof Player) {
        mob.setEnergy(mob.getMaxEnergy() * ((Player) entity).getFoodLevel() / 20);
      }
    }
    entity.setMaximumNoDamageTicks(0);
    return trackedEntities.get(entity);
  }

  public void saveEnergy(Player player) {
    if (trackedEntities.containsKey(player)) {
      StrifeMob mob = trackedEntities.get(player);
      float value = 20f * mob.getEnergy();
      player.setFoodLevel((int) (value / mob.getMaxEnergy()));
    }
  }

  public void updateCollisions(Player player) {
    for (StrifeMob strifeMob : trackedEntities.values()) {
      if (strifeMob.getUniqueEntityId() == null) {
        continue;
      }
      if (!plugin.getUniqueEntityManager()
          .getUnique(strifeMob.getUniqueEntityId()).isCollidable()) {
        strifeMob.getEntity().getCollidableExemptions().add(player.getUniqueId());
      }
    }
  }

  public void despawnAllTempEntities() {
    for (StrifeMob strifeMob : trackedEntities.values()) {
      if (strifeMob.getEntity().getType() != EntityType.PLAYER && strifeMob.getEntity().isValid()) {
        strifeMob.getEntity().remove();
      }
    }
  }

  public void removeStrifeMob(LivingEntity entity) {
    if (entity.getPassengers().size() > 0 && entity.getPassengers().get(0) instanceof Item) {
      entity.getPassengers().get(0).remove();
    }
    trackedEntities.remove(entity);
  }

  public boolean isTrackedEntity(LivingEntity entity) {
    return trackedEntities.containsKey(entity);
  }

  private void buildEquipmentAttributes(StrifeMob mob) {
    EntityEquipment equipment = mob.getEntity().getEquipment();
    PlayerData invyData = null;
    EquipmentCache equipmentCache = mob.getEquipmentCache();

    Map<String, ItemStack> updateItems = new HashMap<>();

    ItemStack handItem = ItemUtil.getItem(equipment, HAND);
    if (!ItemUtil.doesHashMatch(handItem, equipmentCache.getSlotHash("HAND"))) {
      updateItems.put("HAND", handItem);
    }
    boolean isPlayer = false;
    if (mob.getEntity() instanceof Player) {
      isPlayer = true;
      invyData = plugin.getDeluxeInvyPlugin().getPlayerManager()
          .getPlayerData(((Player) mob.getEntity()).getPlayer());
      for (DeluxeSlot slot : EquipmentCache.DELUXE_SLOTS) {
        ItemStack item = ItemUtil.getItem(invyData, slot);
        if (!ItemUtil.doesHashMatch(item, equipmentCache.getSlotHash(slot.toString()))) {
          updateItems.put(slot.toString(), item);
        }
      }
    }

    if (updateItems.isEmpty()) {
      return;
    }
    equipmentCache.setLastUpdate(System.currentTimeMillis());

    if (updateItems.containsKey("HAND")) {
      updateItems.put("OFF_HAND", updateItems.get("OFF_HAND"));
    } else if (updateItems.containsKey("OFF_HAND")) {
      updateItems.put("HAND", handItem);
    }
    for (String slot : updateItems.keySet()) {
      ItemStack item = updateItems.get(slot);
      equipmentCache.setSlotHash(slot, ItemUtil.hashItem(item));
      ItemDataBundle dataBundle = getItemInfo(slot, equipment, invyData);
      if (isPlayer && !ItemUtil.meetsLevelRequirement(item, mob.getLevel())) {
        sendMessage(mob.getEntity(), levelReqMap.get(slot));
        sendMessage(mob.getEntity(), levelReqGeneric);
        equipmentCache.clearSlot(slot);
        continue;
      }
      equipmentCache.setSlotStats(slot, dataBundle.getStats());
      equipmentCache.setSlotAbilities(slot, dataBundle.getAbilities());
      equipmentCache.setSlotTraits(slot, dataBundle.getTraits());
    }
    if (updateItems.containsKey("HAND") && ItemUtil.isDualWield(equipment)) {
      applyDualWieldStatChanges(equipmentCache, "HAND");
      applyDualWieldStatChanges(equipmentCache, "OFF_HAND");
    }
    equipmentCache.recombine(mob);
  }

  private ItemDataBundle getItemInfo(String slot, EntityEquipment equipment,
      PlayerData invyData) {
    ItemDataBundle dataBundle = new ItemDataBundle();
    switch (slot) {
      case "HAND" -> {
        if (ItemUtil.isArmor(equipment.getItemInMainHand().getType())) {
          dataBundle.setStats(new HashMap<>());
          dataBundle.setTraits(new HashSet<>());
          dataBundle.setAbilities(new HashSet<>());
          return dataBundle;
        }
        dataBundle.setStats(plugin.getStatUpdateManager()
            .getItemStats(equipment.getItemInMainHand()));
        dataBundle.setAbilities(plugin.getLoreAbilityManager()
            .getAbilities(equipment.getItemInMainHand()));
        dataBundle.setTraits(ItemUtil.getTraits(equipment.getItemInMainHand()));
        return dataBundle;
      }
      case "OFF_HAND" -> {
        if (invyData == null) {
          dataBundle.setStats(new HashMap<>());
          dataBundle.setTraits(new HashSet<>());
          dataBundle.setAbilities(new HashSet<>());
          return dataBundle;
        }
        ItemStack offhandItem = invyData.getEquipmentItem(DeluxeSlot.valueOf(slot));
        if (offhandItem == null) {
          dataBundle.setStats(new HashMap<>());
          dataBundle.setTraits(new HashSet<>());
          dataBundle.setAbilities(new HashSet<>());
          return dataBundle;
        }
        if (ItemUtil.isArmor(offhandItem.getType())) {
          dataBundle.setStats(new HashMap<>());
          dataBundle.setTraits(new HashSet<>());
          dataBundle.setAbilities(new HashSet<>());
          return dataBundle;
        }
        if (!ItemUtil.isValidOffhand(equipment.getItemInMainHand(), offhandItem)) {
          dataBundle.setStats(new HashMap<>());
          dataBundle.setTraits(new HashSet<>());
          dataBundle.setAbilities(new HashSet<>());
          return dataBundle;
        }
        dataBundle.setStats(plugin.getStatUpdateManager().getItemStats(offhandItem));
        dataBundle.setAbilities(plugin.getLoreAbilityManager().getAbilities(offhandItem));
        dataBundle.setTraits(ItemUtil.getTraits(offhandItem));
        return dataBundle;
      }
      default -> {
        if (invyData == null) {
          dataBundle.setStats(new HashMap<>());
          dataBundle.setTraits(new HashSet<>());
          dataBundle.setAbilities(new HashSet<>());
          return dataBundle;
        }
        ItemStack stack = invyData.getEquipmentItem(DeluxeSlot.valueOf(slot));
        dataBundle.setStats(plugin.getStatUpdateManager().getItemStats(stack));
        dataBundle.setAbilities(plugin.getLoreAbilityManager().getAbilities(stack));
        dataBundle.setTraits(ItemUtil.getTraits(stack));
        return dataBundle;
      }
    }
  }

  private boolean meetsLevelRequirement(Player player, Map<StrifeStat, Float> statMap) {
    return Math.round(statMap.getOrDefault(StrifeStat.LEVEL_REQUIREMENT, 0f)) <= player.getLevel();
  }

  public void updateEquipmentStats(LivingEntity le) {
    updateEquipmentStats(getStatMob(le));
  }

  public void updateEquipmentStats(StrifeMob mob) {
    buildEquipmentAttributes(mob);
    if (mob.getChampion() != null) {
      mob.getChampion().recombineCache();
    }
  }

  private void applyDualWieldStatChanges(EquipmentCache cache, String slot) {
    for (StrifeStat attribute : cache.getSlotStats(slot).keySet()) {
      cache.getSlotStats(slot).put(attribute, cache.getSlotStats(slot).get(attribute) * 0.7f);
    }
    cache.getSlotStats(slot).put(StrifeStat.ATTACK_SPEED,
        cache.getSlotStats(slot).getOrDefault(StrifeStat.ATTACK_SPEED, 0f) + dualWieldAttackSpeed);
  }

  private boolean clearStatsIfReqNotMet(StrifeMob mob, String slot, EquipmentCache cache) {
    if (mob.getChampion() == null) {
      return false;
    }
    Player p = mob.getChampion().getPlayer();
    if (!meetsLevelRequirement(p, cache.getSlotStats(slot))) {
      sendMessage(p, levelReqMap.get(slot));
      sendMessage(p, levelReqGeneric);
      cache.clearSlot(slot);
      return true;
    }
    return false;
  }
}
