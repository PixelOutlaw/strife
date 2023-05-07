package land.face.strife.managers;

import com.sentropic.guiapi.gui.Alignment;
import com.sentropic.guiapi.gui.GUIComponent;
import com.tealcube.minecraft.bukkit.facecore.utilities.PaletteUtil;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang.WordUtils;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.ability.Ability;
import land.face.strife.data.ability.AbilityIconData;
import land.face.strife.data.ability.CooldownTracker;
import land.face.strife.data.champion.Champion;
import land.face.strife.data.champion.ChampionSaveData;
import land.face.strife.data.champion.LifeSkillType;
import land.face.strife.data.champion.StrifeAttribute;
import land.face.strife.events.AbilityChangeEvent;
import land.face.strife.managers.AbilityManager.AbilityType;
import land.face.strife.stats.AbilitySlot;
import land.face.strife.util.LogUtil;
import land.face.strife.util.PlayerDataUtil;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
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
    REQ_STR = PaletteUtil.color(plugin.getSettings().getString("language.abilities.picker-requirement-tag"));
    PASS_STR = PaletteUtil.color(plugin.getSettings().getString("language.abilities.picker-requirement-met-tag"));
    ABILITY_REQ_NOT_MET = PaletteUtil.color(plugin.getSettings().getString("language.abilities.picker-requirement-message", ""));
    ABILITY_ON_COOLDOWN = PaletteUtil.color(plugin.getSettings().getString("language.abilities.picker-on-cooldown", ""));
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
    if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
      return;
    }
    Champion champion = plugin.getChampionManager().getChampion(player);
    ChampionSaveData data = champion.getSaveData();
    player.getInventory().setHeldItemSlot(4);
    for (Entry<AbilitySlot, Ability> entry : data.getAbilities().entrySet()) {
      setAbilityIcon(player, entry.getValue().getAbilityIconData(), entry.getKey());
      AbilityChangeEvent abilityChangeEvent = new AbilityChangeEvent(champion, entry.getValue());
      Bukkit.getPluginManager().callEvent(abilityChangeEvent);
    }
    plugin.getAbilityIconManager().updateChargesGui(player);
  }

  public void setAbilityIcon(Player player, AbilityIconData abilityIconData, AbilitySlot slot) {
    if (abilityIconData == null || abilityIconData.getAbilitySlot() == AbilitySlot.INVALID) {
      return;
    }
    if (slot == null) {
      slot = abilityIconData.getAbilitySlot();
    }
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
    plugin.getChampionManager().getChampion(player).setLastChanged(1);
    AbilitySlot finalSlot = slot;
    Bukkit.getScheduler().runTaskLater(plugin, () ->
        plugin.getAbilityIconManager().updateIconProgress(player, finalSlot), 2L);
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

  public void untoggleDeathToggles(StrifeMob mob) {
    if (mob.getEntity().getType() != EntityType.PLAYER) {
      return;
    }
    Ability abilityA = mob.getChampion().getSaveData().getAbility(AbilitySlot.SLOT_A);
    Ability abilityB = mob.getChampion().getSaveData().getAbility(AbilitySlot.SLOT_B);
    Ability abilityC = mob.getChampion().getSaveData().getAbility(AbilitySlot.SLOT_C);
    Ability abilityD = mob.getChampion().getSaveData().getAbility(AbilitySlot.SLOT_D);

    if (abilityA != null && abilityA.isDeathUntoggle()) {
      plugin.getAbilityManager().unToggleAbility(mob, abilityA.getId());
    }
    if (abilityB != null && abilityB.isDeathUntoggle()) {
      plugin.getAbilityManager().unToggleAbility(mob, abilityB.getId());
    }
    if (abilityC != null && abilityC.isDeathUntoggle()) {
      plugin.getAbilityManager().unToggleAbility(mob, abilityC.getId());
    }
    if (abilityD != null && abilityD.isDeathUntoggle()) {
      plugin.getAbilityManager().unToggleAbility(mob, abilityD.getId());
    }
  }

  public void triggerAbility(Player player, int slotNumber) {
    AbilitySlot slot = AbilitySlot.fromSlot(slotNumber);
    if (slot == AbilitySlot.INVALID) {
      return;
    }

    StrifeMob mob = plugin.getStrifeMobManager().getStatMob(player);
    Ability ability = mob.getChampion().getSaveData().getAbility(slot);
    if (ability == null) {
      if (isAbilityIcon(player.getInventory().getItem(slotNumber))) {
        player.getInventory().setItem(slotNumber, null);
      }
      return;
    }
    // Absolutely stupid fix for clientside armor equip nonsense
    Bukkit.getScheduler().runTaskLater(plugin, () -> {
      updateAllIconProgress(player);
      player.updateInventory();
    }, 0L);
    boolean toggledOn = false;
    CooldownTracker cooldownTracker = plugin.getAbilityManager()
        .getCooldownTracker(player, ability.getId());
    if (cooldownTracker != null) {
      toggledOn = cooldownTracker.isToggleState();
    }
    if (!toggledOn && player.getCooldown(ability.getCastType().getMaterial()) > 2) {
      return;
    }
    boolean abilitySucceeded = plugin.getAbilityManager().execute(
        ability,
        plugin.getStrifeMobManager().getStatMob(player),
        null,
        AbilitySlot.cachedValues[slotNumber],
        toggledOn
    );
    if (!abilitySucceeded) {
      LogUtil.printDebug("Ability " + ability.getId() + " failed execution");
      plugin.getAbilityManager().setGlobalCooldown(player, 5);
      return;
    }
    if (ability.getCastType() == AbilityType.ATTACK) {
      if (ability.getGlobalCooldownTicks() > 5) {
        plugin.getAttackSpeedManager().resetAttack(mob, 1f, (float) ability.getGlobalCooldownTicks() / 20f);
      } else {
        plugin.getAttackSpeedManager().resetAttack(mob, 1f);
      }
    }
    plugin.getAbilityManager().setGlobalCooldown(player, ability.getGlobalCooldownTicks());
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
    setIconDamage(plugin.getChampionManager().getChampion(player),
        champion.getSaveData().getAbility(AbilitySlot.SLOT_D));
  }

  public void updateChargesGui(Player player) {
    Champion champion = plugin.getStrifeMobManager().getStatMob(player).getChampion();
    updateChargesGui(champion);
  }

  public void updateChargesGui(Champion champion) {
    Player player = champion.getPlayer();
    Ability abilityA = champion.getSaveData().getAbility(AbilitySlot.SLOT_A);
    Ability abilityB = champion.getSaveData().getAbility(AbilitySlot.SLOT_B);
    Ability abilityC = champion.getSaveData().getAbility(AbilitySlot.SLOT_C);
    Ability abilityD = champion.getSaveData().getAbility(AbilitySlot.SLOT_D);

    if (abilityA == null || abilityA.getMaxCharges() < 2) {
      plugin.getGuiManager().updateComponent(player, new GUIComponent("slot-a-charges",
          GuiManager.EMPTY, 0, 0, Alignment.CENTER));
    } else {
      String charges = "";
      CooldownTracker cd = plugin.getAbilityManager().getCooldownTracker(player, abilityA.getId());
      if (cd == null) {
        charges = charges + StringUtils.repeat("☊", abilityA.getMaxCharges());
      } else {
        charges = charges + StringUtils.repeat("☊", cd.getChargesLeft());
        charges = charges + StringUtils.repeat("☋", abilityA.getMaxCharges() - cd.getChargesLeft());
      }
      plugin.getGuiManager().updateComponent(player, new GUIComponent("slot-a-charges",
          GuiManager.noShadow(new TextComponent(charges)), charges.length() * 3, -89, Alignment.LEFT));
    }

    if (abilityB == null || abilityB.getMaxCharges() < 2) {
      plugin.getGuiManager().updateComponent(player, new GUIComponent("slot-b-charges",
          GuiManager.EMPTY, 0, 0, Alignment.CENTER));
    } else {
      String charges = "";
      CooldownTracker cd = plugin.getAbilityManager().getCooldownTracker(player, abilityB.getId());
      if (cd == null) {
        charges = charges + StringUtils.repeat("☊", abilityB.getMaxCharges());
      } else {
        charges = charges + StringUtils.repeat("☊", cd.getChargesLeft());
        charges = charges + StringUtils.repeat("☋", abilityB.getMaxCharges() - cd.getChargesLeft());
      }
      plugin.getGuiManager().updateComponent(player, new GUIComponent("slot-b-charges",
          GuiManager.noShadow(new TextComponent(charges)), charges.length() * 3, -69, Alignment.LEFT));
    }

    if (abilityC == null || abilityC.getMaxCharges() < 2) {
      plugin.getGuiManager().updateComponent(player, new GUIComponent("slot-c-charges",
          GuiManager.EMPTY, 0, 0, Alignment.CENTER));
    } else {
      String charges = "";
      CooldownTracker cd = plugin.getAbilityManager().getCooldownTracker(player, abilityC.getId());
      if (cd == null) {
        charges = charges + StringUtils.repeat("☊", abilityC.getMaxCharges());
      } else {
        charges = charges + StringUtils.repeat("☊", cd.getChargesLeft());
        charges = charges + StringUtils.repeat("☋", abilityC.getMaxCharges() - cd.getChargesLeft());
      }
      plugin.getGuiManager().updateComponent(player, new GUIComponent("slot-c-charges",
          GuiManager.noShadow(new TextComponent(charges)), charges.length() * 3, -49, Alignment.LEFT));
    }

    if (abilityD == null || abilityD.getMaxCharges() < 2) {
      plugin.getGuiManager().updateComponent(player, new GUIComponent("slot-d-charges",
          GuiManager.EMPTY, 0, 0, Alignment.CENTER));
    } else {
      String charges = "";
      CooldownTracker cd = plugin.getAbilityManager().getCooldownTracker(player, abilityD.getId());
      if (cd == null) {
        charges = charges + StringUtils.repeat("☊", abilityD.getMaxCharges());
      } else {
        charges = charges + StringUtils.repeat("☊", cd.getChargesLeft());
        charges = charges + StringUtils.repeat("☋", abilityD.getMaxCharges() - cd.getChargesLeft());
      }
      plugin.getGuiManager().updateComponent(player, new GUIComponent("slot-d-charges",
          GuiManager.noShadow(new TextComponent(charges)), charges.length() * 3, -29, Alignment.LEFT));
    }
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
    CooldownTracker container = plugin.getAbilityManager()
        .getCooldownTracker(champion.getPlayer(), ability.getId());
    if (container != null) {
      container.updateIcon();
    }
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
      strings.add(str.replace("{REQ}", ChatColor.stripColor(type.getPrettyName()) + " "
              + data.getLifeSkillRequirements().get(type)));
    }
    for (StrifeAttribute attr : data.getAttributeRequirement().keySet()) {
      String str = champion.getAttributeLevel(attr) < data.getAttributeRequirement().get(attr) ?
          REQ_STR : PASS_STR;
      strings.add(str.replace("{REQ}",
          ChatColor.stripColor(attr.getName() + " " + data.getAttributeRequirement().get(attr))));
    }
    return strings;
  }
}
