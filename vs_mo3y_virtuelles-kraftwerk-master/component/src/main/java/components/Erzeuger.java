package src.main.java.components;

import src.main.java.components.Component;

public abstract class Erzeuger extends Component {

  protected double power = 1.0; // percentage of production
  protected int maxCapacity;

  public Erzeuger(int ID, String name, boolean isConsumer, int maxCapacity) {
    super(ID, name, isConsumer);
    this.maxCapacity = maxCapacity;
  }

  public void setPower(double power) {
    this.power = power;
  }

  public double getPower() {
    return this.power;
  }
}
