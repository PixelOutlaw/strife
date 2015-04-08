package info.faceland.strife.listeners;

import com.tealcube.minecraft.bukkit.bullion.GoldDropEvent;

import info.faceland.strife.StrifePlugin;
import info.faceland.strife.attributes.StrifeAttribute;
import info.faceland.strife.data.Champion;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.Map;

public class BullionListener implements Listener {

    private final StrifePlugin plugin;

    public BullionListener(StrifePlugin plugin) {
        this.plugin = plugin;
    }

    public StrifePlugin getPlugin() {
        return plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onGoldDrop(GoldDropEvent event) {
        if (event.getKiller() == null) {
            return;
        }
        Champion champion = plugin.getChampionManager().getChampion(event.getKiller().getUniqueId());
        Map<StrifeAttribute, Double> attributeDoubleMap = champion.getAttributeValues();
        double amount = event.getAmount() + event.getAmount() * attributeDoubleMap.get(StrifeAttribute.GOLD_FIND);
        event.setAmount(amount);
    }

}
