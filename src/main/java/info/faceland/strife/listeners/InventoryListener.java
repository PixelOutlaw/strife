package info.faceland.strife.listeners;

import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;

import info.faceland.strife.StrifePlugin;
import info.faceland.strife.attributes.AttributeHandler;
import info.faceland.strife.attributes.StrifeAttribute;
import info.faceland.strife.data.Champion;
import info.faceland.strife.stats.StrifeStat;

import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashMap;
import java.util.Map;

public class InventoryListener implements Listener {

    private final StrifePlugin plugin;

    public InventoryListener(StrifePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getInventory() instanceof PlayerInventory)) {
            return;
        }
        PlayerInventory playerInventory = (PlayerInventory) event.getInventory();
        HumanEntity humanEntity = playerInventory.getHolder();
        if (!(humanEntity instanceof Player)) {
            return;
        }
        Player player = (Player) humanEntity;
        Champion champion = plugin.getChampionManager().getChampion(player.getUniqueId());
        Map<StrifeAttribute, Double> attributeDoubleMap = new HashMap<>();
        for (StrifeAttribute attr : StrifeAttribute.values()) {
            attributeDoubleMap.put(attr, attr.getBaseValue());
        }
        for (Map.Entry<StrifeStat, Integer> entry : champion.getLevelMap().entrySet()) {
            for (StrifeAttribute attr : StrifeAttribute.values()) {
                double val = attributeDoubleMap.containsKey(attr) ? attributeDoubleMap.get(attr) : 0;
                attributeDoubleMap.put(attr, val + entry.getKey().getAttribute(attr) * entry.getValue());
            }
        }
        boolean spam = false;
        for (ItemStack itemStack : champion.getPlayer().getEquipment().getArmorContents()) {
            if (itemStack == null || itemStack.getType() == Material.AIR) {
                continue;
            }
            if (!AttributeHandler.meetsLevelRequirement(player, itemStack)) {
                spam = true;
                continue;
            }
            for (StrifeAttribute attr : StrifeAttribute.values()) {
                double val = attributeDoubleMap.containsKey(attr) ? attributeDoubleMap.get(attr) : 0;
                attributeDoubleMap.put(attr, val + AttributeHandler.getValue(itemStack, attr));
                champion.setAttributeInCache(attr, attributeDoubleMap.get(attr));
            }
        }
        if (spam) {
            MessageUtils.sendMessage(player,
                    "<red>You don't meet the requirement for one of your items! It will not give any stats!");
        }
        AttributeHandler.updateHealth(player, attributeDoubleMap);
        double perc = attributeDoubleMap.get(StrifeAttribute.MOVEMENT_SPEED) / 100D;
        float speed = 0.2F * (float) perc;
        player.setWalkSpeed(Math.min(Math.max(-1F, speed), 1F));
        player.setFlySpeed(Math.min(Math.max(-1F, speed), 1F));
    }

}
