package src.main.java.components;

import java.time.LocalDateTime;

public class Erzeuger_Kohlewerk extends Erzeuger {

  public Erzeuger_Kohlewerk(int ID, String name, int maxCapacity) {
    super(ID, name, false, maxCapacity);
  }

  // Produces power all day long with a small random variation of 5%
  int getKW(LocalDateTime timestamp) {
    double randomFactor = getRandomFactor(0.95, 1);
    int kw = (int) (randomFactor * power * maxCapacity);
    if (kw > maxCapacity) {
      kw = maxCapacity;
    }
    return kw;
  }
}

