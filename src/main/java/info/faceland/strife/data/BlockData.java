package info.faceland.strife.data;

public class BlockData {

  private long lastHit;
  private double storedBlock;

  public BlockData(long lastHit, double storedBlock) {
    this.lastHit = lastHit;
    this.storedBlock = storedBlock;
  }

  public long getLastHit() {
    return lastHit;
  }

  public void setLastHit(long lastHit) {
    this.lastHit = lastHit;
  }

  public double getStoredBlock() {
    return storedBlock;
  }

  public void setStoredBlock(double storedBlock) {
    this.storedBlock = storedBlock;
  }
}
