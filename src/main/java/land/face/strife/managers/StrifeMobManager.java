package land.face.strife.managers;

import static com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils.sendMessage;
import static org.bukkit.inventory.EquipmentSlot.HAND;
import static org.bukkit.inventory.EquipmentSlot.OFF_HAND;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import land.face.strife.StrifePlugin;
import land.face.strife.data.LoreAbility;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.champion.EquipmentCache;
import land.face.strife.data.effects.FiniteUsesEffect;
import land.face.strife.stats.StrifeStat;
import land.face.strife.stats.StrifeTrait;
import land.face.strife.util.ItemUtil;
import land.face.strife.util.SpecialStatusUtil;
import land.face.strife.util.StatUtil;
import org.bukkit.entity.Entity;
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
  private final Map<EquipmentSlot, String> levelReqMap = new HashMap<>();

  public StrifeMobManager(StrifePlugin plugin) {
    this.plugin = plugin;
    dualWieldAttackSpeed = (float) plugin.getSettings().getDouble("config.mechanics.dual-wield-attack-speed", 0) / 2;
    levelReqGeneric = plugin.getSettings().getString("language.level-req.generic", "");
    for (EquipmentSlot slot : EquipmentSlot.values()) {
      levelReqMap.put(slot, plugin.getSettings().getString("language.level-req." + slot, ""));
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
      StrifeMob strifeMob;
      if (entity instanceof Player) {
        strifeMob = new StrifeMob(plugin.getChampionManager().getChampion((Player) entity));
      } else {
        strifeMob = new StrifeMob(entity);
      }
      strifeMob.setStats(plugin.getMonsterManager().getBaseStats(entity));
      strifeMob.restoreBarrier(200000);
      strifeMob.setEnergy(entity instanceof Player ?
          StatUtil.updateMaxEnergy(strifeMob) * ((Player) entity).getFoodLevel() / 20 : 200000);
      trackedEntities.put(entity, strifeMob);
    }
    entity.setMaximumNoDamageTicks(0);
    return trackedEntities.get(entity);
  }

  public void addFiniteEffect(StrifeMob mob, LoreAbility loreAbility, int uses, int maxDuration) {
    for (FiniteUsesEffect finiteUsesEffect : mob.getTempEffects()) {
      if (finiteUsesEffect.getExpiration() > System.currentTimeMillis()) {
        mob.getTempEffects().remove(finiteUsesEffect);
        continue;
      }
      if (finiteUsesEffect.getLoreAbility() == loreAbility) {
        finiteUsesEffect.setExpiration(System.currentTimeMillis() + maxDuration);
        finiteUsesEffect.setUses(Math.max(finiteUsesEffect.getUses(), uses));
        return;
      }
    }
    FiniteUsesEffect finiteUsesEffect = new FiniteUsesEffect();
    finiteUsesEffect.setExpiration(System.currentTimeMillis() + maxDuration);
    finiteUsesEffect.setUses(uses);
    mob.getTempEffects().add(finiteUsesEffect);
  }

  public void despawnAllTempEntities() {
    for (StrifeMob strifeMob : trackedEntities.values()) {
      if (strifeMob.getEntity().isValid() && SpecialStatusUtil.isDespawnOnUnload(strifeMob.getEntity())) {
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

  public void doChunkDespawn(Entity entity) {
    if (entity instanceof LivingEntity) {
      removeStrifeMob((LivingEntity) entity);
    }
    if (SpecialStatusUtil.isDespawnOnUnload(entity)) {
      entity.remove();
    }
  }

  public boolean isTrackedEntity(LivingEntity entity) {
    return trackedEntities.containsKey(entity);
  }

  private void buildEquipmentAttributes(StrifeMob mob) {
    EntityEquipment equipment = mob.getEntity().getEquipment();
    EquipmentCache equipmentCache = mob.getEquipmentCache();

    Set<EquipmentSlot> updatedSlots = new HashSet<>();
    for (EquipmentSlot slot : EquipmentCache.ITEM_SLOTS) {
      ItemStack item = ItemUtil.getItem(equipment, slot);
      if (!ItemUtil.doesHashMatch(item, equipmentCache.getSlotHash(slot))) {
        updatedSlots.add(slot);
      }
    }

    if (updatedSlots.contains(HAND)) {
      updatedSlots.add(OFF_HAND);
    } else if (updatedSlots.contains(OFF_HAND)) {
      updatedSlots.add(HAND);
    }

    for (EquipmentSlot slot : updatedSlots) {
      ItemStack item = ItemUtil.getItem(equipment, slot);
      equipmentCache.setSlotHash(slot, ItemUtil.hashItem(item));

      equipmentCache.setSlotStats(slot, getItemStats(slot, equipment));
      if (clearStatsIfReqNotMet(mob, slot, equipmentCache)) {
        continue;
      }
      equipmentCache.setSlotAbilities(slot, getItemAbilities(slot, equipment));
      equipmentCache.setSlotTraits(slot, getItemTraits(slot, equipment));
    }

    if (updatedSlots.contains(HAND) && ItemUtil.isDualWield(equipment)) {
      applyDualWieldStatChanges(equipmentCache, HAND);
      applyDualWieldStatChanges(equipmentCache, OFF_HAND);
    }

    equipmentCache.recombine(mob);
  }

  private Set<LoreAbility> getItemAbilities(EquipmentSlot slot, EntityEquipment equipment) {
    switch (slot) {
      case HAND:
        if (ItemUtil.isArmor(equipment.getItemInMainHand().getType())) {
          return new HashSet<>();
        }
        return plugin.getLoreAbilityManager().getAbilities(equipment.getItemInMainHand());
      case OFF_HAND:
        if (ItemUtil.isArmor(equipment.getItemInMainHand().getType())) {
          return new HashSet<>();
        }
        if (!ItemUtil.isValidOffhand(equipment)) {
          return new HashSet<>();
        }
        return plugin.getLoreAbilityManager().getAbilities(equipment.getItemInOffHand());
      case HEAD:
        return plugin.getLoreAbilityManager().getAbilities(equipment.getHelmet());
      case CHEST:
        return plugin.getLoreAbilityManager().getAbilities(equipment.getChestplate());
      case LEGS:
        return plugin.getLoreAbilityManager().getAbilities(equipment.getLeggings());
      case FEET:
        return plugin.getLoreAbilityManager().getAbilities(equipment.getBoots());
      default:
        return new HashSet<>();
    }
  }

  private Map<StrifeStat, Float> getItemStats(EquipmentSlot slot, EntityEquipment equipment) {
    switch (slot) {
      case HAND:
        if (ItemUtil.isArmor(equipment.getItemInMainHand().getType())) {
          return new HashMap<>();
        }
        return plugin.getStatUpdateManager().getItemStats(equipment.getItemInMainHand());
      case OFF_HAND:
        if (ItemUtil.isArmor(equipment.getItemInMainHand().getType())) {
          return new HashMap<>();
        }
        if (!ItemUtil.isValidOffhand(equipment)) {
          return new HashMap<>();
        }
        return plugin.getStatUpdateManager().getItemStats(equipment.getItemInOffHand());
      case HEAD:
        return plugin.getStatUpdateManager().getItemStats(equipment.getHelmet());
      case CHEST:
        return plugin.getStatUpdateManager().getItemStats(equipment.getChestplate());
      case LEGS:
        return plugin.getStatUpdateManager().getItemStats(equipment.getLeggings());
      case FEET:
        return plugin.getStatUpdateManager().getItemStats(equipment.getBoots());
      default:
        return new HashMap<>();
    }
  }

  private Set<StrifeTrait> getItemTraits(EquipmentSlot slot, EntityEquipment equipment) {
    switch (slot) {
      case HAND:
        return ItemUtil.getTraits(equipment.getItemInMainHand());
      case OFF_HAND:
        if (!ItemUtil.isValidOffhand(equipment)) {
          return new HashSet<>();
        }
        return ItemUtil.getTraits(equipment.getItemInOffHand());
      case HEAD:
        return ItemUtil.getTraits(equipment.getHelmet());
      case CHEST:
        return ItemUtil.getTraits(equipment.getChestplate());
      case LEGS:
        return ItemUtil.getTraits(equipment.getLeggings());
      case FEET:
        return ItemUtil.getTraits(equipment.getBoots());
      default:
        return new HashSet<>();
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

  private void applyDualWieldStatChanges(EquipmentCache cache, EquipmentSlot slot) {
    for (StrifeStat attribute : cache.getSlotStats(slot).keySet()) {
      cache.getSlotStats(slot).put(attribute, cache.getSlotStats(slot).get(attribute) * 0.7f);
    }
    cache.getSlotStats(slot).put(StrifeStat.ATTACK_SPEED,
        cache.getSlotStats(slot).getOrDefault(StrifeStat.ATTACK_SPEED, 0f) + dualWieldAttackSpeed);
  }

  private boolean clearStatsIfReqNotMet(StrifeMob mob, EquipmentSlot slot, EquipmentCache cache) {
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
