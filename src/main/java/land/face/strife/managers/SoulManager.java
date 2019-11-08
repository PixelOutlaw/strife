package land.face.strife.managers;

import static com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils.sendMessage;

import com.tealcube.minecraft.bukkit.TextUtils;
import io.netty.util.internal.ConcurrentSet;
import java.util.HashSet;
import java.util.Set;
import land.face.strife.StrifePlugin;
import land.face.strife.data.ability.Ability;
import land.face.strife.data.ability.Ability.TargetType;
import land.face.strife.data.champion.ChampionSaveData;
import land.face.strife.timers.SoulTimer;
import land.face.strife.util.DamageUtil.OriginLocation;
import land.face.strife.util.TargetingUtil;
import net.minecraft.server.v1_14_R1.ChatBaseComponent;
import net.minecraft.server.v1_14_R1.EntityItem;
import net.minecraft.server.v1_14_R1.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_14_R1.PacketPlayOutEntityMetadata;
import net.minecraft.server.v1_14_R1.PacketPlayOutEntityVelocity;
import net.minecraft.server.v1_14_R1.PacketPlayOutSpawnEntity;
import net.minecraft.server.v1_14_R1.Vec3D;
import net.minecraft.server.v1_14_R1.WorldServer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_14_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_14_R1.inventory.CraftItemStack;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class SoulManager {

  private StrifePlugin plugin;
  private Set<SoulTimer> souls = new ConcurrentSet<>();
  private Set<String> deathWorlds = new HashSet<>();
  private String reviveMessage;
  private String soulName;

  public SoulManager(StrifePlugin plugin) {
    this.plugin = plugin;
    deathWorlds.addAll(plugin.getSettings().getStringList("config.death-worlds"));
    reviveMessage = plugin.getSettings().getString("language.generic.revive-exp-message", "");
    soulName = plugin.getSettings().getString("language.generic.soul-name", "");
  }

  public boolean isDeathWorld(String worldName) {
    return deathWorlds.contains(worldName);
  }

  public boolean canSeeSouls(Player player) {
    ChampionSaveData data = plugin.getChampionManager().getChampion(player).getSaveData();
    for (Ability ability : data.getAbilities().values()) {
      if (ability.getTargetType() == TargetType.NEAREST_SOUL) {
        return true;
      }
    }
    return false;
  }

  public void createSoul(Player player) {
    for (SoulTimer soulTimer : souls) {
      if (soulTimer.getOwner() == player.getUniqueId()) {
        removeSoul(soulTimer);
        break;
      }
    }
    Location location = TargetingUtil.getOriginLocation(player, OriginLocation.CENTER);

    String text = soulName.replace("{n}", player.getName());
    EntityItem soulHead = buildItemHead(player, (CraftWorld) player.getWorld(), location);
    soulHead.setNoGravity(true);
    soulHead.setPickupDelay(9999999);
    soulHead.setCustomName(ChatBaseComponent.ChatSerializer.b(TextUtils.color(text)));
    soulHead.setCustomNameVisible(true);
    soulHead.setLocation(location.getX(), location.getY() - 0.2, location.getZ(), 0, 0);
    souls.add(new SoulTimer(player.getUniqueId(), soulHead, location));
  }

  public void removeSoul(SoulTimer soulTimer) {
    souls.remove(soulTimer);
    PacketPlayOutEntityDestroy killStand = new PacketPlayOutEntityDestroy(soulTimer.getStandId());
    for (Player p : soulTimer.getViewers()) {
      ((CraftPlayer) p).getHandle().playerConnection.sendPacket(killStand);
    }
    soulTimer.cancel();
  }

  public SoulTimer getNearestSoul(LivingEntity le, float maxDistSquared) {
    SoulTimer selectedSoul = null;
    double selectedDist = maxDistSquared;
    for (SoulTimer soulTimer : souls) {
      if (!soulTimer.getLocation().getWorld().equals(le.getWorld())) {
        continue;
      }
      double dist = soulTimer.getLocation().distanceSquared(le.getLocation());
      if (dist < selectedDist) {
        selectedSoul = soulTimer;
        selectedDist = dist;
      }
    }
    return selectedSoul;
  }

  public SoulTimer getSoul(Player player) {
    for (SoulTimer soulTimer : souls) {
      if (soulTimer.getOwner().equals(player.getUniqueId())) {
        return soulTimer;
      }
    }
    return null;
  }

  public void setLostExp(Player player, double amount) {
    SoulTimer soul = getSoul(player);
    if (soul == null) {
      return;
    }
    soul.setLostExp(amount);
  }

  public void revive(Player player, int xpRestored) {
    SoulTimer soul = getSoul(player);
    if (soul == null) {
      sendMessage(player, "Sorry! You've been dead too long to be revived :(");
      return;
    }
    plugin.getExperienceManager().addExperience(player, xpRestored, true);
    sendMessage(player, reviveMessage.replace("{n}", String.valueOf(xpRestored)));
    player.teleport(soul.getLocation());
    removeSoul(soul);
  }

  public void revive(Player player) {
    revive(player, 0);
  }

  public void sendCreationPacket(Player player, EntityItem soulHead) {
    PacketPlayOutSpawnEntity spawnPacket = new PacketPlayOutSpawnEntity(soulHead);
    PacketPlayOutEntityMetadata data = new PacketPlayOutEntityMetadata(soulHead.getId(),
        soulHead.getDataWatcher(), true);
    Vec3D vec3D = new Vec3D(0, 0, 0);
    PacketPlayOutEntityVelocity velo = new PacketPlayOutEntityVelocity(soulHead.getId(), vec3D);

    ((CraftPlayer) player).getHandle().playerConnection.sendPacket(spawnPacket);
    ((CraftPlayer) player).getHandle().playerConnection.sendPacket(velo);
    ((CraftPlayer) player).getHandle().playerConnection.sendPacket(data);
  }

  private EntityItem buildItemHead(Player player, CraftWorld world, Location location) {
    WorldServer w = world.getHandle();
    ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
    SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
    skullMeta.setOwningPlayer(player);
    skull.setItemMeta(skullMeta);

    CraftItemStack craft = CraftItemStack.asCraftCopy(skull);
    net.minecraft.server.v1_14_R1.ItemStack nmsStack = CraftItemStack.asNMSCopy(craft);

    return new EntityItem(w, location.getX(), location.getY() - 0.2, location.getZ(), nmsStack);
  }
}
