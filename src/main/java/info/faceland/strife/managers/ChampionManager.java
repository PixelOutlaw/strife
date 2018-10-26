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

import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;
import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;
import info.faceland.strife.StrifePlugin;
import info.faceland.strife.attributes.StrifeAttribute;
import info.faceland.strife.data.Champion;

import info.faceland.strife.data.ChampionSaveData;
import info.faceland.strife.stats.StrifeStat;
import info.faceland.strife.util.ItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class ChampionManager {

  private final StrifePlugin plugin;
  private Map<UUID, Champion> championMap;

  private final static String LVL_REQ_MAIN_WEAPON =
      "<red>You do not meet the level requirement for your weapon! " +
          "It will not give you any stats when used!";
  private final static String LVL_REQ_OFF_WEAPON =
      "<red>You do not meet the level requirement for your offhand " +
          "item! It will not give you any stats when used!";
  private final static String LVL_REQ_ARMOR =
      "<red>You do not meet the level requirement for a piece of your " +
          "armor! It will not give you any stats while equipped!";

  public ChampionManager(StrifePlugin plugin) {
    this.plugin = plugin;
    championMap = new HashMap<>();
  }

  public Champion getChampion(UUID uuid) {
    return getChampion(uuid, false);
  }

  public Champion getChampion(UUID uuid, boolean forceRefresh) {
    if (uuid == null) {
      return null;
    }
    if (forceRefresh || !hasChampion(uuid)) {
      ChampionSaveData saveData = plugin.getStorage().load(uuid);
      Player player = Bukkit.getPlayer(uuid);
      if (saveData != null) {
        Champion champion = new Champion(player, saveData);
        championMap.put(uuid, champion);
        return champion;
      }
      return createChampion(player, uuid);
    }
    return championMap.get(uuid);
  }

  public boolean hasChampion(UUID uuid) {
    return uuid != null && championMap.containsKey(uuid);
  }

  public Champion createChampion(Player player, UUID uuid) {
    ChampionSaveData saveData = new ChampionSaveData(uuid);
    Champion champ = new Champion(player, saveData);
    championMap.put(uuid, champ);
    return champ;
  }

  public void addChampion(Champion champion) {
    if (!hasChampion(champion.getUniqueId())) {
      championMap.put(champion.getUniqueId(), champion);
    }
  }

  public void removeChampion(UUID uuid) {
    if (hasChampion(uuid)) {
      championMap.remove(uuid);
    }
  }

  private void getBaseAttributes(Champion champion) {
    champion.setAttributeBaseCache(plugin.getMonsterManager().getBaseStats(champion.getPlayer()));
  }

  public void getLevelPointAttributes(Champion champion) {
    Map<StrifeAttribute, Double> attributeDoubleMap = new HashMap<>();
    for (Map.Entry<StrifeStat, Integer> entry : champion.getLevelMap().entrySet()) {
      for (Map.Entry<StrifeAttribute, Double> pointSection : entry.getKey().getAttributeMap()
          .entrySet()) {
        double amount = pointSection.getValue() * entry.getValue();
        if (attributeDoubleMap.containsKey(pointSection.getKey())) {
          amount += attributeDoubleMap.get(pointSection.getKey());
        }
        attributeDoubleMap.put(pointSection.getKey(), amount);
      }
    }
    champion.setAttributeLevelPointCache(attributeDoubleMap);
  }

  public void getArmorAttributes(Champion champion) {
    Map<StrifeAttribute, Double> attributeDoubleMap = new HashMap<>();
    boolean spam = false;
    for (ItemStack itemStack : champion.getPlayer().getEquipment().getArmorContents()) {
      if (itemStack == null || itemStack.getType() == Material.AIR) {
        continue;
      }
      removeAttributes(itemStack);
      Map<StrifeAttribute, Double> itemStatMap = plugin.getAttributeUpdateManager()
          .getItemStats(itemStack);
      if (itemStatMap.containsKey(StrifeAttribute.LEVEL_REQUIREMENT)) {
        if (champion.getPlayer().getLevel() < itemStatMap.get(StrifeAttribute.LEVEL_REQUIREMENT)) {
          spam = true;
          continue;
        }
      }
      attributeDoubleMap = AttributeUpdateManager.combineMaps(attributeDoubleMap, itemStatMap);
    }
    if (spam) {
      MessageUtils.sendMessage(champion.getPlayer(), ChampionManager.LVL_REQ_ARMOR);
    }
    champion.setAttributeArmorCache(attributeDoubleMap);
  }

  public void getWeaponAttributes(Champion champion) {
    Map<StrifeAttribute, Double> attributeDoubleMap = new HashMap<>();
    ItemStack mainHandItemStack = champion.getPlayer().getEquipment().getItemInMainHand();
    ItemStack offHandItemStack = champion.getPlayer().getEquipment().getItemInOffHand();
    if (mainHandItemStack != null && mainHandItemStack.getType() != Material.AIR && !ItemUtil
        .isArmor(mainHandItemStack.getType())) {
      removeAttributes(mainHandItemStack);
      Map<StrifeAttribute, Double> itemStatMap = plugin.getAttributeUpdateManager()
          .getItemStats(mainHandItemStack);
      if (itemStatMap.containsKey(StrifeAttribute.LEVEL_REQUIREMENT)
          && champion.getPlayer().getLevel() < itemStatMap
          .get(StrifeAttribute.LEVEL_REQUIREMENT)) {
        MessageUtils.sendMessage(champion.getPlayer(), ChampionManager.LVL_REQ_MAIN_WEAPON);
      } else {
        attributeDoubleMap = AttributeUpdateManager
            .combineMaps(attributeDoubleMap, itemStatMap);
      }
    }
    if (offHandItemStack != null && offHandItemStack.getType() != Material.AIR && !ItemUtil
        .isArmor(offHandItemStack.getType())) {
      removeAttributes(offHandItemStack);
      double dualWieldEfficiency = ItemUtil
          .getDualWieldEfficiency(mainHandItemStack, offHandItemStack);
      Map<StrifeAttribute, Double> itemStatMap = plugin.getAttributeUpdateManager()
          .getItemStats(offHandItemStack, dualWieldEfficiency);
      if (itemStatMap.containsKey(StrifeAttribute.LEVEL_REQUIREMENT)
          && champion.getPlayer().getLevel() < itemStatMap
          .get(StrifeAttribute.LEVEL_REQUIREMENT) / dualWieldEfficiency) {
        MessageUtils.sendMessage(champion.getPlayer(), ChampionManager.LVL_REQ_OFF_WEAPON);
      } else {
        attributeDoubleMap = AttributeUpdateManager
            .combineMaps(attributeDoubleMap, itemStatMap);
      }
    }
    champion.setAttributeWeaponCache(attributeDoubleMap);
  }

  public void updateWeapons(Champion champion) {
    getWeaponAttributes(champion);
    champion.recombineCache();
  }

  public void updateArmor(Champion champion) {
    getArmorAttributes(champion);
    champion.recombineCache();
  }

  public void updateLevelPoints(Champion champion) {
    getLevelPointAttributes(champion);
    champion.recombineCache();
  }

  public void updateBase(Champion champion) {
    getBaseAttributes(champion);
    champion.recombineCache();
  }

  public void updateAll(Champion champion) {
    getWeaponAttributes(champion);
    getArmorAttributes(champion);
    getLevelPointAttributes(champion);
    getBaseAttributes(champion);
    champion.recombineCache();
    plugin.getEntityStatCache().setEntityStats(champion.getPlayer(), champion.getCombinedCache());
  }

  public void updateChampionWeapons(Champion champion) {
    updateWeapons(champion);
    plugin.getEntityStatCache().setEntityStats(champion.getPlayer(), champion.getCombinedCache());
  }

  public void updateChampionArmor(Champion champion) {
    updateArmor(champion);
    plugin.getEntityStatCache().setEntityStats(champion.getPlayer(), champion.getCombinedCache());
  }

  public void updateChampionLevelPoints(Champion champion) {
    updateLevelPoints(champion);
    plugin.getEntityStatCache().setEntityStats(champion.getPlayer(), champion.getCombinedCache());
  }

  public void updateChampionBaseStats(Champion champion) {
    updateBase(champion);
    plugin.getEntityStatCache().setEntityStats(champion.getPlayer(), champion.getCombinedCache());
  }

  public void updateChampionStats(Champion champion) {
    updateAll(champion);
    plugin.getEntityStatCache().setEntityStats(champion.getPlayer(), champion.getCombinedCache());
  }

  public Collection<Champion> getChampions() {
    return new HashSet<>(championMap.values());
  }

  public Collection<ChampionSaveData> getChampionSaveData() {
    Collection<ChampionSaveData> dataCollection = new LinkedHashSet<>();
    for (Champion champion : getChampions()) {
      dataCollection.add(champion.getSaveData());
    }
    return dataCollection;
  }

  public void clear() {
    championMap.clear();
  }

  private static ItemStack removeAttributes(ItemStack item) {
    if (item.getType().getMaxDurability() < 15) {
      return item;
    }
    if (!MinecraftReflection.isCraftItemStack(item)) {
      item = MinecraftReflection.getBukkitItemStack(item);
    }
    NbtCompound compound = (NbtCompound) NbtFactory.fromItemTag(item);
    compound.put(NbtFactory.ofList("AttributeModifiers"));
    return item;
  }
}
