package info.faceland.strife.managers;

import com.tealcube.minecraft.bukkit.TextUtils;
import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import info.faceland.strife.StrifePlugin;
import info.faceland.strife.data.HotbarIconData;
import info.faceland.strife.data.ability.Ability;
import info.faceland.strife.stats.AbilitySlot;
import info.faceland.strife.util.ItemUtil;
import info.faceland.strife.util.LogUtil;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class AbilityIconManager {

  private final StrifePlugin plugin;
  private final Map<UUID, Map<AbilitySlot, HotbarIconData>> abilitySlotMap;

  public static final String ABILITY_PREFIX = "Ability: ";

  private final String ON_COOLDOWN;

  public AbilityIconManager(StrifePlugin plugin) {
    this.plugin = plugin;
    this.abilitySlotMap = new HashMap<>();
    ON_COOLDOWN = TextUtils
        .color(plugin.getSettings().getString("language.abilities.ability-cooldown"));
  }

  public void addPlayerToMap(Player player) {
    if (abilitySlotMap.containsKey(player.getUniqueId())) {
      return;
    }
    abilitySlotMap.put(player.getUniqueId(), setAllAbilityIcons(player));
  }

  public Map<AbilitySlot, HotbarIconData> setAllAbilityIcons(Player player) {
    Map<AbilitySlot, HotbarIconData> iconMap = new HashMap<>();
    iconMap.put(AbilitySlot.SLOT_A, getAbilityIcon(player, AbilitySlot.SLOT_A.getSlotIndex()));
    iconMap.put(AbilitySlot.SLOT_B, getAbilityIcon(player, AbilitySlot.SLOT_B.getSlotIndex()));
    iconMap.put(AbilitySlot.SLOT_C, getAbilityIcon(player, AbilitySlot.SLOT_C.getSlotIndex()));
    return iconMap;
  }

  public void clearAbilityIcon(Player player, AbilitySlot slot) {
    if (abilitySlotMap.get(player.getUniqueId()).get(slot) != null) {
      abilitySlotMap.get(player.getUniqueId()).put(slot, null);
      player.getInventory().setItem(slot.getSlotIndex(), null);
    }
  }

  public void setAbilityIcon(Player player, Ability ability) {
    addPlayerToMap(player);
    if (ability.getAbilityIconData() == null
        || ability.getAbilityIconData().getAbilitySlot() == AbilitySlot.INVALID) {
      LogUtil.printWarning(player.getName() + " set no-slot ability " + ability.getId());
      return;
    }
    AbilitySlot slot = ability.getAbilityIconData().getAbilitySlot();
    ItemStack displacedItem = player.getInventory().getItem(slot.getSlotIndex());
    player.getInventory().setItem(slot.getSlotIndex(), ability.getAbilityIconData().getStack());
    if (displacedItem != null && !isAbilityIcon(displacedItem)) {
      HashMap<Integer, ItemStack> excessItems = player.getInventory().addItem(displacedItem);
      for (ItemStack extraStack : excessItems.values()) {
        player.getWorld().dropItem(player.getLocation(), extraStack);
      }
    }
    abilitySlotMap.get(player.getUniqueId())
        .put(slot, new HotbarIconData(ability, ability.getAbilityIconData().getStack()));
  }

  public boolean playerHasAbilityIcon(Player player, Ability ability) {
    for (HotbarIconData data : abilitySlotMap.get(player.getUniqueId()).values()) {
      if (data == null) {
        return false;
      }
      if (data.getAbility() == ability) {
        return true;
      }
    }
    return false;
  }

  public boolean isAbilityIcon(ItemStack stack) {
    if (stack == null) {
      return false;
    }
    String name = ChatColor.stripColor(ItemStackExtensionsKt.getDisplayName(stack));
    if (StringUtils.isBlank(name) || !name.startsWith(ABILITY_PREFIX) || name.contains("✫")) {
      return false;
    }
    return true;
  }

  public Ability getAbilityFromSlot(Player player, AbilitySlot slot) {
    ItemStack stack = player.getInventory().getItem(slot.getSlotIndex());
    if (stack == null) {
      return null;
    }
    String name = ChatColor.stripColor(ItemStackExtensionsKt.getDisplayName(stack));
    if (StringUtils.isBlank(name) || !name.startsWith(ABILITY_PREFIX) || name.contains("✫")) {
      return null;
    }
    return plugin.getAbilityManager().getAbility(name.replace(ABILITY_PREFIX, ""));
  }

  private HotbarIconData getAbilityIcon(Player player, int slot) {
    ItemStack stack = player.getInventory().getItem(slot);
    if (stack == null) {
      return null;
    }
    String name = ChatColor.stripColor(ItemStackExtensionsKt.getDisplayName(stack));
    if (StringUtils.isBlank(name) || !name.startsWith(ABILITY_PREFIX) || name.contains("✫")) {
      return null;
    }
    Ability ability = plugin.getAbilityManager().getAbility(name.replace(ABILITY_PREFIX, ""));
    if (ability.getAbilityIconData() == null) {
      player.getInventory().setItem(slot, null);
      return null;
    }
    return new HotbarIconData(ability, stack);
  }

  public boolean triggerAbility(Player player, int slot) {
    AbilitySlot abilitySlot = AbilitySlot.fromSlot(slot);
    if (abilitySlot == AbilitySlot.INVALID) {
      return false;
    }
    HotbarIconData data = abilitySlotMap.get(player.getUniqueId()).get(abilitySlot);
    if (data == null) {
      return false;
    }
    if (!plugin.getAbilityManager().isCooledDown(player, data.getAbility())) {
      if (data.getAbility().isDisplayCd()) {
        MessageUtils.sendActionBar(player, ON_COOLDOWN);
      }
      player.playSound(player.getEyeLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 1, 1.5f);
      return true;
    }
    plugin.getAbilityManager()
        .execute(data.getAbility(), plugin.getStrifeMobManager().getStatMob(player));
    player.playSound(player.getEyeLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
    updateAbilityIconDamageMeters(player);
    return true;
  }

  public void updateAbilityIconDamageMeters(Player player) {
    if (player.isDead()) {
      return;
    }
    Map<AbilitySlot, HotbarIconData> iconMap = abilitySlotMap.get(player.getUniqueId());
    for (AbilitySlot slot : iconMap.keySet()) {
      setIconDamage(player, iconMap.get(slot), slot.ordinal());
    }
  }

  private void setIconDamage(Player player, HotbarIconData data, int slot) {
    if (data == null || data.getAbility() == null || data.getItemStack() == null) {
      return;
    }
    if (plugin.getAbilityManager().isCooledDown(player, data.getAbility())) {
      return;
    }
    double remainingTicks = plugin.getAbilityManager().getCooldownTicks(player, data.getAbility());
    double cooldownTicks = data.getAbility().getCooldown() * 20;
    double percent = (remainingTicks - 4) / cooldownTicks;
    ItemUtil.sendAbilityIconPacket(data, player, slot, percent);
  }
}
