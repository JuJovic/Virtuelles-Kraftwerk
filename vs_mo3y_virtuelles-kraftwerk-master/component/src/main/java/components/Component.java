package src.main.java.components;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import src.main.java.*;

public abstract class Component {

  private int ID;
  private String name;
  private boolean isConsumer;

  public Component(int ID, String name, boolean isConsumer) {
    this.ID = ID;
    this.name = name;
    this.isConsumer = isConsumer;
  }

  public double getRandomFactor(double min, double max) {
    Random r = new Random();
    double random = min + r.nextDouble() * (max - min);
    return random;
  }

  abstract int getKW(LocalDateTime timestamp);

  public void start(String destination, int port, int bufferSize) {
    Publisher publisher = new Publisher(port, this.ID);

    while (true) {
      LocalDateTime timestamp = SimulatedTime.now();
      int kw = getKW(timestamp);
      ComponentInfo info = new ComponentInfo(this.ID, this.name, this.isConsumer, kw, timestamp);
      publisher.sendInfo(info);
      try {
        TimeUnit.MILLISECONDS.sleep(2500);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }
}
