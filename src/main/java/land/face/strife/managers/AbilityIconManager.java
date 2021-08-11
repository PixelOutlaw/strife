package land.face.strife.managers;

import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import io.pixeloutlaw.minecraft.spigot.garbage.ListExtensionsKt;
import io.pixeloutlaw.minecraft.spigot.garbage.StringExtensionsKt;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import land.face.strife.StrifePlugin;
import land.face.strife.data.ability.Ability;
import land.face.strife.data.ability.AbilityCooldownContainer;
import land.face.strife.data.ability.AbilityIconData;
import land.face.strife.data.champion.Champion;
import land.face.strife.data.champion.ChampionSaveData;
import land.face.strife.data.champion.LifeSkillType;
import land.face.strife.data.champion.StrifeAttribute;
import land.face.strife.stats.AbilitySlot;
import land.face.strife.util.ItemUtil;
import land.face.strife.util.LogUtil;
import land.face.strife.util.PlayerDataUtil;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class AbilityIconManager {

  private final StrifePlugin plugin;

  public final static String ABILITY_PREFIX = "Ability: ";
  private final String REQ_STR;
  private final String PASS_STR;
  public static String ABILITY_REQ_NOT_MET;
  public static String ABILITY_ON_COOLDOWN;

  public AbilityIconManager(StrifePlugin plugin) {
    this.plugin = plugin;
    REQ_STR = plugin.getSettings().getString("language.abilities.picker-requirement-tag");
    PASS_STR = plugin.getSettings().getString("language.abilities.picker-requirement-met-tag");
    ABILITY_REQ_NOT_MET = StringExtensionsKt.chatColorize(
        plugin.getSettings().getString("language.abilities.picker-requirement-message", ""));
    ABILITY_ON_COOLDOWN = StringExtensionsKt.chatColorize(
        plugin.getSettings().getString("language.abilities.picker-on-cooldown", ""));
  }

  public void removeIconItem(Player player, AbilitySlot slot) {
    if (slot.getSlotIndex() != -1 && isAbilityIcon(
        player.getInventory().getItem(slot.getSlotIndex()))) {
      player.getInventory().setItem(slot.getSlotIndex(), null);
    }
  }

  public void clearAbilityIcon(Player player, AbilitySlot slot) {
    plugin.getChampionManager().getChampion(player).getSaveData().setAbility(slot, null);
    removeIconItem(player, slot);
  }

  public void setAllAbilityIcons(Player player) {
    ChampionSaveData data = plugin.getChampionManager().getChampion(player).getSaveData();
    for (Ability ability : data.getAbilities().values()) {
      setAbilityIcon(player, ability.getAbilityIconData());
    }
  }

  public void setAbilityIcon(Player player, AbilityIconData abilityIconData) {
    if (abilityIconData == null || abilityIconData.getAbilitySlot() == AbilitySlot.INVALID) {
      return;
    }
    AbilitySlot slot = abilityIconData.getAbilitySlot();
    if (slot.getSlotIndex() == -1) {
      return;
    }
    ItemStack displacedItem = player.getInventory().getItem(slot.getSlotIndex());
    player.getInventory().setItem(slot.getSlotIndex(), abilityIconData.getStack());
    if (displacedItem != null && !isAbilityIcon(displacedItem)) {
      HashMap<Integer, ItemStack> excessItems = player.getInventory().addItem(displacedItem);
      for (ItemStack extraStack : excessItems.values()) {
        player.getWorld().dropItem(player.getLocation(), extraStack);
      }
    }
    Bukkit.getScheduler().runTaskLater(plugin,
        () -> plugin.getAbilityIconManager().updateIconProgress(player, slot), 2L);
  }

  public boolean playerHasAbility(Player player, Ability ability) {
    ChampionSaveData data = plugin.getChampionManager().getChampion(player).getSaveData();
    return data.getAbilities().containsValue(ability);
  }

  public boolean isAbilityIcon(ItemStack stack) {
    if (stack == null) {
      return false;
    }
    String name = ChatColor.stripColor(ItemStackExtensionsKt.getDisplayName(stack));
    return !StringUtils.isBlank(name) && name.startsWith(ABILITY_PREFIX) && !name.contains("✫");
  }

  public void triggerAbility(Player player, int slotNumber) {
    if (player.getCooldown(Material.DIAMOND_CHESTPLATE) > 0) {
      return;
    }
    AbilitySlot slot = AbilitySlot.fromSlot(slotNumber);
    if (slot == AbilitySlot.INVALID) {
      return;
    }
    Ability ability = plugin.getChampionManager().getChampion(player).getSaveData()
        .getAbility(slot);
    if (ability == null) {
      if (isAbilityIcon(player.getInventory().getItem(slotNumber))) {
        player.getInventory().setItem(slotNumber, null);
      }
      return;
    }
    boolean abilitySucceeded = plugin.getAbilityManager().execute(ability,
        plugin.getStrifeMobManager().getStatMob(player), null);
    if (!abilitySucceeded) {
      LogUtil.printDebug("Ability " + ability.getId() + " failed execution");
      plugin.getAbilityManager().setGlobalCooldown(player, 5);
      return;
    }
    plugin.getAbilityManager().setGlobalCooldown(player, ability);
    player.playSound(player.getEyeLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1);
    updateIconProgress(player, ability);
  }

  public void updateAllIconProgress(Player player) {
    Champion champion = plugin.getChampionManager().getChampion(player);
    setIconDamage(plugin.getChampionManager().getChampion(player),
        champion.getSaveData().getAbility(AbilitySlot.SLOT_A));
    setIconDamage(plugin.getChampionManager().getChampion(player),
        champion.getSaveData().getAbility(AbilitySlot.SLOT_B));
    setIconDamage(plugin.getChampionManager().getChampion(player),
        champion.getSaveData().getAbility(AbilitySlot.SLOT_C));
  }

  private void updateIconProgress(Player player, AbilitySlot slot) {
    Champion champion = plugin.getChampionManager().getChampion(player);
    setIconDamage(champion, champion.getSaveData().getAbility(slot));
  }

  public void updateIconProgress(Player player, Ability ability) {
    setIconDamage(plugin.getChampionManager().getChampion(player), ability);
  }

  private void setIconDamage(Champion champion, Ability ability) {
    if (ability == null || !champion.getSaveData().getAbilities().containsValue(ability)) {
      return;
    }
    AbilityCooldownContainer container = plugin.getAbilityManager()
        .getCooldownContainer(champion.getPlayer(), ability.getId());
    int charges = ability.getMaxCharges();
    double percent = 1D;
    boolean toggled = false;
    if (container != null) {
      toggled = container.isToggledOn();
      if (container.getSpentCharges() == charges) {
        charges = 1;
        percent = plugin.getAbilityManager().getCooldownPercent(container);
      } else {
        charges -= container.getSpentCharges();
      }
    }
    ItemUtil.sendAbilityIconPacket(ability.getAbilityIconData().getStack(), champion.getPlayer(),
        ability.getAbilityIconData().getAbilitySlot().getSlotIndex(), percent, charges, toggled);
  }

  public List<String> buildRequirementsLore(Champion champion, AbilityIconData data) {
    List<String> strings = new ArrayList<>();
    if (data.getTotalSkillRequirement() != -1) {
      String str = PlayerDataUtil.getTotalSkillLevel(champion.getPlayer()) <
          data.getTotalSkillRequirement() ? REQ_STR : PASS_STR;
      strings.add(str.replace("{REQ}", "Total Skill Level " + data.getTotalSkillRequirement()));
    }
    if (data.getLevelRequirement() != -1) {
      String str =
          champion.getPlayer().getLevel() < data.getLevelRequirement() ? REQ_STR : PASS_STR;
      strings.add(str.replace("{REQ}", "Level " + data.getLevelRequirement()));
    }
    if (data.getBonusLevelRequirement() != -1) {
      String str = champion.getBonusLevels() < data.getBonusLevelRequirement() ? REQ_STR : PASS_STR;
      strings.add(str.replace("{REQ}", "Bonus Level " + data.getBonusLevelRequirement()));
    }
    for (LifeSkillType type : data.getLifeSkillRequirements().keySet()) {
      String str = champion.getLifeSkillLevel(type) < data.getLifeSkillRequirements().get(type) ?
          REQ_STR : PASS_STR;
      strings.add(str.replace("{REQ}", ChatColor.stripColor(
          WordUtils.capitalize(type.name().toLowerCase().replaceAll("_", " ")) + " "
              + data.getLifeSkillRequirements().get(type))));
    }
    for (StrifeAttribute attr : data.getAttributeRequirement().keySet()) {
      String str = champion.getAttributeLevel(attr) < data.getAttributeRequirement().get(attr) ?
          REQ_STR : PASS_STR;
      strings.add(str.replace("{REQ}",
          ChatColor.stripColor(attr.getName() + " " + data.getAttributeRequirement().get(attr))));
    }
    return ListExtensionsKt.chatColorize(strings);
  }
}
