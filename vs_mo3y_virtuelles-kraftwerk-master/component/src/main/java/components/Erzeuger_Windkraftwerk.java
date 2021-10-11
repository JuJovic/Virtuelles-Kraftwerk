package src.main.java.components;

import java.time.LocalDateTime;

public class Erzeuger_Windkraftwerk extends Erzeuger {

  public Erzeuger_Windkraftwerk(int ID, String name, int maxCapacity) {
    super(ID, name, false, maxCapacity);
  }

  // produces energy all day based on the wind force
  int getKW(LocalDateTime timestamp) {
    double windForce = getRandomFactor(0, 1);
    int kw = (int) (windForce * power * maxCapacity);
    if (kw > maxCapacity) {
      kw = maxCapacity;
    }
    return kw;
  }
}

