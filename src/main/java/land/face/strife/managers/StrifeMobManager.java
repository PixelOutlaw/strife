package land.face.strife.managers;

import static com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils.sendMessage;
import static org.bukkit.inventory.EquipmentSlot.HAND;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import land.face.dinvy.pojo.PlayerData;
import land.face.dinvy.windows.equipment.EquipmentMenu.DeluxeSlot;
import land.face.strife.StrifePlugin;
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
    levelReqGeneric = plugin.getSettings().getString("language.level-req.generic", "");
    baseAirTicks = plugin.getSettings().getInt("config.mechanics.base-oxygen-ticks", 300);
    for (EquipmentSlot slot : EquipmentCache.EQUIPMENT_SLOTS) {
      levelReqMap.put(slot.toString(), plugin.getSettings().getString("language.level-req." + slot, ""));
    }
    for (DeluxeSlot slot : EquipmentCache.DELUXE_SLOTS) {
      levelReqMap.put(slot.toString(), plugin.getSettings().getString("language.level-req." + slot, ""));
    }
  }

  public Map<LivingEntity, StrifeMob> getMobs() {
    return trackedEntities;
  }

  public StrifeMob getStatMob(LivingEntity entity) {
    if (entity == null) {
      return null;
    }
    return getStatMob(entity, entity.getType());
  }

  public StrifeMob getStatMob(LivingEntity entity, EntityType simulationType) {
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
      mob.setStats(plugin.getMonsterManager().getBaseStats(simulationType, level));
      StatUtil.getStat(mob, StrifeStat.BARRIER);
      StatUtil.getStat(mob, StrifeStat.HEALTH);
      StatUtil.getStat(mob, StrifeStat.ENERGY);
      mob.restoreBarrier(200000);
      trackedEntities.put(entity, mob);
    }
    entity.setMaximumNoDamageTicks(0);
    return trackedEntities.get(entity);
  }

  public void saveEnergy(Player player) {
    if (trackedEntities.containsKey(player)) {
      StrifeMob mob = trackedEntities.get(player);
      player.setFoodLevel((int) (20 * mob.getEnergy() / mob.getMaxEnergy()));
    }
  }

  public void loadEnergy(Player player) {
    if (trackedEntities.containsKey(player)) {
      StrifeMob mob = trackedEntities.get(player);
      mob.setEnergy(mob.getMaxEnergy() / ((float) player.getFoodLevel() / 20));
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
    if (mob.getEntity() instanceof Player) {
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
      equipmentCache.setSlotStats(slot, getItemStats(slot, equipment, invyData));
      if (clearStatsIfReqNotMet(mob, slot, equipmentCache)) {
        continue;
      }
      equipmentCache.setSlotAbilities(slot, getItemAbilities(slot, equipment, invyData));
      equipmentCache.setSlotTraits(slot, getItemTraits(slot, equipment, invyData));
      ItemUtil.isTool(item);
    }

    if (updateItems.containsKey("HAND") && ItemUtil.isDualWield(equipment)) {
      applyDualWieldStatChanges(equipmentCache, "HAND");
      applyDualWieldStatChanges(equipmentCache, "OFF_HAND");
    }

    equipmentCache.recombine(mob);
  }

  private Set<LoreAbility> getItemAbilities(String slot, EntityEquipment equipment,
      PlayerData invyData) {
    switch (slot) {
      case "HAND" -> {
        if (ItemUtil.isArmor(equipment.getItemInMainHand().getType())) {
          return new HashSet<>();
        }
        return plugin.getLoreAbilityManager().getAbilities(equipment.getItemInMainHand());
      }
      case "OFF_HAND" -> {
        if (invyData == null) {
          return new HashSet<>();
        }
        ItemStack offhandItem = invyData.getEquipmentItem(DeluxeSlot.valueOf(slot));
        if (offhandItem == null) {
          return new HashSet<>();
        }
        if (ItemUtil.isArmor(offhandItem.getType())) {
          return new HashSet<>();
        }
        if (!ItemUtil.isValidOffhand(equipment.getItemInMainHand(), offhandItem)) {
          return new HashSet<>();
        }
        return plugin.getLoreAbilityManager().getAbilities(offhandItem);
      }
      default -> {
        if (invyData == null) {
          return new HashSet<>();
        }
        return plugin.getLoreAbilityManager()
            .getAbilities(invyData.getEquipmentItem(DeluxeSlot.valueOf(slot)));
      }
    }
  }

  private Map<StrifeStat, Float> getItemStats(String slot, EntityEquipment equipment,
      PlayerData invyData) {
    switch (slot) {
      case "HAND" -> {
        if (ItemUtil.isArmor(equipment.getItemInMainHand().getType())) {
          return new HashMap<>();
        }
        return plugin.getStatUpdateManager().getItemStats(equipment.getItemInMainHand());
      }
      case "OFF_HAND" -> {
        if (invyData == null) {
          return new HashMap<>();
        }
        ItemStack offhandItem = invyData.getEquipmentItem(DeluxeSlot.valueOf(slot));
        if (offhandItem == null) {
          return new HashMap<>();
        }
        if (ItemUtil.isArmor(offhandItem.getType())) {
          return new HashMap<>();
        }
        if (!ItemUtil.isValidOffhand(equipment.getItemInMainHand(), offhandItem)) {
          return new HashMap<>();
        }
        return plugin.getStatUpdateManager().getItemStats(offhandItem);
      }
      default -> {
        if (invyData == null) {
          return new HashMap<>();
        }
        return plugin.getStatUpdateManager()
            .getItemStats(invyData.getEquipmentItem(DeluxeSlot.valueOf(slot)));
      }
    }
  }

  private Set<StrifeTrait> getItemTraits(String slot, EntityEquipment equipment,
      PlayerData invyData) {
    switch (slot) {
      case "HAND" -> {
        if (ItemUtil.isArmor(equipment.getItemInMainHand().getType())) {
          return new HashSet<>();
        }
        return ItemUtil.getTraits(equipment.getItemInMainHand());
      }
      case "OFF_HAND" -> {
        if (invyData == null) {
          return new HashSet<>();
        }
        ItemStack offhandItem = invyData.getEquipmentItem(DeluxeSlot.valueOf(slot));
        if (offhandItem == null) {
          return new HashSet<>();
        }
        if (ItemUtil.isArmor(offhandItem.getType())) {
          return new HashSet<>();
        }
        if (!ItemUtil.isValidOffhand(equipment.getItemInMainHand(), offhandItem)) {
          return new HashSet<>();
        }
        return ItemUtil.getTraits(offhandItem);
      }
      default -> {
        if (invyData == null) {
          return new HashSet<>();
        }
        return ItemUtil.getTraits(invyData.getEquipmentItem(DeluxeSlot.valueOf(slot)));
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
    mob.updateBarrierScale();
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
