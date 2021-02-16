package land.face.strife.util;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Chunk;

public class ChunkUtil {

  private static final Map<String, Long> chunkActiveStamp = new HashMap<>();

  public static void stampChunk(Chunk chunk) {
    chunkActiveStamp.put(chunk.getWorld().getName() + chunk.getChunkKey(), System.currentTimeMillis() + 1200);
  }

  public static void unstampChunk(Chunk chunk) {
    chunkActiveStamp.remove(chunk.getWorld().getName() + chunk.getChunkKey());
  }

  public static boolean isChuckLoaded(String chunkId) {
    if (chunkActiveStamp.containsKey(chunkId)) {
      return chunkActiveStamp.get(chunkId) < System.currentTimeMillis();
    }
    return false;
  }
}
