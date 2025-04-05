package land.face.strife.managers;

import static com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils.sendMessage;
import static land.face.strife.util.UberConfig.DUAL_WIELD_STAT_RATIO;
import static land.face.strife.util.UberConfig.DUAL_WIELD_BONUS_STATS;
import static org.bukkit.inventory.EquipmentSlot.HAND;

import com.tealcube.minecraft.bukkit.facecore.utilities.PaletteUtil;
import info.faceland.loot.data.pdc.EquipmentData;
import info.faceland.loot.data.pdc.ItemDetails;
import info.faceland.loot.items.NewCustomItem;
import info.faceland.loot.items.SoulGem;
import info.faceland.loot.utils.PersistHelper;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import land.face.dinvy.entity.PlayerData;
import land.face.dinvy.windows.equipment.EquipmentMenu.DeluxeSlot;
import land.face.pet.data.pdc.PetData;
import land.face.strife.StrifePlugin;
import land.face.strife.data.ItemDataBundle;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.champion.Champion;
import land.face.strife.data.champion.EquipmentCache;
import land.face.strife.stats.StrifeStat;
import land.face.strife.stats.StrifeTrait;
import land.face.strife.util.ItemUtil;
import land.face.strife.util.StatUtil;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class StrifeMobManager {

  private final StrifePlugin plugin;

  private final Map<LivingEntity, StrifeMob> trackedEntities = new HashMap<>();
  private final String levelReqGeneric;
  private final Map<String, String> levelReqMap = new HashMap<>();

  public StrifeMobManager(StrifePlugin plugin) {
    this.plugin = plugin;
    var settings = plugin.getSettings();
    levelReqGeneric = PaletteUtil.color(settings.getString("language.level-req.generic", ""));
    for (EquipmentSlot slot : EquipmentCache.EQUIPMENT_SLOTS) {
      levelReqMap.put(slot.toString(), PaletteUtil.color(settings.getString("language.level-req." + slot, "")));
    }
    for (DeluxeSlot slot : EquipmentCache.DELUXE_SLOTS) {
      levelReqMap.put(slot.toString(), PaletteUtil.color(settings.getString("language.level-req." + slot, "")));
    }
  }

  public Map<LivingEntity, StrifeMob> getMobs() {
    return trackedEntities;
  }

  public StrifeMob getStatMob(LivingEntity entity, boolean forceUpdate) {
    if (forceUpdate) {
      trackedEntities.remove(entity);
    }
    return getStatMob(entity);
  }

  public StrifeMob getStatMob(LivingEntity entity) {
    if (entity == null || entity.hasMetadata("NPC") || entity.hasMetadata("pet")) {
      return null;
    }
    if (!trackedEntities.containsKey(entity)) {
      Champion champion = entity.getType() == EntityType.PLAYER ? plugin.getChampionManager().getChampion((Player) entity) : null;
      StrifeMob mob = new StrifeMob(entity, champion);
      int level = StatUtil.getMobLevel(entity);
      mob.setStats(plugin.getMonsterManager().getBaseStats(entity.getType(), level));
      StatUtil.getStat(mob, StrifeStat.BARRIER);
      StatUtil.getStat(mob, StrifeStat.HEALTH);
      StatUtil.getStat(mob, StrifeStat.ENERGY);
      StatUtil.getStat(mob, StrifeStat.MAX_PRAYER_POINTS);
      StatUtil.getStat(mob, StrifeStat.AIR_JUMPS);
      mob.restoreBarrier(200000);
      entity.lockFreezeTicks(true);
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

  public void despawnMinions(Player player) {
    if (trackedEntities.containsKey(player)) {
      StrifeMob mob = trackedEntities.get(player);
      for (StrifeMob m : mob.getMinions()) {
        m.getEntity().remove();
      }
    }
  }

  public void updateCollisions(Player player) {
    if (!player.isOnline()) {
      return;
    }
    for (StrifeMob strifeMob : trackedEntities.values()) {
      if (strifeMob.getUniqueEntityId() == null || strifeMob.getEntity() == null) {
        continue;
      }
      if (!plugin.getUniqueEntityManager().getUnique(strifeMob.getUniqueEntityId()).isCollidable()) {
        strifeMob.getEntity().getCollidableExemptions().add(player.getUniqueId());
      }
    }
  }

  public void despawnAllTempEntities() {
    for (StrifeMob strifeMob : trackedEntities.values()) {
      if (strifeMob.getEntity() != null && strifeMob.getEntity().getType() != EntityType.PLAYER &&
          strifeMob.getEntity().isValid() && !strifeMob.getEntity().hasMetadata("NPC")) {
        strifeMob.getEntity().remove();
      }
    }
  }

  public void removeStrifeMob(LivingEntity entity) {
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
    if (!ItemUtil.doesHashMatch(handItem, equipmentCache, "HAND")) {
      updateItems.put("HAND", handItem);
    }
    boolean isPlayer = false;
    if (mob.getEntity() instanceof Player) {
      isPlayer = true;
      invyData = plugin.getDeluxeInvyPlugin().getPlayerManager().getPlayerData(((Player) mob.getEntity()).getPlayer());
      if (invyData != null) {
        for (DeluxeSlot slot : EquipmentCache.DELUXE_SLOTS) {
          ItemStack item = ItemUtil.getItem(invyData, slot);
          if (!ItemUtil.doesHashMatch(item, equipmentCache, slot.toString())) {
            updateItems.put(slot.toString(), item);
          }
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
    boolean dualWieldAllowed = true;
    for (String slot : updateItems.keySet()) {
      ItemStack item = updateItems.get(slot);
      if ("PET".equals(slot)) {
        equipmentCache.setSlotStats(slot, PetData.getPetStats(item));
        equipmentCache.setSlotAbilities(slot, new HashSet<>());
        equipmentCache.setSlotTraits(slot, new HashSet<>());
        continue;
      }
      ItemDataBundle dataBundle = getItemInfo(slot, equipment, invyData);
      if (isPlayer && !ItemUtil.meetsRequirements(item, mob)) {
        sendMessage(mob.getEntity(), levelReqMap.get(slot));
        sendMessage(mob.getEntity(), levelReqGeneric);
        equipmentCache.clearSlot(slot);
        continue;
      }
      equipmentCache.setSlotStats(slot, dataBundle.getStats());
      equipmentCache.setSlotAbilities(slot, dataBundle.getAbilities());
      equipmentCache.setSlotTraits(slot, dataBundle.getTraits());
      if (dataBundle.getTraits().contains(StrifeTrait.NO_DUAL_WIELD)) {
        dualWieldAllowed = false;
      }
    }
    if (updateItems.containsKey("HAND")) {
      assert equipment != null;
      if (ItemUtil.isDualWield(equipment)) {
        if (dualWieldAllowed) {
          applyDualWieldStatChanges(equipmentCache, "HAND");
          applyDualWieldStatChanges(equipmentCache, "OFF_HAND");
        } else {
          equipmentCache.getSlotStats("OFF_HAND").clear();
          equipmentCache.getSlotAbilities("OFF_HAND").clear();
          equipmentCache.getSlotTraits("OFF_HAND").clear();
        }
      }
    }
    applyWeaponMods(handItem, updateItems.get("OFF_HAND"), equipmentCache);
    equipmentCache.recombine(plugin, mob);
  }

  private static void applyWeaponMods(ItemStack hand, ItemStack offhand, EquipmentCache equipmentCache) {
    EquipmentData d1 = PersistHelper.getEquipmentData(hand);
    EquipmentData d2 = PersistHelper.getEquipmentData(offhand);
    Map<StrifeStat, Float> addStats = new HashMap<>();

    equipmentCache.getWeaponMods().clear();
    if (d1 != null && d1.getTier() != null && !d1.getTier().getWeaponMods().isEmpty()) {
      addStats.putAll(d1.getTier().getWeaponMods());
    }
    if (d2 != null && d2.getTier() != null && !d2.getTier().getWeaponMods().isEmpty()) {
      for (var entry : d2.getTier().getWeaponMods().entrySet()) {
        addStats.put(entry.getKey(), Math.min(addStats.getOrDefault(entry.getKey(), 0f), entry.getValue()));
      }
    }
    equipmentCache.getWeaponMods().putAll(addStats);
  }

  private ItemDataBundle getItemInfo(String slot, EntityEquipment equipment, PlayerData invyData) {
    ItemDataBundle dataBundle = new ItemDataBundle();

    dataBundle.setStats(new HashMap<>());
    dataBundle.setTraits(new HashSet<>());
    dataBundle.setAbilities(new HashSet<>());

    if (invyData == null) {
      return dataBundle;
    }

    ItemStack stack = getItemStack(slot, equipment, invyData);

    if ("HAND".equals(slot) && (ItemUtil.isArmor(stack.getType()) || ItemUtil.isAccessory(stack.getType()))) {
      return dataBundle;
    } else if ("OFF_HAND".equals(slot) && !ItemUtil.isValidOffhand(equipment.getItemInMainHand(), stack)) {
      return dataBundle;
    }

    switch (slot) {
      case "SOUL_GEM_1", "SOUL_GEM_2", "SOUL_GEM_3", "SOUL_GEM_4" -> {
        SoulGem gemb = SoulGem.get(stack);
        if (gemb != null) {
          dataBundle.getStats().putAll(StatUpdateManager.combineMaps(gemb.getStats(), gemb.getPercentStats()));
          dataBundle.getAbilities().addAll(gemb.getLoreAbilities());
          dataBundle.getTraits().addAll(gemb.getTraits());
          return dataBundle;
        }
      }
      /*
      case "PET" -> {
        PetData pet = PetData.getPetData(stack);
        if (pet != null) {
          Map<StrifeStat, Float> stats = new HashMap<>();
          for (StoredStat ss : pet.getStats()) {
            stats.put(ss.getStat().getStat(), stats.getOrDefault(ss.getStat().getStat(), 0f) + ss.getCachedValue());
          }
          dataBundle.getStats().putAll(stats);
          return dataBundle;
        }
      }
       */
    }

    // CustomOverride
    NewCustomItem nci = PersistHelper.getNonEquipmentUniqueItem(stack);
    if (nci != null && !nci.getOverrideStats().isEmpty()) {
      dataBundle.setStats(nci.getOverrideStats());
      return dataBundle;
    }

    // NEW METHOD
    EquipmentData data = PersistHelper.getEquipmentData(stack);
    if (data != null) {
      ItemDetails details = data.buildItemDetails();
      dataBundle.setStats(details.getStats());
      dataBundle.setTraits(details.getTraits());
      dataBundle.setAbilities(details.getLoreAbilities());
      return dataBundle;
    }

    // TODO: remove old method
    switch (slot) {
      case "HAND" -> {
        if (ItemUtil.isArmor(equipment.getItemInMainHand().getType()) ||
            ItemUtil.isAccessory(equipment.getItemInMainHand().getType())) {
          return dataBundle;
        }
        dataBundle.setStats(plugin.getStatUpdateManager().getItemStatsFromLore(equipment.getItemInMainHand()));
        dataBundle.setAbilities(plugin.getLoreAbilityManager().getAbilities(equipment.getItemInMainHand()));
        dataBundle.setTraits(ItemUtil.getTraits(equipment.getItemInMainHand()));
        return dataBundle;
      }
      case "OFF_HAND" -> {
        ItemStack offhandItem = invyData.getEquipmentItem(DeluxeSlot.valueOf(slot));
        if (offhandItem == null) {
          return dataBundle;
        }
        if (ItemUtil.isArmor(offhandItem.getType())) {
          return dataBundle;
        }
        if (!ItemUtil.isValidOffhand(equipment.getItemInMainHand(), offhandItem)) {
          return dataBundle;
        }
        dataBundle.setStats(plugin.getStatUpdateManager().getItemStatsFromLore(offhandItem));
        dataBundle.setAbilities(plugin.getLoreAbilityManager().getAbilities(offhandItem));
        dataBundle.setTraits(ItemUtil.getTraits(offhandItem));
        return dataBundle;
      }
      default -> {
        ItemStack stack2 = invyData.getEquipmentItem(DeluxeSlot.valueOf(slot));
        dataBundle.setStats(plugin.getStatUpdateManager().getItemStatsFromLore(stack2));
        dataBundle.setAbilities(plugin.getLoreAbilityManager().getAbilities(stack2));
        dataBundle.setTraits(ItemUtil.getTraits(stack2));
        return dataBundle;
      }
    }
  }

  private ItemStack getItemStack(String slot, EntityEquipment equipment, PlayerData invyData) {
    //noinspection SwitchStatementWithTooFewBranches
    return switch (slot) {
      case "HAND" -> equipment.getItemInMainHand();
      default -> invyData.getEquipmentItem(DeluxeSlot.valueOf(slot));
    };
  }

  public void updateEquipmentStats(LivingEntity le) {
    updateEquipmentStats(getStatMob(le));
  }

  public void updateEquipmentStats(StrifeMob mob) {
    buildEquipmentAttributes(mob);
    if (mob.getChampion() != null) {
      mob.getChampion().recombineCache(plugin);
    }
  }

  private void applyDualWieldStatChanges(EquipmentCache cache, String slot) {
    var stats = cache.getSlotStats(slot);
    stats.replaceAll((a, v) -> stats.get(a) * DUAL_WIELD_STAT_RATIO);
    for (var statEntry : DUAL_WIELD_BONUS_STATS.entrySet()) {
      stats.put(statEntry.getKey(), stats.getOrDefault(statEntry.getKey(), 0f) + statEntry.getValue());
    }
  }
}
