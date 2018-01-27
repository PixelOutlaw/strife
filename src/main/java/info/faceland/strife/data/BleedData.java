package info.faceland.strife.data;

public class BleedData {

  private double bleedAmount;
  private int ticksRemaining;

  public BleedData(double amount, int ticks) {
    this.bleedAmount = amount;
    this.ticksRemaining = ticks;
  }

  public double getBleedAmount() {
    return bleedAmount;
  }

  public void setBleedAmount(double bleedAmount) {
    this.bleedAmount = bleedAmount;
  }

  public int getTicksRemaining() {
    return ticksRemaining;
  }

  public void setTicksRemaining(int ticksRemaining) {
    this.ticksRemaining = ticksRemaining;
  }
}
