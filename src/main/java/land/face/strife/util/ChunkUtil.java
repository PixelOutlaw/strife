package land.face.strife.util;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Chunk;

public class ChunkUtil {

  private static final Map<String, Long> chunkActiveStamp = new HashMap<>();

  public static void stampChunk(Chunk chunk) {
    chunkActiveStamp.put(buildChunkKey(chunk), System.currentTimeMillis() + 1200);
  }

  public static void unstampChunk(Chunk chunk) {
    chunkActiveStamp.remove(buildChunkKey(chunk));
  }

  public static boolean isChuckLoaded(String chunkKey) {
    if (chunkActiveStamp.containsKey(chunkKey)) {
      return System.currentTimeMillis() > chunkActiveStamp.get(chunkKey);
    }
    return false;
  }

  private static String buildChunkKey(Chunk chunk) {
    return chunk.getWorld().getName() + chunk.getChunkKey();
  }
}
