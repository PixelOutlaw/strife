package info.faceland.strife.data;

public class RageData {

  private double rageStacks;
  private int graceTicksRemaining;

  public RageData(double amount, int ticks) {
    this.rageStacks = amount;
    this.graceTicksRemaining = ticks;
  }

  public double getRageStacks() {
    return rageStacks;
  }

  public void setRageStacks(double rageStacks) {
    this.rageStacks = rageStacks;
  }

  public void addRageStacks(double amount) {
    this.rageStacks += amount;
  }

  public int getGraceTicksRemaining() {
    return graceTicksRemaining;
  }

  public void setGraceTicksRemaining(int graceTicksRemaining) {
    this.graceTicksRemaining = graceTicksRemaining;
  }
}
