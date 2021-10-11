package src.main.java.components;

import java.time.LocalDateTime;

public class Erzeuger_Solarwerk extends Erzeuger {

  public Erzeuger_Solarwerk(int ID, String name, int maxCapacity) {
    super(ID, name, false, maxCapacity);
  }

  int getKW(LocalDateTime timestamp) {
    double randomFactor = getRandomFactor(0.8, 1);
    if (timestamp.getHour() >= 6 && timestamp.getHour() <= 20) {
      // bei tag
      int kw = (int) (power * randomFactor * maxCapacity);
      if (kw > maxCapacity) {
        kw = maxCapacity;
      }
      return kw;
    }

    // bei nacht kein sonnenlicht
    return 0;
  }
}
