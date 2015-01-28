package info.faceland.strife.listeners;

import info.faceland.loot.events.LootDetermineChanceEvent;
import info.faceland.strife.StrifePlugin;
import info.faceland.strife.attributes.StrifeAttribute;
import info.faceland.strife.data.Champion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.Map;

public class DropListener implements Listener {

    private final StrifePlugin plugin;

    public DropListener(StrifePlugin plugin) {
        this.plugin = plugin;
    }

    public StrifePlugin getPlugin() {
        return plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onLootDetermineChance(LootDetermineChanceEvent event) {
        Champion champion = plugin.getChampionManager().getChampion(event.getKiller().getUniqueId());
        Map<StrifeAttribute, Double> attributeDoubleMap = champion.getAttributeValues();
        double chance = event.getChance() + event.getChance() * attributeDoubleMap.get(StrifeAttribute.ITEM_DISCOVERY);
        event.setChance(chance);
    }
}
