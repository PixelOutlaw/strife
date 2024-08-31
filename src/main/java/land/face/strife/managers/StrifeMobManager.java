package land.face.strife.managers;

import static com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils.sendMessage;
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
import java.util.WeakHashMap;
import land.face.dinvy.entity.PlayerData;
import land.face.dinvy.windows.equipment.EquipmentMenu.DeluxeSlot;
import land.face.strife.StrifePlugin;
import land.face.strife.data.ItemDataBundle;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.champion.Champion;
import land.face.strife.data.champion.EquipmentCache;
import land.face.strife.stats.StrifeStat;
import land.face.strife.util.ItemUtil;
import land.face.strife.util.StatUtil;
import lombok.Getter;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class StrifeMobManager {

  private final StrifePlugin plugin;

  private final WeakHashMap<LivingEntity, StrifeMob> trackedEntities = new WeakHashMap<>();
  private final String levelReqGeneric;
  private final float dualWieldAttackSpeed;
  private final float dualWieldRatio;
  private final float dualWieldExtraProjectiles;
  private final Map<String, String> levelReqMap = new HashMap<>();

  @Getter
  private final float baseAirTicks;

  public StrifeMobManager(StrifePlugin plugin) {
    this.plugin = plugin;
    dualWieldAttackSpeed =
        (float) plugin.getSettings().getDouble("config.mechanics.dual-wield-attack-speed", 0) / 2;
    dualWieldRatio =
        (float) plugin.getSettings().getDouble("config.mechanics.dual-wield-stat-mult", 0.625);
    dualWieldExtraProjectiles =
        (float) plugin.getSettings().getDouble("config.mechanics.dual-wield-extra-projectiles", 1) / 2;
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

  public WeakHashMap<LivingEntity, StrifeMob> getMobs() {
    return trackedEntities;
  }

  public StrifeMob getStatMob(LivingEntity entity) {
    if (entity == null || entity.hasMetadata("NPC") || entity.hasMetadata("pet")) {
      return null;
    }
    Champion champion = entity.getType() == EntityType.PLAYER ?
        plugin.getChampionManager().getChampion((Player) entity) : null;
    if (!trackedEntities.containsKey(entity)) {
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
    for (String slot : updateItems.keySet()) {
      ItemStack item = updateItems.get(slot);
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
    }
    if (updateItems.containsKey("HAND")) {
      assert equipment != null;
      if (ItemUtil.isDualWield(equipment)) {
        applyDualWieldStatChanges(equipmentCache, "HAND");
        applyDualWieldStatChanges(equipmentCache, "OFF_HAND");
      }
    }
    addWeaponMods(handItem, updateItems.get("OFF_HAND"), equipmentCache);

    equipmentCache.recombine(plugin, mob);
  }

  private static void addWeaponMods(ItemStack hand, ItemStack offhand, EquipmentCache equipmentCache) {
    EquipmentData d1 = PersistHelper.getEquipmentData(hand);
    EquipmentData d2 = PersistHelper.getEquipmentData(offhand);
    Map<StrifeStat, Float> addStats = new HashMap<>();

    if (d1 != null && d1.getTier() != null && !d1.getTier().getWeaponMods().isEmpty()) {
      addStats.putAll(d1.getTier().getWeaponMods());
    }
    if (d2 != null && d2.getTier() != null && !d2.getTier().getWeaponMods().isEmpty()) {
      for (var entry : d2.getTier().getWeaponMods().entrySet()) {
        addStats.put(entry.getKey(), Math.min(addStats.getOrDefault(entry.getKey(), 0f), entry.getValue()));
      }
    }
    for (var entry : addStats.entrySet()) {
      equipmentCache.getSlotStats("HAND").put(entry.getKey(),
          equipmentCache.getSlotStats("HAND").getOrDefault(entry.getKey(), 0F) + entry.getValue());
    }
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
        dataBundle.setStats(plugin.getStatUpdateManager().getItemStats(equipment.getItemInMainHand()));
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
        dataBundle.setStats(plugin.getStatUpdateManager().getItemStats(offhandItem));
        dataBundle.setAbilities(plugin.getLoreAbilityManager().getAbilities(offhandItem));
        dataBundle.setTraits(ItemUtil.getTraits(offhandItem));
        return dataBundle;
      }
      default -> {
        if (invyData == null) {
          return dataBundle;
        }
        ItemStack stack2 = invyData.getEquipmentItem(DeluxeSlot.valueOf(slot));
        dataBundle.setStats(plugin.getStatUpdateManager().getItemStats(stack2));
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
    for (StrifeStat attribute : cache.getSlotStats(slot).keySet()) {
      cache.getSlotStats(slot).put(attribute, cache.getSlotStats(slot).get(attribute) * dualWieldRatio);
    }
    cache.getSlotStats(slot).put(StrifeStat.ATTACK_SPEED,
        cache.getSlotStats(slot).getOrDefault(StrifeStat.ATTACK_SPEED, 0f) + dualWieldAttackSpeed);
    cache.getSlotStats(slot).put(StrifeStat.EXTRA_PROJECTILES,
        cache.getSlotStats(slot).getOrDefault(StrifeStat.EXTRA_PROJECTILES, 0f) + dualWieldExtraProjectiles);
  }
}
