/**
 * The MIT License Copyright (c) 2015 Teal Cube Games
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package land.face.strife.managers;

import static com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils.sendMessage;
import static org.bukkit.inventory.EquipmentSlot.HAND;
import static org.bukkit.inventory.EquipmentSlot.OFF_HAND;

import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import land.face.strife.StrifePlugin;
import land.face.strife.data.LoreAbility;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.champion.Champion;
import land.face.strife.data.champion.ChampionSaveData;
import land.face.strife.data.champion.PlayerEquipmentCache;
import land.face.strife.data.champion.StrifeAttribute;
import land.face.strife.managers.LoreAbilityManager.TriggerType;
import land.face.strife.stats.StrifeStat;
import land.face.strife.stats.StrifeTrait;
import land.face.strife.util.ItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class ChampionManager {

  private StrifePlugin plugin;
  private String levelReqGeneric;
  private float dualWieldAttackSpeed;
  private Map<EquipmentSlot, String> levelReqMap = new HashMap<>();
  private Map<UUID, Champion> championMap = new HashMap<>();

  private final static String RESET_MESSAGE =
      "&a&lYour Levelpoints have been automatically reset due to an update!";

  public ChampionManager(StrifePlugin plugin) {
    this.plugin = plugin;
    dualWieldAttackSpeed =
        (float) plugin.getSettings().getDouble("config.mechanics.dual-wield-attack-speed", 0) / 2;
    levelReqGeneric = plugin.getSettings().getString("language.level-req.generic", "");
    for (EquipmentSlot slot : EquipmentSlot.values()) {
      levelReqMap.put(slot, plugin.getSettings().getString("language.level-req." + slot, ""));
    }
  }

  public Champion getChampion(Player player) {
    UUID uuid = player.getUniqueId();
    if (championExists(uuid)) {
      championMap.get(uuid).setPlayer(player);
      return championMap.get(uuid);
    }
    ChampionSaveData saveData = plugin.getStorage().load(player.getUniqueId());
    if (saveData == null) {
      saveData = new ChampionSaveData(player.getUniqueId());
      Champion champ = new Champion(player, saveData);
      championMap.put(uuid, champ);
      return champ;
    }
    Champion champion = new Champion(player, saveData);
    championMap.put(uuid, champion);
    return championMap.get(uuid);
  }

  public boolean championExists(UUID uuid) {
    return championMap.containsKey(uuid);
  }

  public Collection<Champion> getChampions() {
    return new HashSet<>(championMap.values());
  }

  public void tickPassiveLoreAbilities() {
    for (Player p : Bukkit.getOnlinePlayers()) {
      if (!p.isValid()) {
        continue;
      }
      Champion champion = getChampion(p);
      Set<LoreAbility> abilities = champion.getEquipmentCache().getCombinedAbilities().get(
          TriggerType.TIMER);
      if (abilities == null || abilities.isEmpty()) {
        continue;
      }
      StrifeMob mob = plugin.getStrifeMobManager().getStatMob(p);
      for (LoreAbility ability : abilities) {
        plugin.getLoreAbilityManager().applyLoreAbility(ability, mob, mob.getEntity());
      }
    }
  }

  public boolean hasPendingChanges(Player player) {
    return hasPendingChanges(getChampion(player));
  }

  public boolean hasPendingChanges(Champion champion) {
    return !champion.getLevelMap().equals(champion.getPendingLevelMap());
  }

  public void resetPendingStats(Champion champion) {
    champion.getSaveData().resetPendingStats();
  }

  public void savePendingStats(Champion champion) {
    for (StrifeAttribute stat : champion.getPendingLevelMap().keySet()) {
      if (champion.getPendingLevel(stat) > champion.getAttributeLevel(stat)) {
        sendMessage(champion.getPlayer(),
            stat.getName() + " increased to " + champion.getPendingLevel(stat) + "!");
        champion.getPlayer().playSound(champion.getPlayer().getLocation(), stat.getLevelSound(), 1f,
            stat.getLevelPitch());
      }
    }
    champion.getSaveData().savePendingStats();
  }

  public void verifyStatValues(Champion champion) {
    Player player = champion.getPlayer();
    if (getTotalChampionStats(champion) != player.getLevel()) {
      notifyResetPoints(player);
      for (StrifeAttribute stat : plugin.getAttributeManager().getAttributes()) {
        champion.setLevel(stat, 0);
      }
      champion.setHighestReachedLevel(player.getLevel());
      champion.setUnusedStatPoints(player.getLevel());
    }
  }

  public void addListOfChampions(List<Champion> champions) {
    for (Champion c : champions) {
      championMap.put(c.getUniqueId(), c);
    }
  }

  public void clearAllChampions() {
    championMap.clear();
  }

  private void buildBaseAttributes(Champion champion) {
    champion.setAttributeBaseCache(plugin.getMonsterManager().getBaseStats(champion.getPlayer()));
  }

  private void buildPointAttributes(Champion champion) {
    Map<StrifeStat, Float> attributeMap = new HashMap<>();
    for (StrifeAttribute stat : champion.getLevelMap().keySet()) {
      int statLevel = champion.getLevelMap().get(stat);
      if (statLevel == 0) {
        continue;
      }
      for (StrifeStat attr : stat.getAttributeMap().keySet()) {
        float amount = stat.getAttributeMap().get(attr) * statLevel;
        if (attributeMap.containsKey(attr)) {
          amount += attributeMap.get(attr);
        }
        attributeMap.put(attr, amount);
      }
    }
    champion.setAttributeLevelPointCache(attributeMap);
  }

  private void buildEquipmentAttributes(Champion champion) {
    EntityEquipment equipment = champion.getPlayer().getEquipment();
    PlayerEquipmentCache equipmentCache = champion.getEquipmentCache();

    Set<EquipmentSlot> updatedSlots = new HashSet<>();
    for (EquipmentSlot slot : PlayerEquipmentCache.itemSlots) {
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
      if (clearStatsIfReqNotMet(champion.getPlayer(), slot, equipmentCache)) {
        continue;
      }
      equipmentCache.setSlotAbilities(slot, getItemAbilities(slot, equipment));
      equipmentCache.setSlotTraits(slot, getItemTraits(slot, equipment));
    }

    if (updatedSlots.contains(HAND) && ItemUtil.isDualWield(equipment)) {
      applyDualWieldStatChanges(equipmentCache, HAND);
      applyDualWieldStatChanges(equipmentCache, OFF_HAND);
    }

    equipmentCache.recombine(champion);
  }

  private void applyDualWieldStatChanges(PlayerEquipmentCache cache, EquipmentSlot slot) {
    for (StrifeStat attribute : cache.getSlotStats(slot).keySet()) {
      cache.getSlotStats(slot).put(attribute, cache.getSlotStats(slot).get(attribute) * 0.7f);
    }
    cache.getSlotStats(slot).put(StrifeStat.ATTACK_SPEED,
        cache.getSlotStats(slot).getOrDefault(StrifeStat.ATTACK_SPEED, 0f) + dualWieldAttackSpeed);
  }

  private boolean clearStatsIfReqNotMet(Player p, EquipmentSlot slot, PlayerEquipmentCache cache) {
    if (!meetsLevelRequirement(p, cache.getSlotStats(slot))) {
      sendMessage(p, levelReqMap.get(slot));
      sendMessage(p, levelReqGeneric);
      cache.clearSlot(slot);
      return true;
    }
    return false;
  }

  public boolean addBoundLoreAbility(Champion champion, LoreAbility loreAbility) {
    if (champion.getSaveData().getBoundAbilities().contains(loreAbility)) {
      return false;
    }
    champion.getSaveData().getBoundAbilities().add(loreAbility);
    champion.getEquipmentCache().recombineAbilities(champion);
    return true;
  }

  public boolean removeBoundLoreAbility(Champion champion, LoreAbility loreAbility) {
    if (!champion.getSaveData().getBoundAbilities().contains(loreAbility)) {
      return false;
    }
    champion.getSaveData().getBoundAbilities().remove(loreAbility);
    champion.getEquipmentCache().recombineAbilities(champion);
    return true;
  }

  public void updatePointAttributes(Champion champion) {
    buildPointAttributes(champion);
    pushChampionUpdate(champion);
  }

  public void updateBaseAttributes(Champion champion) {
    buildBaseAttributes(champion);
    pushChampionUpdate(champion);
  }

  public void updateEquipmentStats(Player player) {
    updateEquipmentStats(getChampion(player));
  }

  public void updateEquipmentStats(Champion champion) {
    buildEquipmentAttributes(champion);
    pushChampionUpdate(champion);
  }

  public void updateAll(Champion champion) {
    buildPointAttributes(champion);
    buildBaseAttributes(champion);
    buildEquipmentAttributes(champion);

    pushChampionUpdate(champion);
  }

  private void pushChampionUpdate(Champion champion) {
    champion.recombineCache();
    plugin.getStrifeMobManager().setEntityStats(champion.getPlayer(), StatUpdateManager
        .combineMaps(champion.getCombinedCache(), plugin.getGlobalBoostManager().getAttributes()));
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

  private int getTotalChampionStats(Champion champion) {
    ChampionSaveData championSaveData = champion.getSaveData();
    int total = championSaveData.getUnusedStatPoints();
    for (StrifeAttribute stat : championSaveData.getLevelMap().keySet()) {
      total += champion.getLevelMap().getOrDefault(stat, 0);
    }
    return total;
  }

  private void notifyResetPoints(final Player player) {
    Bukkit.getScheduler().runTaskLater(plugin,
        () -> MessageUtils.sendMessage(player, RESET_MESSAGE), 20L * 3);
  }
}
