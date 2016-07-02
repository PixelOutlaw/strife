package info.faceland.strife.storage;

import com.tealcube.minecraft.bukkit.facecore.database.Database;
import com.tealcube.minecraft.bukkit.facecore.database.MySqlDatabasePool;
import info.faceland.strife.StrifePlugin;
import info.faceland.strife.data.Champion;

import java.util.Collection;
import java.util.UUID;

public class MySqlDataStorage implements DataStorage {

    private final StrifePlugin plugin;
    private Database database;

    public MySqlDataStorage(StrifePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void init() {
        if (database != null) {
            // do nothing
            return;
        }
        this.database = new MySqlDatabasePool(
                plugin.getSettings().getString("config.storage.host", "localhost"),
                plugin.getSettings().getString("config.storage.port", "3306"),
                plugin.getSettings().getString("config.storage.database", "localdb"),
                plugin.getSettings().getString("config.storage.user", "localuser"),
                plugin.getSettings().getString("config.storage.pass", "localpass"));
    }

    @Override
    public void shutdown() {

    }

    @Override
    public void save(Champion champion) {

    }

    @Override
    public void save(Collection<Champion> champions) {

    }

    @Override
    public Collection<Champion> load() {
        return null;
    }

    @Override
    public Champion load(UUID uuid) {
        return null;
    }
}
