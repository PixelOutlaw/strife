package info.faceland.strife.managers;

import com.tealcube.minecraft.bukkit.TextUtils;
import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import info.faceland.strife.StrifePlugin;
import info.faceland.strife.data.AbilityIconData;
import info.faceland.strife.data.ability.Ability;
import info.faceland.strife.stats.AbilitySlot;
import info.faceland.strife.util.ItemUtil;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class AbilityIconManager {

  private final StrifePlugin plugin;
  private final Map<UUID, Map<AbilitySlot, AbilityIconData>> abilitySlotMap;

  private static final String ABILITY_PREFIX = "Ability: ";

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
    abilitySlotMap.put(player.getUniqueId(), updateAbilityIcons(player));
  }

  public Map<AbilitySlot, AbilityIconData> updateAbilityIcons(Player player) {
    Map<AbilitySlot, AbilityIconData> iconMap = new HashMap<>();
    iconMap.put(AbilitySlot.SLOT_A, getAbilityIcon(player, AbilitySlot.SLOT_A.getSlotIndex()));
    iconMap.put(AbilitySlot.SLOT_B, getAbilityIcon(player, AbilitySlot.SLOT_B.getSlotIndex()));
    iconMap.put(AbilitySlot.SLOT_C, getAbilityIcon(player, AbilitySlot.SLOT_C.getSlotIndex()));
    return iconMap;
  }

  public void setAbilityIcon(Player player, AbilitySlot slot, Ability ability) {
    addPlayerToMap(player);
    ItemStack stack = new ItemStack(Material.DIAMOND_CHESTPLATE);
    ItemStackExtensionsKt.setDisplayName(stack, ABILITY_PREFIX + ability.getId());
    player.getInventory().setItem(slot.getSlotIndex(), stack);
    abilitySlotMap.get(player.getUniqueId()).put(slot, new AbilityIconData(ability, stack));
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

  public AbilityIconData getAbilityIcon(Player player, int slot) {
    ItemStack stack = player.getInventory().getItem(slot);
    if (stack == null) {
      return null;
    }
    String name = ChatColor.stripColor(ItemStackExtensionsKt.getDisplayName(stack));
    if (StringUtils.isBlank(name) || !name.startsWith(ABILITY_PREFIX) || name.contains("✫")) {
      return null;
    }
    Ability ability = plugin.getAbilityManager().getAbility(name.replace(ABILITY_PREFIX, ""));
    return new AbilityIconData(ability, stack);
  }

  public boolean triggerAbility(Player player, int slot) {
    AbilitySlot abilitySlot = AbilitySlot.fromSlot(slot);
    if (abilitySlot == AbilitySlot.INVALID) {
      return false;
    }
    AbilityIconData data = abilitySlotMap.get(player.getUniqueId()).get(abilitySlot);
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
    updateIcons(player);
    return true;
  }

  public void updateIcons(Player player) {
    Map<AbilitySlot, AbilityIconData> iconMap = abilitySlotMap.get(player.getUniqueId());
    for (AbilitySlot slot : iconMap.keySet()) {
      setIconDamage(player, iconMap.get(slot), slot.ordinal());
    }
  }

  private void setIconDamage(Player player, AbilityIconData data, int slot) {
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
