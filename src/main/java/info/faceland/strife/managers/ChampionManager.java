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
package info.faceland.strife.managers;

import static com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils.sendMessage;
import static info.faceland.strife.stats.StrifeStat.ATTACK_SPEED;
import static info.faceland.strife.stats.StrifeStat.LEVEL_REQUIREMENT;
import static info.faceland.strife.util.ItemUtil.doesHashMatch;
import static info.faceland.strife.util.ItemUtil.getItem;
import static info.faceland.strife.util.ItemUtil.hashItem;
import static info.faceland.strife.util.ItemUtil.removeAttributes;
import static org.bukkit.inventory.EquipmentSlot.CHEST;
import static org.bukkit.inventory.EquipmentSlot.FEET;
import static org.bukkit.inventory.EquipmentSlot.HAND;
import static org.bukkit.inventory.EquipmentSlot.HEAD;
import static org.bukkit.inventory.EquipmentSlot.LEGS;
import static org.bukkit.inventory.EquipmentSlot.OFF_HAND;

import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;
import info.faceland.strife.StrifePlugin;
import info.faceland.strife.data.LoreAbility;
import info.faceland.strife.data.champion.Champion;
import info.faceland.strife.data.champion.ChampionSaveData;
import info.faceland.strife.data.champion.PlayerEquipmentCache;
import info.faceland.strife.data.champion.StrifeAttribute;
import info.faceland.strife.stats.StrifeStat;
import info.faceland.strife.stats.StrifeTrait;
import info.faceland.strife.util.ItemUtil;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class ChampionManager {

  private final StrifePlugin plugin;
  private final Map<UUID, Champion> championMap = new HashMap<>();
  private final Map<EquipmentSlot, String> levelReqMap = new HashMap<>();
  private final String levelReqGeneric;

  private final static String RESET_MESSAGE =
      "&a&lYour Levelpoints have been automatically reset due to an update!";

  public ChampionManager(StrifePlugin plugin) {
    this.plugin = plugin;
    this.levelReqGeneric = plugin.getSettings().getString("language.level-req.generic", "");
    this.levelReqMap.put(HAND, plugin.getSettings().getString("language.level-req.main", ""));
    this.levelReqMap.put(OFF_HAND, plugin.getSettings().getString("language.level-req.off", ""));
    this.levelReqMap.put(HEAD, plugin.getSettings().getString("language.level-req.head", ""));
    this.levelReqMap.put(CHEST, plugin.getSettings().getString("language.level-req.body", ""));
    this.levelReqMap.put(LEGS, plugin.getSettings().getString("language.level-req.legs", ""));
    this.levelReqMap.put(FEET, plugin.getSettings().getString("language.level-req.feet", ""));
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
    Map<StrifeStat, Double> attributeDoubleMap = new HashMap<>();
    for (StrifeAttribute stat : champion.getLevelMap().keySet()) {
      int statLevel = champion.getLevelMap().get(stat);
      if (statLevel == 0) {
        continue;
      }
      for (StrifeStat attr : stat.getAttributeMap().keySet()) {
        double amount = stat.getAttributeMap().get(attr) * statLevel;
        if (attributeDoubleMap.containsKey(attr)) {
          amount += attributeDoubleMap.get(attr);
        }
        attributeDoubleMap.put(attr, amount);
      }
    }
    champion.setAttributeLevelPointCache(attributeDoubleMap);
  }

  private void buildEquipmentAttributes(Champion champion) {
    EntityEquipment equipment = champion.getPlayer().getEquipment();
    PlayerEquipmentCache equipmentCache = champion.getEquipmentCache();

    boolean recombine = false;

    for (EquipmentSlot slot : PlayerEquipmentCache.itemSlots) {
      ItemStack item = getItem(equipment, slot);
      if (!doesHashMatch(item, equipmentCache.getSlotHash(slot))) {
        equipmentCache.setSlotStats(slot, getItemStats(slot, equipment));
        equipmentCache.setSlotAbilities(slot, getItemAbilities(slot, equipment));
        equipmentCache.setSlotTraits(slot, getItemTraits(slot, equipment));
        if ((slot == HAND || slot == OFF_HAND) && ItemUtil.isDualWield(equipment)) {
          applyDualWieldStatChanges(equipmentCache, slot);
        }
        removeAttributes(item);
        equipmentCache.setSlotHash(slot, hashItem(item));
        recombine = true;
      }
    }

    for (EquipmentSlot slot : PlayerEquipmentCache.itemSlots) {
      clearStatsIfReqNotMet(champion.getPlayer(), slot, equipmentCache);
    }

    if (recombine) {
      equipmentCache.recombine(champion);
    }
  }

  private void applyDualWieldStatChanges(PlayerEquipmentCache cache, EquipmentSlot slot) {
    for (StrifeStat attribute : cache.getSlotStats(slot).keySet()) {
      cache.getSlotStats(slot).put(attribute, cache.getSlotStats(slot).get(attribute) * 0.7D);
    }
    cache.getSlotStats(slot)
        .put(ATTACK_SPEED, cache.getSlotStats(slot).getOrDefault(ATTACK_SPEED, 0D) + 25D);
  }

  private void clearStatsIfReqNotMet(Player p, EquipmentSlot slot, PlayerEquipmentCache cache) {
    if (!meetsLevelRequirement(p, cache.getSlotStats(slot))) {
      sendMessage(p, levelReqMap.get(slot));
      sendMessage(p, levelReqGeneric);
      cache.clearSlot(slot);
    }
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
        return plugin.getLoreAbilityManager().getAbilities(equipment.getItemInMainHand());
      case OFF_HAND:
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

  private Map<StrifeStat, Double> getItemStats(EquipmentSlot slot, EntityEquipment equipment) {
    switch (slot) {
      case HAND:
        return plugin.getStatUpdateManager().getItemStats(equipment.getItemInMainHand());
      case OFF_HAND:
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

  private boolean meetsLevelRequirement(Player player, Map<StrifeStat, Double> statMap) {
    return statMap.getOrDefault(LEVEL_REQUIREMENT, 0D) <= player.getLevel();
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
