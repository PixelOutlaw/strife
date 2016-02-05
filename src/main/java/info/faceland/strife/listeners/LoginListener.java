package info.faceland.strife.listeners;

import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;

import info.faceland.strife.StrifePlugin;
import info.faceland.strife.attributes.AttributeHandler;
import info.faceland.strife.attributes.StrifeAttribute;
import info.faceland.strife.data.Champion;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

public class LoginListener implements Listener {

    private final StrifePlugin plugin;

    public LoginListener(StrifePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Champion champion = plugin.getChampionManager().getChampion(player.getUniqueId());
        champion.getAttributeValues(true);
        boolean spam = false;
        for (ItemStack itemStack : champion.getPlayer().getEquipment().getArmorContents()) {
            if (itemStack == null || itemStack.getType() == Material.AIR) {
                continue;
            }
            if (!AttributeHandler.meetsLevelRequirement(player, itemStack)) {
                spam = true;
            }
        }
        if (spam) {
            MessageUtils.sendMessage(player,
                    "<red>You don't meet the requirement for one of your items! It will not give any stats!");
        }
        AttributeHandler.updateHealth(player, champion.getCacheAttribute(StrifeAttribute.HEALTH,
                StrifeAttribute.HEALTH.getBaseValue()));
        double perc = champion.getCacheAttribute(StrifeAttribute.MOVEMENT_SPEED,
                StrifeAttribute.MOVEMENT_SPEED.getBaseValue()) / 100D;
        float speed = 0.2F * (float) perc;
        player.setWalkSpeed(Math.min(Math.max(-1F, speed), 1F));
        player.setFlySpeed(Math.min(Math.max(-1F, speed), 1F));
    }

}
