/**
 * The MIT License Copyright (c) 2015 Teal Cube Games
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package info.faceland.strife.managers;

import static info.faceland.strife.attributes.StrifeAttribute.BLOCK;
import static info.faceland.strife.attributes.StrifeAttribute.BLOCK_RECOVERY;
import static info.faceland.strife.attributes.StrifeAttribute.EARTH_DAMAGE;
import static info.faceland.strife.attributes.StrifeAttribute.MAX_EARTH_RUNES;

import info.faceland.strife.attributes.StrifeAttribute;
import info.faceland.strife.data.AttributedEntity;
import info.faceland.strife.data.BlockData;
import info.faceland.strife.util.LogUtil;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class BlockManager {

  private final Map<UUID, BlockData> blockDataMap = new HashMap<>();
  private final Random random = new Random();

  private static final double FLAT_BLOCK_S = 10;
  private static final double PERCENT_BLOCK_S = 0.05;
  private static final long DEFAULT_BLOCK_MILLIS = 10000;
  private static final double MAX_BLOCK_CHANCE = 0.6;

  public boolean rollBlock(AttributedEntity attributedEntity, boolean isBlocking) {
    if (attributedEntity.getAttribute(StrifeAttribute.BLOCK) < 1) {
      return false;
    }
    UUID uuid = attributedEntity.getEntity().getUniqueId();
    updateStoredBlock(attributedEntity);
    double blockChance = Math.min(blockDataMap.get(uuid).getStoredBlock() / 100, MAX_BLOCK_CHANCE);
    if (isBlocking) {
      blockChance *= 2;
    }
    LogUtil.printDebug("Block chance: " + blockChance);
    return random.nextDouble() < blockChance;
  }

  public long getMillisSinceBlock(UUID uuid) {
    return System.currentTimeMillis() - blockDataMap.get(uuid).getLastHit();
  }

  public double getSecondsSinceBlock(UUID uuid) {
    return ((double) getMillisSinceBlock(uuid)) / 1000;
  }

  public int getEarthRunes(UUID uuid) {
    if (!blockDataMap.containsKey(uuid)) {
      return 0;
    }
    return blockDataMap.get(uuid).getRunes();
  }

  public void setEarthRunes(UUID uuid, int runes) {
    if (!blockDataMap.containsKey(uuid)) {
      return;
    }
    blockDataMap.get(uuid).setRunes(runes);
  }

  public void bumpRunes(AttributedEntity aEntity) {
    if (aEntity.getAttribute(EARTH_DAMAGE) < 1) {
      return;
    }
    UUID uuid = aEntity.getEntity().getUniqueId();
    if (!blockDataMap.containsKey(uuid)) {
      return;
    }
    if (blockDataMap.get(uuid).getRunes() < Math.round(aEntity.getAttribute(MAX_EARTH_RUNES))) {
      blockDataMap.get(uuid).addRune();
    }
  }

  private void updateStoredBlock(AttributedEntity attributedEntity) {
    UUID uuid = attributedEntity.getEntity().getUniqueId();
    if (blockDataMap.get(uuid) == null) {
      BlockData data = new BlockData(System.currentTimeMillis() - DEFAULT_BLOCK_MILLIS, 0);
      blockDataMap.put(uuid, data);
    }
    double maximumBlock = attributedEntity.getAttribute(BLOCK);
    double block = blockDataMap.get(uuid).getStoredBlock();
    double restoredBlock = FLAT_BLOCK_S + PERCENT_BLOCK_S * maximumBlock;
    if (attributedEntity.getAttribute(BLOCK_RECOVERY) > 1) {
      restoredBlock *= 1 + attributedEntity.getAttribute(BLOCK_RECOVERY) / 100;
    }

    block += restoredBlock * getSecondsSinceBlock(uuid);
    blockDataMap.get(uuid).setStoredBlock(Math.min(block, maximumBlock));
    blockDataMap.get(uuid).setLastHit(System.currentTimeMillis());
    LogUtil.printDebug("New block before clamp: " + block);
  }

  public void blockFatigue(UUID uuid, double attackMultipler, boolean isBlocking) {
    BlockData data = blockDataMap.get(uuid);
    LogUtil.printDebug("Pre reduction block: " + data.getStoredBlock());
    double blockFatigue = attackMultipler * (isBlocking ? 50D : 100D);
    data.setStoredBlock(Math.max(0, data.getStoredBlock() - blockFatigue));
    LogUtil.printDebug("Post reduction block: " + data.getStoredBlock());
  }
}
