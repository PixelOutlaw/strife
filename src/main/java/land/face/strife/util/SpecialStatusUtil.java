package land.face.strife.util;

import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;

public class SpecialStatusUtil {

  private static final Map<Entity, Boolean> BURN_IMMUNE = new WeakHashMap<>();
  private static final Map<Entity, Boolean> IGNORE_TARGET_LEVEL = new WeakHashMap<>();
  private static final Map<Entity, Boolean> WEAK_AGGRO = new WeakHashMap<>();
  private static final Map<Entity, Boolean> FALL_IMMUNE = new WeakHashMap<>();
  private static final Map<Entity, Boolean> SNEAK_IMMUNE = new WeakHashMap<>();
  private static final Map<Entity, Boolean> SPAWNER_SPAWNED = new WeakHashMap<>();
  private static final Map<Entity, Boolean> GUILD_MOB = new WeakHashMap<>();

  private static final Map<Entity, UUID> HERDED_BY = new WeakHashMap<>();

  private static final Map<Entity, String> HANDLED_BLOCK = new WeakHashMap<>();

  private static final Map<Entity, Integer> MOB_LEVEL = new WeakHashMap<>();
  private static final Map<Entity, String> UNIQUE_ID = new WeakHashMap<>();

  public static void setBurnImmune(Entity e) {
    BURN_IMMUNE.put(e, true);
  }

  public static boolean isBurnImmune(Entity e) {
    return BURN_IMMUNE.getOrDefault(e, false);
  }

  public static void setIgnoreTargetLevel(Entity e) {
    IGNORE_TARGET_LEVEL.put(e, true);
  }

  public static boolean isIgnoreTargetLevel(Entity e) {
    return IGNORE_TARGET_LEVEL.getOrDefault(e, false);
  }

  public static void setWeakAggro(Entity e) {
    WEAK_AGGRO.put(e, true);
  }

  public static boolean isWeakAggro(Entity e) {
    return WEAK_AGGRO.getOrDefault(e, false);
  }

  public static void setHerdedBy(Entity e, Player p) {
    HERDED_BY.put(e, p.getUniqueId());
  }

  public static UUID getHerdedBy(Entity e) {
    return HERDED_BY.get(e);
  }

  public static void setFallImmune(Entity e) {
    FALL_IMMUNE.put(e, true);
  }

  public static boolean isFallImmune(Entity e) {
    return FALL_IMMUNE.getOrDefault(e, false);
  }

  public static void setIsGuildMob(Entity e) {
    GUILD_MOB.put(e, true);
  }

  public static boolean isGuildMob(Entity e) {
    return GUILD_MOB.getOrDefault(e, false);
  }

  public static void setSneakImmune(Entity e) {
    SNEAK_IMMUNE.put(e, true);
  }

  public static boolean isSneakImmune(Entity e) {
    return SNEAK_IMMUNE.getOrDefault(e, false);
  }

  public static void setSpawnerMob(Entity e) {
    SPAWNER_SPAWNED.put(e, true);
  }

  public static boolean isSpawnerMob(Entity e) {
    return SPAWNER_SPAWNED.getOrDefault(e, false);
  }

  public static void setMobLevel(Entity e, int level) {
    MOB_LEVEL.put(e, level);
  }

  public static String getUniqueId(Entity e) {
    return UNIQUE_ID.getOrDefault(e, StringUtils.EMPTY);
  }

  public static void setUniqueId(Entity e, String id) {
    UNIQUE_ID.put(e, id);
  }

  public static boolean isHandledBlock(FallingBlock e) {
    return HANDLED_BLOCK.containsKey(e);
  }

  public static String getHandledBlockEffects(FallingBlock e) {
    return HANDLED_BLOCK.getOrDefault(e, StringUtils.EMPTY);
  }

  public static void setHandledBlock(FallingBlock e, String effects) {
    HANDLED_BLOCK.put(e, effects);
  }

  public static int getMobLevel(Entity e) {
    return MOB_LEVEL.getOrDefault(e, -1);
  }
}
